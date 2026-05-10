package com.ihor.thesystem.feature.status.ui.components.dialogs

import android.graphics.BlurMaskFilter
import android.graphics.Paint as NativePaint
import android.graphics.Path as NativePath
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import kotlin.math.cos
import kotlin.math.min
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
            animation = tween(durationMillis = 1650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plasma_phase"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plasma_pulse"
    )

    val plasmaPadding = 24.dp
    Box(
        modifier = modifier
            .drawBehind {
                drawBluePlasmaFrame(
                    phase = phase,
                    pulse = pulse,
                    cornerRadiusPx = cornerRadius.toPx(),
                    insetPx = plasmaPadding.toPx()
                )
            }
            .padding(plasmaPadding),
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
    val radius = cornerRadiusPx.coerceAtMost(min(frameSize.width, frameSize.height) / 2f)
    val cornerRadius = CornerRadius(radius, radius)
    val points = buildRoundedPlasmaContour(left, top, right, bottom, radius)
    val plasma = Color(0xFF00DFFF)
    val core = Color(0xFFC6FDFF)
    val deep = Color(0xFF0070FF)
    val violetBlue = Color(0xFF004CFF)

    drawPlasmaBand(
        points = points,
        phase = phase,
        seed = 0.17f,
        pulse = pulse,
        innerOffsetPx = 4.dp.toPx(),
        outerBasePx = 28.dp.toPx(),
        outerVariancePx = 18.dp.toPx(),
        tangentJitterPx = 6.dp.toPx(),
        color = violetBlue,
        alpha = 0.30f,
        blurPx = 25.dp.toPx()
    )
    drawPlasmaBand(
        points = points,
        phase = phase,
        seed = 1.93f,
        pulse = pulse,
        innerOffsetPx = 3.dp.toPx(),
        outerBasePx = 14.dp.toPx(),
        outerVariancePx = 20.dp.toPx(),
        tangentJitterPx = 8.dp.toPx(),
        color = deep,
        alpha = 0.30f,
        blurPx = 15.dp.toPx()
    )
    drawPlasmaBand(
        points = points,
        phase = phase,
        seed = 3.41f,
        pulse = pulse,
        innerOffsetPx = 2.dp.toPx(),
        outerBasePx = 7.dp.toPx(),
        outerVariancePx = 16.dp.toPx(),
        tangentJitterPx = 6.dp.toPx(),
        color = plasma,
        alpha = 0.36f,
        blurPx = 8.dp.toPx()
    )
    drawPlasmaBand(
        points = points,
        phase = phase,
        seed = 5.12f,
        pulse = pulse,
        innerOffsetPx = 1.dp.toPx(),
        outerBasePx = 5.dp.toPx(),
        outerVariancePx = 7.dp.toPx(),
        tangentJitterPx = 3.dp.toPx(),
        color = core,
        alpha = 0.36f,
        blurPx = 3.dp.toPx()
    )
    drawPlasmaFlares(
        points = points,
        phase = phase,
        pulse = pulse,
        plasma = plasma,
        core = core,
        deep = deep
    )
    drawPlasmaCells(
        points = points,
        phase = phase,
        pulse = pulse,
        plasma = plasma,
        core = core,
        deep = deep
    )
    drawPlasmaContourStroke(
        points = points,
        phase = phase,
        seed = 7.8f,
        pulse = pulse,
        offsetBasePx = 10.dp.toPx(),
        offsetVariancePx = 8.dp.toPx(),
        widthPx = 2.5.dp.toPx(),
        color = plasma,
        alpha = 0.24f,
        blurPx = 4.dp.toPx()
    )
    drawPlasmaContourStroke(
        points = points,
        phase = phase,
        seed = 9.24f,
        pulse = pulse,
        offsetBasePx = 3.dp.toPx(),
        offsetVariancePx = 4.dp.toPx(),
        widthPx = 1.35.dp.toPx(),
        color = core,
        alpha = 0.58f,
        blurPx = 1.4.dp.toPx()
    )

    drawRoundRect(
        color = deep.copy(alpha = 0.12f * pulse),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 18.dp.toPx())
    )
    drawRoundRect(
        color = plasma.copy(alpha = 0.15f * pulse),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 8.dp.toPx())
    )

    val cornerBoost = 0.70f + abs(sin((phase * TWO_PI * 1.7f).toDouble())).toFloat() * 0.34f
    listOf(
        Offset(left + radius * 0.55f, top + radius * 0.55f),
        Offset(right - radius * 0.55f, top + radius * 0.55f),
        Offset(right - radius * 0.55f, bottom - radius * 0.55f),
        Offset(left + radius * 0.55f, bottom - radius * 0.55f)
    ).forEachIndexed { index, corner ->
        drawNativeCircle(
            center = corner,
            radiusPx = (17.dp.toPx() + index * 1.5f) * pulse,
            color = plasma,
            alpha = 0.22f * cornerBoost,
            blurPx = 12.dp.toPx()
        )
        drawNativeCircle(
            center = corner,
            radiusPx = 5.dp.toPx() * pulse,
            color = core,
            alpha = 0.50f * cornerBoost,
            blurPx = 2.dp.toPx()
        )
    }

    drawRoundRect(
        color = plasma.copy(alpha = 0.78f),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 2.2.dp.toPx())
    )
    drawRoundRect(
        color = core.copy(alpha = 0.92f),
        topLeft = Offset(left, top),
        size = frameSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 0.9.dp.toPx())
    )
}

