package com.ihor.thesystem.feature.status.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.components.GlitchText
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.*
import com.ihor.thesystem.feature.status.ui.components.CurrentDateBlock
import com.ihor.thesystem.feature.status.ui.components.dialogs.AddTaskDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.LevelUpDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.PenaltyActivatedDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.PenaltyDeactivatedDialog
import com.ihor.thesystem.feature.status.viewmodel.*
import kotlin.system.exitProcess

@Composable
fun StatusScreen(
    navController: NavHostController,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState      by viewModel.uiState.collectAsState()
    val dialogState  by viewModel.dialogState.collectAsState()

    var levelUpEvent by remember { mutableStateOf<Any?>(null) }
    var showPenaltyOn by remember { mutableStateOf(false) }
    var showPenaltyOff by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> navController.popBackStack()
                is UiEvent.ShowError -> {
                    // Handle error if needed, e.g., Toast or Snackbar
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StatusOneOffEvent.ShowLevelUp -> levelUpEvent = event
                is StatusOneOffEvent.ShowPenaltyActivated -> showPenaltyOn = true
                is StatusOneOffEvent.ShowPenaltyDeactivated -> showPenaltyOff = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedPremiumBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = NeonCyan) }
                }

                is UiState.Content<*> -> {
                    val data = state.data as StatusUiData
                    
                    StatusProfileHeader(data)
                    
                    MainWorkoutCard(
                        data = data,
                        onStartWorkout = { /* viewModel.onStartWorkout() */ }
                    )

                    DailyQuestsSection(
                        dailyQuest = data.dailyQuest,
                        promotionQuests = data.promotionQuests,
                        onTaskToggled = { task, qId -> viewModel.onTaskToggled(task, qId) },
                        onAddTask = { qId -> viewModel.onAddTaskTap(qId) },
                        onRemoveTask = { id -> viewModel.onRemoveTask(id) }
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }

                is UiState.Error -> {
                    DatabaseErrorScreen(state.message)
                }
            }
        }

        levelUpEvent?.let { event ->
            val levelUp = event as StatusOneOffEvent.ShowLevelUp
            LevelUpDialog(
                newClass = levelUp.newClass,
                newMonth = levelUp.newMonth,
                onDismiss = { levelUpEvent = null }
            )
        }

        if (showPenaltyOn) {
            PenaltyActivatedDialog(onDismiss = { showPenaltyOn = false })
        }
        if (showPenaltyOff) {
            PenaltyDeactivatedDialog(onDismiss = { showPenaltyOff = false })
        }

        when (val dState = dialogState) {
            is StatusDialogState.AddTask -> {
                AddTaskDialog(
                    onConfirm = { name -> viewModel.onAddTaskConfirmed(dState.questId, name) },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun DatabaseErrorScreen(message: UiText) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlitchText(
            text = "CRITICAL ERROR",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            primaryColor = Color.Red
        )
        Text(
            text = message.asString(context),
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { exitProcess(0) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f))
        ) {
            Text("TERMINATE SYSTEM", color = Color.Red)
        }
    }
}

@Composable
private fun MainWorkoutCard(
    data: StatusUiData,
    onStartWorkout: () -> Unit
) {
    val mainQuest = data.mainQuest
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ОСНОВНЕ ТРЕНУВАННЯ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            )

            if (mainQuest != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = mainQuest.title,
                        color = Color(0xFFB257FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = mainQuest.subtitle,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .blur(30.dp)
                        .background(Color(0xFF00F0FF).copy(alpha = 0.3f), CircleShape)
                )
                Text(
                    text = if (mainQuest != null) "🔥" else "🛌",
                    fontSize = 48.sp
                )
            }

            if (mainQuest != null && !mainQuest.isCompleted) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onStartWorkout() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00F0FF), Color(0xFF00D9E8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ПОЧАТИ ТРЕНУВАННЯ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else if (mainQuest?.isCompleted == true) {
                Text(
                    text = "ТРЕНУВАННЯ ЗАВЕРШЕНО ✓",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun DailyQuestsSection(
    dailyQuest: QuestUiModel?,
    promotionQuests: List<QuestUiModel>,
    onTaskToggled: (TaskUiModel, Int) -> Unit,
    onAddTask: (Int) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Завдання на день",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val quests = mutableListOf<QuestUiModel>()
                    dailyQuest?.let { quests.add(it) }
                    quests.addAll(promotionQuests)

                    quests.forEach { quest ->
                        quest.tasks.forEach { task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Кружечок виконання
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (task.isCompleted) Color(0xFF00F0FF).copy(alpha = 0.2f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (task.isCompleted) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .clickable { onTaskToggled(task, quest.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF00F0FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = task.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (task.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                                    )
                                )

                                IconButton(
                                    onClick = { onRemoveTask(task.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Видалити",
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Кнопка додавання завдання (Креативне рішення: стильна капсула внизу списку)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(44.dp)
                                .clickable { onAddTask(quest.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.03f),
                            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF00F0FF).copy(alpha = 0.3f), Color.Transparent)))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "НОВЕ ЗАВДАННЯ",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusProfileHeader(data: StatusUiData) {
    val locale = java.util.Locale("uk")
    val today = java.time.LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale)
        .replaceFirstChar { it.uppercase() }
    val day = String.format(locale, "%02d", today.dayOfMonth)
    val month = String.format(locale, "%02d", today.monthValue)
    val dateDisplay = "$dayOfWeek. $day/$month"

    val infiniteTransition = rememberInfiniteTransition(label = "BorderPulse")
    val borderColorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderAlpha"
    )

    val progress = if (data.xpMax > 0) data.xpTotal.toFloat() / data.xpMax else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "THE SYSTEM: LEVEL UP",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF00F0FF).copy(alpha = borderColorAlpha),
                                        Color(0xFFB257FF).copy(alpha = borderColorAlpha),
                                        Color(0xFF00F0FF).copy(alpha = borderColorAlpha)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = data.playerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Surface(
                            color = Color(0xFF00F0FF).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Рівень ${data.level}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF00F0FF),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Text(
                    text = dateDisplay.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF00F0FF).copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "XP: ${data.xpTotal} / ${data.xpMax}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00F0FF), Color(0xFF00D9E8))
                            )
                        )
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(6.dp), ambientColor = Color(0xFF00F0FF), spotColor = Color(0xFF00F0FF))
                )
            }
        }
    }
}

@Composable
private fun AnimatedPremiumBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundAnim")
    
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0F051D),
        targetValue = Color(0xFF1A0B2E),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Color1"
    )
    
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF050B1D),
        targetValue = Color(0xFF0B162E),
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Color2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(color1, color2)
            )
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00F0FF).copy(alpha = 0.05f), Color.Transparent),
                center = center,
                radius = size.maxDimension / 2
            ),
            center = center,
            radius = size.maxDimension / 2
        )
    }
}
