package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AppLocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (latitude: Double, longitude: Double) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        // Default location (e.g., Lat 19.4326, Lon -99.1332)
                        onLocationReceived(19.4326, -99.1332)
                    }
                }
                .addOnFailureListener {
                    onLocationReceived(19.4326, -99.1332)
                }
        } catch (e: Exception) {
            onLocationReceived(19.4326, -99.1332)
        }
    }
}
