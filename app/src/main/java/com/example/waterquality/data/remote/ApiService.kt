package com.example.waterquality.data.remote

import com.example.waterquality.data.model.AccessTokenResponse
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.AuthRequest
import com.example.waterquality.data.model.AuthResponse
import com.example.waterquality.data.model.GoogleAuthRequest
import com.example.waterquality.data.model.HealthResponse
import com.example.waterquality.data.model.RefreshTokenRequest
import com.example.waterquality.data.model.ReportRequest
import com.example.waterquality.data.model.ReportResponse
import com.example.waterquality.data.model.ReportWithUser
import com.example.waterquality.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Sahyadri-Siri API Service
 * Base URL: http://localhost:8000/api (dev) or https://api.sahyadri-siri.com/api (prod)
 */
interface ApiService {
    // ========================
    // Authentication Endpoints
    // ========================
    
    /**
     * POST /auth/register
     * Register a new user account
     * Returns: { accessToken, refreshToken, user }
     */
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    /**
     * POST /auth/login
     * Login with email and password
     * Returns: { accessToken, refreshToken, user }
     */
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    /**
     * POST /auth/google
     * Login or signup with Google ID token
     * Returns: { accessToken, refreshToken, user }
     */
    @POST("auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse

    /**
     * POST /auth/refresh
     * Refresh access token using refresh token
     * Returns: { accessToken }
     */
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AccessTokenResponse

    // ========================
    // Report Endpoints
    // ========================

    /**
     * POST /report
     * Submit a new water quality report
     * Headers: Authorization: Bearer <accessToken>
     * Returns: ReportResponse
     */
    @POST("report")
    suspend fun submitReport(@Body request: ReportRequest): ReportResponse

    /**
     * GET /history
     * Get user's report history
     * Headers: Authorization: Bearer <accessToken>
     * Returns: List<WaterReport>
     */
    @GET("history")
    suspend fun getHistory(): List<ReportWithUser>

    /**
     * GET /map-data
     * Get all reports for map display (cached 30s)
     * Returns: List<MapMarker>
     */
    @GET("map-data")
    suspend fun getMapData(): List<ReportWithUser>

    // ========================
    // File Upload Endpoint
    // ========================

    /**
     * POST /upload-image
     * Upload water quality image (JPEG/PNG, max 10MB)
     * Headers: Authorization: Bearer <accessToken>
     * Returns: { imageUrl, imagePath }
     */
    @Multipart
    @POST("upload-image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): UploadResponse

    // ========================
    // Alerts Endpoint
    // ========================

    /**
     * GET /alerts
     * Get list of advisories/alerts
     * Returns: List<Advisory>
     */
    @GET("alerts")
    suspend fun getAlerts(): List<Advisory>

    // ========================
    // Health Check (No Auth)
    // ========================

    /**
     * GET /health
     * Health check endpoint (no authentication required)
     * Returns: { status, database, redis }
     */
    @GET("health")
    suspend fun healthCheck(): HealthResponse
}
