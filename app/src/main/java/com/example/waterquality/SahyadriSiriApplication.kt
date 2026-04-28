package com.example.waterquality

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class SahyadriSiriApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // osmdroid configuration — must be called before any MapView is created.
        // Uses the app's private cache dir: no WRITE_EXTERNAL_STORAGE needed on API 29+.
        Configuration.getInstance().apply {
            userAgentValue = packageName          // required by OSM tile policy
            osmdroidBasePath = cacheDir           // app-private dir
            osmdroidTileCache = cacheDir.resolve("osmdroid/tiles")
        }
    }
}
