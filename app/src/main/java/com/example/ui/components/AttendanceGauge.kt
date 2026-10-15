package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyanSecondary
import com.example.ui.theme.MotionTokens
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning

@Composable
fun AttendanceGauge(
    percentage: Double,
    targetPercentage: Double,
    warningMargin: Double = 5.0,
    safeBunks: Int = 0,
    classesNeeded: Int = 0,
    size: Dp = 108.dp,
    strokeWidth: Dp = 7.dp,
    modifier: Modifier = Modifier
) {
    val outerProgress = (percentage / 100.0).toFloat().coerceIn(0f, 1f)
    val middleProgress = (percentage / targetPercentage.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val innerProgress = when {
        safeBunks > 0 -> (0.5f + (safeBunks.coerceAtMost(10) / 20f)).coerceIn(0.4f, 1f)
        percentage >= targetPercentage -> 0.72f
        else -> (percentage / 100.0).toFloat().coerceIn(0.15f, 0.65f)
    }

    val animatedOuter by animateFloatAsState(
        targetValue = outerProgress,
        animationSpec = MotionTokens.ProgressSpring,
        label = "OuterRingAnimation"
    )
    val animatedMiddle by animateFloatAsState(
        targetValue = middleProgress,
        animationSpec = MotionTokens.ProgressSpring,
        label = "MiddleRingAnimation"
    )
    val animatedInner by animateFloatAsState(
        targetValue = innerProgress,
        animationSpec = MotionTokens.ProgressSpring,
        label = "InnerRingAnimation"
    )

    val (glowColor, statusText, statusIcon) = when {
        percentage >= targetPercentage + warningMargin -> Triple(StatusSafe, "Safe & On Track", Icons.Default.CheckCircle)
        percentage >= targetPercentage -> Triple(StatusSafe, "Meeting ${targetPercentage.toInt()}% Target", Icons.Default.CheckCircle)
        percentage >= targetPercentage - warningMargin -> Triple(StatusWarning, "Borderline Warning", Icons.Default.Info)
        else -> Triple(StatusCritical, "Critical Debar Risk", Icons.Default.Warning)
    }

    val secondaryRingColor = AccentCyanSecondary
    val innerRingColor = if (percentage >= targetPercentage) Color(0xFF00B4D8) else StatusWarning
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)

    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            glowColor.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            glowColor.copy(alpha = 0.15f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, glassBorderBrush),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.14f),
                            secondaryRingColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(140f, 140f),
                        radius = 420f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Muzli 3-Ring Concentric Glowing Circular Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(size)
                ) {
                    val outerColors = remember(percentage, targetPercentage, glowColor) {
                        if (percentage >= targetPercentage) {
                            listOf(
                                Color(0xFF00E676), // Bright Emerald
                                Color(0xFF00E5FF), // Diamond Aqua highlight
                                Color(0xFF00B0FF)  // Electric Cyan
                            )
                        } else {
                            listOf(
                                glowColor.copy(alpha = 0.75f),
                                glowColor,
                                glowColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                    val outerLinearBrush = remember(outerColors) {
                        Brush.linearGradient(
                            colors = outerColors,
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    }

                    val middleColors = remember(secondaryRingColor) {
                        listOf(
                            secondaryRingColor.copy(alpha = 0.7f),
                            Color(0xFF00E5FF),
                            secondaryRingColor
                        )
                    }
                    val middleLinearBrush = remember(middleColors) {
                        Brush.linearGradient(
                            colors = middleColors,
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    }

                    val innerColors = remember(innerRingColor) {
                        listOf(
                            innerRingColor.copy(alpha = 0.7f),
                            Color(0xFF48CAE4),
                            innerRingColor
                        )
                    }
                    val innerLinearBrush = remember(innerColors) {
                        Brush.linearGradient(
                            colors = innerColors,
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .size(size)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Auto
                            }
                    ) {
                        val strokePx = strokeWidth.toPx()
                        val ringGap = strokePx * 1.38f

                        // Ring 1 (Outer): Overall Attendance %
                        val d1 = minOf(this.size.width, this.size.height) - strokePx
                        val tl1 = Offset((this.size.width - d1) / 2f, (this.size.height - d1) / 2f)
                        drawArc(
                            color = trackColor,
                            startAngle = 270f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = tl1,
                            size = Size(d1, d1),
                            style = Stroke(width = strokePx)
                        )
                        if (animatedOuter > 0.005f) {
                            val sweep1 = animatedOuter * 360f
                            drawArc(
                                brush = outerLinearBrush,
                                startAngle = 270f,
                                sweepAngle = sweep1,
                                useCenter = false,
                                topLeft = tl1,
                                size = Size(d1, d1),
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }

                        // Ring 2 (Middle): Goal Fulfillment Ratio
                        val d2 = d1 - (ringGap * 2f)
                        val tl2 = Offset((this.size.width - d2) / 2f, (this.size.height - d2) / 2f)
                        drawArc(
                            color = trackColor.copy(alpha = 0.38f),
                            startAngle = 270f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = tl2,
                            size = Size(d2, d2),
                            style = Stroke(width = strokePx * 0.85f)
                        )
                        if (animatedMiddle > 0.005f) {
                            val sweep2 = animatedMiddle * 360f
                            drawArc(
                                brush = middleLinearBrush,
                                startAngle = 270f,
                                sweepAngle = sweep2,
                                useCenter = false,
                                topLeft = tl2,
                                size = Size(d2, d2),
                                style = Stroke(width = strokePx * 0.85f, cap = StrokeCap.Round)
                            )
                        }

                        // Ring 3 (Inner): Buffer / Stability Ring
                        val d3 = d2 - (ringGap * 1.85f)
                        val tl3 = Offset((this.size.width - d3) / 2f, (this.size.height - d3) / 2f)
                        drawArc(
                            color = trackColor.copy(alpha = 0.28f),
                            startAngle = 270f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = tl3,
                            size = Size(d3, d3),
                            style = Stroke(width = strokePx * 0.72f)
                        )
                        if (animatedInner > 0.005f) {
                            val sweep3 = animatedInner * 360f
                            drawArc(
                                brush = innerLinearBrush,
                                startAngle = 270f,
                                sweepAngle = sweep3,
                                useCenter = false,
                                topLeft = tl3,
                                size = Size(d3, d3),
                                style = Stroke(width = strokePx * 0.72f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center Percentage Readout
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.0f", percentage)}%",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // Right column: Title, Target Pill, Status Badge & Multi-Ring Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Attendance",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Goal ${targetPercentage.toInt()}%",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = glowColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = glowColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = statusText,
                                color = glowColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val insightText = when {
                        percentage >= targetPercentage && safeBunks > 0 ->
                            "Safe buffer: $safeBunks bunk${if (safeBunks > 1) "s" else ""} available across courses."
                        percentage >= targetPercentage && safeBunks == 0 ->
                            "Meeting ${targetPercentage.toInt()}% goal — attend upcoming classes to build a bunk buffer."
                        classesNeeded > 0 ->
                            "Debarment alert: attend $classesNeeded upcoming class${if (classesNeeded > 1) "es" else ""} to recover."
                        else ->
                            "Debarment alert: attend upcoming sessions."
                    }

                    Text(
                        text = insightText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // Mini Concentric Ring Legend Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RingLegendDot(color = glowColor, label = "${String.format(java.util.Locale.US, "%.1f", percentage)}% Avg")
                        RingLegendDot(color = secondaryRingColor, label = "$safeBunks Bunks")
                    }
                }
            }
        }
    }
}

@Composable
private fun RingLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
