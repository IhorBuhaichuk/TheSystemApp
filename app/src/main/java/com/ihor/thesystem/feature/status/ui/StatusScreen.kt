package com.ihor.thesystem.feature.status.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.components.GlitchText
import com.ihor.thesystem.feature.status.ui.components.dialogs.AddTaskDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.LevelUpDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.MainQuestWorkoutDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.WorkoutReportDialog
import com.ihor.thesystem.feature.status.viewmodel.*
import kotlin.system.exitProcess

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StatusScreen(
    navController: NavHostController,
    statusViewModel: StatusViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by statusViewModel.uiState.collectAsStateWithLifecycle()
    val statusDialogState by statusViewModel.dialogState.collectAsStateWithLifecycle()
    val workoutDialogState by workoutViewModel.dialogState.collectAsStateWithLifecycle()
    val activeDayWorkout by workoutViewModel.activeWorkoutState.collectAsStateWithLifecycle()
    val settingsUiState by workoutViewModel.settingsUiState.collectAsStateWithLifecycle()

    var levelUpEvent by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        statusViewModel.events.collect { event ->
            when (event) {
                is StatusOneOffEvent.ShowLevelUp -> levelUpEvent = event
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020408))) {
        AnimatedPremiumBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                }
                is UiState.Content<*> -> {
                    val data = state.data as StatusUiData
                    
                    HeaderSection(
                        data = data,
                        onAvatarSelected = { statusViewModel.updateAvatarUri(it) },
                        onEditNameTap = { statusViewModel.onEditNameTap() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (data.mainQuest != null && data.mainQuest.tasks.isNotEmpty()) {
                        MainWorkoutCardPremium(
                            data = data,
                            onStartWorkout = { workoutViewModel.onOpenMainWorkout() },
                            onOpenWorkoutSettings = { workoutViewModel.onOpenWorkoutSettings() }
                        )
                    } else {
                        ActiveRecoveryCard(
                            onOpenWorkoutSettings = { workoutViewModel.onOpenWorkoutSettings() }
                        )
                    }

                    DailyQuestsSectionPremium(
                        data = data,
                        onTaskToggled = { task, qId -> statusViewModel.onTaskToggled(task, qId) },
                        onAddTask = { qId -> statusViewModel.onAddTaskTap(qId) },
                        onRemoveTask = { id -> statusViewModel.onRemoveTask(id) }
                    )

                    Spacer(modifier = Modifier.height(88.dp))
                }
                is UiState.Error -> DatabaseErrorScreen(state.message)
            }
        }

        // Overlay Dialogs
        levelUpEvent?.let { event ->
            val levelUp = event as StatusOneOffEvent.ShowLevelUp
            LevelUpDialog(
                newClass = levelUp.newClass,
                newMonth = levelUp.newMonth,
                onDismiss = { levelUpEvent = null }
            )
        }
        
        // Status Dialogs
        when (val dState = statusDialogState) {
            is StatusDialogState.EditName -> {
                val currentData = (uiState as? UiState.Content)?.data
                if (currentData != null) {
                    com.ihor.thesystem.feature.statistics.ui.components.dialogs.EditNameDialog(
                        currentName = currentData.playerName,
                        onConfirm = { statusViewModel.onNameConfirmed(it) },
                        onDismiss = { statusViewModel.onDismissDialog() }
                    )
                }
            }
            is StatusDialogState.AddTask -> AddTaskDialog(
                onConfirm = { statusViewModel.onAddTaskConfirmed(dState.questId, it) },
                onDismiss = { statusViewModel.onDismissDialog() }
            )
            else -> {}
        }

        // Workout Dialogs
        when (val dState = workoutDialogState) {
            is StatusDialogState.MainQuestWorkout -> MainQuestWorkoutDialog(
                data = activeDayWorkout,
                onSetWeightChanged = { exId, setId, w -> workoutViewModel.onSetWeightChanged(exId, setId, w) },
                onSetRepsChanged = { exId, setId, r -> workoutViewModel.onSetRepsChanged(exId, setId, r) },
                onSetFocusLost = { exId, setId -> workoutViewModel.onSetFocusLost(exId, setId) },
                onSetCompleted = { exId, setId -> workoutViewModel.onSetCompleted(exId, setId) },
                onOpenSetup = { workoutViewModel.onOpenSetup(it, fromWorkout = true) },
                onFinishWorkout = { workoutViewModel.onFinishWorkout() },
                onDismiss = { workoutViewModel.onDismissDialog() }
            )
            is StatusDialogState.SetupMatrix -> {
                com.ihor.thesystem.feature.statistics.ui.dialogs.SetupMatrixDialog(
                    exerciseName = dState.entry.exerciseName,
                    initialStart = dState.startWeight,
                    initialTarget = dState.targetWeight,
                    onConfirm = { start, target ->
                        workoutViewModel.onConfirmSetup(dState.entry.exerciseId, start, target)
                    },
                    onDismiss = { 
                        if (dState.showWorkoutAfter) {
                            workoutViewModel.onOpenMainWorkout()
                        } else {
                            workoutViewModel.onDismissDialog()
                        }
                    }
                )
            }
            is StatusDialogState.LogWorkoutSets -> {
                com.ihor.thesystem.feature.statistics.ui.dialogs.LogWorkoutSetsDialog(
                    exerciseName = dState.entry.exerciseName,
                    sets = dState.sets,
                    onUpdate = { id, w, r -> 
                        workoutViewModel.updateLogSetInput(id, w, r)
                    },
                    onAdd = { workoutViewModel.addLogSet() },
                    onRemove = { workoutViewModel.removeLogSet() },
                    onSave = { feedback ->
                        workoutViewModel.onLogSetsConfirmed(dState.entry.exerciseId, dState.sets, feedback)
                    },
                    onDismiss = { 
                        if (dState.showWorkoutAfter) {
                            workoutViewModel.onOpenMainWorkout()
                        } else {
                            workoutViewModel.onDismissDialog()
                        }
                    },
                    existingLogs = dState.existingLogs
                )
            }
            is StatusDialogState.WorkoutScheduleSettings -> {
                com.ihor.thesystem.feature.status.ui.components.dialogs.WorkoutScheduleSettingsDialog(
                    uiState = settingsUiState,
                    onDismiss = { workoutViewModel.onDismissDialog() },
                    onSelectDay = { workoutViewModel.onSettingsSelectDay(it) },
                    onWorkoutNameChange = { workoutViewModel.onWorkoutNameChange(it) },
                    onSaveWorkoutName = { workoutViewModel.onSaveWorkoutName() },
                    onAddExercise = { workoutViewModel.onAddExerciseToDay(it.toInt()) },
                    onRemoveExercise = { workoutViewModel.onRemoveExerciseFromDay(it) },
                    onDeleteAllExercises = { },
                    onCreateNewExercise = { workoutViewModel.onCreateExercise(it) },
                    onDeleteExercise = { workoutViewModel.onDeleteExercise(it) }
                )
            }
            is StatusDialogState.WorkoutReport -> WorkoutReportDialog(
                report = dState.report,
                onDismiss = { workoutViewModel.onDismissDialog() }
            )
            else -> {}
        }
    }
}

