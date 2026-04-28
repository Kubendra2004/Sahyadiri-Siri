package com.example.waterquality.di

import android.content.Context
import androidx.room.Room
import com.example.waterquality.data.local.WaterDao
import com.example.waterquality.data.local.WaterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideWaterDatabase(@ApplicationContext context: Context): WaterDatabase {
        return Room.databaseBuilder(
            context,
            WaterDatabase::class.java,
            "water_quality_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideWaterDao(database: WaterDatabase): WaterDao = database.waterDao()
}
