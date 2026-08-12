package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Earthquake(
    val id: String,
    val title: String,
    val magnitude: Double,
    val place: String,
    val latitude: Double,
    val longitude: Double,
    val depthKm: Double,
    val timeMillis: Long,
    val alertLevel: String? = null,
    val tsunami: Int = 0,
    val url: String? = null,
    val mmi: Double? = null,
    val isSimulated: Boolean = false,
    val distanceKm: Double? = null
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            return sdf.format(Date(timeMillis))
        }

    val timeAgoString: String
        get() {
            val diffMs = System.currentTimeMillis() - timeMillis
            if (diffMs < 0) return "Ahora mismo"
            val mins = diffMs / (1000 * 60)
            if (mins < 1) return "Hace momentos"
            if (mins < 60) return "Hace ${mins}m"
            val hours = mins / 60
            if (hours < 24) return "Hace ${hours}h"
            val days = hours / 24
            return "Hace ${days}d"
        }

    val depthCategory: DepthCategory
        get() = when {
            depthKm < 70 -> DepthCategory.SUPERFICIAL
            depthKm < 300 -> DepthCategory.INTERMEDIO
            else -> DepthCategory.PROFUNDO
        }
}

enum class DepthCategory(val label: String) {
    SUPERFICIAL("Superficial (< 70 km)"),
    INTERMEDIO("Intermedio (70 - 300 km)"),
    PROFUNDO("Profundo (> 300 km)")
}

// GeoJSON API DTOs for USGS REST endpoint
@JsonClass(generateAdapter = true)
data class UsgsGeoJsonResponse(
    val type: String,
    val features: List<UsgsFeature>
)

@JsonClass(generateAdapter = true)
data class UsgsFeature(
    val id: String,
    val properties: UsgsProperties,
    val geometry: UsgsGeometry
)

@JsonClass(generateAdapter = true)
data class UsgsProperties(
    val mag: Double?,
    val place: String?,
    val time: Long?,
    val updated: Long?,
    val url: String?,
    val detail: String?,
    val alert: String?,
    val status: String?,
    val tsunami: Int?,
    val sig: Int?,
    val title: String?,
    val mmi: Double?
)

@JsonClass(generateAdapter = true)
data class UsgsGeometry(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude, depth]
)
