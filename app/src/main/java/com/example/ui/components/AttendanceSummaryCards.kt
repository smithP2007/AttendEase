package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.OverallAttendanceStats
import com.example.ui.theme.AccentAmberPrimary
import com.example.ui.theme.AccentCyanSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.interactiveSpring
import com.example.util.TouchFeedback

@Composable
fun AttendanceSummaryCards(
    stats: OverallAttendanceStats,
    targetPercentage: Double,
    onCardClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Safe Bunk Reserve (Mint-Teal Glow) & Critical Debar Warning (Coral/Gold Glow)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialSummaryCard(
                title = "Safe Bunks",
                value = "${stats.totalSafeBunks}",
                subtitle = if (stats.totalSafeBunks > 0) "Classes missable safely" else "No buffer available",
                icon = Icons.Default.Shield,
                accentColor = StatusSafe,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_card_safe_bunks"),
                onClick = { onCardClick?.invoke("SAFE") }
            )
            MaterialSummaryCard(
                title = "Debar Risk",
                value = "${stats.subjectsInDanger}",
                subtitle = if (stats.subjectsInDanger > 0) "Below ${targetPercentage.toInt()}% target" else "All courses safe",
                icon = Icons.Default.Warning,
                accentColor = if (stats.subjectsInDanger > 0) StatusCritical else AccentAmberPrimary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_card_danger"),
                onClick = { onCardClick?.invoke("DANGER") }
            )
        }

        // Row 2: Classes Needed to Recover & Classes Attended
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialSummaryCard(
                title = "Classes Needed",
                value = "${stats.totalClassesNeeded}",
                subtitle = if (stats.totalClassesNeeded > 0) "Consecutive for ${targetPercentage.toInt()}%" else "Target requirement met",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = if (stats.totalClassesNeeded > 0) StatusWarning else StatusSafe,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_card_needed"),
                onClick = { onCardClick?.invoke("NEEDED") }
            )
            MaterialSummaryCard(
                title = "Classes Attended",
                value = "${stats.totalAttended}/${stats.totalConducted}",
                subtitle = "${String.format(java.util.Locale.US, "%.1f", stats.overallPercentage)}% semester rate",
                icon = Icons.Default.School,
                accentColor = AccentCyanSecondary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_card_attended"),
                onClick = { onCardClick?.invoke("ATTENDED") }
            )
        }
    }
}

@Composable
fun MaterialSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.48f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
            accentColor.copy(alpha = 0.12f)
        )
    )

    Surface(
        modifier = modifier
            .interactiveSpring(interactionSource, pressedScale = 0.96f)
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(22.dp)
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true)
                    ) {
                        TouchFeedback.tick(context)
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, borderBrush),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.20f),
                            accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(56f, 52f),
                        radius = 230f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Row: Glowing squircle icon container + subtle 3-dot menu indicator (Muzli card style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Row: Metric, Title, and Subtitle
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
