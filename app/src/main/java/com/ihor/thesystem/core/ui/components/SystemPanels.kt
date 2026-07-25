package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemDisplayFamily
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import kotlin.math.min

@Immutable
data class SystemCutCornerShape(
    val cut: Dp = 14.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutPx = with(density) { cut.toPx() }.coerceAtMost(min(size.width, size.height) * 0.28f)
        val path = Path().apply {
            moveTo(cutPx, 0f)
            lineTo(size.width - cutPx, 0f)
            lineTo(size.width, cutPx)
            lineTo(size.width, size.height - cutPx)
            lineTo(size.width - cutPx, size.height)
            lineTo(cutPx, size.height)
            lineTo(0f, size.height - cutPx)
            lineTo(0f, cutPx)
            close()
        }
        return Outline.Generic(path)
    }
}

@Immutable
data class SystemHexagonShape(
    val inset: Float = 0.12f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val x = size.width * inset.coerceIn(0.05f, 0.24f)
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width - x, size.height * 0.22f)
            lineTo(size.width - x, size.height * 0.78f)
            lineTo(size.width / 2f, size.height)
            lineTo(x, size.height * 0.78f)
            lineTo(x, size.height * 0.22f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun systemLargePanelShape(): Shape =
    SystemCutCornerShape(SystemTheme.shapes.panelCut)

@Composable
fun systemPlateShape(): Shape =
    SystemCutCornerShape(SystemTheme.shapes.plateCut)

@Composable
fun systemControlShape(): Shape =
    SystemCutCornerShape(SystemTheme.shapes.controlCut)

@Composable
fun systemDialogShape(): Shape =
    SystemCutCornerShape(SystemTheme.shapes.dialogCut)

enum class TechSurfaceRole {
    Panel,
    Plate,
    Button,
    Dialog,
    Navigation
}

@Composable
fun Modifier.techSurface(
    shape: Shape = systemLargePanelShape(),
    active: Boolean = false,
    accent: Color = SystemTheme.colors.accentPrimary,
    role: TechSurfaceRole = TechSurfaceRole.Panel,
    enabled: Boolean = true
): Modifier {
    val colors = SystemTheme.colors
    val material = SystemTheme.material
    val depth = SystemTheme.depth
    val glow = SystemTheme.glow
    val baseBrush = when {
        role == TechSurfaceRole.Button && enabled -> Brush.linearGradient(
            listOf(
                accent.copy(alpha = if (active) 0.34f else 0.22f),
                material.buttonTop,
                material.buttonBottom
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
        role == TechSurfaceRole.Plate || !enabled -> Brush.linearGradient(
            listOf(material.plateTop, material.plateMid, material.plateBottom),
            start = Offset.Zero,
            end = Offset.Infinite
        )
        role == TechSurfaceRole.Dialog -> Brush.linearGradient(
            listOf(
                material.panelTop.copy(alpha = 1f),
                material.panelMid.copy(alpha = 1f),
                material.panelBottom.copy(alpha = 1f)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
        else -> Brush.linearGradient(
            listOf(material.panelTop, material.panelMid, material.panelBottom),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }
    val elevation = when {
        !enabled -> 0.dp
        role == TechSurfaceRole.Dialog -> depth.dialogElevation
        active && role == TechSurfaceRole.Button -> depth.buttonActiveElevation
        active -> depth.activeElevation
        role == TechSurfaceRole.Button -> depth.buttonElevation
        role == TechSurfaceRole.Plate -> depth.plateElevation
        role == TechSurfaceRole.Navigation -> depth.plateElevation
        else -> depth.panelElevation
    }
    val ambientColor = when {
        !enabled -> Color.Transparent
        active -> accent.copy(alpha = glow.activeAlpha)
        else -> depth.ambientShadow
    }
    val borderBrush = Brush.linearGradient(
        listOf(
            material.edgeHighlight.copy(alpha = if (enabled) 0.92f else 0.32f),
            if (enabled && active) accent.copy(alpha = 0.72f) else colors.borderSubtle.copy(alpha = 0.58f),
            material.edgeShade.copy(alpha = 0.88f),
            if (enabled) material.edgeShade.copy(alpha = 0.94f) else colors.borderMuted.copy(alpha = 0.24f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    val reflectedAlpha = when {
        !enabled -> 0.018f
        active -> glow.activeAlpha
        role == TechSurfaceRole.Button -> 0.085f
        role == TechSurfaceRole.Navigation -> 0.070f
        else -> glow.restingAlpha
    }
    val reflectedColor = when (accent) {
        colors.accentAi -> material.reflectedAi
        colors.accentPrimary -> material.reflectedPrimary
        else -> accent.copy(alpha = 0.10f)
    }
    val textureAlpha = when (role) {
        TechSurfaceRole.Panel -> 0.022f
        TechSurfaceRole.Plate -> 0.014f
        TechSurfaceRole.Button -> 0.018f
        TechSurfaceRole.Dialog -> 0.018f
        TechSurfaceRole.Navigation -> 0.014f
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = ambientColor,
            spotColor = if (enabled) depth.contactShadow else Color.Transparent
        )
        .clip(shape)
        .background(baseBrush)
        .drawWithCache {
            val textureStroke = 0.55.dp.toPx()
            val textureSpacing = when (role) {
                TechSurfaceRole.Panel, TechSurfaceRole.Dialog -> 22.dp.toPx()
                else -> 26.dp.toPx()
            }
            val surfaceSize = size
            val textureLines = buildList {
                var textureX = -surfaceSize.height
                while (textureX < surfaceSize.width + surfaceSize.height) {
                    add(
                        Offset(textureX, surfaceSize.height) to
                            Offset(textureX + surfaceSize.height, 0f)
                    )
                    textureX += textureSpacing
                }
            }
            val innerBrush = Brush.linearGradient(
                colors = listOf(
                    material.innerHighlight,
                    Color.Transparent,
                    material.innerShade
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
            val reflectedBrush = Brush.radialGradient(
                colors = listOf(
                    reflectedColor.copy(alpha = reflectedAlpha),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.08f, size.height * 0.04f),
                radius = size.maxDimension * 0.48f
            )
            val outerStroke = 1.dp.toPx()
            val bevelStroke = 1.35.dp.toPx()
            val innerStroke = 0.65.dp.toPx()
            val railStroke = 1.15.dp.toPx()
            val frameInset = when (role) {
                TechSurfaceRole.Panel, TechSurfaceRole.Dialog -> 5.5.dp.toPx()
                TechSurfaceRole.Navigation -> 4.dp.toPx()
                TechSurfaceRole.Button -> 3.5.dp.toPx()
                TechSurfaceRole.Plate -> 3.dp.toPx()
            }
            val nominalCut = when (role) {
                TechSurfaceRole.Panel -> 12.dp.toPx()
                TechSurfaceRole.Dialog -> 14.dp.toPx()
                TechSurfaceRole.Navigation -> 10.dp.toPx()
                TechSurfaceRole.Button -> 7.dp.toPx()
                TechSurfaceRole.Plate -> 8.dp.toPx()
            }
            val isArmorPanel = role == TechSurfaceRole.Panel ||
                role == TechSurfaceRole.Dialog ||
                role == TechSurfaceRole.Navigation

            fun chamferedFrame(inset: Float, requestedCut: Float): Path {
                val left = inset.coerceAtMost(size.width * 0.24f)
                val top = inset.coerceAtMost(size.height * 0.24f)
                val right = (size.width - inset).coerceAtLeast(left)
                val bottom = (size.height - inset).coerceAtLeast(top)
                val cut = requestedCut
                    .coerceAtLeast(1.dp.toPx())
                    .coerceAtMost((right - left) * 0.24f)
                    .coerceAtMost((bottom - top) * 0.24f)
                return Path().apply {
                    moveTo(left + cut, top)
                    lineTo(right - cut, top)
                    lineTo(right, top + cut)
                    lineTo(right, bottom - cut)
                    lineTo(right - cut, bottom)
                    lineTo(left + cut, bottom)
                    lineTo(left, bottom - cut)
                    lineTo(left, top + cut)
                    close()
                }
            }

            val bevelFrame = chamferedFrame(
                inset = 2.dp.toPx(),
                requestedCut = nominalCut - 1.dp.toPx()
            )
            val innerFrame = chamferedFrame(
                inset = frameInset,
                requestedCut = nominalCut - frameInset * 0.46f
            )
            val armorInset = if (isArmorPanel) 8.5.dp.toPx() else 5.dp.toPx()
            val armorFrame = chamferedFrame(
                inset = armorInset,
                requestedCut = nominalCut - armorInset * 0.42f
            )
            val cornerRail = material.railInactive
            val fastenerRadius = 2.15.dp.toPx()
            val fastenerInset = 9.5.dp.toPx()
            val fasteners = listOf(
                Offset(fastenerInset, fastenerInset),
                Offset(size.width - fastenerInset, fastenerInset),
                Offset(fastenerInset, size.height - fastenerInset),
                Offset(size.width - fastenerInset, size.height - fastenerInset)
            )
            val topNotch = Path().apply {
                val half = size.width * 0.095f
                val notch = 3.5.dp.toPx()
                moveTo(size.width / 2f - half, frameInset)
                lineTo(size.width / 2f - half * 0.76f, frameInset + notch)
                lineTo(size.width / 2f + half * 0.76f, frameInset + notch)
                lineTo(size.width / 2f + half, frameInset)
            }

            onDrawBehind {
                drawRect(brush = innerBrush)
                textureLines.forEach { (start, end) ->
                    drawLine(
                        color = material.edgeHighlight.copy(alpha = textureAlpha),
                        start = start,
                        end = end,
                        strokeWidth = textureStroke
                    )
                }
                drawRect(brush = reflectedBrush)

                drawPath(
                    path = bevelFrame,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            material.edgeHighlight.copy(alpha = if (enabled) 0.78f else 0.24f),
                            material.railInactive.copy(alpha = 0.72f),
                            material.edgeShade.copy(alpha = 0.96f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    style = Stroke(width = bevelStroke)
                )
                drawPath(
                    path = innerFrame,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            material.edgeHighlight.copy(alpha = 0.52f),
                            material.railInactive,
                            material.edgeShade.copy(alpha = 0.82f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    style = Stroke(width = outerStroke)
                )
                drawPath(
                    path = armorFrame,
                    color = colors.borderMuted.copy(alpha = if (isArmorPanel) 0.42f else 0.24f),
                    style = Stroke(width = innerStroke)
                )

                if (isArmorPanel && size.width > 150.dp.toPx() && size.height > 58.dp.toPx()) {
                    drawPath(
                        path = topNotch,
                        color = material.edgeHighlight.copy(alpha = 0.50f),
                        style = Stroke(width = innerStroke)
                    )
                    fasteners.forEach { center ->
                        drawCircle(
                            color = material.edgeShade.copy(alpha = 0.90f),
                            radius = fastenerRadius,
                            center = center
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    material.edgeHighlight.copy(alpha = 0.72f),
                                    material.railInactive,
                                    material.edgeShade
                                ),
                                center = center - Offset(
                                    fastenerRadius * 0.32f,
                                    fastenerRadius * 0.32f
                                ),
                                radius = fastenerRadius * 1.2f
                            ),
                            radius = fastenerRadius * 0.72f,
                            center = center
                        )
                    }

                    val bracket = 10.dp.toPx()
                    val bracketInset = armorInset + 2.dp.toPx()
                    listOf(
                        Offset(bracketInset, bracketInset),
                        Offset(size.width - bracketInset, bracketInset),
                        Offset(bracketInset, size.height - bracketInset),
                        Offset(size.width - bracketInset, size.height - bracketInset)
                    ).forEachIndexed { index, corner ->
                        val horizontalDirection = if (index % 2 == 0) 1f else -1f
                        val verticalDirection = if (index < 2) 1f else -1f
                        drawLine(
                            color = cornerRail,
                            start = corner,
                            end = corner + Offset(horizontalDirection * bracket, 0f),
                            strokeWidth = railStroke
                        )
                        drawLine(
                            color = material.edgeHighlight.copy(alpha = 0.34f),
                            start = corner,
                            end = corner + Offset(0f, verticalDirection * bracket),
                            strokeWidth = innerStroke
                        )
                    }

                    repeat(3) { index ->
                        val tickX = size.width - armorInset - (index * 5.dp.toPx())
                        drawLine(
                            color = material.railInactive,
                            start = Offset(tickX, armorInset),
                            end = Offset(tickX - 2.5.dp.toPx(), armorInset + 3.dp.toPx()),
                            strokeWidth = innerStroke
                        )
                    }
                }
            }
        }
        .border(BorderStroke(1.dp, borderBrush), shape)
}

@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accent: Color = SystemTheme.colors.accentPrimary,
    contentPadding: Dp = SystemCardPadding,
    content: @Composable () -> Unit
) {
    val shape = systemLargePanelShape()
    Box(
        modifier = modifier
            .techSurface(
                shape = shape,
                active = active,
                accent = accent,
                role = TechSurfaceRole.Panel
            )
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
private fun PanelTexture(
    accent: Color,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = if (active) 0.18f else 0.07f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.10f, size.height * 0.06f),
                radius = size.maxDimension * 0.62f
            )
        )
        drawRect(
            brush = Brush.linearGradient(
                listOf(
                    Color.Transparent,
                    accent.copy(alpha = if (active) 0.07f else 0.035f),
                    Color.Transparent
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
    }
}

@Composable
fun SystemSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title.toSystemSentenceCase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentPrimary,
                    fontFamily = SystemDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.7.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SystemMetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary,
    icon: ImageVector? = null,
    subtitle: String? = null
) {
    val colors = SystemTheme.colors
    val shape = systemPlateShape()
    Column(
        modifier = modifier
            .techSurface(
                shape = shape,
                active = false,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label.toSystemSentenceCase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SystemAvatarBadge(
    avatarUri: String?,
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary,
    onClick: (() -> Unit)? = null
) {
    val colors = SystemTheme.colors
    val context = LocalContext.current
    val shape = SystemHexagonShape()
    Box(
        modifier = modifier
            .shadow(
                elevation = SystemTheme.depth.activeElevation,
                shape = shape,
                ambientColor = accent.copy(alpha = SystemTheme.glow.activeAlpha),
                spotColor = SystemTheme.depth.contactShadow
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.backgroundElevated,
                        colors.backgroundSecondary,
                        colors.background
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .drawWithCache {
                fun avatarHex(inset: Float): Path {
                    val left = inset
                    val top = inset
                    val right = size.width - inset
                    val bottom = size.height - inset
                    val shoulder = (right - left) * 0.12f
                    return Path().apply {
                        moveTo((left + right) / 2f, top)
                        lineTo(right - shoulder, top + (bottom - top) * 0.22f)
                        lineTo(right - shoulder, top + (bottom - top) * 0.78f)
                        lineTo((left + right) / 2f, bottom)
                        lineTo(left + shoulder, top + (bottom - top) * 0.78f)
                        lineTo(left + shoulder, top + (bottom - top) * 0.22f)
                        close()
                    }
                }

                val outerFrame = avatarHex(3.dp.toPx())
                val innerFrame = avatarHex(8.dp.toPx())
                onDrawWithContent {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.28f, size.height * 0.20f),
                            radius = size.maxDimension * 0.72f
                        )
                    )
                    drawContent()
                    drawPath(
                        path = outerFrame,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                colors.accentAi.copy(alpha = 0.76f),
                                colors.textPrimary.copy(alpha = 0.56f),
                                accent,
                                colors.borderMuted,
                                colors.accentAi.copy(alpha = 0.76f)
                            ),
                            center = center
                        ),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawPath(
                        path = innerFrame,
                        color = colors.background.copy(alpha = 0.82f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawPath(
                        path = innerFrame,
                        color = colors.textSecondary.copy(alpha = 0.62f),
                        style = Stroke(width = 0.8.dp.toPx())
                    )
                }
            }
            .border(
                BorderStroke(
                    2.dp,
                    Brush.sweepGradient(
                        listOf(
                            colors.overlayStrong,
                            accent,
                            colors.overlayMedium,
                            colors.accentAi.copy(alpha = 0.34f),
                            accent
                        )
                    )
                ),
                shape
            )
            .then(
                if (onClick != null) {
                    Modifier.systemClickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Аватар",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.46f), Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.62f
                    )
                )
                val hood = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.86f)
                    cubicTo(
                        size.width * 0.18f,
                        size.height * 0.48f,
                        size.width * 0.28f,
                        size.height * 0.18f,
                        size.width * 0.50f,
                        size.height * 0.12f
                    )
                    cubicTo(
                        size.width * 0.72f,
                        size.height * 0.18f,
                        size.width * 0.82f,
                        size.height * 0.48f,
                        size.width * 0.78f,
                        size.height * 0.86f
                    )
                    cubicTo(
                        size.width * 0.64f,
                        size.height * 0.74f,
                        size.width * 0.36f,
                        size.height * 0.74f,
                        size.width * 0.22f,
                        size.height * 0.86f
                    )
                    close()
                }
                drawPath(
                    path = hood,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1D2732), Color(0xFF05080C))
                    )
                )
                drawPath(
                    path = hood,
                    color = accent.copy(alpha = 0.35f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                    color = accent,
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.43f, size.height * 0.52f)
                )
                drawCircle(
                    color = accent,
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.57f, size.height * 0.52f)
                )
            }
        }
    }
}

@Composable
fun SystemHoodBadge(
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary,
    onClick: (() -> Unit)? = null
) {
    Canvas(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.systemClickable(onClick = onClick)
            } else {
                Modifier
            }
        )
    ) {
        val hex = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.02f)
            lineTo(size.width * 0.90f, size.height * 0.25f)
            lineTo(size.width * 0.90f, size.height * 0.70f)
            lineTo(size.width * 0.50f, size.height * 0.98f)
            lineTo(size.width * 0.10f, size.height * 0.70f)
            lineTo(size.width * 0.10f, size.height * 0.25f)
            close()
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.30f), Color.Transparent),
                center = Offset(size.width * 0.50f, size.height * 0.98f),
                radius = size.width * 0.34f
            ),
            radius = size.width * 0.34f,
            center = Offset(size.width * 0.50f, size.height * 0.98f)
        )
        drawPath(hex, color = Color(0x0A00E5FF))
        drawPath(
            hex,
            color = accent.copy(alpha = 0.96f),
            style = Stroke(width = 1.85.dp.toPx())
        )
        val hood = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.72f)
            cubicTo(
                size.width * 0.29f, size.height * 0.20f,
                size.width * 0.71f, size.height * 0.20f,
                size.width * 0.71f, size.height * 0.72f
            )
            cubicTo(
                size.width * 0.61f, size.height * 0.66f,
                size.width * 0.39f, size.height * 0.66f,
                size.width * 0.29f, size.height * 0.72f
            )
            close()
        }
        drawPath(hood, color = Color(0x3302070D))
        drawPath(
            hood,
            color = accent.copy(alpha = 0.74f),
            style = Stroke(width = 0.8.dp.toPx())
        )
        drawCircle(accent, radius = 2.55.dp.toPx(), center = Offset(size.width * 0.43f, size.height * 0.52f))
        drawCircle(accent, radius = 2.55.dp.toPx(), center = Offset(size.width * 0.57f, size.height * 0.52f))
        drawCircle(accent.copy(alpha = 0.92f), radius = 2.1.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.98f))
    }
}

