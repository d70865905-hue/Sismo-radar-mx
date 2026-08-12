package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Earthquake
import com.example.ui.theme.AccentRadarGrid
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.PrimarySeismic
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SecondaryOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningYellow
import java.util.Locale
import kotlin.math.sqrt

// Color Palette for Epic Map
private val OceanColor = Color(0xFF0D1527)
private val LandColor = Color(0xFF1E293B)
private val LandBorderColor = Color(0xFF334155)
private val TectonicFaultColor = Color(0xFFFF6B00)
private val GridLineColor = Color(0xFF1F293D)
private val GridMajorColor = Color(0xFF334155)

@Composable
fun InteractiveSeismicMap(
    earthquakes: List<Earthquake>,
    userLocation: Pair<Double, Double>?,
    selectedEarthquake: Earthquake?,
    onSelectEarthquake: (Earthquake) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.8f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showTectonicPlates by remember { mutableStateOf(true) }
    var showLegend by remember { mutableStateOf(true) }

    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Center map on selected earthquake when selection changes
    LaunchedEffect(selectedEarthquake) {
        selectedEarthquake?.let { eq ->
            // Delay slightly until size is available, or use nominal center
            val normX = (eq.longitude + 180.0) / 360.0
            val normY = (90.0 - eq.latitude) / 180.0
            // Target center offset assuming container width ~ 1000f
            scale = scale.coerceAtLeast(2.2f)
            offsetX = (500f) - (normX * 1000f * scale).toFloat()
            offsetY = (500f) - (normY * 1000f * scale).toFloat()
        }
    }

    Box(
        modifier = modifier
            .testTag("interactive_seismic_map")
            .background(OceanColor)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1.0f, 12.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(earthquakes, scale, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        var closestEq: Earthquake? = null
                        var minDistance = Float.MAX_VALUE

                        earthquakes.forEach { eq ->
                            val epX = lonToX(eq.longitude, size.width.toFloat(), scale, offsetX)
                            val epY = latToY(eq.latitude, size.height.toFloat(), scale, offsetY)
                            val dx = tapOffset.x - epX
                            val dy = tapOffset.y - epY
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < 55f && dist < minDistance) {
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

            // 1. Draw Latitude & Longitude Grid Lines
            val lonDegrees = listOf(-150.0, -120.0, -90.0, -60.0, -30.0, 0.0, 30.0, 60.0, 90.0, 120.0, 150.0)
            lonDegrees.forEach { lon ->
                val xPos = lonToX(lon, width, scale, offsetX)
                val isPrime = lon == 0.0
                drawLine(
                    color = if (isPrime) GridMajorColor else GridLineColor,
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, height),
                    strokeWidth = if (isPrime) 2f else 1f
                )
                if (scale >= 1.5f && xPos in 10f..(width - 10f)) {
                    val label = if (lon < 0) "${(-lon).toInt()}°W" else if (lon > 0) "${lon.toInt()}°E" else "0°"
                    drawSafeText(
                        textMeasurer = textMeasurer,
                        text = label,
                        style = TextStyle(color = TextMuted.copy(alpha = 0.6f), fontSize = 9.sp),
                        topLeft = Offset(xPos + 4f, 12f)
                    )
                }
            }

            val latDegrees = listOf(60.0, 30.0, 0.0, -30.0, -60.0)
            latDegrees.forEach { lat ->
                val yPos = latToY(lat, height, scale, offsetY)
                val isEquator = lat == 0.0
                drawLine(
                    color = if (isEquator) PrimarySeismic.copy(alpha = 0.4f) else GridLineColor,
                    start = Offset(0f, yPos),
                    end = Offset(width, yPos),
                    strokeWidth = if (isEquator) 2.5f else 1f
                )
                if (scale >= 1.5f && yPos in 10f..(height - 10f)) {
                    val label = if (lat > 0) "${lat.toInt()}°N" else if (lat < 0) "${(-lat).toInt()}°S" else "Ecuador (0°)"
                    drawSafeText(
                        textMeasurer = textMeasurer,
                        text = label,
                        style = TextStyle(color = if (isEquator) PrimarySeismic.copy(alpha = 0.7f) else TextMuted.copy(alpha = 0.6f), fontSize = 9.sp),
                        topLeft = Offset(12f, yPos - 14f)
                    )
                }
            }

            // 2. Render Full World Continents & Major Islands
            val continentPolygons = listOf(
                // North America Main
                listOf(
                    Pair(-168.0, 65.0), Pair(-150.0, 71.0), Pair(-130.0, 70.0), Pair(-100.0, 74.0),
                    Pair(-75.0, 62.0), Pair(-60.0, 46.0), Pair(-66.0, 44.0), Pair(-75.0, 35.0),
                    Pair(-80.0, 25.0), Pair(-97.0, 26.0), Pair(-117.0, 32.0), Pair(-124.0, 48.0),
                    Pair(-135.0, 58.0), Pair(-160.0, 55.0)
                ),
                // Mexico & Central America
                listOf(
                    Pair(-117.0, 32.0), Pair(-97.0, 26.0), Pair(-88.0, 21.0), Pair(-83.0, 9.0),
                    Pair(-77.0, 8.0), Pair(-88.0, 13.0), Pair(-96.0, 16.0), Pair(-105.0, 20.0),
                    Pair(-115.0, 30.0)
                ),
                // Greenland
                listOf(Pair(-55.0, 60.0), Pair(-20.0, 70.0), Pair(-20.0, 83.0), Pair(-60.0, 82.0)),
                // South America
                listOf(
                    Pair(-77.0, 8.0), Pair(-60.0, 10.0), Pair(-50.0, 0.0), Pair(-35.0, -5.0),
                    Pair(-38.0, -18.0), Pair(-48.0, -28.0), Pair(-58.0, -34.0), Pair(-65.0, -54.0),
                    Pair(-75.0, -50.0), Pair(-71.0, -30.0), Pair(-77.0, -12.0), Pair(-80.0, -2.0)
                ),
                // Europe & Western Asia
                listOf(
                    Pair(-10.0, 36.0), Pair(-10.0, 43.0), Pair(0.0, 48.0), Pair(-5.0, 58.0),
                    Pair(10.0, 58.0), Pair(5.0, 62.0), Pair(18.0, 70.0), Pair(30.0, 70.0),
                    Pair(40.0, 65.0), Pair(50.0, 55.0), Pair(40.0, 42.0), Pair(28.0, 41.0),
                    Pair(22.0, 38.0), Pair(15.0, 38.0), Pair(10.0, 44.0), Pair(3.0, 42.0), Pair(-3.0, 37.0)
                ),
                // Scandinavia
                listOf(Pair(5.0, 58.0), Pair(10.0, 68.0), Pair(28.0, 71.0), Pair(25.0, 60.0), Pair(12.0, 56.0)),
                // British Isles
                listOf(Pair(-10.0, 51.0), Pair(-6.0, 58.0), Pair(-2.0, 58.0), Pair(1.0, 50.0), Pair(-5.0, 50.0)),
                // Africa
                listOf(
                    Pair(-17.0, 15.0), Pair(-6.0, 36.0), Pair(11.0, 37.0), Pair(25.0, 31.0),
                    Pair(34.0, 27.0), Pair(43.0, 12.0), Pair(51.0, 11.0), Pair(40.0, -10.0),
                    Pair(33.0, -26.0), Pair(28.0, -33.0), Pair(18.0, -34.0), Pair(12.0, -15.0),
                    Pair(9.0, 5.0), Pair(-14.0, 12.0)
                ),
                // Madagascar
                listOf(Pair(44.0, -12.0), Pair(50.0, -15.0), Pair(47.0, -25.0), Pair(43.0, -25.0)),
                // Arabia
                listOf(Pair(35.0, 30.0), Pair(55.0, 25.0), Pair(60.0, 22.0), Pair(53.0, 16.0), Pair(43.0, 12.0)),
                // India & South Asia
                listOf(Pair(68.0, 24.0), Pair(77.0, 35.0), Pair(88.0, 27.0), Pair(80.0, 10.0), Pair(73.0, 15.0)),
                // Indochina & SE Asia
                listOf(Pair(88.0, 27.0), Pair(102.0, 22.0), Pair(108.0, 12.0), Pair(104.0, 1.0), Pair(98.0, 8.0)),
                // China, East Asia & Siberia
                listOf(
                    Pair(70.0, 40.0), Pair(100.0, 50.0), Pair(120.0, 53.0), Pair(140.0, 55.0),
                    Pair(170.0, 65.0), Pair(180.0, 68.0), Pair(180.0, 75.0), Pair(100.0, 75.0),
                    Pair(60.0, 60.0), Pair(50.0, 40.0)
                ),
                // Australia
                listOf(
                    Pair(114.0, -22.0), Pair(130.0, -12.0), Pair(142.0, -11.0), Pair(153.0, -28.0),
                    Pair(148.0, -38.0), Pair(138.0, -35.0), Pair(115.0, -34.0)
                ),
                // New Zealand
                listOf(Pair(166.0, -46.0), Pair(174.0, -36.0), Pair(178.0, -38.0), Pair(170.0, -47.0)),
                // Japan
                listOf(Pair(130.0, 31.0), Pair(136.0, 35.0), Pair(141.0, 42.0), Pair(145.0, 45.0), Pair(140.0, 36.0)),
                // Philippines
                listOf(Pair(120.0, 18.0), Pair(126.0, 12.0), Pair(125.0, 6.0), Pair(120.0, 10.0)),
                // Indonesia (Sumatra, Java)
                listOf(Pair(95.0, 5.0), Pair(106.0, -6.0), Pair(114.0, -8.0), Pair(105.0, -2.0)),
                // Indonesia (Borneo/Celebes/Papua)
                listOf(Pair(109.0, 4.0), Pair(118.0, 5.0), Pair(140.0, -3.0), Pair(150.0, -10.0), Pair(130.0, -1.0)),
                // Caribbean
                listOf(Pair(-84.0, 22.0), Pair(-74.0, 20.0), Pair(-68.0, 18.0), Pair(-80.0, 18.0)),
                // Antarctica
                listOf(
                    Pair(-180.0, -70.0), Pair(-120.0, -75.0), Pair(-60.0, -65.0), Pair(0.0, -70.0),
                    Pair(60.0, -68.0), Pair(120.0, -68.0), Pair(180.0, -70.0)
                )
            )

            continentPolygons.forEach { coords ->
                if (coords.isNotEmpty()) {
                    val path = Path().apply {
                        val firstX = lonToX(coords[0].first, width, scale, offsetX)
                        val firstY = latToY(coords[0].second, height, scale, offsetY)
                        moveTo(firstX, firstY)
                        for (i in 1 until coords.size) {
                            val px = lonToX(coords[i].first, width, scale, offsetX)
                            val py = latToY(coords[i].second, height, scale, offsetY)
                            lineTo(px, py)
                        }
                        close()
                    }
                    // Fill Continent
                    drawPath(path = path, color = LandColor)
                    // Stroke Continent Shorelines
                    drawPath(path = path, color = LandBorderColor, style = Stroke(width = 1.5f))
                }
            }

            // 3. Render Major Tectonic Fault Lines / Ring of Fire (USGS Style)
            if (showTectonicPlates) {
                val faultLines = listOf(
                    // Pacific Ring of Fire - East Coast (Americas)
                    listOf(
                        Pair(-170.0, 53.0), Pair(-150.0, 58.0), Pair(-135.0, 54.0), Pair(-125.0, 42.0),
                        Pair(-118.0, 34.0), Pair(-105.0, 18.0), Pair(-88.0, 12.0), Pair(-78.0, 5.0),
                        Pair(-78.0, -15.0), Pair(-72.0, -35.0), Pair(-75.0, -50.0), Pair(-65.0, -60.0)
                    ),
                    // Pacific Ring of Fire - West Coast (Asia/Oceania)
                    listOf(
                        Pair(160.0, 55.0), Pair(150.0, 46.0), Pair(142.0, 38.0), Pair(130.0, 26.0),
                        Pair(126.0, 10.0), Pair(125.0, 1.0), Pair(140.0, -6.0), Pair(160.0, -15.0),
                        Pair(178.0, -22.0), Pair(175.0, -42.0)
                    ),
                    // Sunda Trench (Indonesia)
                    listOf(Pair(92.0, 14.0), Pair(96.0, 4.0), Pair(105.0, -6.0), Pair(120.0, -10.0), Pair(130.0, -9.0)),
                    // Mid-Atlantic Ridge
                    listOf(
                        Pair(-20.0, 65.0), Pair(-30.0, 45.0), Pair(-40.0, 25.0), Pair(-30.0, 0.0),
                        Pair(-15.0, -25.0), Pair(-10.0, -50.0)
                    ),
                    // Alpine-Himalayan Belt
                    listOf(
                        Pair(-5.0, 36.0), Pair(15.0, 38.0), Pair(30.0, 38.0), Pair(45.0, 35.0),
                        Pair(60.0, 30.0), Pair(80.0, 28.0), Pair(95.0, 25.0), Pair(100.0, 18.0)
                    )
                )

                faultLines.forEach { line ->
                    if (line.size >= 2) {
                        val faultPath = Path().apply {
                            val fX = lonToX(line[0].first, width, scale, offsetX)
                            val fY = latToY(line[0].second, height, scale, offsetY)
                            moveTo(fX, fY)
                            for (i in 1 until line.size) {
                                val px = lonToX(line[i].first, width, scale, offsetX)
                                val py = latToY(line[i].second, height, scale, offsetY)
                                lineTo(px, py)
                            }
                        }
                        // Dashed Tectonic Fault Line
                        drawPath(
                            path = faultPath,
                            color = TectonicFaultColor.copy(alpha = 0.85f),
                            style = Stroke(
                                width = 2.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )
                    }
                }
            }

            // 4. Region Name Labels on Map Canvas
            val regionLabels = listOf(
                Pair("ANILLO DE FUEGO", Pair(-115.0, 22.0)),
                Pair("PLACA DE NAZCA", Pair(-85.0, -25.0)),
                Pair("AMÉRICA DEL SUR", Pair(-60.0, -15.0)),
                Pair("AMÉRICA DEL NORTE", Pair(-102.0, 48.0)),
                Pair("EUROPA", Pair(15.0, 50.0)),
                Pair("ÁFRICA", Pair(20.0, 8.0)),
                Pair("ASIA", Pair(95.0, 52.0)),
                Pair("OCEANÍA", Pair(135.0, -25.0)),
                Pair("OCÉANO PACÍFICO", Pair(-155.0, 0.0))
            )

            regionLabels.forEach { (name, pos) ->
                val lx = lonToX(pos.first, width, scale, offsetX)
                val ly = latToY(pos.second, height, scale, offsetY)
                if (lx in 0f..width && ly in 0f..height) {
                    drawSafeText(
                        textMeasurer = textMeasurer,
                        text = name,
                        style = TextStyle(
                            color = Color.White.copy(alpha = if (scale >= 1.5f) 0.35f else 0.2f),
                            fontSize = (10 * scale.coerceAtMost(2.5f)).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        topLeft = Offset(lx, ly)
                    )
                }
            }

            // 5. Draw User Location Indicator
            userLocation?.let { (userLat, userLon) ->
                val uX = lonToX(userLon, width, scale, offsetX)
                val uY = latToY(userLat, height, scale, offsetY)

                if (uX in -50f..(width + 50f) && uY in -50f..(height + 50f)) {
                    drawCircle(
                        color = SafeEmerald.copy(alpha = pulseAlpha * 0.5f),
                        radius = pulseRadius * 1.8f,
                        center = Offset(uX, uY)
                    )
                    drawCircle(
                        color = SafeEmerald,
                        radius = 8f,
                        center = Offset(uX, uY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(uX, uY)
                    )
                    drawSafeText(
                        textMeasurer = textMeasurer,
                        text = "Tu ubicación",
                        style = TextStyle(color = SafeEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        topLeft = Offset(uX + 10f, uY - 12f)
                    )
                }
            }

            // 6. Draw Earthquake Epicenters
            earthquakes.forEach { eq ->
                val epX = lonToX(eq.longitude, width, scale, offsetX)
                val epY = latToY(eq.latitude, height, scale, offsetY)

                if (epX in -100f..(width + 100f) && epY in -100f..(height + 100f)) {
                    val baseRadius = (eq.magnitude * 3.2).coerceIn(8.0, 30.0).toFloat() * (scale * 0.45f).coerceIn(0.8f, 2.5f)
                    val isSelected = selectedEarthquake?.id == eq.id

                    val depthColor = when {
                        eq.depthKm < 70 -> PrimarySeismic // Superficial (<70 km) - Red
                        eq.depthKm < 300 -> SecondaryOrange // Intermedio (70-300 km) - Orange
                        else -> DeepBlue // Profundo (>300 km) - Blue
                    }

                    // Pulse wave for strong or selected earthquakes
                    if (eq.magnitude >= 5.2 || isSelected || eq.isSimulated) {
                        drawCircle(
                            color = depthColor.copy(alpha = pulseAlpha),
                            radius = baseRadius + pulseRadius,
                            center = Offset(epX, epY),
                            style = Stroke(width = 3f)
                        )
                    }

                    // Outer shockwave glow aura
                    drawCircle(
                        color = depthColor.copy(alpha = if (isSelected) 0.55f else 0.3f),
                        radius = baseRadius * 1.8f,
                        center = Offset(epX, epY)
                    )

                    // Core epicenter circle
                    drawCircle(
                        color = depthColor,
                        radius = baseRadius,
                        center = Offset(epX, epY)
                    )

                    // Inner highlight dot
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = (baseRadius * 0.3f).coerceAtLeast(3f),
                        center = Offset(epX, epY)
                    )

                    // Selection Ring
                    if (isSelected) {
                        drawCircle(
                            color = Color.White,
                            radius = baseRadius + 6f,
                            center = Offset(epX, epY),
                            style = Stroke(width = 3.5f)
                        )
                    }

                    // Magnitude Label Badge above epicenter
                    if (scale >= 1.2f || eq.magnitude >= 6.0 || isSelected) {
                        val magStr = String.format(Locale.US, "M%.1f", eq.magnitude)
                        drawSafeText(
                            textMeasurer = textMeasurer,
                            text = magStr,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (10 * (scale * 0.6f).coerceIn(0.9f, 1.4f)).sp,
                                fontWeight = FontWeight.Black
                            ),
                            topLeft = Offset(epX - 14f, epY - baseRadius - 16f)
                        )
                    }
                }
            }
        }

        // Floating Control Tools Panel (Zoom +, Zoom -, Recenter, Tectonic Layer)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { scale = (scale * 1.35f).coerceAtMost(12.0f) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.92f))
                    .border(1.dp, LandBorderColor, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar Zoom", tint = Color.White)
            }

            IconButton(
                onClick = { scale = (scale / 1.35f).coerceAtLeast(1.0f) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.92f))
                    .border(1.dp, LandBorderColor, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar Zoom", tint = Color.White)
            }

            IconButton(
                onClick = {
                    scale = 1.8f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.92f))
                    .border(1.dp, LandBorderColor, CircleShape)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar Mapa", tint = PrimarySeismic)
            }

            IconButton(
                onClick = { showTectonicPlates = !showTectonicPlates },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (showTectonicPlates) TectonicFaultColor.copy(alpha = 0.25f) else DarkSurface.copy(alpha = 0.92f))
                    .border(1.dp, if (showTectonicPlates) TectonicFaultColor else LandBorderColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Capa de Placas Tectónicas",
                    tint = if (showTectonicPlates) TectonicFaultColor else TextMuted
                )
            }
        }

        // Legend Overlay Card (Bottom Left)
        AnimatedVisibility(
            visible = showLegend,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 60.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LandBorderColor),
                modifier = Modifier.width(180.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simbología USGS",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar leyenda",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { showLegend = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LegendRow(color = PrimarySeismic, label = "Superficial (<70 km)")
                    LegendRow(color = SecondaryOrange, label = "Intermedio (70-300 km)")
                    LegendRow(color = DeepBlue, label = "Profundo (>300 km)")

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(TectonicFaultColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Falla Tectónica / Anillo",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        if (!showLegend) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface.copy(alpha = 0.9f))
                    .clickable { showLegend = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Leyenda ℹ️",
                    color = PrimarySeismic,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

fun lonToX(lon: Double, width: Float, scale: Float, offsetX: Float): Float {
    val normX = (lon + 180.0) / 360.0
    return (normX * width * scale + offsetX).toFloat()
}

fun latToY(lat: Double, height: Float, scale: Float, offsetY: Float): Float {
    val normY = (90.0 - lat) / 180.0
    return (normY * height * scale + offsetY).toFloat()
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSafeText(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    style: TextStyle,
    topLeft: Offset
) {
    if (topLeft.x > size.width || topLeft.y > size.height) return
    val textLayoutResult = textMeasurer.measure(
        text = androidx.compose.ui.text.AnnotatedString(text),
        style = style
    )
    if (topLeft.x + textLayoutResult.size.width < 0 || topLeft.y + textLayoutResult.size.height < 0) return
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = topLeft
    )
}
