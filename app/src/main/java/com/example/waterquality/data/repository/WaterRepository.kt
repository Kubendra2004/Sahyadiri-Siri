package com.example.waterquality.data.repository

import com.example.waterquality.data.local.WaterDao
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepository @Inject constructor(
    private val waterDao: WaterDao,
    private val apiService: ApiService
) {
    val allReports: Flow<List<WaterReport>> = waterDao.getAllReports()

    suspend fun submitReport(report: WaterReport) {
        waterDao.insertReport(report)
        // Simple sync logic for now
        try {
            apiService.submitReport(report)
            waterDao.insertReport(report.copy(status = "SYNCED"))
        } catch (e: Exception) {
            // Keep status as PENDING if network fails
        }
    }

    suspend fun getPendingReports() = waterDao.getPendingReports()
}
