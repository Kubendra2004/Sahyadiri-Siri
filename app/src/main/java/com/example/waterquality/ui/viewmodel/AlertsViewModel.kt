package com.example.waterquality.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class AlertsViewModel @Inject constructor(
    repository: WaterRepository
) : ViewModel() {

    /** IDs dismissed by the user in this session. */
    private val _dismissedIds = MutableStateFlow<Set<String>>(emptySet())

    /** All alerts derived from Room reports. */
    private val allAlerts = repository.allReports
        .map { reports ->
            val list = reports
                .filter { waterQualityFromReport(it.clarity, it.smell) != WaterQuality.CLEAN }
                .map { r ->
                    val q = waterQualityFromReport(r.clarity, r.smell)
                    AlertItem(
                        id          = r.id,
                        title       = if (q == WaterQuality.POLLUTED) "Pollution Alert" else "Water Quality Warning",
                        message     = if (q == WaterQuality.POLLUTED)
                            "Contamination detected. Clarity ${r.clarity}/5. Avoid contact for 48 hours."
                        else
                            "Reduced water clarity (${r.clarity}/5). Monitor usage closely.",
                        severity    = if (q == WaterQuality.POLLUTED) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
                        timestamp   = r.timestamp,
                        locationTag = "%.4f, %.4f".format(r.latitude, r.longitude)
                    )
                }
            // Always include seed alerts when no real reports exist
            if (list.isEmpty()) seedAlerts() else list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAlerts())

    /** Visible alerts after removing dismissed ones. */
    val alerts: StateFlow<List<AlertItem>> =
        combine(allAlerts, _dismissedIds) { list, dismissed ->
            list.filter { it.id !in dismissed }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAlerts())

    /** Filter state: null = All, otherwise matches AlertSeverity. */
    private val _activeFilter = MutableStateFlow<AlertSeverity?>(null)
    val activeFilter: StateFlow<AlertSeverity?> = _activeFilter.asStateFlow()

    val filteredAlerts: StateFlow<List<AlertItem>> =
        combine(alerts, _activeFilter) { list, filter ->
            if (filter == null) list else list.filter { it.severity == filter }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun dismissAlert(id: String) = _dismissedIds.update { it + id }
    fun setFilter(severity: AlertSeverity?) = _activeFilter.update { severity }

    private fun seedAlerts() = listOf(
        AlertItem(
            id        = "seed1",
            title     = "Cauvery River — Warning",
            message   = "Increased turbidity near Mysuru reach. Boil water before potable use.",
            severity  = AlertSeverity.WARNING,
            timestamp = System.currentTimeMillis() - 3_600_000L,
            locationTag = "12.2958, 76.6394"
        ),
        AlertItem(
            id        = "seed2",
            title     = "Tungabhadra — Critical",
            message   = "Industrial discharge suspected upstream. Avoid all water contact immediately.",
            severity  = AlertSeverity.CRITICAL,
            timestamp = System.currentTimeMillis() - 7_200_000L,
            locationTag = "15.1394, 76.9214"
        ),
        AlertItem(
            id        = "seed3",
            title     = "Kabini Reservoir — Info",
            message   = "Water level slightly below seasonal average. Quality tests nominal.",
            severity  = AlertSeverity.INFO,
            timestamp = System.currentTimeMillis() - 86_400_000L,
            locationTag = "11.9891, 76.3610"
        )
    )
}
