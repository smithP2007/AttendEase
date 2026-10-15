package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SubjectEntity
import com.example.ui.theme.MotionTokens
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.interactiveSpring
import com.example.util.AttendanceCalculator
import com.example.util.AttendanceStatus
import com.example.util.TouchFeedback

@Composable
fun SubjectCard(
    subject: SubjectEntity,
    targetPercentage: Double,
    warningMargin: Double = 5.0,
    todayStatus: String? = null, // "PRESENT", "ABSENT", or null
    isCompact: Boolean = false,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardInteractionSource = remember { MutableInteractionSource() }
    val presentInteractionSource = remember { MutableInteractionSource() }
    val absentInteractionSource = remember { MutableInteractionSource() }

    val calc = AttendanceCalculator.calculate(
        attended = subject.attendedClasses,
        total = subject.totalClasses,
        targetPercentage = targetPercentage,
        warningMargin = warningMargin
    )
    val progress = (calc.percentage / 100.0).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = MotionTokens.ProgressSpring,
        label = "Progress"
    )

    val isPresent = todayStatus == "PRESENT"
    val isAbsent = todayStatus == "ABSENT"

    val statusColor = when (calc.status) {
        AttendanceStatus.SAFE -> StatusSafe
        AttendanceStatus.WARNING -> StatusWarning
        AttendanceStatus.CRITICAL -> StatusCritical
    }

    val subjectAccentColor = remember(subject.colorHex, statusColor) {
        try {
            if (subject.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(subject.colorHex))
            else statusColor
        } catch (_: Exception) {
            statusColor
        }
    }

    val topGlowColor by animateColorAsState(
        targetValue = when {
            isPresent -> StatusSafe
            isAbsent -> StatusCritical
            calc.status == AttendanceStatus.CRITICAL -> StatusCritical
            else -> statusColor
        },
        animationSpec = MotionTokens.ColorSpring,
        label = "SubjectCardGlow"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            topGlowColor.copy(alpha = 0.48f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            topGlowColor.copy(alpha = 0.14f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}")
            .interactiveSpring(cardInteractionSource, pressedScale = 0.98f)
            .graphicsLayer {
                // Hardware layer optimization for smooth 120fps scrolling
                clip = true
                shape = RoundedCornerShape(22.dp)
            }
            .clickable(
                interactionSource = cardInteractionSource,
                indication = ripple(bounded = true)
            ) {
                TouchFeedback.tick(context)
                onClick()
            },
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
                            topGlowColor.copy(alpha = 0.16f),
                            subjectAccentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(80f, 60f),
                        radius = 340f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 12.dp else 16.dp)
            ) {
                // Header: Code Badge + Title + Muzli Mini Circular Donut Gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (subject.code.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = statusColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = subject.code,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (subject.roomLocation.isNotBlank()) {
                                Text(
                                    text = "• ${subject.roomLocation}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subject.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isCompact) 15.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Muzli Circular Ring & Percentage Readout (like the 52% & 75% cards in 16642.jpg)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", calc.percentage)}%",
                                color = statusColor,
                                fontSize = if (isCompact) 15.sp else 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${subject.attendedClasses}/${subject.totalClasses} held",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(36.dp)
                        ) {
                            val ringTrack = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            val diamondColors = remember(statusColor, calc.percentage, targetPercentage) {
                                if (calc.percentage >= targetPercentage) {
                                    listOf(
                                        Color(0xFF00E676), // Bright Emerald
                                        Color(0xFF00E5FF), // Diamond Aqua highlight
                                        Color(0xFF00B0FF)  // Electric Blue
                                    )
                                } else {
                                    listOf(
                                        statusColor.copy(alpha = 0.7f),
                                        statusColor,
                                        statusColor.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            val ringLinearBrush = remember(diamondColors) {
                                Brush.linearGradient(
                                    colors = diamondColors,
                                    start = Offset.Zero,
                                    end = Offset.Infinite
                                )
                            }

                            Canvas(
                                modifier = Modifier.size(36.dp)
                            ) {
                                val sw = 3.5.dp.toPx()
                                val d = size.minDimension - sw
                                val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
                                drawArc(
                                    color = ringTrack,
                                    startAngle = 270f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = tl,
                                    size = Size(d, d),
                                    style = Stroke(width = sw)
                                )
                                if (animatedProgress > 0.005f) {
                                    val sweep = animatedProgress * 360f
                                    drawArc(
                                        brush = ringLinearBrush,
                                        startAngle = 270f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = tl,
                                        size = Size(d, d),
                                        style = Stroke(width = sw, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            Text(
                                text = "${calc.percentage.toInt()}%",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Glowing Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    strokeCap = StrokeCap.Round
                )

                if (!isCompact) {
                    Spacer(modifier = Modifier.height(9.dp))

                    // Safe Bunks / Classes Needed Callout + Mini Sparkline
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (calc.safeBunks > 0) StatusSafe.copy(alpha = 0.08f) else StatusCritical.copy(alpha = 0.08f),
                        border = BorderStroke(
                            1.dp,
                            if (calc.safeBunks > 0) StatusSafe.copy(alpha = 0.25f) else StatusCritical.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (calc.safeBunks > 0) {
                                    "Can bunk ${calc.safeBunks} class${if (calc.safeBunks > 1) "es" else ""} safely"
                                } else if (calc.classesNeeded > 0) {
                                    "Attend next ${calc.classesNeeded} class${if (calc.classesNeeded > 1) "es" else ""} for ${targetPercentage.toInt()}%"
                                } else {
                                    "At threshold! Next absence drops below ${targetPercentage.toInt()}%"
                                },
                                color = if (calc.safeBunks > 0) StatusSafe else StatusCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            MuzliSparkline(
                                points = if (calc.safeBunks > 0) {
                                    listOf(0.4f, 0.55f, 0.5f, 0.75f, 0.9f)
                                } else {
                                    listOf(0.75f, 0.6f, 0.65f, 0.45f, 0.35f)
                                },
                                color = if (calc.safeBunks > 0) StatusSafe else StatusCritical,
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Mark Buttons with Low-Latency Tactile Feedback & Spring Motion
                val presentBgColor by animateColorAsState(
                    targetValue = if (isPresent) StatusSafe else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = MotionTokens.ColorSpring,
                    label = "PresentBtnBg"
                )
                val absentBgColor by animateColorAsState(
                    targetValue = if (isAbsent) StatusCritical else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = MotionTokens.ColorSpring,
                    label = "AbsentBtnBg"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Present Button
                    Button(
                        onClick = {
                            TouchFeedback.click(context)
                            onMarkPresent()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .interactiveSpring(presentInteractionSource, pressedScale = 0.94f)
                            .testTag("present_btn_${subject.id}"),
                        interactionSource = presentInteractionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = presentBgColor,
                            contentColor = if (isPresent) Color(0xFF041311) else StatusSafe
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPresent) StatusSafe else StatusSafe.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Present",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Present",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Absent Button
                    Button(
                        onClick = {
                            TouchFeedback.click(context)
                            onMarkAbsent()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .interactiveSpring(absentInteractionSource, pressedScale = 0.94f)
                            .testTag("absent_btn_${subject.id}"),
                        interactionSource = absentInteractionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = absentBgColor,
                            contentColor = if (isAbsent) Color.White else StatusCritical
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isAbsent) StatusCritical else StatusCritical.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Absent",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Absent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
