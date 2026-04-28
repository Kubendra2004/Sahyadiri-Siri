package com.example.waterquality.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.waterquality.ui.components.ConfettiCanvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.waterquality.ui.components.hapticClickable
import com.example.waterquality.ui.components.verticalSwipeResistance
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.GradientOceanColors
import com.example.waterquality.ui.viewmodel.ReportSubmissionViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSubmissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportSubmissionViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val context       = LocalContext.current
    val haptic        = LocalHapticFeedback.current
    val snackbarState = remember { SnackbarHostState() }

    var showCamera by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Auto-navigate on success with confetti
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showConfetti = true
            kotlinx.coroutines.delay(2600)
            viewModel.resetSuccess()
            onNavigateBack()
        }
    }

    // Show errors in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    if (showCamera) {
        CameraView(
            onImageCaptured = { path ->
                viewModel.onImageCaptured(path)
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalSwipeResistance(onDismiss = onNavigateBack)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(GradientOceanColors))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onNavigateBack() }) {
                        Icon(Icons.Default.Close, "Cancel", tint = Color.White)
                    }
                    Text("Submit Report",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.width(48.dp))
                }
            }

            // Step indicator
            StepIndicator(currentStep = uiState.currentStep)

            // Step content with slide transition
            AnimatedContent(
                targetState   = uiState.currentStep,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * dir } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it * dir } + fadeOut())
                },
                label = "step_content",
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) { step ->
                when (step) {
                    0 -> PhotoStep(
                        imagePath = uiState.imagePath,
                        onCaptureClick = { showCamera = true }
                    )
                    1 -> DetailsStep(
                        clarity  = uiState.clarity,
                        smell    = uiState.smell,
                        flow     = uiState.flow,
                        onClarityChange = { viewModel.onClarityChange(it) },
                        onSmellChange   = { viewModel.onSmellChange(it) },
                        onFlowChange    = { viewModel.onFlowChange(it) }
                    )
                    2 -> LocationStep(
                        latitude  = uiState.latitude,
                        longitude = uiState.longitude,
                        onLocationFetched = { lat, lon -> viewModel.onLocationCaptured(lat, lon) },
                        context   = context
                    )
                }
            }

            // Bottom navigation buttons
            StepNavigationButtons(
                currentStep     = uiState.currentStep,
                isSubmitting    = uiState.isSubmitting,
                canProceed      = when (uiState.currentStep) {
                    0 -> uiState.imagePath != null
                    1 -> true
                    2 -> true
                    else -> false
                },
                onBack          = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.prevStep() },
                onNext          = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.nextStep() },
                onSubmit        = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.submitReport() }
            )
        }

        SnackbarHost(
            hostState = snackbarState,
            modifier  = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )

        // Confetti burst on success
        if (showConfetti) {
            Box(Modifier.fillMaxSize()) {
                ConfettiCanvas(onFinished = { showConfetti = false })
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("✅", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("Report Submitted!", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Thank you for contributing", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


// ─── Step Indicator ───────────────────────────────────────────────────────────
@Composable
private fun StepIndicator(currentStep: Int) {
    val steps = listOf("Photo", "Details", "Location")
    Row(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isComplete = index < currentStep
            val isActive   = index == currentStep

            // Circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when {
                            isComplete -> CleanBlue
                            isActive   -> MaterialTheme.colorScheme.primary
                            else       -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isComplete) {
                    Icon(Icons.Default.Check, null,
                        tint     = Color.White,
                        modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        "${index + 1}",
                        color = if (isActive) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Label
            if (isActive) {
                Text(label,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }

            // Connector line (not after last)
            if (index < steps.size - 1 && !isActive) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (isComplete) CleanBlue.copy(0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(1.dp)
                        )
                )
            } else if (index < steps.size - 1) {
                Spacer(Modifier.weight(1f))
            }
        }
    }

    // Progress bar
    LinearProgressIndicator(
        progress = { (currentStep + 1) / 3f },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
        color    = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    Spacer(Modifier.height(8.dp))
}

// ─── Step 0: Photo ────────────────────────────────────────────────────────────
@Composable
private fun PhotoStep(imagePath: String?, onCaptureClick: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Capture Water Photo",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Take a clear photo of the water source",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        Card(
            modifier  = Modifier.fillMaxWidth().height(220.dp),
            shape     = RoundedCornerShape(20.dp),
            onClick   = onCaptureClick,
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (imagePath != null) {
                    Image(
                        painter          = rememberAsyncImagePainter(File(imagePath)),
                        contentDescription = "Captured water",
                        modifier         = Modifier.fillMaxSize(),
                        contentScale     = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.2f))
                    )
                    Icon(Icons.Default.CheckCircle,
                        null, tint = Color.White,
                        modifier = Modifier.size(48.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null,
                            modifier = Modifier.size(56.dp),
                            tint     = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Tap to open camera",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (imagePath != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onCaptureClick, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.CameraAlt, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retake Photo")
            }
        }
    }
}

// ─── Step 1: Details ─────────────────────────────────────────────────────────
@Composable
private fun DetailsStep(
    clarity:         Int,
    smell:           String,
    flow:            String,
    onClarityChange: (Int) -> Unit,
    onSmellChange:   (String) -> Unit,
    onFlowChange:    (String) -> Unit
) {
    val haptic    = LocalHapticFeedback.current
    var lastInt   by remember { mutableIntStateOf(clarity) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Water Details",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        // Clarity slider
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Water Clarity",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("$clarity / 5",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value         = clarity.toFloat(),
                onValueChange = { v ->
                    val newInt = v.toInt()
                    if (newInt != lastInt) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        lastInt = newInt
                    }
                    onClarityChange(newInt)
                },
                valueRange  = 1f..5f,
                steps       = 3,
                colors      = SliderDefaults.colors(
                    thumbColor        = MaterialTheme.colorScheme.primary,
                    activeTrackColor  = MaterialTheme.colorScheme.primary
                )
            )
            // Clarity label row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Murky", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Crystal Clear", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Smell
        Column {
            Text("Smell", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Normal", "Bad").forEach { option ->
                    FilterChip(
                        selected = smell == option,
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSmellChange(option)
                        },
                        label    = { Text(option) },
                        shape    = RoundedCornerShape(50),
                        leadingIcon = if (smell == option) ({
                            Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                        }) else null
                    )
                }
            }
        }

        // Flow
        Column {
            Text("Flow Rate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Low", "Medium", "High").forEach { option ->
                    FilterChip(
                        selected  = flow == option,
                        onClick   = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onFlowChange(option)
                        },
                        label     = {
                            Text(option, modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center)
                        },
                        shape     = RoundedCornerShape(50),
                        modifier  = Modifier.weight(1f),
                        colors    = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }
        }
    }
}

