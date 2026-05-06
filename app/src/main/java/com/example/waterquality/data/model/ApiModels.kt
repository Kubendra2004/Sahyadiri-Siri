package com.example.waterquality.data.model

import com.squareup.moshi.Json

/**
 * Authentication request for register/login
 */
data class AuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "displayName") val displayName: String? = null
)

data class GoogleAuthRequest(
    @Json(name = "idToken") val idToken: String,
    @Json(name = "displayName") val displayName: String? = null
)

/**
 * Authentication response with tokens and user info
 */
data class AuthResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "user") val user: UserPublic? = null,
    @Json(name = "tokenType") val tokenType: String = "Bearer"
)

/**
 * Refresh token request
 */
data class RefreshTokenRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

/**
 * Refresh token response
 */
data class AccessTokenResponse(
    @Json(name = "accessToken") val accessToken: String
)

/**
 * Report submission request
 */
data class ReportRequest(
    @Json(name = "clarity") val clarity: Int,
    @Json(name = "smell") val smell: String,
    @Json(name = "flow") val flow: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "imagePath") val imagePath: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)

/**
 * Report response from server
 */
data class ReportResponse(
    @Json(name = "id") val id: String,
    @Json(name = "clarity") val clarity: Int,
    @Json(name = "smell") val smell: String,
    @Json(name = "flow") val flow: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "imagePath") val imagePath: String?,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "status") val status: String,
    @Json(name = "wqiScore") val wqiScore: Float,
    @Json(name = "userId") val userId: String,
    @Json(name = "advisoryId") val advisoryId: String?
)

/**
 * Public user info returned by backend
 */
data class UserPublic(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "createdAt") val createdAt: Long? = null
)

/**
 * Advisory returned by backend
 */
data class AdvisoryRead(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "status") val status: String,
    @Json(name = "timestamp") val timestamp: Long
)

/**
 * Report with nested user/advisory, used by /history and /map-data
 */
data class ReportWithUser(
    @Json(name = "id") val id: String,
    @Json(name = "clarity") val clarity: Int,
    @Json(name = "smell") val smell: String,
    @Json(name = "flow") val flow: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "imagePath") val imagePath: String?,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "status") val status: String,
    @Json(name = "wqiScore") val wqiScore: Float,
    @Json(name = "user") val user: UserPublic,
    @Json(name = "advisory") val advisory: AdvisoryRead?
)

/**
 * Image upload response
 */
data class UploadResponse(
    @Json(name = "imageUrl") val imageUrl: String,
    @Json(name = "imagePath") val imagePath: String
)

/**
 * Health check response
 */
data class HealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "dbConnected") val dbConnected: Boolean,
    @Json(name = "redisConnected") val redisConnected: Boolean,
    @Json(name = "uptimeS") val uptimeS: Double,
    @Json(name = "version") val version: String
)
