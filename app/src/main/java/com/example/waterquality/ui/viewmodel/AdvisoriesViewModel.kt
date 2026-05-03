package com.example.waterquality.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterquality.data.repository.WaterRepository
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.components.waterQualityFromReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AdvisoryItem(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity
)

@HiltViewModel
class AdvisoriesViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    val advisories: StateFlow<List<AdvisoryItem>> = repository.allReports
        .map { reports ->
            val list = reports
                .filter { waterQualityFromReport(it.clarity, it.smell) != WaterQuality.CLEAN }
                .map { r ->
                    val q = waterQualityFromReport(r.clarity, r.smell)
                    AdvisoryItem(
                        id = r.id,
                        title = if (q == WaterQuality.POLLUTED) "Critical Pollution Advisory" else "Cautionary Advisory",
                        description = if (q == WaterQuality.POLLUTED) 
                            "Gemini AI detects severe contamination patterns in recent reports. Avoid all contact with the water source and notify local authorities."
                        else 
                            "Gemini AI notes reduced water clarity. Safe for secondary usage, but avoid consumption. Monitor the situation closely.",
                        severity = if (q == WaterQuality.POLLUTED) AlertSeverity.CRITICAL else AlertSeverity.WARNING
                    )
                }
            if (list.isEmpty()) seedAdvisories() else list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAdvisories())

    private fun seedAdvisories() = listOf(
        AdvisoryItem(
            id = "adv1",
            title = "Seasonal Algal Bloom",
            description = "Gemini AI predicts a high probability of algal blooms due to rising temperatures. Avoid swimming in stagnant areas.",
            severity = AlertSeverity.WARNING
        ),
        AdvisoryItem(
            id = "adv2",
            title = "Heavy Rainfall Runoff",
            description = "Recent heavy rains have increased turbidity levels significantly. Water may appear muddy but is generally safe for non-potable use.",
            severity = AlertSeverity.INFO
        )
    )
}
