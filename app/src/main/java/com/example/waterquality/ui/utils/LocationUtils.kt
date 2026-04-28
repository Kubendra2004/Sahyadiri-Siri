package com.example.waterquality.ui.utils

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Resolve a human-readable city/region name from the device's last known GPS location.
 * Falls back to "Bengaluru Region" when location/geocoder is unavailable.
 */
suspend fun resolveRegionName(context: Context): String {
    return try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location: Location? = suspendCancellableCoroutine { cont ->
            try {
                client.lastLocation
                    .addOnSuccessListener { loc: Location? -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            } catch (e: SecurityException) {
                cont.resume(null)
            }
        }
        if (location == null) return "Bengaluru Region"

        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    val city = addresses.firstOrNull()?.locality
                        ?: addresses.firstOrNull()?.subAdminArea
                        ?: "Bengaluru Region"
                    cont.resume(city)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: "Bengaluru Region"
        }
    } catch (e: Exception) {
        "Bengaluru Region"
    }
}
