package com.example.waterquality.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val backendUrlManager: BackendUrlManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val activeBase = backendUrlManager.getActiveBaseUrl().toHttpUrlOrNull()
        if (activeBase == null) {
            return chain.proceed(request)
        }

        val originalUrl = request.url
        val mergedPath = buildMergedPath(activeBase.encodedPath, originalUrl.encodedPath)

        val newUrl = originalUrl.newBuilder()
            .scheme(activeBase.scheme)
            .host(activeBase.host)
            .port(activeBase.port)
            .encodedPath(mergedPath)
            .build()

        return chain.proceed(request.newBuilder().url(newUrl).build())
    }

    private fun buildMergedPath(basePath: String, requestPath: String): String {
        val normalizedBase = if (basePath.endsWith("/")) basePath.dropLast(1) else basePath
        val normalizedRequest = requestPath.trimStart('/')
        return if (normalizedBase.isBlank()) {
            "/$normalizedRequest"
        } else {
            "$normalizedBase/$normalizedRequest"
        }
    }
}
