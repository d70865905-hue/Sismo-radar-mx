package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SismoDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.Earthquake
import com.example.data.remote.UsgsApiService
import com.example.data.repository.EarthquakeRepository
import com.example.service.AlertEngine
import com.example.service.AppLocationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SortMode(val label: String) {
    RECENT("Más Recientes"),
    MAGNITUDE("Mayor Magnitud"),
    DISTANCE("Más Cercanos"),
    DEPTH("Más Superficiales")
}

data class AlertSettingsState(
    val minMagnitudeThreshold: Double = 4.5,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

class EarthquakeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SismoDatabase.getDatabase(application)
    private val apiService = UsgsApiService.create()
    val userPreferences = UserPreferences(application)
    val alertEngine = AlertEngine(application)
    private val locationManager = AppLocationManager(application)

    val repository = EarthquakeRepository(db.earthquakeDao(), apiService, userPreferences)

    val isOnline: StateFlow<Boolean> = repository.isOnline
    val lastRefreshTime: StateFlow<Long?> = repository.lastRefreshTime

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedEarthquake = MutableStateFlow<Earthquake?>(null)
    val selectedEarthquake: StateFlow<Earthquake?> = _selectedEarthquake.asStateFlow()

    private val _activeSimulatedAlert = MutableStateFlow<Earthquake?>(null)
    val activeSimulatedAlert: StateFlow<Earthquake?> = _activeSimulatedAlert.asStateFlow()

    private val _minMagFilter = MutableStateFlow(0.0)
    val minMagFilter: StateFlow<Double> = _minMagFilter.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.RECENT)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    private val _alertSettings = MutableStateFlow(
        AlertSettingsState(
            minMagnitudeThreshold = userPreferences.minMagnitudeAlertThreshold,
            soundEnabled = userPreferences.soundAlertsEnabled,
            vibrationEnabled = userPreferences.vibrationEnabled
        )
    )
    val alertSettings: StateFlow<AlertSettingsState> = _alertSettings.asStateFlow()

    val filteredEarthquakes: StateFlow<List<Earthquake>> = combine(
        repository.getEarthquakesFlow(),
        _minMagFilter,
        _sortMode,
        _userLocation
    ) { rawList, minMag, sort, location ->
        var list = rawList.filter { it.magnitude >= minMag }

        // Recalculate distances if location updated
        if (location != null) {
            val (lat, lon) = location
            list = list.map { eq ->
                val dist = com.example.data.local.calculateHaversineDistanceKm(
                    lat, lon, eq.latitude, eq.longitude
                )
                eq.copy(distanceKm = dist)
            }
        }

        when (sort) {
            SortMode.RECENT -> list.sortedByDescending { it.timeMillis }
            SortMode.MAGNITUDE -> list.sortedByDescending { it.magnitude }
            SortMode.DISTANCE -> list.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            SortMode.DEPTH -> list.sortedBy { it.depthKm }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Load initial user location if stored or query location manager
        val savedLat = userPreferences.userLat
        val savedLon = userPreferences.userLon
        if (savedLat != null && savedLon != null) {
            _userLocation.value = Pair(savedLat, savedLon)
        } else {
            requestDeviceLocation()
        }

        // Periodic auto-refresh every 45 seconds for USGS real feed
        viewModelScope.launch {
            while (isActive) {
                repository.refreshEarthquakes()
                delay(45000)
            }
        }
    }

    fun requestDeviceLocation() {
        locationManager.getCurrentLocation { lat, lon ->
            _userLocation.value = Pair(lat, lon)
            userPreferences.userLat = lat
            userPreferences.userLon = lon
        }
    }

    fun fetchLatestEarthquakes() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshEarthquakes()
            _isRefreshing.value = false
        }
    }

    fun setMinMagFilter(minMag: Double) {
        _minMagFilter.value = minMag
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun selectEarthquake(earthquake: Earthquake?) {
        _selectedEarthquake.value = earthquake
    }

    fun triggerSimulationAlert(
        magnitude: Double = 6.8,
        place: String = "Sismo de Prueba (Modo Simulación)",
        depthKm: Double = 20.0
    ) {
        val userLoc = _userLocation.value ?: Pair(19.4326, -99.1332)
        val simEq = alertEngine.createSimulatedEarthquake(
            magnitude = magnitude,
            place = place,
            latitude = userLoc.first + 0.85,
            longitude = userLoc.second - 0.75,
            depthKm = depthKm
        )

        _activeSimulatedAlert.value = simEq
        userPreferences.simulationCount += 1

        if (_alertSettings.value.soundEnabled) {
            alertEngine.playAlertSound()
        }
        if (_alertSettings.value.vibrationEnabled) {
            alertEngine.triggerVibration()
        }
    }

    fun dismissSimulationAlert() {
        _activeSimulatedAlert.value = null
        alertEngine.stopAlertSound()
    }

    fun updateSettings(minMag: Double, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        _alertSettings.value = AlertSettingsState(
            minMagnitudeThreshold = minMag,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled
        )
        userPreferences.minMagnitudeAlertThreshold = minMag
        userPreferences.soundAlertsEnabled = soundEnabled
        userPreferences.vibrationEnabled = vibrationEnabled
    }
}
