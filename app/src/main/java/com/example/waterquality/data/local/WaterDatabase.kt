package com.example.waterquality.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.data.model.WaterReport

@Database(entities = [WaterReport::class, Advisory::class], version = 1, exportSchema = false)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
}