private data class PlasmaPoint(
    val position: Offset,
    val normal: Offset,
    val tangent: Offset,
    val progress: Float
)

private fun buildRoundedPlasmaContour(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float
): List<PlasmaPoint> {
    val raw = mutableListOf<PlasmaPoint>()

    fun add(position: Offset, normal: Offset, tangent: Offset) {
        raw += PlasmaPoint(position, normal, tangent, progress = 0f)
    }

    fun addEdge(
        start: Offset,
        end: Offset,
        normal: Offset,
        tangent: Offset,
        samples: Int
    ) {
        for (index in 0 until samples) {
            val t = index / samples.toFloat()
            add(
                position = Offset(
                    x = start.x + (end.x - start.x) * t,
                    y = start.y + (end.y - start.y) * t
                ),
                normal = normal,
                tangent = tangent
            )
        }
    }

    fun addArc(center: Offset, fromAngle: Float, toAngle: Float, samples: Int) {
        for (index in 0 until samples) {
            val t = index / samples.toFloat()
            val angle = fromAngle + (toAngle - fromAngle) * t
            val normal = Offset(cos(angle), sin(angle))
            val tangent = Offset(-sin(angle), cos(angle))
            add(
                position = Offset(
                    x = center.x + normal.x * radius,
                    y = center.y + normal.y * radius
                ),
                normal = normal,
                tangent = tangent
            )
        }
    }

    val edgeSamples = 44
    val cornerSamples = 18
    addEdge(Offset(left + radius, top), Offset(right - radius, top), Offset(0f, -1f), Offset(1f, 0f), edgeSamples)
    addArc(Offset(right - radius, top + radius), -HALF_PI, 0f, cornerSamples)
    addEdge(Offset(right, top + radius), Offset(right, bottom - radius), Offset(1f, 0f), Offset(0f, 1f), edgeSamples)
    addArc(Offset(right - radius, bottom - radius), 0f, HALF_PI, cornerSamples)
    addEdge(Offset(right - radius, bottom), Offset(left + radius, bottom), Offset(0f, 1f), Offset(-1f, 0f), edgeSamples)
    addArc(Offset(left + radius, bottom - radius), HALF_PI, PI.toFloat(), cornerSamples)
    addEdge(Offset(left, bottom - radius), Offset(left, top + radius), Offset(-1f, 0f), Offset(0f, -1f), edgeSamples)
    addArc(Offset(left + radius, top + radius), PI.toFloat(), PI.toFloat() + HALF_PI, cornerSamples)

    val count = raw.size.coerceAtLeast(1)
    return raw.mapIndexed { index, point ->
        point.copy(progress = index / count.toFloat())
    }
}

private fun DrawScope.drawPlasmaBand(
    points: List<PlasmaPoint>,
    phase: Float,
    seed: Float,
    pulse: Float,
    innerOffsetPx: Float,
    outerBasePx: Float,
    outerVariancePx: Float,
    tangentJitterPx: Float,
    color: Color,
    alpha: Float,
    blurPx: Float
) {
    if (points.isEmpty()) return

    val path = NativePath()
    points.forEachIndexed { index, point ->
        val crest = plasmaCrest(point.progress, phase, seed)
        val breath = 0.80f + 0.20f * sin((point.progress * 5.0f + phase * 1.45f + seed) * TWO_PI)
        val distance = (outerBasePx + outerVariancePx * crest * breath) * pulse
        val jitter = (plasmaSignal(point.progress, phase * 0.83f, seed + 4.6f) - 0.5f) * tangentJitterPx
        val outer = point.offset(outwardPx = distance, tangentPx = jitter)
        if (index == 0) {
            path.moveTo(outer.x, outer.y)
        } else {
            path.lineTo(outer.x, outer.y)
        }
    }

    points.asReversed().forEach { point ->
        val innerSwim = (plasmaSignal(point.progress, phase * 1.17f, seed + 8.3f) - 0.5f) * innerOffsetPx * 0.55f
        val inner = point.offset(outwardPx = -innerOffsetPx + innerSwim, tangentPx = 0f)
        path.lineTo(inner.x, inner.y)
    }
    path.close()

    drawNativePath(path = path, color = color, alpha = alpha * pulse, blurPx = blurPx, strokeWidthPx = null)
}

