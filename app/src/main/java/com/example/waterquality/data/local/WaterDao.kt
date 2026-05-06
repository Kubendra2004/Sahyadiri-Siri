package com.example.waterquality.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    // ========================
    // Report Operations
    // ========================

    @Query("SELECT * FROM water_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WaterReport>>

    @Query("SELECT * FROM water_reports ORDER BY timestamp DESC")
    suspend fun getAllReportsSync(): List<WaterReport>

    @Query("SELECT * FROM water_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): WaterReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WaterReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<WaterReport>)

    @Query("SELECT * FROM water_reports WHERE status = 'PENDING'")
    suspend fun getPendingReports(): List<WaterReport>

    @Query("DELETE FROM water_reports WHERE id = :id")
    suspend fun deleteReport(id: String)

    @Query("DELETE FROM water_reports")
    suspend fun deleteAllReports()

    @Query("SELECT COUNT(*) FROM water_reports")
    suspend fun getReportCount(): Int

    @Query("SELECT * FROM water_reports WHERE status = 'SYNCED' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSyncedReports(limit: Int = 10): List<WaterReport>

    // ========================
    // Advisory Operations
    // ========================

    @Query("SELECT * FROM advisories ORDER BY timestamp DESC")
    fun getAllAdvisories(): Flow<List<Advisory>>

    @Query("SELECT * FROM advisories WHERE status = 'Critical' ORDER BY timestamp DESC")
    suspend fun getCriticalAdvisories(): List<Advisory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisory(advisory: Advisory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisories(advisories: List<Advisory>)

    @Delete
    suspend fun deleteAdvisory(advisory: Advisory)

    @Query("DELETE FROM advisories")
    suspend fun deleteAllAdvisories()
}

