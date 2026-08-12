package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Earthquake
import com.example.ui.components.EarthquakeCard
import com.example.ui.components.InteractiveSeismicMap
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface

@Composable
fun MapScreen(
    earthquakes: List<Earthquake>,
    userLocation: Pair<Double, Double>?,
    selectedEarthquake: Earthquake?,
    onSelectEarthquake: (Earthquake) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("map_screen")
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        InteractiveSeismicMap(
            earthquakes = earthquakes,
            userLocation = userLocation,
            selectedEarthquake = selectedEarthquake,
            onSelectEarthquake = onSelectEarthquake,
            modifier = Modifier.fillMaxSize()
        )

        // Selected Earthquake Quick Floating Card
        selectedEarthquake?.let { eq ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                EarthquakeCard(
                    earthquake = eq,
                    onClick = { onSelectEarthquake(eq) }
                )
            }
        }

        // Top Floating Info Chip
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface.copy(alpha = 0.92f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Mapa Epicentros:",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${earthquakes.size} sismos activos",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "Fuente Oficial: USGS Earthquake Catalog",
                    color = com.example.ui.theme.PrimarySeismic,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