private fun DrawScope.drawPlasmaContourStroke(
    points: List<PlasmaPoint>,
    phase: Float,
    seed: Float,
    pulse: Float,
    offsetBasePx: Float,
    offsetVariancePx: Float,
    widthPx: Float,
    color: Color,
    alpha: Float,
    blurPx: Float
) {
    if (points.isEmpty()) return

    val path = NativePath()
    points.forEachIndexed { index, point ->
        val crest = plasmaCrest(point.progress, phase, seed)
        val distance = (offsetBasePx + offsetVariancePx * crest) * pulse
        val jitter = (plasmaSignal(point.progress, phase * 0.91f, seed + 3.7f) - 0.5f) * 3.dp.toPx()
        val next = point.offset(outwardPx = distance, tangentPx = jitter)
        if (index == 0) {
            path.moveTo(next.x, next.y)
        } else {
            path.lineTo(next.x, next.y)
        }
    }
    path.close()

    drawNativePath(path = path, color = color, alpha = alpha * pulse, blurPx = blurPx, strokeWidthPx = widthPx)
}

private fun DrawScope.drawPlasmaFlares(
    points: List<PlasmaPoint>,
    phase: Float,
    pulse: Float,
    plasma: Color,
    core: Color,
    deep: Color
) {
    points.forEachIndexed { index, point ->
        if (index % 4 != 0) return@forEachIndexed

        val heat = plasmaCrest(point.progress, phase, seed = 14.5f + index * 0.021f)
        if (heat <= 0.08f) return@forEachIndexed

        val tangentDrift = (plasmaSignal(point.progress, phase * 1.07f, seed = 4.4f + index * 0.03f) - 0.5f) * 12.dp.toPx()
        val length = (9.dp.toPx() + 24.dp.toPx() * heat) * pulse
        val width = (4.5.dp.toPx() + 12.dp.toPx() * heat) * pulse
        val lean = (plasmaSignal(point.progress, phase * 0.77f, seed = 8.0f + index * 0.017f) - 0.5f) * width

        val rootLeft = point.offset(outwardPx = 1.dp.toPx(), tangentPx = tangentDrift - width * 0.62f)
        val rootRight = point.offset(outwardPx = 1.dp.toPx(), tangentPx = tangentDrift + width * 0.62f)
        val tip = point.offset(outwardPx = length, tangentPx = tangentDrift + lean)
        val controlLeft = point.offset(outwardPx = length * 0.44f, tangentPx = tangentDrift - width)
        val controlRight = point.offset(outwardPx = length * 0.40f, tangentPx = tangentDrift + width)

        val flamePath = NativePath().apply {
            moveTo(rootLeft.x, rootLeft.y)
            cubicTo(controlLeft.x, controlLeft.y, tip.x, tip.y, tip.x, tip.y)
            cubicTo(tip.x, tip.y, controlRight.x, controlRight.y, rootRight.x, rootRight.y)
            close()
        }

        drawNativePath(
            path = flamePath,
            color = deep,
            alpha = 0.17f * heat * pulse,
            blurPx = 9.dp.toPx(),
            strokeWidthPx = null
        )
        drawNativePath(
            path = flamePath,
            color = plasma,
            alpha = 0.42f * heat * pulse,
            blurPx = 5.dp.toPx(),
            strokeWidthPx = null
        )

        if (index % 8 == 0) {
            val coreWidth = width * 0.28f
            val coreTip = point.offset(outwardPx = length * 0.70f, tangentPx = tangentDrift + lean * 0.42f)
            val coreRootLeft = point.offset(outwardPx = 1.5.dp.toPx(), tangentPx = tangentDrift - coreWidth)
            val coreRootRight = point.offset(outwardPx = 1.5.dp.toPx(), tangentPx = tangentDrift + coreWidth)
            val corePath = NativePath().apply {
                moveTo(coreRootLeft.x, coreRootLeft.y)
                lineTo(coreTip.x, coreTip.y)
                lineTo(coreRootRight.x, coreRootRight.y)
                close()
            }
            drawNativePath(
                path = corePath,
                color = core,
                alpha = 0.50f * heat,
                blurPx = 2.2.dp.toPx(),
                strokeWidthPx = null
            )
        }
    }
}

