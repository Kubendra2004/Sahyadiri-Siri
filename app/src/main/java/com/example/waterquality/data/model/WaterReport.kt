package com.example.waterquality.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

/**
 * Water Quality Report
 * Synced with backend via API
 * Stored locally in Room database
 */
@Serializable
@Entity(
    tableName = "water_reports",
    foreignKeys = [
        ForeignKey(
            entity = Advisory::class,
            parentColumns = ["id"],
            childColumns = ["advisoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("timestamp"),
        Index("advisoryId")
    ]
)
data class WaterReport(
    @PrimaryKey
    val id: String,

    @Json(name = "userId")
    val userId: String,

    @Json(name = "clarity")
    val clarity: Int, // 1-5 scale: 1=Opaque, 5=Crystal Clear

    @Json(name = "smell")
    val smell: String, // Normal, Bad

    @Json(name = "flow")
    val flow: String, // Low, Medium, High

    @Json(name = "latitude")
    val latitude: Double,

    @Json(name = "longitude")
    val longitude: Double,

    @Json(name = "imagePath")
    val imagePath: String? = null,

    @Json(name = "timestamp")
    val timestamp: Long, // Epoch milliseconds from backend

    @Json(name = "status")
    val status: String = "PENDING", // PENDING, SYNCED, FAILED

    @Json(name = "wqiScore")
    val wqiScore: Float = 0f, // 0-100 scale

    @Json(name = "advisoryId")
    val advisoryId: String? = null,

    // Local fields (not from server)
    val localImagePath: String? = null, // Temporary local file path before upload
    val syncTimestamp: Long = 0 // When synced to server
) {
    val isSynced: Boolean
        get() = status == "SYNCED"

    val isPending: Boolean
        get() = status == "PENDING"

    val hasFailed: Boolean
        get() = status == "FAILED"

    val wqiStatus: String
        get() = when {
            wqiScore >= 65 -> "Safe"
            wqiScore >= 35 -> "Caution"
            else -> "Critical"
        }
}

