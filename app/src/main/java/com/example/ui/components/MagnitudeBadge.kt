package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimarySeismic
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SecondaryOrange
import com.example.ui.theme.WarningYellow
import java.util.Locale

@Composable
fun MagnitudeBadge(
    magnitude: Double,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        magnitude >= 6.0 -> Pair(PrimarySeismic, Color.White)
        magnitude >= 5.0 -> Pair(SecondaryOrange, Color.White)
        magnitude >= 4.0 -> Pair(WarningYellow, Color.Black)
        else -> Pair(SafeEmerald, Color.White)
    }

    Box(
        modifier = modifier
            .testTag("magnitude_badge_${magnitude}")
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format(Locale.US, "M %.1f", magnitude),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
