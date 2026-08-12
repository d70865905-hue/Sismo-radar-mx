package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sismo_radar_prefs", Context.MODE_PRIVATE)

    var minMagnitudeAlertThreshold: Double
        get() = prefs.getFloat("min_mag_threshold", 4.5f).toDouble()
        set(value) = prefs.edit().putFloat("min_mag_threshold", value.toFloat()).apply()

    var soundAlertsEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    var userLat: Double?
        get() {
            val valStr = prefs.getString("user_lat", null) ?: return null
            return valStr.toDoubleOrNull()
        }
        set(value) = prefs.edit().putString("user_lat", value?.toString()).apply()

    var userLon: Double?
        get() {
            val valStr = prefs.getString("user_lon", null) ?: return null
            return valStr.toDoubleOrNull()
        }
        set(value) = prefs.edit().putString("user_lon", value?.toString()).apply()

    var simulationCount: Int
        get() = prefs.getInt("simulation_count", 0)
        set(value) = prefs.edit().putInt("simulation_count", value).apply()
}
