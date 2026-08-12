package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Earthquake

@Entity(tableName = "earthquakes")
data class EarthquakeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val magnitude: Double,
    val place: String,
    val latitude: Double,
    val longitude: Double,
    val depthKm: Double,
    val timeMillis: Long,
    val alertLevel: String?,
    val tsunami: Int,
    val url: String?,
    val mmi: Double?,
    val isSimulated: Boolean = false,
    val cachedAtMillis: Long = System.currentTimeMillis()
)

fun EarthquakeEntity.toDomain(userLat: Double? = null, userLon: Double? = null): Earthquake {
    val distance = if (userLat != null && userLon != null) {
        calculateHaversineDistanceKm(userLat, userLon, latitude, longitude)
    } else null

    return Earthquake(
        id = id,
        title = title,
        magnitude = magnitude,
        place = place,
        latitude = latitude,
        longitude = longitude,
        depthKm = depthKm,
        timeMillis = timeMillis,
        alertLevel = alertLevel,
        tsunami = tsunami,
        url = url,
        mmi = mmi,
        isSimulated = isSimulated,
        distanceKm = distance
    )
}

fun Earthquake.toEntity(): EarthquakeEntity {
    return EarthquakeEntity(
        id = id,
        title = title,
        magnitude = magnitude,
        place = place,
        latitude = latitude,
        longitude = longitude,
        depthKm = depthKm,
        timeMillis = timeMillis,
        alertLevel = alertLevel,
        tsunami = tsunami,
        url = url,
        mmi = mmi,
        isSimulated = isSimulated
    )
}

fun calculateHaversineDistanceKm(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}
