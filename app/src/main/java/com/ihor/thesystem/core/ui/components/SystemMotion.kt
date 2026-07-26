package com.ihor.thesystem.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun Modifier.systemClickable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return systemPressMotion(interactionSource = interactionSource, enabled = enabled)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}

@Composable
fun Modifier.systemToggleable(
    value: Boolean,
    enabled: Boolean = true,
    onValueChange: (Boolean) -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return systemPressMotion(interactionSource = interactionSource, enabled = enabled)
        .toggleable(
            value = value,
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onValueChange
        )
}

@Composable
fun Modifier.systemCombinedClickable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return systemPressMotion(interactionSource = interactionSource, enabled = enabled)
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick,
            onLongClick = onLongClick
        )
}

/**
 * Shared, draw-phase interaction motion. It changes perceived depth without changing layout or
 * touch-target bounds.
 */
@Composable
fun Modifier.systemPressMotion(
    interactionSource: InteractionSource,
    enabled: Boolean = true
): Modifier {
    val motion = SystemTheme.motion
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 0f,
        animationSpec = if (isPressed && enabled) {
            tween(durationMillis = motion.pressMillis, easing = EaseOutCubic)
        } else {
            spring(
                dampingRatio = motion.spatialDampingRatio,
                stiffness = motion.spatialStiffness
            )
        },
        label = "system_press_progress"
    )

    return graphicsLayer {
        val scale = 1f - ((1f - motion.pressedScale) * pressProgress)
        scaleX = scale
        scaleY = scale
        translationY = motion.pressedDepth.toPx() * pressProgress
    }
}

/**
 * One-shot dialog/surface entrance. Kept subtle so it guides attention without delaying input.
 */
@Composable
fun Modifier.systemEnterMotion(
    initialScale: Float = SystemTheme.motion.enterScale,
    initialOffset: Dp = SystemTheme.motion.enterOffset
): Modifier {
    val motion = SystemTheme.motion
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = motion.enterExitMillis,
                easing = EaseOutCubic
            )
        )
    }

    return graphicsLayer {
        val value = progress.value
        alpha = value
        val scale = initialScale + ((1f - initialScale) * value)
        scaleX = scale
        scaleY = scale
        translationY = initialOffset.toPx() * (1f - value)
    }
}

/**
 * One-layer directional state transition. The previous state is removed before this modifier
 * animates the incoming state, avoiding the cost of drawing two full dashboards at once.
 */
@Composable
fun Modifier.systemStateEnterMotion(
    enterFromEnd: Boolean,
    initialOffset: Dp = 14.dp,
    animate: Boolean = true
): Modifier {
    val motion = SystemTheme.motion
    val progress = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(animate) {
        if (animate && progress.value < 1f) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = motion.effectsDampingRatio,
                    stiffness = motion.effectsStiffness
                )
            )
        } else {
            progress.snapTo(1f)
        }
    }

    return graphicsLayer {
        val value = progress.value
        alpha = 0.94f + (0.06f * value)
        translationX = initialOffset.toPx() *
            (if (enterFromEnd) 1f else -1f) *
            (1f - value)
    }
}

/**
 * Finite reward motion for confirmations such as level-up. It settles and stops drawing frames.
 */
@Composable
fun Modifier.systemCelebrationMotion(): Modifier {
    val motion = SystemTheme.motion
    val scale = remember { Animatable(motion.celebrationStartScale) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = motion.celebrationPeakScale,
            animationSpec = tween(
                durationMillis = motion.celebrationMillis / 2,
                easing = EaseOutCubic
            )
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
