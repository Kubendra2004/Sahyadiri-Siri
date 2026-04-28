package com.example.waterquality.data.remote

import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("report")
    suspend fun submitReport(@Body report: WaterReport): WaterReport

    @GET("map-data")
    suspend fun getMapData(): List<WaterReport>

    @GET("alerts")
    suspend fun getAlerts(): List<Advisory>

    @GET("history")
    suspend fun getHistory(): List<WaterReport>
}
