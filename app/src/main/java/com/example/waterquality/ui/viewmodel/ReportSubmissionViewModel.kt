package com.example.waterquality.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterquality.data.repository.WaterRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 3-step wizard: Photo(0) → Details(1) → Location(2) */
data class ReportSubmissionUiState(
    val currentStep:      Int     = 0,
    val clarity:          Int     = 3,
    val smell:            String  = "Normal",
    val flow:             String  = "Medium",
    val latitude:         Double  = 0.0,
    val longitude:        Double  = 0.0,
    val imagePath:        String? = null,
    val isSubmitting:     Boolean = false,
    val submissionSuccess:Boolean = false,
    val errorMessage:     String? = null
)

@HiltViewModel
class ReportSubmissionViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportSubmissionUiState())
    val uiState: StateFlow<ReportSubmissionUiState> = _uiState.asStateFlow()

    // ─── Step navigation ─────────────────────────────────────────────────────
    fun nextStep() = _uiState.update {
        it.copy(currentStep = (it.currentStep + 1).coerceAtMost(2))
    }
    fun prevStep() = _uiState.update {
        it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0))
    }

    // ─── Field updates ────────────────────────────────────────────────────────
    fun onClarityChange(c: Int) = _uiState.update { it.copy(clarity = c) }
    fun onSmellChange(s: String) = _uiState.update { it.copy(smell = s) }
    fun onFlowChange(f: String)  = _uiState.update { it.copy(flow = f) }

    fun onImageCaptured(path: String) {
        _uiState.update { it.copy(imagePath = path, currentStep = 1) }
    }

    fun onLocationCaptured(lat: Double, lon: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lon) }
    }

    /** Auto-fetch last known location — called from the composable after permissions are granted. */
    fun fetchLocation(context: Context) {
        val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        try {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let { onLocationCaptured(it.latitude, it.longitude) }
                }
        } catch (e: SecurityException) {
            Log.e("ReportVM", "Location permission error", e)
        }
    }

    // ─── Submit ───────────────────────────────────────────────────────────────
    fun submitReport() {
        if (_uiState.value.latitude == 0.0) {
            _uiState.update { it.copy(errorMessage = "Please capture your location before submitting.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val s = _uiState.value
                val result = repository.submitReport(
                    clarity        = s.clarity,
                    smell          = s.smell,
                    flow           = s.flow,
                    latitude       = s.latitude,
                    longitude      = s.longitude,
                    localImagePath = s.imagePath
                )
                if (result.isSuccess) {
                    _uiState.update { it.copy(isSubmitting = false, submissionSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = "Submission failed. Saved offline.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = "Submission failed. Saved offline.")
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun resetSuccess() = _uiState.update { it.copy(submissionSuccess = false) }
}
