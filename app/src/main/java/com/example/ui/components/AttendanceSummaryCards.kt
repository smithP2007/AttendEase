package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
                sparklinePoints = listOf(0.35f, 0.55f, 0.45f, 0.78f, 0.68f, 0.92f),
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
                sparklinePoints = if (stats.subjectsInDanger > 0) {
                    listOf(0.8f, 0.65f, 0.7f, 0.4f, 0.45f, 0.25f)
                } else {
                    listOf(0.4f, 0.52f, 0.48f, 0.7f, 0.75f, 0.88f)
                },
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
                sparklinePoints = listOf(0.25f, 0.48f, 0.42f, 0.68f, 0.62f, 0.85f),
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
                sparklinePoints = listOf(0.3f, 0.45f, 0.6f, 0.55f, 0.8f, 0.95f),
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
    sparklinePoints: List<Float> = listOf(0.3f, 0.55f, 0.45f, 0.75f, 0.65f, 0.9f),
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

                // Bottom Row: Metric + Title on left, Muzli Smooth Bezier Wave Sparkline on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

                    Spacer(modifier = Modifier.width(6.dp))

                    // Muzli Smooth Bezier Sparkline Curve
                    MuzliSparkline(
                        points = sparklinePoints,
                        color = accentColor,
                        modifier = Modifier
                            .width(46.dp)
                            .height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MuzliSparkline(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (points.size - 1).coerceAtLeast(1)

        val coords = points.mapIndexed { idx, value ->
            val clamped = value.coerceIn(0.08f, 0.95f)
            Offset(x = idx * stepX, y = h * (1f - clamped))
        }

        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) * 0.5f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) * 0.5f
                val controlY2 = p1.y
                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.25f), Color.Transparent)
            )
        )

        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        val last = coords.last()
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = 4.5.dp.toPx(),
            center = last
        )
        drawCircle(
            color = color,
            radius = 2.2.dp.toPx(),
            center = last
        )
    }
}
