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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    // Language exposed so the composable can push the current language into the VM
    private val _lang = MutableStateFlow("English")
    fun setLanguage(lang: String) = _lang.update { lang }

    val advisories: StateFlow<List<AdvisoryItem>> =
        combine(repository.getLocalAdvisories(), _lang) { remoteAdvisories, lang ->
            val resolved = remoteAdvisories.map { advisory -> advisory.toAdvisoryItem() }
            if (resolved.isEmpty()) seedAdvisories(lang) else resolved
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedAdvisories("English"))

    private fun seedAdvisories(lang: String) = listOf(
        AdvisoryItem(
            id = "adv_seed_c",
            title = appStr(lang, "adv_title_clean"),
            description = appStr(lang, "adv_welcome_desc"),
            severity = AlertSeverity.INFO
        ),
        AdvisoryItem(
            id = "adv_seed_w",
            title = appStr(lang, "adv_title_mod"),
            description = String.format(appStr(lang, "adv_desc_mod_bad"), "3"),
            severity = AlertSeverity.WARNING
        ),
        AdvisoryItem(
            id = "adv_seed_p",
            title = appStr(lang, "adv_title_pol"),
            description = String.format(appStr(lang, "adv_desc_pol_bad"), "1", appStr(lang, "rep_bad")),
            severity = AlertSeverity.CRITICAL
        )
    )

    private fun Advisory.toAdvisoryItem(): AdvisoryItem {
        return AdvisoryItem(
            id = id,
            title = title,
            description = description,
            severity = when (status) {
                "Critical" -> AlertSeverity.CRITICAL
                "Caution" -> AlertSeverity.WARNING
                else -> AlertSeverity.INFO
            }
        )
    }
}
