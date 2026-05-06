package com.example.waterquality.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterquality.data.remote.AuthenticationManager
import com.example.waterquality.data.remote.BackendEndpointResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val backendEndpointResolver: BackendEndpointResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            isRegisterMode = !it.isRegisterMode,
            errorMessage = null
        )
    }

    fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun submit(onSuccess: () -> Unit) {
        val snapshot = _uiState.value
        if (snapshot.email.isBlank() || snapshot.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }
        if (snapshot.isRegisterMode && snapshot.displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Display name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            backendEndpointResolver.resolveAndPersist()

            val result = if (snapshot.isRegisterMode) {
                authenticationManager.register(snapshot.email.trim(), snapshot.password, snapshot.displayName.trim())
            } else {
                authenticationManager.login(snapshot.email.trim(), snapshot.password)
            }

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                onSuccess()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Authentication failed."
                    )
                }
            }
        }
    }

    fun submitGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            backendEndpointResolver.resolveAndPersist()

            authenticationManager.loginWithGoogle(idToken)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Google authentication failed."
                        )
                    }
                }
        }
    }
}
