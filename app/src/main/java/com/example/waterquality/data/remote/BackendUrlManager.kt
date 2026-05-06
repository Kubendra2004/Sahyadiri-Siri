package com.example.waterquality.data.remote

import android.content.Context
import com.example.waterquality.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendUrlManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getActiveBaseUrl(): String {
        val persisted = prefs.getString(KEY_ACTIVE_BASE_URL, null)
        if (!persisted.isNullOrBlank()) {
            return ensureTrailingSlash(persisted)
        }
        return ensureTrailingSlash(BuildConfig.API_BASE_URL)
    }

    fun setActiveBaseUrl(url: String) {
        prefs.edit().putString(KEY_ACTIVE_BASE_URL, ensureTrailingSlash(url)).apply()
    }

    fun getCandidates(): List<String> {
        val configured = BuildConfig.API_BASE_URL_CANDIDATES
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map(::ensureTrailingSlash)

        val active = getActiveBaseUrl()
        return listOf(active) + configured
    }

    private fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    private companion object {
        const val PREFS_NAME = "network_config"
        const val KEY_ACTIVE_BASE_URL = "active_base_url"
    }
}
