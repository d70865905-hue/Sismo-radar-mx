package com.example.data.repository

import com.example.data.local.EarthquakeDao
import com.example.data.local.UserPreferences
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.model.Earthquake
import com.example.data.remote.UsgsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class EarthquakeRepository(
    private val dao: EarthquakeDao,
    private val apiService: UsgsApiService,
    private val userPreferences: UserPreferences
) {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow<Long?>(null)
    val lastRefreshTime: StateFlow<Long?> = _lastRefreshTime.asStateFlow()

    fun getEarthquakesFlow(): Flow<List<Earthquake>> {
        val userLat = userPreferences.userLat
        val userLon = userPreferences.userLon
        return dao.getAllEarthquakes().map { entities ->
            entities.map { it.toDomain(userLat, userLon) }
        }
    }

    suspend fun refreshEarthquakes(): Result<Unit> {
        return try {
            val response = try {
                apiService.getRecentEarthquakes(limit = 100)
            } catch (e: Exception) {
                // Fallback to static summary feed if REST query fails
                apiService.getSummaryAllDay()
            }

            val entities = response.features.mapNotNull { feature ->
                val coords = feature.geometry.coordinates
                if (coords.size < 3) return@mapNotNull null
                val lon = coords[0]
                val lat = coords[1]
                val depth = coords[2]
                val props = feature.properties
                val mag = props.mag ?: 0.0
                val title = props.title ?: "M $mag - ${props.place ?: "Ubicación desconocida"}"

                Earthquake(
                    id = feature.id,
                    title = title,
                    magnitude = mag,
                    place = props.place ?: "Ubicación no especificada",
                    latitude = lat,
                    longitude = lon,
                    depthKm = depth,
                    timeMillis = props.time ?: System.currentTimeMillis(),
                    alertLevel = props.alert,
                    tsunami = props.tsunami ?: 0,
                    url = props.url,
                    mmi = props.mmi,
                    isSimulated = false
                ).toEntity()
            }

            if (entities.isNotEmpty()) {
                dao.insertEarthquakes(entities)
            }
            _isOnline.value = true
            _lastRefreshTime.value = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            _isOnline.value = false
            Result.failure(e)
        }
    }

    suspend fun deleteEarthquakeById(id: String) {
        dao.deleteById(id)
    }
}