// ─── Step 2: Location ─────────────────────────────────────────────────────────
@Composable
private fun LocationStep(
    latitude:         Double,
    longitude:        Double,
    onLocationFetched: (Double, Double) -> Unit,
    context:          Context
) {
    val haptic = LocalHapticFeedback.current

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLastLocation(context, onLocationFetched)
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("GPS Location",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("We need your location to pin the report on the map.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = if (latitude != 0.0)
                    CleanBlue.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier          = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn, null,
                    tint     = if (latitude != 0.0) CleanBlue
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (latitude != 0.0) {
                        Text("Location captured ✓",
                            fontWeight = FontWeight.Bold, color = CleanBlue)
                        Text("Lat: ${"%.6f".format(latitude)}\nLon: ${"%.6f".format(longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Location not captured",
                            fontWeight = FontWeight.Bold)
                        Text("Tap below to get your GPS coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasFine || hasCoarse) {
                    fetchLastLocation(context, onLocationFetched)
                } else {
                    permLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            },
            shape  = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (latitude != 0.0) "Refresh Location" else "Get My Location")
        }
    }
}

private fun fetchLastLocation(context: Context, onResult: (Double, Double) -> Unit) {
    try {
        val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return
        com.google.android.gms.location.LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { loc -> loc?.let { onResult(it.latitude, it.longitude) } }
    } catch (e: SecurityException) {
        Log.e("LocationStep", "Permission error", e)
    }
}

// ─── Navigation Buttons ───────────────────────────────────────────────────────
@Composable
private fun StepNavigationButtons(
    currentStep:  Int,
    isSubmitting: Boolean,
    canProceed:   Boolean,
    onBack:       () -> Unit,
    onNext:       () -> Unit,
    onSubmit:     () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick   = onBack,
                shape     = RoundedCornerShape(16.dp),
                modifier  = Modifier.weight(1f)
            ) { Text("Back") }
        }

        Button(
            onClick   = if (currentStep < 2) onNext else onSubmit,
            enabled   = canProceed && !isSubmitting,
            shape     = RoundedCornerShape(16.dp),
            modifier  = Modifier.weight(if (currentStep > 0) 1f else 1f).height(52.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    if (currentStep < 2) "Continue" else "Submit Report",
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                )
            }
        }
    }
}

// ─── Camera View ──────────────────────────────────────────────────────────────
@Composable
fun CameraView(onImageCaptured: (String) -> Unit, onClose: () -> Unit) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller     = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
            onClose()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory  = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick  = onClose,
            modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopStart)
                .background(Color.Black.copy(0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Close, "Close", tint = Color.White)
        }

        // Shutter button
        Box(
            modifier         = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
                .size(80.dp)
                .background(Color.White.copy(0.3f), CircleShape)
                .hapticClickable {
                    val file = File(context.filesDir, "IMG_${System.currentTimeMillis()}.jpg")
                    val opts = ImageCapture.OutputFileOptions.Builder(file).build()
                    controller.takePicture(opts, ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(file.absolutePath)
                            }
                            override fun onError(e: ImageCaptureException) {
                                Log.e("Camera", "Capture failed", e)
                            }
                        })
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}
