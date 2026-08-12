package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EarthquakeDetailBottomSheet
import com.example.ui.components.SimulationAlertModal
import com.example.ui.screens.AlertsAndSimulationScreen
import com.example.ui.screens.EarthquakeListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimarySeismic
import com.example.ui.theme.SismoRadarTheme
import com.example.ui.viewmodel.EarthquakeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EarthquakeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.requestDeviceLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            SismoRadarTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Inicio", Icons.Filled.Home, Icons.Outlined.Home, "nav_home_tab"),
    MAP("Mapa", Icons.Filled.Map, Icons.Outlined.Map, "nav_map_tab"),
    LIST("Sismos", Icons.Filled.List, Icons.Outlined.List, "nav_list_tab"),
    ALERTS("Alertas", Icons.Filled.NotificationsActive, Icons.Outlined.NotificationsActive, "nav_alerts_tab")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: EarthquakeViewModel) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val earthquakes by viewModel.filteredEarthquakes.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val selectedEarthquake by viewModel.selectedEarthquake.collectAsStateWithLifecycle()
    val activeSimulatedAlert by viewModel.activeSimulatedAlert.collectAsStateWithLifecycle()
    val minMagFilter by viewModel.minMagFilter.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val alertSettings by viewModel.alertSettings.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = PrimarySeismic,
                            indicatorColor = PrimarySeismic,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (selectedTabIndex) {
            0 -> HomeScreen(
                earthquakes = earthquakes,
                isOnline = isOnline,
                isRefreshing = isRefreshing,
                userLocation = userLocation,
                selectedEarthquake = selectedEarthquake,
                minMagFilter = minMagFilter,
                onSelectEarthquake = { viewModel.selectEarthquake(it) },
                onFilterMinMag = { viewModel.setMinMagFilter(it) },
                onRefresh = { viewModel.fetchLatestEarthquakes() },
                onNavigateToSimulation = { selectedTabIndex = 3 },
                modifier = screenModifier
            )
            1 -> MapScreen(
                earthquakes = earthquakes,
                userLocation = userLocation,
                selectedEarthquake = selectedEarthquake,
                onSelectEarthquake = { viewModel.selectEarthquake(it) },
                modifier = screenModifier
            )
            2 -> EarthquakeListScreen(
                earthquakes = earthquakes,
                sortMode = sortMode,
                onSortModeChange = { viewModel.setSortMode(it) },
                onSelectEarthquake = { viewModel.selectEarthquake(it) },
                modifier = screenModifier
            )
            3 -> AlertsAndSimulationScreen(
                alertSettings = alertSettings,
                onUpdateSettings = { minMag, sound, vib ->
                    viewModel.updateSettings(minMag, sound, vib)
                },
                onTriggerSimulation = { mag, title ->
                    viewModel.triggerSimulationAlert(magnitude = mag, place = title)
                },
                modifier = screenModifier
            )
        }

        // Selected Earthquake Detail Sheet
        selectedEarthquake?.let { eq ->
            EarthquakeDetailBottomSheet(
                earthquake = eq,
                sheetState = sheetState,
                onDismiss = { viewModel.selectEarthquake(null) }
            )
        }

        // Active Simulation Modal Dialog Overlay
        activeSimulatedAlert?.let { simEq ->
            SimulationAlertModal(
                earthquake = simEq,
                onDismiss = { viewModel.dismissSimulationAlert() }
            )
        }
    }
}
