package com.example.ecowatch.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Locale

object LocationHelper {

    /** Essaie de lire la dernière localisation en cache. Peut retourner null. */
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return runCatching { client.lastLocation.await() }.getOrNull()
    }

    /** Reverse geocoding simple -> adresse lisible (ou null). */
    fun reverseGeocode(context: Context, lat: Double, lon: Double): String? {
        return try {
            val geo = Geocoder(context, Locale.getDefault())
            val list = geo.getFromLocation(lat, lon, 1)
            if (!list.isNullOrEmpty()) {
                val a = list[0]
                a.getAddressLine(0) ?: "${a.locality.orEmpty()} ${a.countryName.orEmpty()}".trim()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Best-effort: tente d’abord le cache, sinon demande une mesure “fraîche”
     * en haute précision. Peut aussi retourner null.
     */
    @SuppressLint("MissingPermission")
    suspend fun getBestLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)

        // 1) cache
        getLastLocation(context)?.let { return it }

        // 2) mesure actuelle
        return runCatching {
            client.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                /* cancellationToken = */ null
            ).await()
        }.getOrNull()
    }
}
