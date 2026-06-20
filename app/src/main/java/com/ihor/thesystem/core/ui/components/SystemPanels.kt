package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
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
fun systemLargePanelShape(): RoundedCornerShape =
    RoundedCornerShape(SystemTheme.shapes.large)

enum class TechSurfaceRole {
    Panel,
    Plate,
    Button
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
    val baseBrush = when {
        role == TechSurfaceRole.Button && enabled -> Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.50f),
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
        else -> Brush.linearGradient(
            listOf(material.panelTop, material.panelMid, material.panelBottom),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }
    val elevation = when {
        !enabled -> 0.dp
        active -> material.activeElevation
        role == TechSurfaceRole.Button -> material.buttonElevation
        role == TechSurfaceRole.Plate -> material.plateElevation
        else -> material.raisedElevation
    }
    val ambientColor = when {
        !enabled -> Color.Transparent
        active -> accent.copy(alpha = if (role == TechSurfaceRole.Button) 0.24f else 0.16f)
        else -> material.ambientShadow
    }
    val borderBrush = Brush.linearGradient(
        listOf(
            material.edgeHighlight.copy(alpha = if (enabled) 0.92f else 0.34f),
            if (enabled && active) accent.copy(alpha = 0.78f) else colors.borderSubtle.copy(alpha = 0.78f),
            material.edgeShade.copy(alpha = 0.72f),
            if (enabled) colors.borderMuted.copy(alpha = 0.92f) else colors.borderMuted.copy(alpha = 0.42f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    val reflectedAlpha = when {
        !enabled -> 0.025f
        role == TechSurfaceRole.Button -> if (active) 0.22f else 0.15f
        active -> 0.14f
        else -> 0.065f
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = ambientColor,
            spotColor = if (enabled) material.contactShadow else Color.Transparent
        )
        .shadow(
            elevation = if (enabled) material.contactElevation else 0.dp,
            shape = shape,
            ambientColor = Color.Transparent,
            spotColor = material.contactShadow
        )
        .clip(shape)
        .background(baseBrush)
        .drawBehind {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        material.innerHighlight,
                        Color.Transparent,
                        material.innerShade
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = reflectedAlpha), Color.Transparent),
                    center = Offset(size.width * 0.18f, size.height * 0.10f),
                    radius = size.maxDimension * 0.62f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = reflectedAlpha * 0.58f), Color.Transparent),
                    center = Offset(size.width * 0.90f, size.height * 0.82f),
                    radius = size.maxDimension * 0.52f
                )
            )
            val stroke = 1.dp.toPx()
            drawLine(
                color = material.edgeHighlight,
                start = Offset(stroke / 2f, stroke / 2f),
                end = Offset(size.width - stroke / 2f, stroke / 2f),
                strokeWidth = stroke
            )
            drawLine(
                color = material.edgeHighlight.copy(alpha = 0.14f),
                start = Offset(stroke / 2f, stroke / 2f),
                end = Offset(stroke / 2f, size.height - stroke / 2f),
                strokeWidth = stroke
            )
            drawLine(
                color = material.edgeShade,
                start = Offset(stroke / 2f, size.height - stroke / 2f),
                end = Offset(size.width - stroke / 2f, size.height - stroke / 2f),
                strokeWidth = stroke
            )
            drawLine(
                color = material.edgeShade.copy(alpha = 0.38f),
                start = Offset(size.width - stroke / 2f, stroke / 2f),
                end = Offset(size.width - stroke / 2f, size.height - stroke / 2f),
                strokeWidth = stroke
            )
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
                center = Offset(size.width * 0.88f, size.height * 0.06f),
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
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height)
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
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
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
    Column(
        modifier = modifier
            .clip(SystemCutCornerShape(10.dp))
            .background(colors.overlayLight)
            .border(1.dp, accent.copy(alpha = 0.18f), SystemCutCornerShape(10.dp))
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
            text = label.uppercase(),
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
                maxLines = 1,
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
            .clip(shape)
            .background(colors.backgroundElevated)
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
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
            .clip(shape)
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.38f), shape),
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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2f
            drawArc(
                color = colors.overlayMedium,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(accent, colors.accentAi, accent)
                ),
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = label.uppercase(),
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SystemCutCornerShape(10.dp))
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, SystemCutCornerShape(10.dp))
            .clickable(onClick = onClick)
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
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
