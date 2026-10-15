package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset

/**
 * Standard Compose physics-based motion specifications configured for
 * high-frequency frame smoothing (90Hz, 120Hz, 144Hz displays).
 */
object MotionTokens {

    // Snappy spring for interactive touch feedback (cards, buttons, chips, circles)
    val InteractiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Ultra-fluid screen and tab transition spring
    val ScreenTransitionSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // IntOffset spring for directional sliding transitions
    val SlideTransitionSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Smooth physics spring for percentage and progress meters
    val ProgressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Color transition spring
    val ColorSpring = spring<androidx.compose.ui.graphics.Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * High-frequency frame smoothed interactive touch response modifier.
 * Uses GPU-accelerated scaling with physics spring interpolation.
 */
@Composable
fun Modifier.interactiveSpring(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.97f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = MotionTokens.InteractiveSpring,
        label = "InteractiveSpringScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
