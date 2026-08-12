package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.Earthquake
import java.util.UUID

class AlertEngine(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 90)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAlertSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlertSound() {
        try {
            toneGenerator?.stopTone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 300, 200, 300, 200, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createSimulatedEarthquake(
        magnitude: Double = 6.8,
        place: String = "SIMULACIÓN - Zona de Falla Subducción (Prueba)",
        latitude: Double = 19.4326,
        longitude: Double = -99.1332,
        depthKm: Double = 22.0
    ): Earthquake {
        return Earthquake(
            id = "sim_" + UUID.randomUUID().toString().take(8),
            title = "M $magnitude - $place",
            magnitude = magnitude,
            place = place,
            latitude = latitude,
            longitude = longitude,
            depthKm = depthKm,
            timeMillis = System.currentTimeMillis(),
            alertLevel = if (magnitude >= 6.0) "red" else "orange",
            tsunami = if (magnitude >= 7.0) 1 else 0,
            url = null,
            mmi = magnitude + 0.5,
            isSimulated = true,
            distanceKm = 120.0
        )
    }
}