@Composable
fun SystemHexIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary,
    contentDescription: String? = null
) {
    val colors = SystemTheme.colors
    val shape = SystemHexagonShape()
    Box(
        modifier = modifier
            .shadow(
                elevation = SystemTheme.depth.plateElevation,
                shape = shape,
                ambientColor = accent.copy(alpha = SystemTheme.glow.restingAlpha),
                spotColor = SystemTheme.depth.contactShadow
            )
            .clip(shape)
            .background(accent.copy(alpha = 0.12f))
            .drawWithCache {
                fun hexPath(inset: Float): Path {
                    val left = inset
                    val top = inset
                    val right = size.width - inset
                    val bottom = size.height - inset
                    val shoulder = (right - left) * 0.16f
                    return Path().apply {
                        moveTo((left + right) / 2f, top)
                        lineTo(right - shoulder, top + (bottom - top) * 0.20f)
                        lineTo(right - shoulder, top + (bottom - top) * 0.80f)
                        lineTo((left + right) / 2f, bottom)
                        lineTo(left + shoulder, top + (bottom - top) * 0.80f)
                        lineTo(left + shoulder, top + (bottom - top) * 0.20f)
                        close()
                    }
                }

                val outerHex = hexPath(1.5.dp.toPx())
                val innerHex = hexPath(5.dp.toPx())
                onDrawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.34f, size.height * 0.28f),
                            radius = size.maxDimension * 0.58f
                        )
                    )
                    drawPath(
                        path = outerHex,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.textPrimary.copy(alpha = 0.62f),
                                accent.copy(alpha = 0.82f),
                                Color.Black.copy(alpha = 0.86f)
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        ),
                        style = Stroke(width = 1.15.dp.toPx())
                    )
                    drawPath(
                        path = innerHex,
                        color = colors.borderMuted.copy(alpha = 0.58f),
                        style = Stroke(width = 0.65.dp.toPx())
                    )
                }
            }
            .border(1.dp, accent.copy(alpha = 0.42f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent,
            modifier = Modifier.fillMaxSize(0.46f)
        )
    }
}

