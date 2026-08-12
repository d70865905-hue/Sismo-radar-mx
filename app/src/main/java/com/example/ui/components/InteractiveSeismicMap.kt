package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Earthquake
import com.example.ui.theme.AccentRadarGrid
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.PrimarySeismic
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SecondaryOrange
import com.example.ui.theme.WarningYellow
import kotlin.math.sqrt

@Composable
fun InteractiveSeismicMap(
    earthquakes: List<Earthquake>,
    userLocation: Pair<Double, Double>?,
    selectedEarthquake: Earthquake?,
    onSelectEarthquake: (Earthquake) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.2f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .testTag("interactive_seismic_map")
            .background(DarkBackground)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(earthquakes, scale, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        // Convert tap position back to screen coords to find clicked epicenter
                        var closestEq: Earthquake? = null
                        var minDistance = Float.MAX_VALUE

                        earthquakes.forEach { eq ->
                            val screenX = lonToX(eq.longitude, size.width.toFloat(), scale, offsetX)
                            val screenY = latToY(eq.latitude, size.height.toFloat(), scale, offsetY)
                            val dx = tapOffset.x - screenX
                            val dy = tapOffset.y - screenY
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < 45f && dist < minDistance) {
                                minDistance = dist
                                closestEq = eq
                            }
                        }

                        closestEq?.let { onSelectEarthquake(it) }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Radar Grid Lines & Equator
            val gridStep = 40.dp.toPx()
            var x = (offsetX % gridStep)
            while (x < width) {
                drawLine(
                    color = AccentRadarGrid,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = (offsetY % gridStep)
            while (y < height) {
                drawLine(
                    color = AccentRadarGrid,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Draw Equator & Prime Meridian
            val equatorY = latToY(0.0, height, scale, offsetY)
            val primeX = lonToX(0.0, width, scale, offsetX)
            drawLine(
                color = AccentRadarGrid.copy(alpha = 0.6f),
                start = Offset(0f, equatorY),
                end = Offset(width, equatorY),
                strokeWidth = 2f
            )
            drawLine(
                color = AccentRadarGrid.copy(alpha = 0.6f),
                start = Offset(primeX, 0f),
                end = Offset(primeX, height),
                strokeWidth = 2f
            )

            // 2. Draw Simplified World Continent Outlines (Ring of Fire / Continents representation)
            val landPath = Path().apply {
                // North America
                val na1 = Offset(lonToX(-120.0, width, scale, offsetX), latToY(50.0, height, scale, offsetY))
                val na2 = Offset(lonToX(-70.0, width, scale, offsetX), latToY(40.0, height, scale, offsetY))
                val na3 = Offset(lonToX(-100.0, width, scale, offsetX), latToY(20.0, height, scale, offsetY))
                moveTo(na1.x, na1.y)
                lineTo(na2.x, na2.y)
                lineTo(na3.x, na3.y)
                close()

                // South America
                val sa1 = Offset(lonToX(-80.0, width, scale, offsetX), latToY(10.0, height, scale, offsetY))
                val sa2 = Offset(lonToX(-35.0, width, scale, offsetX), latToY(-5.0, height, scale, offsetY))
                val sa3 = Offset(lonToX(-70.0, width, scale, offsetX), latToY(-50.0, height, scale, offsetY))
                moveTo(sa1.x, sa1.y)
                lineTo(sa2.x, sa2.y)
                lineTo(sa3.x, sa3.y)
                close()

                // Eurasia & Asia / Japan
                val ea1 = Offset(lonToX(-10.0, width, scale, offsetX), latToY(55.0, height, scale, offsetY))
                val ea2 = Offset(lonToX(140.0, width, scale, offsetX), latToY(60.0, height, scale, offsetY))
                val ea3 = Offset(lonToX(100.0, width, scale, offsetX), latToY(15.0, height, scale, offsetY))
                moveTo(ea1.x, ea1.y)
                lineTo(ea2.x, ea2.y)
                lineTo(ea3.x, ea3.y)
                close()
            }
            drawPath(
                path = landPath,
                color = Color(0xFF1E293B).copy(alpha = 0.5f),
                style = Stroke(width = 2f)
            )

            // 3. Draw User Location Pin
            userLocation?.let { (userLat, userLon) ->
                val uX = lonToX(userLon, width, scale, offsetX)
                val uY = latToY(userLat, height, scale, offsetY)

                // Outer radar scan circle around user
                drawCircle(
                    color = SafeEmerald.copy(alpha = pulseAlpha * 0.4f),
                    radius = pulseRadius * 1.5f,
                    center = Offset(uX, uY)
                )
                drawCircle(
                    color = SafeEmerald,
                    radius = 7f,
                    center = Offset(uX, uY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(uX, uY)
                )
            }

            // 4. Draw Earthquake Epicenters
            earthquakes.forEach { eq ->
                val epX = lonToX(eq.longitude, width, scale, offsetX)
                val epY = latToY(eq.latitude, height, scale, offsetY)

                val baseRadius = (eq.magnitude * 2.8).coerceIn(6.0, 24.0).toFloat() * (scale * 0.5f).coerceIn(0.8f, 2.0f)
                val isSelected = selectedEarthquake?.id == eq.id

                val depthColor = when {
                    eq.depthKm < 70 -> PrimarySeismic
                    eq.depthKm < 300 -> SecondaryOrange
                    else -> DeepBlue
                }

                // Wave pulse for strong or selected earthquakes
                if (eq.magnitude >= 5.5 || isSelected || eq.isSimulated) {
                    drawCircle(
                        color = depthColor.copy(alpha = pulseAlpha),
                        radius = baseRadius + pulseRadius,
                        center = Offset(epX, epY),
                        style = Stroke(width = 3f)
                    )
                }

                // Outer shockwave aura
                drawCircle(
                    color = depthColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                    radius = baseRadius * 1.8f,
                    center = Offset(epX, epY)
                )

                // Core epicenter circle
                drawCircle(
                    color = depthColor,
                    radius = baseRadius,
                    center = Offset(epX, epY)
                )

                // White border if selected
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius + 4f,
                        center = Offset(epX, epY),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        // Map Legend & Controls Overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface.copy(alpha = 0.9f))
                .padding(8.dp)
        ) {
            Text(
                text = "Arrastra / Pellizca para Zoom\nToca epicentro para ver detalles",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

private fun lonToX(lon: Double, width: Float, scale: Float, offsetX: Float): Float {
    val normX = (lon + 180.0) / 360.0
    return (normX * width * scale + offsetX).toFloat()
}

private fun latToY(lat: Double, height: Float, scale: Float, offsetY: Float): Float {
    val normY = (90.0 - lat) / 180.0
    return (normY * height * scale + offsetY).toFloat()
}
