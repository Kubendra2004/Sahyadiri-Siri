package com.example.waterquality.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    // Reports
    @Query("SELECT * FROM water_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WaterReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WaterReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<WaterReport>)

    @Query("SELECT * FROM water_reports WHERE status = 'PENDING'")
    suspend fun getPendingReports(): List<WaterReport>

    // Advisories
    @Query("SELECT * FROM advisories ORDER BY timestamp DESC")
    fun getAllAdvisories(): Flow<List<Advisory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisory(advisory: Advisory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisories(advisories: List<Advisory>)
}