@Composable
fun SystemReadinessRing(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary
) {
    val colors = SystemTheme.colors
    val boundedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = boundedProgress,
                range = 0f..1f
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2f
            val segmentCount = 44
            val segmentStep = 360f / segmentCount
            val segmentSweep = segmentStep - 2.4f
            val activeSegments = (boundedProgress * segmentCount).toInt()
            val arcSize = Size(size.width - stroke, size.height - stroke)

            repeat(segmentCount) { index ->
                drawArc(
                    color = colors.overlayMedium.copy(alpha = if (index % 4 == 0) 0.95f else 0.68f),
                    startAngle = -90f + index * segmentStep,
                    sweepAngle = segmentSweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                if (index < activeSegments) {
                    val segmentColor = when {
                        accent == colors.accentAi && index < segmentCount * 0.30f -> colors.accentPrimary
                        else -> accent
                    }
                    drawArc(
                        color = segmentColor.copy(alpha = if (index % 5 == 0) 1f else 0.86f),
                        startAngle = -90f + index * segmentStep,
                        sweepAngle = segmentSweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                }
            }
            drawCircle(
                color = colors.borderMuted.copy(alpha = 0.52f),
                radius = size.minDimension * 0.36f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.20f),
                radius = size.minDimension * 0.31f,
                style = Stroke(width = 0.6.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(boundedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = label.toSystemSentenceCase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SystemSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accent: Color = SystemTheme.colors.textSecondary,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = systemPlateShape()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .techSurface(
                shape = shape,
                active = false,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .systemClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        if (icon != null) {
            SystemHexIcon(
                icon = icon,
                accent = accent,
                modifier = Modifier.size(42.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SystemInlineStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary
) {
    Row(
        modifier = modifier.widthIn(min = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(SystemHexagonShape())
                .background(accent)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = accent,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = SystemTheme.colors.textSecondary,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SystemDivider(
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.34f),
                        SystemTheme.colors.borderSubtle,
                        Color.Transparent
                    )
                )
            )
    )
}
