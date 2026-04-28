package com.example.waterquality.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "advisories")
data class Advisory(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String, // e.g., Critical, Safe
    val timestamp: Long
)
