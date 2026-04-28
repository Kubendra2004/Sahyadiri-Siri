package com.example.waterquality.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.ui.components.BlurScrim
import com.example.waterquality.ui.components.EmptyState
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.components.WaterScoreMeter
import com.example.waterquality.ui.components.WaterStatusChip
import com.example.waterquality.ui.components.waterQualityFromReport
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.viewmodel.WaterViewModel
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private enum class MapFilter { ALL, CLEAN, MODERATE, POLLUTED }

// ── Osmdroid MapView wrapped in Compose ──────────────────────────────────────
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context        = LocalContext.current
    val lifecycle      = LocalLifecycleOwner.current.lifecycle
    val mapView        = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)       // OSM Mapnik — free, no key
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(10.0)
            controller.setCenter(GeoPoint(12.9716, 77.5946)) // Bengaluru default
        }
    }

    // Pause / resume MapView following the Activity lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                else                       -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}

// ── Colored circle marker bitmap ─────────────────────────────────────────────
private fun coloredMarkerBitmap(context: Context, color: Color): android.graphics.drawable.BitmapDrawable {
    val dp  = context.resources.displayMetrics.density
    val size = (24 * dp).toInt()
    val bmp  = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    // Outer filled circle
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, fill)

    // White ring
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * dp
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3, ring)

    return android.graphics.drawable.BitmapDrawable(context.resources, bmp)
}

// ── Main screen ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: WaterViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    var activeFilter   by remember { mutableStateOf(MapFilter.ALL) }
    var selectedReport by remember { mutableStateOf<WaterReport?>(null) }

    val sheetState = rememberStandardBottomSheetState(
        initialValue    = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    val sheetProgress = when (sheetState.currentValue) {
        SheetValue.Expanded           -> 1f
        SheetValue.PartiallyExpanded  -> 0.5f
        else                          -> 0f
    }

    val filteredReports = reports.filter { r ->
        val q = waterQualityFromReport(r.clarity, r.smell)
        when (activeFilter) {
            MapFilter.ALL      -> true
            MapFilter.CLEAN    -> q == WaterQuality.CLEAN
            MapFilter.MODERATE -> q == WaterQuality.MODERATE
            MapFilter.POLLUTED -> q == WaterQuality.POLLUTED
        }
    }

    // osmdroid MapView — survives recompositions
    val mapView = rememberMapViewWithLifecycle()

    // Update markers whenever filteredReports changes
    LaunchedEffect(filteredReports) {
        mapView.overlays.clear()
        filteredReports.forEach { report ->
            val quality = waterQualityFromReport(report.clarity, report.smell)
            val markerColor = when (quality) {
                WaterQuality.CLEAN    -> CleanBlue
                WaterQuality.MODERATE -> ModerateAmber
                WaterQuality.POLLUTED -> PollutedRed
            }
            Marker(mapView).apply {
                position  = GeoPoint(report.latitude, report.longitude)
                title     = "Clarity ${report.clarity}/5 · ${report.smell}"
                snippet   = "${report.flow} flow"
                icon      = coloredMarkerBitmap(context, markerColor)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                setOnMarkerClickListener { _, _ ->
                    selectedReport = report
                    scope.launch { sheetState.expand() }
                    true
                }
                mapView.overlays.add(this)
            }
        }
        mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState   = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetShape      = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetDragHandle = {
                Box(
                    Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            },
            sheetContent = {
                selectedReport?.let { report ->
                    MapBottomSheetContent(
                        report  = report,
                        onClose = {
                            scope.launch { sheetState.hide() }
                            selectedReport = null
                        }
                    )
                }
            }
        ) { _ ->   // Do NOT apply innerPadding to map — it must fill full screen
            Box(modifier = Modifier.fillMaxSize()) {
                // ── OSM MapView via AndroidView ──────────────────────────────
                AndroidView(
                    factory  = { mapView },
                    modifier = Modifier.fillMaxSize()
                )

                // ── Filter chips ─────────────────────────────────────────────
                LazyRow(
                    modifier              = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding        = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    items(MapFilter.values().toList()) { filter ->
                        val selected = filter == activeFilter
                        FilterChip(
                            selected = selected,
                            onClick  = { activeFilter = filter },
                            label    = { Text(filter.name) },
                            shape    = RoundedCornerShape(50),
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = Color.White
                            ),
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                RoundedCornerShape(50)
                            )
                        )
                    }
                }

                // ── Legend ───────────────────────────────────────────────────
                OsmLegend(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 120.dp)
                )

                // ── Empty state ──────────────────────────────────────────────
                if (filteredReports.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon     = Icons.Default.WaterDrop,
                        title    = "No reports here",
                        subtitle = "Submit a report to pin it on the map."
                    )
                }
            }
        }

        // Blur scrim behind bottom sheet
        BlurScrim(progress = sheetProgress, modifier = Modifier.fillMaxSize())
    }
}

// ── Legend card ───────────────────────────────────────────────────────────────
@Composable
private fun OsmLegend(modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OsmLegendItem(CleanBlue,    "Clean")
            OsmLegendItem(ModerateAmber,"Moderate")
            OsmLegendItem(PollutedRed,  "Polluted")
            Spacer(Modifier.height(2.dp))
            Text("© OpenStreetMap",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun OsmLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// ── Bottom sheet content (reused from previous MapScreen) ─────────────────────
@Composable
private fun MapBottomSheetContent(report: WaterReport, onClose: () -> Unit) {
    val quality = waterQualityFromReport(report.clarity, report.smell)
    val score   = when (quality) {
        WaterQuality.CLEAN    -> 70f + report.clarity * 6f
        WaterQuality.MODERATE -> 35f + report.clarity * 5f
        WaterQuality.POLLUTED -> report.clarity * 6f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Water Source Detail",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WaterScoreMeter(score = score, size = 100.dp)
            Spacer(Modifier.width(20.dp))
            Column {
                WaterStatusChip(quality)
                Spacer(Modifier.height(8.dp))
                MapInfoRow("Clarity",  "${report.clarity}/5")
                MapInfoRow("Smell",    report.smell)
                MapInfoRow("Flow",     report.flow)
                MapInfoRow("Location", "%.4f, %.4f".format(report.latitude, report.longitude))
            }
        }

        Spacer(Modifier.height(16.dp))

        val advisory = when (quality) {
            WaterQuality.CLEAN    -> "✅ Water appears safe for non-potable use. Continue monitoring."
            WaterQuality.MODERATE -> "⚠️ Reduced quality detected. Avoid drinking without treatment."
            WaterQuality.POLLUTED -> "🚨 Contamination likely. Avoid all contact. Report to authorities."
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(14.dp),
            colors   = CardDefaults.cardColors(
                containerColor = when (quality) {
                    WaterQuality.CLEAN    -> CleanBlue.copy(alpha = 0.1f)
                    WaterQuality.MODERATE -> ModerateAmber.copy(alpha = 0.1f)
                    WaterQuality.POLLUTED -> PollutedRed.copy(alpha = 0.1f)
                }
            )
        ) {
            Text(
                text     = advisory,
                modifier = Modifier.padding(14.dp),
                style    = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MapInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ",
            style      = MaterialTheme.typography.bodySmall,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium)
        Text(value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface)
    }
}
