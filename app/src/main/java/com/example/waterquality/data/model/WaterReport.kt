package com.example.waterquality.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "water_reports")
data class WaterReport(
    @PrimaryKey val id: String,
    val clarity: Int, // 1-5
    val smell: String,
    val flow: String, // Low, Medium, High
    val latitude: Double,
    val longitude: Double,
    val imagePath: String?,
    val timestamp: Long,
    val status: String = "PENDING" // PENDING, SYNCED
)