@Composable
private fun HeaderSection(
    data: StatusUiData,
    onAvatarSelected: (android.net.Uri) -> Unit,
    onEditNameTap: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onAvatarSelected(it) } }
    )

    val locale = java.util.Locale("uk")
    val today = java.time.LocalDate.now()
    val dateStr = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() } + ", " + today.dayOfMonth

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00F0FF))
                    )
                    Text(
                        text = stringResource(R.string.text_system_online),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF00F0FF),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
                Text(
                    text = stringResource(R.string.text_greeting, data.playerName),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    ),
                    modifier = Modifier.clickable { onEditNameTap() }
                )
                Text(
                    text = dateStr.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // Avatar with pulsing shadow
            val infiniteTransition = rememberInfiniteTransition(label = "AvatarPulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.4f,
                animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
            )

            Box(contentAlignment = Alignment.Center) {
                // Pulsing Gradient Background
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Black,
                                    Color(0xFF00F0FF).copy(alpha = pulseAlpha),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                val avatarSize = 77.dp // 110 * 0.7

                if (data.avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.2f), CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(avatarSize * 0.6f),
                            tint = Color(0xFF00F0FF).copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // XP Bar Integrated
        XpProgressBarPremium(data)
    }
}


@Composable
private fun XpProgressBarPremium(data: StatusUiData) {
    val progress = if (data.xpMax > 0) data.xpTotal.toFloat() / data.xpMax else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1500, easing = EaseOutExpo))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.text_level_label, data.level),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.text_next_xp, data.xpMax - data.xpTotal),
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.4f))
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00F0FF), Color(0xFFB257FF))
                        )
                    )
            )
        }
    }
}

