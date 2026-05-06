package com.example.waterquality.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendEndpointResolver @Inject constructor(
    private val backendUrlManager: BackendUrlManager
) {
    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun resolveAndPersist(): String = withContext(Dispatchers.IO) {
        val candidates = backendUrlManager.getCandidates().distinct()
        for (baseUrl in candidates) {
            if (isHealthy(baseUrl)) {
                backendUrlManager.setActiveBaseUrl(baseUrl)
                return@withContext baseUrl
            }
        }
        backendUrlManager.getActiveBaseUrl()
    }

    suspend fun isActiveHealthy(): Boolean = withContext(Dispatchers.IO) {
        isHealthy(backendUrlManager.getActiveBaseUrl())
    }

    private fun isHealthy(baseUrl: String): Boolean {
        return try {
            val request = Request.Builder()
                .url("${baseUrl}health")
                .get()
                .build()

            probeClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }
}
