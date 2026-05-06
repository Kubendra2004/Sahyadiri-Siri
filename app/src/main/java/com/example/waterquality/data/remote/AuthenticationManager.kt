package com.example.waterquality.data.remote

import android.content.Context
import com.example.waterquality.data.model.AuthRequest
import com.example.waterquality.data.model.GoogleAuthRequest
import com.example.waterquality.data.model.UserPublic
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Authentication manager
 * Handles login, registration, and token management
 */
@Singleton
class AuthenticationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    private val _currentUser = MutableStateFlow<UserPublic?>(null)
    val currentUser: StateFlow<UserPublic?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // Check if user is already logged in
        _isLoggedIn.value = AuthInterceptor.isLoggedIn(context)
    }

    suspend fun register(email: String, password: String, displayName: String): Result<UserPublic> {
        return try {
            val response = apiService.register(
                AuthRequest(
                    email = email,
                    password = password,
                    displayName = displayName
                )
            )
            // Save tokens
            response.refreshToken?.let { refreshToken ->
                AuthInterceptor.saveTokens(context, response.accessToken, refreshToken)
            }
            // Update state
            response.user?.let { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                return Result.success(user)
            }
            Result.failure(Exception("No user info in response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserPublic> {
        return try {
            val response = apiService.login(
                AuthRequest(
                    email = email,
                    password = password
                )
            )
            // Save tokens
            response.refreshToken?.let { refreshToken ->
                AuthInterceptor.saveTokens(context, response.accessToken, refreshToken)
            }
            // Update state
            response.user?.let { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                return Result.success(user)
            }
            Result.failure(Exception("No user info in response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String, displayName: String? = null): Result<UserPublic> {
        return try {
            val response = apiService.googleAuth(
                GoogleAuthRequest(
                    idToken = idToken,
                    displayName = displayName
                )
            )
            response.refreshToken?.let { refreshToken ->
                AuthInterceptor.saveTokens(context, response.accessToken, refreshToken)
            }
            response.user?.let { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                return Result.success(user)
            }
            Result.failure(Exception("No user info in response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        AuthInterceptor.clearAllTokens(context)
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun getAccessToken(): String? = AuthInterceptor.getAccessToken(context)

    fun isLoggedIn(): Boolean = AuthInterceptor.isLoggedIn(context)
}
