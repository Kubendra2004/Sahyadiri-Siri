package com.example.waterquality.data.repository

import com.example.waterquality.data.local.WaterDao
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.data.remote.ApiService
import com.example.waterquality.data.model.ReportRequest
import com.example.waterquality.data.model.AdvisoryRead
import com.example.waterquality.data.model.ReportWithUser
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Water Quality Report Repository
 * Manages local (Room) and remote (API) data synchronization
 */
@Singleton
class WaterRepository @Inject constructor(
    private val waterDao: WaterDao,
    private val apiService: ApiService
) {
    val allReports: Flow<List<WaterReport>> = waterDao.getAllReports()

    // ========================
    // Report Operations
    // ========================

    /**
     * Submit a water quality report
     * 1. Saves to local database
     * 2. Uploads image if provided
     * 3. Submits to backend
     * 4. Updates local status
     */
    suspend fun submitReport(
        clarity: Int,
        smell: String,
        flow: String,
        latitude: Double,
        longitude: Double,
        localImagePath: String? = null
    ): Result<WaterReport> {
        return try {
            var imagePath: String? = null

            // Step 1: Upload image if provided
            if (localImagePath != null) {
                val imageFile = File(localImagePath)
                if (imageFile.exists() && imageFile.length() <= 10 * 1024 * 1024) {
                    val mimeType = when {
                        localImagePath.endsWith(".png", ignoreCase = true) -> "image/png"
                        else -> "image/jpeg"
                    }
                    val requestBody = imageFile.asRequestBody(mimeType.toMediaType())
                    val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                    
                    try {
                        val uploadResponse = apiService.uploadImage(imagePart)
                        imagePath = uploadResponse.imagePath
                    } catch (e: Exception) {
                        // Log but continue - image upload is optional
                        e.printStackTrace()
                    }
                }
            }

            // Step 2: Submit report to backend
            val request = ReportRequest(
                clarity = clarity,
                smell = smell,
                flow = flow,
                latitude = latitude,
                longitude = longitude,
                imagePath = imagePath
            )

            val response = apiService.submitReport(request)

            // Step 3: Save to local database
            val report = WaterReport(
                id = response.id,
                userId = response.userId,
                clarity = response.clarity,
                smell = response.smell,
                flow = response.flow,
                latitude = response.latitude,
                longitude = response.longitude,
                imagePath = response.imagePath,
                timestamp = response.timestamp,
                status = "SYNCED",
                wqiScore = response.wqiScore,
                advisoryId = response.advisoryId,
                localImagePath = localImagePath,
                syncTimestamp = System.currentTimeMillis()
            )
            waterDao.insertReport(report)

            Result.success(report)
        } catch (e: Exception) {
            // Save pending report locally on error
            try {
                val pendingReport = WaterReport(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = "",
                    clarity = clarity,
                    smell = smell,
                    flow = flow,
                    latitude = latitude,
                    longitude = longitude,
                    imagePath = null,
                    timestamp = System.currentTimeMillis(),
                    status = "PENDING",
                    localImagePath = localImagePath
                )
                waterDao.insertReport(pendingReport)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
            Result.failure(e)
        }
    }

    // ========================
    // Report Retrieval
    // ========================

    /**
     * Get user's report history
     */
    suspend fun getHistory(): Result<List<WaterReport>> {
        return try {
            val reports = apiService.getHistory()
            val mapped = reports.map { it.toWaterReport() }
            waterDao.insertAll(mapped)
            reports.forEach { report -> report.advisory?.let { waterDao.insertAdvisory(it.toAdvisory()) } }
            Result.success(mapped)
        } catch (e: Exception) {
            // Fallback to local cache
            val cached = waterDao.getAllReportsSync()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun refreshAlerts(): Result<List<Advisory>> {
        return try {
            val alerts = apiService.getAlerts()
            val mapped = alerts.map {
                Advisory(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    status = it.status,
                    timestamp = it.timestamp,
                    reportId = null
                )
            }
            waterDao.deleteAllAdvisories()
            waterDao.insertAdvisories(mapped)
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshRemoteData(): Result<Unit> {
        val historyResult = getHistory()
        val alertsResult = refreshAlerts()
        return when {
            historyResult.isSuccess && alertsResult.isSuccess -> Result.success(Unit)
            historyResult.isFailure -> Result.failure(historyResult.exceptionOrNull()!!)
            else -> Result.failure(alertsResult.exceptionOrNull()!!)
        }
    }

    /**
     * Get all reports for map display (cached at backend)
     */
    suspend fun getMapData(): Result<List<ReportWithUser>> {
        return try {
            val reports = apiService.getMapData()
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get pending reports (not yet synced)
     */
    suspend fun getPendingReports(): List<WaterReport> {
        return waterDao.getPendingReports()
    }

    /**
     * Retry syncing pending reports
     */
    suspend fun retrySyncPending() {
        val pending = getPendingReports()
        for (report in pending) {
            try {
                val request = ReportRequest(
                    clarity = report.clarity,
                    smell = report.smell,
                    flow = report.flow,
                    latitude = report.latitude,
                    longitude = report.longitude,
                    imagePath = report.imagePath
                )
                apiService.submitReport(request)
                // Update status to synced
                waterDao.insertReport(report.copy(status = "SYNCED"))
            } catch (e: Exception) {
                // Keep as pending
                e.printStackTrace()
            }
        }
    }

    // ========================
    // Advisory/Alert Operations
    // ========================

    /**
     * Get all advisories
     */
    suspend fun getAlerts(): Result<List<Advisory>> {
        return try {
            refreshAlerts()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get local advisories
     */
    fun getLocalAdvisories(): Flow<List<Advisory>> {
        return waterDao.getAllAdvisories()
    }

    /**
     * Save advisory locally
     */
    suspend fun saveAdvisory(advisory: Advisory) {
        waterDao.insertAdvisory(advisory)
    }

    // ========================
    // Local Data Management
    // ========================

    /**
     * Clear all local reports
     */
    suspend fun clearAllReports() {
        waterDao.deleteAllReports()
    }

    /**
     * Delete a specific report
     */
    suspend fun deleteReport(reportId: String) {
        waterDao.deleteReport(reportId)
    }

    /**
     * Get total report count
     */
    suspend fun getReportCount(): Int {
        return waterDao.getReportCount()
    }

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

    private fun AdvisoryRead.toAdvisory(): Advisory {
        return Advisory(
            id = id,
            title = title,
            description = description,
            status = status,
            timestamp = timestamp,
            reportId = null
        )
    }
}

