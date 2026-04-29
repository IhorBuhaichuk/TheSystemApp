package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.OnSurfaceVariant
import com.ihor.thesystem.core.theme.Primary
import com.ihor.thesystem.core.theme.StatusError
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.domain.model.BodyWeightLog
import com.ihor.thesystem.domain.model.MuscleGroup
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.feature.statistics.ui.components.RadarChartCanvas
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogAgeDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogHeightDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogWeightDialog
import com.ihor.thesystem.feature.statistics.ui.dialogs.LogWorkoutSetsDialog
import com.ihor.thesystem.feature.statistics.ui.dialogs.SetupMatrixDialog
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsDialogState
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsViewModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
import java.util.Locale
import kotlin.math.roundToInt

private val StatsPanel = Color(0xFF07111F)
private val StatsPanelSoft = Color(0xFF0D1728)
private val StatsLine = Color(0xFF22304A)
private val StatsText = Color(0xFFF3F7FF)
private val StatsMuted = Color(0xFF8FA0B9)
private val StatsBlue = Color(0xFF6EA8FF)
private val StatsCyan = Color(0xFF67E8F9)
private val StatsViolet = Color(0xFF8B5CF6)
private val StatsGreen = Color(0xFF76F0A2)
private val StatsGold = Color(0xFFFFD07A)
private val StatsRed = Color(0xFFFF6B76)

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(event.uiText.asString(context))
                UiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RpgStatusBackdrop()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Crossfade(
                targetState = uiState,
                label = "stats_state",
                modifier = Modifier.padding(padding)
            ) { state ->
                when (state) {
                    UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    is UiState.Content -> {
                        StatsDashboard(
                            data = state.data,
                            onWeightTap = viewModel::onOpenLogWeight,
                            onHeightTap = viewModel::onOpenEditHeight,
                            onAgeTap = viewModel::onOpenEditAge,
                            onOpenLogSets = viewModel::onOpenLogSets,
                            onOpenSetup = viewModel::onOpenSetup
                        )

                        StatsDialogs(
                            dialogState = dialogState,
                            data = state.data,
                            viewModel = viewModel
                        )
                    }

                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message.asString(context),
                                color = StatusError,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsDashboard(
    data: StatisticsUiData,
    onWeightTap: () -> Unit,
    onHeightTap: () -> Unit,
    onAgeTap: () -> Unit,
    onOpenLogSets: (MatrixEntryUiModel) -> Unit,
    onOpenSetup: (MatrixEntryUiModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StatsTopBar(data)
        StatsHeroPanel(data)
        VitalsCard(
            data = data,
            onWeightTap = onWeightTap,
            onHeightTap = onHeightTap,
            onAgeTap = onAgeTap
        )
        CoreAttributesCard(data.characterAttributes)
        MatrixExercisesCard(
            entries = data.matrixEntries,
            onOpenLogSets = onOpenLogSets,
            onOpenSetup = onOpenSetup
        )
        if (data.weightHistory.size >= 2) {
            WeightTrendPanel(data.weightHistory)
        }
    }
}

@Composable
private fun StatsTopBar(data: StatisticsUiData) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, StatsBlue.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (data.avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = StatsMuted,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = data.playerName.ifBlank { "TheSystem" },
                    color = StatsText,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = data.playerClass.asUiText().asString(context),
                    color = StatsViolet,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        Surface(
            color = StatsGold.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StatsGold.copy(alpha = 0.22f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = StatsGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${data.currentStreak} дн.",
                    color = StatsGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatsHeroPanel(data: StatisticsUiData) {
    val progress = (data.xpTotal.toFloat() / data.xpMax.coerceAtLeast(1)).coerceIn(0f, 1f)

    SectionPanel(contentPadding = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.status_rpg_hero_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF050A13).copy(alpha = 0.92f),
                                Color(0xFF050A13).copy(alpha = 0.58f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("РІВЕНЬ", color = StatsMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = data.level.toString().padStart(2, '0'),
                            color = StatsText,
                            fontSize = 48.sp,
                            lineHeight = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${data.globalRank.name}-CLASS",
                            color = rankColor(data.globalRank),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    RankEmblem(rank = data.globalRank, size = 58.dp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("XP ДО НАСТУПНОГО РІВНЯ", color = StatsMuted, fontSize = 10.sp)
                            Text(
                                text = "${data.xpTotal} / ${data.xpMax} XP",
                                color = StatsText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${(progress * 100f).roundToInt()}%",
                            color = StatsBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    GlowingProgressBar(progress = progress, color = StatsBlue)
                }
            }
        }
    }
}

@Composable
private fun VitalsCard(
    data: StatisticsUiData,
    onWeightTap: () -> Unit,
    onHeightTap: () -> Unit,
    onAgeTap: () -> Unit
) {
    SectionPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VitalItem(
                icon = Icons.Filled.MonitorWeight,
                label = "Вага",
                value = formatKg(data.currentWeight),
                color = StatsCyan,
                onClick = onWeightTap,
                modifier = Modifier.weight(1f)
            )
            VitalItem(
                icon = Icons.Filled.Height,
                label = "Зріст",
                value = formatCm(data.currentHeight),
                color = StatsGreen,
                onClick = onHeightTap,
                modifier = Modifier.weight(1f)
            )
            VitalItem(
                icon = Icons.Filled.Cake,
                label = "Вік",
                value = if (data.age > 0) "${data.age}" else "-",
                color = StatsGold,
                onClick = onAgeTap,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VitalItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.07f))
            .border(1.dp, color.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Text(label, color = StatsMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = value,
            color = StatsText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CoreAttributesCard(attributes: Map<MuscleGroup, Float>) {
    SectionPanel {
        PanelHeader(title = "ХАРАКТЕРИСТИКА", trailing = "павутина")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(252.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            StatsBlue.copy(alpha = 0.16f),
                            StatsPanelSoft.copy(alpha = 0.72f)
                        )
                    )
                )
                .border(1.dp, StatsBlue.copy(alpha = 0.16f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            RadarChartCanvas(
                attributes = radarAttributes(attributes),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            attributeOrder().forEachIndexed { index, group ->
                AttributeRow(
                    label = attributeLabel(group),
                    value = attributes[group] ?: 0f,
                    color = attributeColor(index)
                )
            }
        }
    }
}

@Composable
private fun AttributeRow(label: String, value: Float, color: Color) {
    val normalized = (value / 50f).coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = StatsText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(86.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        GlowingProgressBar(
            progress = normalized,
            color = color,
            modifier = Modifier.weight(1f).height(6.dp)
        )
        Text(
            text = value.roundToInt().toString().padStart(2, '0'),
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(30.dp)
        )
    }
}

@Composable
private fun MatrixExercisesCard(
    entries: List<MatrixEntryUiModel>,
    onOpenLogSets: (MatrixEntryUiModel) -> Unit,
    onOpenSetup: (MatrixEntryUiModel) -> Unit
) {
    val visibleEntries = entries.filter { it.isActive }.ifEmpty { entries }

    SectionPanel {
        PanelHeader(title = "МАТРИЦЯ ВПРАВ", trailing = "${visibleEntries.size}")

        if (visibleEntries.isEmpty()) {
            Text(
                text = "Матриця ще порожня",
                color = StatsMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                visibleEntries.forEach { entry ->
                    MatrixExerciseRow(
                        entry = entry,
                        onOpenLogSets = { onOpenLogSets(entry) },
                        onOpenSetup = { onOpenSetup(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixExerciseRow(
    entry: MatrixEntryUiModel,
    onOpenLogSets: () -> Unit,
    onOpenSetup: () -> Unit
) {
    val color = rankColor(entry.currentRank)
    val progress = entry.progressPercent.coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = if (entry.isActive) 0.045f else 0.025f))
            .border(1.dp, color.copy(alpha = if (entry.isActive) 0.22f else 0.1f), RoundedCornerShape(18.dp))
            .clickable(enabled = entry.isActive, onClick = onOpenLogSets)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RankEmblem(rank = entry.currentRank, size = 44.dp)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.exerciseName,
                    color = if (entry.isActive) StatsText else StatsMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(progress * 100f).roundToInt()}%",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            GlowingProgressBar(progress = progress, color = color)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeightPill("Старт", formatKg(entry.startWeight), StatsMuted)
                WeightPill("Ціль", formatTarget(entry), StatsGold)
            }
        }

        IconButton(
            onClick = onOpenSetup,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Icon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = null,
                tint = StatsGold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WeightPill(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(label, color = StatsMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun WeightTrendPanel(history: List<BodyWeightLog>) {
    SectionPanel {
        PanelHeader(title = "ДИНАМІКА ВАГИ", trailing = "${history.size} записів")
        WeightSparkline(history)
    }
}

@Composable
private fun WeightSparkline(history: List<BodyWeightLog>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(top = 14.dp)
    ) {
        val weights = history.map { it.weight }
        val min = weights.minOrNull() ?: return@Canvas
        val max = weights.maxOrNull() ?: return@Canvas
        val range = (max - min).coerceAtLeast(1f)
        val points = history.mapIndexed { index, item ->
            val x = if (history.size == 1) 0f else size.width * (index.toFloat() / (history.lastIndex))
            val y = size.height - ((item.weight - min) / range * size.height * 0.78f) - size.height * 0.1f
            Offset(x, y)
        }

        val path = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        val fill = Path().apply {
            addPath(path)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }

        repeat(4) { index ->
            val y = size.height * (index + 1) / 5f
            drawLine(
                color = StatsLine.copy(alpha = 0.36f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                listOf(StatsBlue.copy(alpha = 0.22f), Color.Transparent)
            )
        )
        drawPath(
            path = path,
            color = StatsBlue,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        points.forEach { point ->
            drawCircle(StatsText, radius = 3.dp.toPx(), center = point)
            drawCircle(StatsBlue, radius = 6.dp.toPx(), center = point, style = Stroke(1.dp.toPx()))
        }
    }
}

@Composable
private fun SectionPanel(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(StatsPanel.copy(alpha = 0.82f))
            .border(1.dp, StatsLine.copy(alpha = 0.78f), RoundedCornerShape(22.dp))
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun PanelHeader(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = StatsText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        if (trailing != null) {
            Text(
                text = trailing,
                color = StatsMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GlowingProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(7.dp)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(650, easing = EaseOutCubic),
        label = "stats_progress"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.07f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.72f), color)
                    )
                )
        )
    }
}

@Composable
private fun RankEmblem(rank: Rank, size: Dp) {
    val color = rankColor(rank)
    val letterPlateSize = (size.value * 0.54f).dp
    val letterColor = if (rank == Rank.E) Color.White else color

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cut = this.size.minDimension * 0.2f
            val crest = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w - cut, cut * 0.55f)
                lineTo(w - cut * 0.55f, h - cut)
                lineTo(w / 2f, h)
                lineTo(cut * 0.55f, h - cut)
                lineTo(cut, cut * 0.55f)
                close()
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                    radius = this.size.minDimension * 0.72f
                )
            )
            drawPath(crest, StatsPanelSoft.copy(alpha = 0.94f))
            drawPath(
                path = crest,
                color = color.copy(alpha = 0.85f),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Image(
            painter = painterResource(rankBadgeRes(rank)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.82f)
        )
        Box(
            modifier = Modifier
                .size(letterPlateSize)
                .clip(CircleShape)
                .background(Color(0xFF020713).copy(alpha = 0.94f))
                .border(1.dp, color.copy(alpha = 0.92f), CircleShape)
        )
        Text(
            text = rank.name,
            style = MaterialTheme.typography.titleMedium.copy(
                color = letterColor,
                fontSize = (size.value * 0.32f).sp,
                fontWeight = FontWeight.Black,
                shadow = Shadow(color = color.copy(alpha = 0.9f), blurRadius = 10f)
            )
        )
    }
}

@Composable
private fun StatsDialogs(
    dialogState: StatisticsDialogState,
    data: StatisticsUiData,
    viewModel: StatisticsViewModel
) {
    when (dialogState) {
        StatisticsDialogState.None -> Unit
        StatisticsDialogState.LogWeight -> {
            LogWeightDialog(
                currentWeight = data.currentWeight,
                onConfirm = viewModel::onWeightConfirmed,
                onDismiss = viewModel::onDismissDialog
            )
        }
        StatisticsDialogState.EditHeight -> {
            LogHeightDialog(
                currentHeight = data.currentHeight,
                onConfirm = viewModel::onHeightConfirmed,
                onDismiss = viewModel::onDismissDialog
            )
        }
        StatisticsDialogState.EditAge -> {
            LogAgeDialog(
                currentAge = data.age,
                onConfirm = viewModel::onAgeConfirmed,
                onDismiss = viewModel::onDismissDialog
            )
        }
        is StatisticsDialogState.SetupMatrix -> {
            SetupMatrixDialog(
                exerciseName = dialogState.entry.exerciseName,
                initialStart = dialogState.startWeight,
                initialTarget = dialogState.targetWeight,
                onConfirm = { start, target ->
                    viewModel.onConfirmSetup(dialogState.entry.exerciseId, start, target)
                },
                onDismiss = viewModel::onDismissDialog
            )
        }
        is StatisticsDialogState.LogWorkoutSets -> {
            LogWorkoutSetsDialog(
                exerciseName = dialogState.entry.exerciseName,
                sets = dialogState.sets,
                onUpdate = viewModel::updateSetInput,
                onAdd = viewModel::addSet,
                onRemove = viewModel::removeSet,
                onSave = { feedback ->
                    viewModel.onLogSetsConfirmed(dialogState.entry.exerciseId, dialogState.sets, feedback)
                },
                onDismiss = viewModel::onDismissDialog
            )
        }
    }
}

private fun rankColor(rank: Rank): Color = when (rank) {
    Rank.E -> Color(0xFFC4D7FF)
    Rank.D -> Color(0xFF38BDF8)
    Rank.C -> Color(0xFF22C55E)
    Rank.B -> Color(0xFFF59E0B)
    Rank.A -> Color(0xFFA855F7)
    Rank.S -> Color(0xFFF43F5E)
}

private fun rankBadgeRes(rank: Rank): Int = when (rank) {
    Rank.E -> R.drawable.rank_badge_e
    Rank.D -> R.drawable.rank_badge_d
    Rank.C -> R.drawable.rank_badge_c
    Rank.B -> R.drawable.rank_badge_b
    Rank.A -> R.drawable.rank_badge_a
    Rank.S -> R.drawable.rank_badge_s
}

private fun attributeOrder(): List<MuscleGroup> = listOf(
    MuscleGroup.CHEST,
    MuscleGroup.BACK,
    MuscleGroup.SHOULDERS,
    MuscleGroup.QUADS,
    MuscleGroup.HAMSTRINGS_GLUTES,
    MuscleGroup.ARMS,
    MuscleGroup.ABS,
    MuscleGroup.LEGS,
    MuscleGroup.CORE
)

private fun attributeLabel(group: MuscleGroup): String = when (group) {
    MuscleGroup.CHEST -> "Груди"
    MuscleGroup.BACK -> "Спина"
    MuscleGroup.SHOULDERS -> "Плечі"
    MuscleGroup.QUADS -> "Квадри"
    MuscleGroup.HAMSTRINGS_GLUTES -> "Задня ланка"
    MuscleGroup.ARMS -> "Руки"
    MuscleGroup.ABS -> "Прес"
    MuscleGroup.LEGS -> "Ноги"
    MuscleGroup.CORE -> "Кор"
}

private fun attributeColor(index: Int): Color = when (index % 5) {
    0 -> StatsViolet
    1 -> StatsBlue
    2 -> StatsCyan
    3 -> StatsGreen
    else -> StatsGold
}

private fun radarAttributes(attributes: Map<MuscleGroup, Float>): Map<MuscleGroup, Float> {
    val scale = if ((attributes.values.maxOrNull() ?: 0f) <= 50f) 2f else 1f
    return attributes.mapValues { (_, value) -> (value * scale).coerceIn(0f, 100f) }
}

private fun formatKg(value: Float): String {
    if (value <= 0f) return "-"
    return if (value % 1f == 0f) {
        "${value.toInt()} кг"
    } else {
        String.format(Locale.US, "%.1f кг", value)
    }
}

private fun formatCm(value: Float): String {
    if (value <= 0f) return "-"
    return "${value.roundToInt()} см"
}

private fun formatTarget(entry: MatrixEntryUiModel): String {
    if (entry.targetWeight < 0f) return entry.targetWeightNote ?: "-"
    return formatKg(entry.targetWeight)
}
