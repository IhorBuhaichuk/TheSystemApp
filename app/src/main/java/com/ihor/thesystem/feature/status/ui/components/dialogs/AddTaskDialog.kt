package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.BorderActive
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.SystemRadiusLarge
import com.ihor.thesystem.core.theme.SystemSurfaceGlass
import com.ihor.thesystem.core.theme.SystemSurfaceGlassStrong
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.components.SystemButton
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun AddTaskDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    titleText: String? = null,
    subtitleText: String = "To-do",
    placeholderText: String? = null
) {
    var taskName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmedTaskName = taskName.trim()
    val dialogTitle = titleText ?: stringResource(R.string.text_add_task_title)
    val dialogPlaceholder = placeholderText ?: stringResource(R.string.text_add_task_placeholder)

    fun dismiss() {
        keyboardController?.hide()
        onDismiss()
    }

    fun submit() {
        if (trimmedTaskName.isNotEmpty()) {
            keyboardController?.hide()
            onConfirm(trimmedTaskName)
        }
    }

    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = ::dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val shape = RoundedCornerShape(SystemRadiusLarge)
            BluePlasmaModalFrame(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                cornerRadius = SystemRadiusLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                    .shadow(
                        elevation = 24.dp,
                        shape = shape,
                        ambientColor = AccentPrimary.copy(alpha = 0.18f),
                        spotColor = Color.Black.copy(alpha = 0.56f)
                    )
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SystemSurfaceGlassStrong.copy(alpha = 0.90f),
                                SystemSurfaceGlass.copy(alpha = 0.90f),
                                SystemSurfaceGlass.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    BorderActive.copy(alpha = 0.72f),
                                    BorderSubtle
                                )
                            )
                        ),
                        shape
                    )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(AccentPrimary.copy(alpha = 0.12f))
                            .border(1.dp, AccentPrimary.copy(alpha = 0.34f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = dialogTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = dialogPlaceholder,
                            color = TextMuted
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = Color.White.copy(alpha = 0.045f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.035f),
                        cursorColor = AccentPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable(onClick = ::dismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.text_add_task_cancel),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    SystemButton(
                        text = stringResource(R.string.text_add_task_confirm),
                        icon = Icons.Filled.Add,
                        onClick = ::submit,
                        enabled = trimmedTaskName.isNotEmpty(),
                        glow = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
}

@Composable
private fun BluePlasmaModalFrame(
    modifier: Modifier = Modifier,
    cornerRadius: Dp,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "blue_plasma_frame")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plasma_phase"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 760, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plasma_pulse"
    )

    Box(
        modifier = modifier
            .drawBehind {
                drawBluePlasmaFrame(
                    phase = phase,
                    pulse = pulse,
                    cornerRadiusPx = cornerRadius.toPx(),
                    insetPx = 17.dp.toPx()
                )
            }
            .padding(17.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun DrawScope.drawBluePlasmaFrame(
    phase: Float,
    pulse: Float,
    cornerRadiusPx: Float,
    insetPx: Float
) {
    val left = insetPx
    val top = insetPx
    val right = size.width - insetPx
    val bottom = size.height - insetPx
    if (right <= left || bottom <= top) return

    val frameSize = Size(right - left, bottom - top)
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    val plasma = Color(0xFF00E5FF)
    val core = Color(0xFF7DFBFF)
    val deep = Color(0xFF006DFF)

    drawRoundRect(
        color = plasma.copy(alpha = 0.10f * pulse),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 30.dp.toPx())
    )
    drawRoundRect(
        color = plasma.copy(alpha = 0.18f * pulse),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 18.dp.toPx())
    )

    drawPlasmaEdge(
        start = Offset(left + cornerRadiusPx, top),
        end = Offset(right - cornerRadiusPx, top),
        outward = Offset(0f, -1f),
        phase = phase,
        seed = 0.13f,
        maxLength = 25.dp.toPx(),
        coreColor = core,
        flameColor = plasma,
        deepColor = deep,
        pulse = pulse,
        samples = 34
    )
    drawPlasmaEdge(
        start = Offset(right, top + cornerRadiusPx),
        end = Offset(right, bottom - cornerRadiusPx),
        outward = Offset(1f, 0f),
        phase = phase,
        seed = 1.41f,
        maxLength = 26.dp.toPx(),
        coreColor = core,
        flameColor = plasma,
        deepColor = deep,
        pulse = pulse,
        samples = 42
    )
    drawPlasmaEdge(
        start = Offset(right - cornerRadiusPx, bottom),
        end = Offset(left + cornerRadiusPx, bottom),
        outward = Offset(0f, 1f),
        phase = phase,
        seed = 2.73f,
        maxLength = 28.dp.toPx(),
        coreColor = core,
        flameColor = plasma,
        deepColor = deep,
        pulse = pulse,
        samples = 34
    )
    drawPlasmaEdge(
        start = Offset(left, bottom - cornerRadiusPx),
        end = Offset(left, top + cornerRadiusPx),
        outward = Offset(-1f, 0f),
        phase = phase,
        seed = 3.62f,
        maxLength = 26.dp.toPx(),
        coreColor = core,
        flameColor = plasma,
        deepColor = deep,
        pulse = pulse,
        samples = 42
    )

    val cornerBoost = 0.70f + abs(sin((phase * TWO_PI * 1.7f).toDouble())).toFloat() * 0.34f
    listOf(
        Offset(left + cornerRadiusPx * 0.35f, top + cornerRadiusPx * 0.35f),
        Offset(right - cornerRadiusPx * 0.35f, top + cornerRadiusPx * 0.35f),
        Offset(right - cornerRadiusPx * 0.35f, bottom - cornerRadiusPx * 0.35f),
        Offset(left + cornerRadiusPx * 0.35f, bottom - cornerRadiusPx * 0.35f)
    ).forEachIndexed { index, corner ->
        drawCircle(
            color = plasma.copy(alpha = 0.11f * cornerBoost),
            radius = (18.dp.toPx() + index * 1.5f) * pulse,
            center = corner
        )
        drawCircle(
            color = core.copy(alpha = 0.20f * cornerBoost),
            radius = 4.5.dp.toPx() * pulse,
            center = corner
        )
    }

    drawRoundRect(
        color = plasma.copy(alpha = 0.58f),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 1.8.dp.toPx())
    )
    drawRoundRect(
        color = core.copy(alpha = 0.92f),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 0.7.dp.toPx())
    )
}

