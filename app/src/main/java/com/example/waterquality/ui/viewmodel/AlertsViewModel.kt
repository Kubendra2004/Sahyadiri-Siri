package com.example.waterquality.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterquality.data.repository.WaterRepository
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.ui.utils.appStr
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

    // Language state — composable pushes the current language here
    private val _lang = MutableStateFlow("English")
    fun setLanguage(lang: String) = _lang.update { lang }

    /** IDs dismissed by the user in this session. */
    private val _dismissedIds = MutableStateFlow<Set<String>>(emptySet())

    private val allAlerts =
        combine(repository.getLocalAdvisories(), _lang) { advisories, lang ->
            val list = advisories.map { advisory -> advisory.toAlertItem(lang) }
            if (list.isEmpty()) seedAlerts(lang) else list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAlerts("English"))

    /** Visible alerts after removing dismissed ones. */
    val alerts: StateFlow<List<AlertItem>> =
        combine(allAlerts, _dismissedIds) { list, dismissed ->
            list.filter { it.id !in dismissed }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAlerts("English"))

    /** Filter state: null = All, otherwise matches AlertSeverity. */
    private val _activeFilter = MutableStateFlow<AlertSeverity?>(null)
    val activeFilter: StateFlow<AlertSeverity?> = _activeFilter.asStateFlow()

    val filteredAlerts: StateFlow<List<AlertItem>> =
        combine(alerts, _activeFilter) { list, filter ->
            if (filter == null) list else list.filter { it.severity == filter }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun dismissAlert(id: String) = _dismissedIds.update { it + id }
    fun setFilter(severity: AlertSeverity?) = _activeFilter.update { severity }

    private fun seedAlerts(lang: String = "English") = listOf(
        AlertItem(
            id        = "seed1",
            title     = appStr(lang, "seed_c_title"),
            message   = appStr(lang, "seed_c_desc"),
            severity  = AlertSeverity.WARNING,
            timestamp = System.currentTimeMillis() - 3_600_000L,
            locationTag = "12.2958, 76.6394"
        ),
        AlertItem(
            id        = "seed2",
            title     = appStr(lang, "seed_t_title"),
            message   = appStr(lang, "seed_t_desc"),
            severity  = AlertSeverity.CRITICAL,
            timestamp = System.currentTimeMillis() - 7_200_000L,
            locationTag = "15.1394, 76.9214"
        ),
        AlertItem(
            id        = "seed3",
            title     = appStr(lang, "seed_k_title"),
            message   = appStr(lang, "seed_k_desc"),
            severity  = AlertSeverity.INFO,
            timestamp = System.currentTimeMillis() - 86_400_000L,
            locationTag = "11.9891, 76.3610"
        )
    )

    private fun Advisory.toAlertItem(lang: String): AlertItem {
        val severity = when (status) {
            "Critical" -> AlertSeverity.CRITICAL
            "Caution" -> AlertSeverity.WARNING
            else -> AlertSeverity.INFO
        }
        return AlertItem(
            id = id,
            title = title.ifBlank { appStr(lang, "nav_alerts") },
            message = description,
            severity = severity,
            timestamp = timestamp,
            locationTag = reportId ?: appStr(lang, "location")
        )
    }
}
