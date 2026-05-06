package com.example.waterquality.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.data.model.ReportWithUser
import com.example.waterquality.data.remote.AuthenticationManager
import com.example.waterquality.data.remote.BackendEndpointResolver
import com.example.waterquality.data.repository.WaterRepository
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.components.waterQualityFromReport
import com.example.waterquality.ui.utils.ConnectivityObserver
import com.example.waterquality.ui.utils.resolveRegionName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val repository: WaterRepository,
    private val authenticationManager: AuthenticationManager,
    private val backendEndpointResolver: BackendEndpointResolver,
    private val connectivityObserver: ConnectivityObserver,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ─── Raw reports from Room ───────────────────────────────────────────────
    val reports: StateFlow<List<WaterReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── Connectivity ────────────────────────────────────────────────────────
    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    // ─── Region name (GPS geocoder) ──────────────────────────────────────────
    private val _regionName = MutableStateFlow("Bengaluru Region")
    val regionName: StateFlow<String> = _regionName.asStateFlow()

    // ─── Gemini AI insight ───────────────────────────────────────────────────
    private val _geminiInsight = MutableStateFlow<String?>(null)
    val geminiInsight: StateFlow<String?> = _geminiInsight.asStateFlow()

    // ─── Alert badge count (unread non-clean reports) ────────────────────────
    val alertBadgeCount: StateFlow<Int> = reports.map { list ->
        list.count { waterQualityFromReport(it.clarity, it.smell) != WaterQuality.CLEAN }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ─── WQI trend (last 7 scores for sparkline) ─────────────────────────────
    val wqiTrend: StateFlow<List<Float>> = reports.map { list ->
        list.takeLast(7).map { r ->
            when (waterQualityFromReport(r.clarity, r.smell)) {
                WaterQuality.CLEAN    -> 70f + r.clarity * 6f
                WaterQuality.MODERATE -> 35f + r.clarity * 5f
                WaterQuality.POLLUTED -> r.clarity * 6f
            }.coerceIn(0f, 100f)
        }.ifEmpty { listOf(68f, 72f, 65f, 80f, 74f, 70f, 75f) } // demo data
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf(68f, 72f, 65f, 80f, 74f, 70f, 75f))

    // ─── Loading state ───────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ─── Error message ───────────────────────────────────────────────────────
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _backendBaseUrl = MutableStateFlow("Resolving backend...")
    val backendBaseUrl: StateFlow<String> = _backendBaseUrl.asStateFlow()

    private val _isBackendReachable = MutableStateFlow(false)
    val isBackendReachable: StateFlow<Boolean> = _isBackendReachable.asStateFlow()

    private val _mapReports = MutableStateFlow<List<WaterReport>>(emptyList())
    val mapReports: StateFlow<List<WaterReport>> = combine(reports, _mapReports) { localReports, remoteReports ->
        remoteReports.ifEmpty { localReports }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadRegionName()
        resolveBackendEndpoint()
        syncRemoteData()
    }

    fun loadRegionName() {
        viewModelScope.launch {
            _regionName.value = resolveRegionName(context)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            loadRegionName()
            resolveBackendEndpoint()
            syncRemoteData()
            kotlinx.coroutines.delay(800)
            _isLoading.value = false
        }
    }

    private fun resolveBackendEndpoint() {
        viewModelScope.launch {
            val resolvedBaseUrl = backendEndpointResolver.resolveAndPersist()
            _backendBaseUrl.value = resolvedBaseUrl
            _isBackendReachable.value = backendEndpointResolver.isActiveHealthy()
        }
    }

    private fun syncRemoteData() {
        if (!authenticationManager.isLoggedIn()) {
            return
        }

        viewModelScope.launch {
            repository.refreshRemoteData()
                .onFailure { _errorMessage.value = it.message ?: "Unable to sync remote data." }

            repository.getMapData()
                .onSuccess { remoteReports ->
                    _mapReports.value = remoteReports.map { it.toWaterReport() }
                }
                .onFailure {
                    _mapReports.value = emptyList()
                    if (_errorMessage.value == null) {
                        _errorMessage.value = it.message ?: "Unable to load map data."
                    }
                }
        }
    }


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
                    title       = "adv_welcome_title",
                    description = "adv_welcome_desc",
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
                    WaterQuality.CLEAN    -> "adv_title_clean"
                    WaterQuality.MODERATE -> "adv_title_mod"
                    WaterQuality.POLLUTED -> "adv_title_pol"
                }
                val desc = when (quality) {
                    WaterQuality.CLEAN    -> "adv_desc_clean|${r.clarity}"
                    WaterQuality.MODERATE ->
                        if (r.smell == "Bad") "adv_desc_mod_bad|${r.clarity}" else "adv_desc_mod_low|${r.clarity}"
                    WaterQuality.POLLUTED ->
                        if (r.smell == "Bad") "adv_desc_pol_bad|${r.clarity}" else "adv_desc_pol_abn|${r.clarity}"
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
                    title       = if (quality == WaterQuality.POLLUTED) "alt_pol_title" else "alt_warn_title",
                    message     = if (quality == WaterQuality.POLLUTED) "alt_pol_desc|${r.clarity}|${r.smell}" else "alt_warn_desc|${r.clarity}|${r.flow}",
                    severity    = if (quality == WaterQuality.POLLUTED) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
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
            title    = "seed_c_title",
            message  = "seed_c_desc",
            severity = AlertSeverity.WARNING,
            timestamp = System.currentTimeMillis() - 3_600_000L,
            locationTag = "12.2958, 76.6394"
        ),
        AlertItem(
            id       = "seed2",
            title    = "seed_t_title",
            message  = "seed_t_desc",
            severity = AlertSeverity.CRITICAL,
            timestamp = System.currentTimeMillis() - 7_200_000L,
            locationTag = "15.1394, 76.9214"
        ),
        AlertItem(
            id       = "seed3",
            title    = "seed_k_title",
            message  = "seed_k_desc",
            severity = AlertSeverity.INFO,
            timestamp = System.currentTimeMillis() - 86_400_000L,
            locationTag = "11.9891, 76.3610"
        )
    )

    private fun ReportWithUser.toWaterReport(): WaterReport {
        return WaterReport(
            id = id,
            userId = user.id,
            clarity = clarity,
            smell = smell,
            flow = flow,
            latitude = latitude,
            longitude = longitude,
            imagePath = imagePath,
            timestamp = timestamp,
            status = status,
            wqiScore = wqiScore,
            advisoryId = advisory?.id,
            localImagePath = null,
            syncTimestamp = System.currentTimeMillis()
        )
    }
}