private fun DrawScope.drawPlasmaEdge(
    start: Offset,
    end: Offset,
    outward: Offset,
    phase: Float,
    seed: Float,
    maxLength: Float,
    coreColor: Color,
    flameColor: Color,
    deepColor: Color,
    pulse: Float,
    samples: Int
) {
    val tangent = Offset(end.x - start.x, end.y - start.y)
    val tangentLength = kotlin.math.sqrt(tangent.x * tangent.x + tangent.y * tangent.y).coerceAtLeast(1f)
    val tangentUnit = Offset(tangent.x / tangentLength, tangent.y / tangentLength)

    for (index in 0..samples) {
        val t = index / samples.toFloat()
        val base = Offset(
            x = start.x + (end.x - start.x) * t,
            y = start.y + (end.y - start.y) * t
        )
        val noise = plasmaSignal(t, phase, seed)
        val lick = ((noise - 0.22f).coerceAtLeast(0f) / 0.78f).pow(1.55f)
        if (lick <= 0.015f) continue

        val sideJitter = (plasmaSignal(t, phase * 0.7f, seed + 4.8f) - 0.5f) * 12.dp.toPx()
        val root = Offset(
            x = base.x + tangentUnit.x * sideJitter - outward.x * 2.dp.toPx(),
            y = base.y + tangentUnit.y * sideJitter - outward.y * 2.dp.toPx()
        )
        val length = (6.dp.toPx() + maxLength * lick) * pulse
        val tip = Offset(
            x = root.x + outward.x * length + tangentUnit.x * sideJitter * 0.22f,
            y = root.y + outward.y * length + tangentUnit.y * sideJitter * 0.22f
        )
        val width = (2.2.dp.toPx() + 7.5.dp.toPx() * lick) * pulse

        drawLine(
            color = deepColor.copy(alpha = 0.14f * lick),
            start = root,
            end = tip,
            strokeWidth = width * 2.4f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = flameColor.copy(alpha = 0.30f * lick),
            start = root,
            end = tip,
            strokeWidth = width * 1.35f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = coreColor.copy(alpha = 0.55f * lick),
            start = root,
            end = Offset(
                x = root.x + (tip.x - root.x) * 0.62f,
                y = root.y + (tip.y - root.y) * 0.62f
            ),
            strokeWidth = width * 0.42f,
            cap = StrokeCap.Round
        )

        if (index % 4 == 0) {
            drawCircle(
                color = coreColor.copy(alpha = 0.16f * lick),
                radius = width * 1.1f,
                center = tip
            )
        }
    }

    val ribbon = Path()
    for (index in 0..samples) {
        val t = index / samples.toFloat()
        val base = Offset(
            x = start.x + (end.x - start.x) * t,
            y = start.y + (end.y - start.y) * t
        )
        val wave = plasmaSignal(t, phase, seed + 8.1f)
        val distance = (4.dp.toPx() + maxLength * 0.42f * wave) * pulse
        val point = Offset(base.x + outward.x * distance, base.y + outward.y * distance)
        if (index == 0) ribbon.moveTo(point.x, point.y) else ribbon.lineTo(point.x, point.y)
    }
    drawPath(
        path = ribbon,
        color = flameColor.copy(alpha = 0.13f * pulse),
        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun plasmaSignal(position: Float, phase: Float, seed: Float): Float {
    val angle = phase * TWO_PI
    val a = sin((position * 7.0f + seed) * TWO_PI + angle)
    val b = sin((position * 13.0f + seed * 1.7f) * TWO_PI - angle * 1.35f)
    val c = sin((position * 23.0f + seed * 0.31f) * TWO_PI + angle * 2.25f)
    return ((a * 0.52f + b * 0.32f + c * 0.16f) * 0.5f + 0.5f).coerceIn(0f, 1f)
}

private const val TWO_PI = (PI * 2.0).toFloat()
