package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimarySeismic
import com.example.ui.theme.SecondaryOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.AlertSettingsState
import java.util.Locale

@Composable
fun AlertsAndSimulationScreen(
    alertSettings: AlertSettingsState,
    onUpdateSettings: (minMag: Double, sound: Boolean, vibration: Boolean) -> Unit,
    onTriggerSimulation: (magnitude: Double, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .testTag("alerts_and_simulation_screen")
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Alertas y Modo Simulación",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Prueba el sistema de alerta sísmica de forma segura y ajusta tus preferencias",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MODO SIMULACIÓN HERO CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryOrange.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = SecondaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Modo Simulación de Alerta",
                        color = SecondaryOrange,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Modo de prueba local e independiente. Permite probar los tonos de sonido, vibración del dispositivo y protocolo de evacuación. Los eventos simulados NO se agregan a la lista de sismos reales de USGS ni generan falsas alertas públicas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scenario Simulation Triggers
                Button(
                    onClick = {
                        onTriggerSimulation(6.8, "SIMULACIÓN - Sismo Fuerte M 6.8 en Zona Costera")
                    },
                    modifier = Modifier
                        .testTag("trigger_sim_6_8_button")
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PROBAR SIMULACIÓN M 6.8 (ALERTA ROJA)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onTriggerSimulation(5.5, "SIMULACIÓN - Sismo Moderado M 5.5")
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("M 5.5 Moderado", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onTriggerSimulation(7.4, "SIMULACIÓN - Gran Sismo M 7.4 (Tsunami)")
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("M 7.4 Tsunami", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CONFIGURACIÓN DE ALERTAS
        Text(
            text = "Configuración de Notificaciones",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Min Magnitude Threshold Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Umbral Mínimo de Alerta", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = String.format(Locale.US, "M %.1f+", alertSettings.minMagnitudeThreshold),
                        color = WarningYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = alertSettings.minMagnitudeThreshold.toFloat(),
                    onValueChange = { newMag ->
                        onUpdateSettings(
                            newMag.toDouble(),
                            alertSettings.soundEnabled,
                            alertSettings.vibrationEnabled
                        )
                    },
                    valueRange = 3.0f..7.0f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimarySeismic,
                        activeTrackColor = PrimarySeismic
                    ),
                    modifier = Modifier.testTag("alert_magnitude_slider")
                )

                Text(
                    text = "Solo sonar / vibrar para sismos de magnitud igual o mayor a M ${String.format(Locale.US, "%.1f", alertSettings.minMagnitudeThreshold)}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Alarma Sonora", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reproduce tono de sirena sísmica", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = alertSettings.soundEnabled,
                        onCheckedChange = { soundOn ->
                            onUpdateSettings(
                                alertSettings.minMagnitudeThreshold,
                                soundOn,
                                alertSettings.vibrationEnabled
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimarySeismic
                        ),
                        modifier = Modifier.testTag("sound_alert_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Vibración Háptica", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Patrón de pulso en vibrador", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = alertSettings.vibrationEnabled,
                        onCheckedChange = { vibOn ->
                            onUpdateSettings(
                                alertSettings.minMagnitudeThreshold,
                                alertSettings.soundEnabled,
                                vibOn
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimarySeismic
                        ),
                        modifier = Modifier.testTag("vibration_alert_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FUENTES OFICIALES Y GUÍA
        Text(
            text = "Fuentes Sísmicas Oficiales",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Esta aplicación consulta en tiempo real el Catálogo GeoJSON de la USGS (United States Geological Survey).",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val agencies = listOf(
            "USGS (EE.UU. / Global - Fuente Integrada)" to "https://earthquake.usgs.gov",
            "EMSC (Europa / Mediterráneo)" to "https://www.emsc-csem.org",
            "SSN (Servicio Sismológico Nacional México)" to "http://www.ssn.unam.mx",
            "CSN (Centro Sismológico Nacional Chile)" to "https://www.sismologia.cl",
            "IGP (Instituto Geofísico del Perú)" to "https://www.igp.gob.pe"
        )

        agencies.forEach { (agencyName, url) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(agencyName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = "Abrir fuente oficial", tint = SecondaryOrange, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
