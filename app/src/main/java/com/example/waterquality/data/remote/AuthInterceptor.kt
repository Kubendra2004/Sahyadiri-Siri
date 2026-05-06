package com.example.waterquality.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.waterquality.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

import javax.inject.Provider

/**
 * OkHttp interceptor that adds JWT Bearer token to all requests
 * Automatically handles token refresh on 401 responses
 */
class AuthInterceptor(
    private val context: Context,
    private val apiServiceProvider: Provider<ApiService>
) : Interceptor {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Add token to request if available
        val accessToken = getAccessToken()
        if (accessToken != null) {
            request = addTokenToRequest(request, accessToken)
        }

        var response = chain.proceed(request)

        // Handle 401 Unauthorized - try to refresh token
        if (response.code == 401 && accessToken != null) {
            response.close()

            val refreshToken = getRefreshToken()
            if (refreshToken != null) {
                val newAccessToken = refreshAccessToken(refreshToken)
                if (newAccessToken != null) {
                    // Retry original request with new token
                    request = addTokenToRequest(request, newAccessToken)
                    response = chain.proceed(request)
                } else {
                    // Refresh failed - clear tokens and logout
                    clearTokens()
                }
            } else {
                // No refresh token - clear tokens and logout
                clearTokens()
            }
        }

        return response
    }

    private fun addTokenToRequest(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val response = runBlocking {
                apiServiceProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
            }
            val newAccessToken = response.accessToken
            saveAccessToken(newAccessToken)
            newAccessToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    private fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    private fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    private fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    private fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
        }

        fun getAccessToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_ACCESS_TOKEN, null)
        }

        fun clearAllTokens(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply()
        }

        fun isLoggedIn(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_ACCESS_TOKEN, null) != null
        }
    }
}