@Composable
private fun ActiveRecoveryCard(
    onOpenWorkoutSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onOpenWorkoutSettings,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = OnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("💤", fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.text_active_recovery),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFF00F0FF).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = stringResource(R.string.text_rest_day_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun RestDayCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("💤", fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.text_rest_day_title),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = stringResource(R.string.text_rest_day_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun MainWorkoutCardPremium(
    data: StatusUiData,
    onStartWorkout: () -> Unit,
    onOpenWorkoutSettings: () -> Unit
) {
    val mainQuest = data.mainQuest ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF0F0F1A)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(
                BorderStroke(
                    1.dp, 
                    Brush.sweepGradient(listOf(Color(0xFF00F0FF).copy(alpha = 0.2f), Color(0xFFB257FF).copy(alpha = 0.2f), Color(0xFF00F0FF).copy(alpha = 0.2f)))
                ),
                RoundedCornerShape(32.dp)
            )
    ) {
        // Decorative Elements
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-50).dp)
                .background(Color(0xFFB257FF).copy(alpha = 0.15f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = stringResource(R.string.text_main_quest),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mainQuest.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = onOpenWorkoutSettings,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Exercise List
            if (mainQuest.tasks.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mainQuest.tasks.take(5).forEach { task ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00F0FF).copy(alpha = 0.6f))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = task.nameUk ?: task.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        }
                    }
                    if (mainQuest.tasks.size > 5) {
                        Text(
                            text = stringResource(R.string.text_more_exercises, mainQuest.tasks.size - 5),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = mainQuest.subtitle.asString(),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.5f))
                )
                
                Button(
                    onClick = onStartWorkout,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mainQuest.isCompleted) Color(0xFF00F0FF).copy(alpha = 0.1f) else Color(0xFF00F0FF),
                        contentColor = if (mainQuest.isCompleted) Color(0xFF00F0FF) else Color.Black
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (mainQuest.isCompleted) stringResource(R.string.text_completed_check) else stringResource(R.string.text_start),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyQuestsSectionPremium(
    data: StatusUiData,
    onTaskToggled: (TaskUiModel, Int) -> Unit,
    onAddTask: (Int) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    val quests = mutableListOf<QuestUiModel>()
    data.dailyQuest?.let { quests.add(it) }
    quests.addAll(data.promotionQuests)

    // questId для кнопки "Додати завдання":
    // якщо вже є dailyQuest — беремо його id,
    // якщо ще немає — передаємо 0 (ViewModel обробить як "перший квест")
    val addTaskQuestId = data.dailyQuest?.id ?: 0

    val completedCount = quests.flatMap { it.tasks }.count { it.isCompleted }
    val totalCount     = quests.flatMap { it.tasks }.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.text_daily_goals),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )
            // Показуємо лічильник тільки якщо є таски
            if (totalCount > 0) {
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF00F0FF))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Таски (якщо є)
        quests.forEach { quest ->
            quest.tasks.forEach { task ->
                TaskItemPremium(
                    task = task,
                    onToggle = { onTaskToggled(task, quest.id) },
                    onDelete = { onRemoveTask(task.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Кнопка "Додати завдання" — ЗАВЖДИ видима, незалежно від кількості тасків
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .clickable { onAddTask(addTaskQuestId) }, // clickable має бути останнім
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.text_add_new_goal),
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TasksSkeletonPlaceholder() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue  = 0.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "shimmer"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha))
            )
        }
    }
}

@Composable
private fun TaskItemPremium(
    task: TaskUiModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val alpha by animateFloatAsState(targetValue = if (task.isCompleted) 0.5f else 1f)
    val scale by animateFloatAsState(targetValue = if (task.isCompleted) 0.98f else 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (task.isCompleted) Color(0xFF00F0FF) else Color.Transparent)
                .border(2.dp, if (task.isCompleted) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = task.nameUk ?: task.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
        )

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AnimatedPremiumBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Dark Base
        drawRect(Color(0xFF020408))
        
        // Moving Ambient Light 1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00F0FF).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.2f + (size.width * 0.1f * colorShift), size.height * 0.3f),
                radius = 600.dp.toPx()
            )
        )

        // Moving Ambient Light 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB257FF).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.8f - (size.width * 0.1f * colorShift), size.height * 0.7f),
                radius = 700.dp.toPx()
            )
        )
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
            text = stringResource(R.string.text_critical_error_capital),
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
            Text(stringResource(R.string.text_terminate_system), color = Color.Red)
        }
    }
}
