package com.example.waterquality.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.data.repository.WaterRepository
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.components.waterQualityFromReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HomeStats(
    val totalReports: Int = 0,
    val cleanSources: Int = 0,
    val moderateSources: Int = 0,
    val pollutedSources: Int = 0,
    val activeAlerts: Int = 0
)

data class AlertItem(
    val id:          String,
    val title:       String,
    val message:     String,
    val severity:    AlertSeverity,
    val timestamp:   Long,
    val locationTag: String = ""
)

enum class AlertSeverity { CRITICAL, WARNING, INFO }

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    // ─── Raw reports from Room ───────────────────────────────────────────────
    val reports: StateFlow<List<WaterReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── Loading state ───────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ─── Error message ───────────────────────────────────────────────────────
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ─── Home stats (derived) ────────────────────────────────────────────────
    val homeStats: StateFlow<HomeStats> = reports.map { list ->
        val clean    = list.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.CLEAN }
        val moderate = list.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.MODERATE }
        val polluted = list.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.POLLUTED }
        HomeStats(
            totalReports    = list.size,
            cleanSources    = clean,
            moderateSources = moderate,
            pollutedSources = polluted,
            activeAlerts    = polluted + moderate / 2
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeStats())

    // ─── Advisories (flash cards) ────────────────────────────────────────────
    val advisories: StateFlow<List<Advisory>> = reports.map { list ->
        if (list.isEmpty()) {
            listOf(
                Advisory(
                    id          = "welcome",
                    title       = "Welcome to Sahyadri-Siri",
                    description = "Start by submitting a water report near you to see AI-generated advisories here.",
                    status      = "Info",
                    timestamp   = System.currentTimeMillis()
                )
            )
        } else {
            list.take(10).map { r ->
                val quality = waterQualityFromReport(r.clarity, r.smell)
                val status  = when (quality) {
                    WaterQuality.CLEAN    -> "Safe"
                    WaterQuality.MODERATE -> "Caution"
                    WaterQuality.POLLUTED -> "Critical"
                }
                val title = when (quality) {
                    WaterQuality.CLEAN    -> "Water Source: Good Condition"
                    WaterQuality.MODERATE -> "Water Source: Monitor Closely"
                    WaterQuality.POLLUTED -> "Alert: Possible Contamination"
                }
                val desc = when (quality) {
                    WaterQuality.CLEAN    ->
                        "Clarity ${r.clarity}/5 with normal odour. Safe for non-potable use. Continue monitoring."
                    WaterQuality.MODERATE ->
                        "Clarity ${r.clarity}/5. ${if (r.smell == "Bad") "Slight odour detected." else "Low clarity."} Avoid drinking without treatment."
                    WaterQuality.POLLUTED ->
                        "Low clarity (${r.clarity}/5) and ${if (r.smell == "Bad") "strong odour" else "abnormal readings"} detected. Avoid all contact. Report to local authorities."
                }
                Advisory(r.id, title, desc, status, r.timestamp)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── Alerts feed ─────────────────────────────────────────────────────────
    val alerts: StateFlow<List<AlertItem>> = reports.map { list ->
        list
            .filter { waterQualityFromReport(it.clarity, it.smell) != WaterQuality.CLEAN }
            .mapIndexed { i, r ->
                val quality = waterQualityFromReport(r.clarity, r.smell)
                AlertItem(
                    id          = r.id,
                    title       = if (quality == WaterQuality.POLLUTED)
                        "Pollution Alert" else "Water Quality Warning",
                    message     = if (quality == WaterQuality.POLLUTED)
                        "Sudden contamination detected. Clarity ${r.clarity}/5, ${r.smell} odour. Avoid all contact for 48 hours."
                        else
                        "Reduced water clarity (${r.clarity}/5) in this area. ${r.flow} flow rate observed.",
                    severity    = if (quality == WaterQuality.POLLUTED)
                        AlertSeverity.CRITICAL else AlertSeverity.WARNING,
                    timestamp   = r.timestamp,
                    locationTag = "%.4f, %.4f".format(r.latitude, r.longitude)
                )
            }
            .plus(
                // Append some seeded demo alerts if no real data
                if (list.isEmpty()) seedAlerts() else emptyList()
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAlerts())

    fun clearError() = _errorMessage.update { null }

    // ─── Seed data for empty state demo ─────────────────────────────────────
    private fun seedAlerts() = listOf(
        AlertItem(
            id       = "seed1",
            title    = "Cauvery River — Warning",
            message  = "Increased turbidity reported near Mysuru reach. Boil water before use.",
            severity = AlertSeverity.WARNING,
            timestamp = System.currentTimeMillis() - 3_600_000L,
            locationTag = "12.2958, 76.6394"
        ),
        AlertItem(
            id       = "seed2",
            title    = "Tungabhadra — Critical",
            message  = "Industrial discharge suspected upstream. Avoid water contact. Authorities notified.",
            severity = AlertSeverity.CRITICAL,
            timestamp = System.currentTimeMillis() - 7_200_000L,
            locationTag = "15.1394, 76.9214"
        ),
        AlertItem(
            id       = "seed3",
            title    = "Kabini Reservoir — Info",
            message  = "Water level slightly below seasonal average. Quality normal.",
            severity = AlertSeverity.INFO,
            timestamp = System.currentTimeMillis() - 86_400_000L,
            locationTag = "11.9891, 76.3610"
        )
    )
}