private fun DrawScope.drawPlasmaCells(
    points: List<PlasmaPoint>,
    phase: Float,
    pulse: Float,
    plasma: Color,
    core: Color,
    deep: Color
) {
    points.forEachIndexed { index, point ->
        if (index % 3 != 0) return@forEachIndexed

        val heat = plasmaCrest(point.progress, phase, seed = 11.0f + index * 0.013f)
        if (heat <= 0.045f) return@forEachIndexed

        val outward = (7.dp.toPx() + 15.dp.toPx() * heat) * pulse
        val jitter = (plasmaSignal(point.progress, phase * 1.31f, seed = 2.2f + index * 0.07f) - 0.5f) * 16.dp.toPx()
        val center = point.offset(outwardPx = outward, tangentPx = jitter)
        val largeRadius = (2.5.dp.toPx() + 11.dp.toPx() * heat) * pulse

        drawNativeCircle(
            center = center,
            radiusPx = largeRadius * 1.55f,
            color = deep,
            alpha = 0.16f * heat * pulse,
            blurPx = 10.dp.toPx()
        )
        drawNativeCircle(
            center = center,
            radiusPx = largeRadius,
            color = plasma,
            alpha = 0.34f * heat * pulse,
            blurPx = 6.dp.toPx()
        )
        if (index % 6 == 0) {
            drawNativeCircle(
                center = center,
                radiusPx = (1.4.dp.toPx() + 3.8.dp.toPx() * heat) * pulse,
                color = core,
                alpha = 0.72f * heat,
                blurPx = 1.5.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawNativePath(
    path: NativePath,
    color: Color,
    alpha: Float,
    blurPx: Float,
    strokeWidthPx: Float?
) {
    val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        this.color = color.copy(alpha = alpha.coerceIn(0f, 1f)).toArgb()
        style = if (strokeWidthPx == null) NativePaint.Style.FILL else NativePaint.Style.STROKE
        strokeWidthPx?.let {
            strokeWidth = it
            strokeJoin = NativePaint.Join.ROUND
            strokeCap = NativePaint.Cap.ROUND
        }
        if (blurPx > 0f) {
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawPath(path, paint)
    }
}

private fun DrawScope.drawNativeCircle(
    center: Offset,
    radiusPx: Float,
    color: Color,
    alpha: Float,
    blurPx: Float
) {
    val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        this.color = color.copy(alpha = alpha.coerceIn(0f, 1f)).toArgb()
        style = NativePaint.Style.FILL
        if (blurPx > 0f) {
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawCircle(center.x, center.y, radiusPx, paint)
    }
}

private fun PlasmaPoint.offset(outwardPx: Float, tangentPx: Float): Offset =
    Offset(
        x = position.x + normal.x * outwardPx + tangent.x * tangentPx,
        y = position.y + normal.y * outwardPx + tangent.y * tangentPx
    )

private fun plasmaCrest(position: Float, phase: Float, seed: Float): Float {
    val signal = plasmaSignal(position, phase, seed)
    val flare = plasmaSignal(position, phase * 1.37f, seed + 6.9f)
    return ((signal * 0.72f + flare * 0.28f - 0.16f) / 0.84f)
        .coerceIn(0f, 1f)
        .pow(1.55f)
}

private fun plasmaSignal(position: Float, phase: Float, seed: Float): Float {
    val angle = phase * TWO_PI
    val a = sin((position * 8.0f + seed) * TWO_PI + angle * 1.15f)
    val b = sin((position * 17.0f + seed * 1.7f) * TWO_PI - angle * 1.85f)
    val c = sin((position * 31.0f + seed * 0.31f) * TWO_PI + angle * 2.65f)
    val d = sin((position * 53.0f + seed * 2.1f) * TWO_PI - angle * 3.35f)
    return ((a * 0.40f + b * 0.28f + c * 0.20f + d * 0.12f) * 0.5f + 0.5f).coerceIn(0f, 1f)
}

private const val HALF_PI = (PI / 2.0).toFloat()
private const val TWO_PI = (PI * 2.0).toFloat()
