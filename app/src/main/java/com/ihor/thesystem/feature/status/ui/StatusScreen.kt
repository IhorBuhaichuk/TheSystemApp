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
import androidx.compose.ui.draw.blur
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
import com.ihor.thesystem.feature.status.viewmodel.*
import kotlin.system.exitProcess

@Composable
fun StatusScreen(
    navController: NavHostController,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val activeDayWorkout by viewModel.activeWorkoutState.collectAsState()

    var levelUpEvent by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
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
                        onAvatarSelected = { viewModel.updateAvatarUri(it) },
                        onEditNameTap = { viewModel.onEditNameTap() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (data.mainQuest != null) {
                        MainWorkoutCardPremium(
                            data = data,
                            onStartWorkout = { viewModel.onOpenMainWorkout() }
                        )
                    } else {
                        RestDayCard()
                    }

                    DailyQuestsSectionPremium(
                        data = data,
                        onTaskToggled = { task, qId -> viewModel.onTaskToggled(task, qId) },
                        onAddTask = { qId -> viewModel.onAddTaskTap(qId) },
                        onRemoveTask = { id -> viewModel.onRemoveTask(id) }
                    )

                    Spacer(modifier = Modifier.height(110.dp))
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
        
        when (val dState = dialogState) {
            is StatusDialogState.EditName -> {
                val currentData = (uiState as? UiState.Content)?.data
                if (currentData != null) {
                    com.ihor.thesystem.feature.statistics.ui.components.dialogs.EditNameDialog(
                        currentName = currentData.playerName,
                        onConfirm = { viewModel.onNameConfirmed(it) },
                        onDismiss = { viewModel.onDismissDialog() }
                    )
                }
            }
            is StatusDialogState.AddTask -> AddTaskDialog(
                onConfirm = { viewModel.onAddTaskConfirmed(dState.questId, it) },
                onDismiss = { viewModel.onDismissDialog() }
            )
            is StatusDialogState.MainQuestWorkout -> MainQuestWorkoutDialog(
                data = activeDayWorkout,
                onSetWeightChanged = { exId, setId, w -> viewModel.onSetWeightChanged(exId, setId, w) },
                onSetRepsChanged = { exId, setId, r -> viewModel.onSetRepsChanged(exId, setId, r) },
                onSetCompleted = { exId, setId -> viewModel.onSetCompleted(exId, setId) },
                onOpenSetup = { viewModel.onOpenSetup(it, fromWorkout = true) },
                onDismiss = { viewModel.onDismissDialog() }
            )
            is StatusDialogState.SetupMatrix -> {
                com.ihor.thesystem.feature.statistics.ui.dialogs.SetupMatrixDialog(
                    exerciseName = dState.entry.exerciseName,
                    initialStart = dState.startWeight,
                    initialTarget = dState.targetWeight,
                    onConfirm = { start, target ->
                        viewModel.onConfirmSetup(dState.entry.exerciseId, start, target)
                    },
                    onDismiss = { 
                        if (dState.showWorkoutAfter) {
                            viewModel.onOpenMainWorkout()
                        } else {
                            viewModel.onDismissDialog()
                        }
                    }
                )
            }
            is StatusDialogState.LogWorkoutSets -> {
                com.ihor.thesystem.feature.statistics.ui.dialogs.LogWorkoutSetsDialog(
                    exerciseName = dState.entry.exerciseName,
                    sets = dState.sets,
                    onUpdate = { id, w, r -> viewModel.updateSetInput(id, w, r) },
                    onAdd = { viewModel.addSet() },
                    onRemove = { viewModel.removeSet() },
                    onSave = { feedback ->
                        viewModel.onLogSetsConfirmed(dState.entry.exerciseId, dState.sets, feedback)
                    },
                    onDismiss = { 
                        if (dState.showWorkoutAfter) {
                            viewModel.onOpenMainWorkout()
                        } else {
                            viewModel.onDismissDialog()
                        }
                    },
                    existingLog = dState.existingLog
                )
            }
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
                        text = "SYSTEM ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF00F0FF),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
                Text(
                    text = "Вітаю, ${data.playerName}",
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
                    text = "LEVEL ${data.level}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEXT: ${data.xpMax - data.xpTotal} XP",
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
                text = "СЬОГОДНІ ВІДПОЧИНОК",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = "Час для відновлення сил",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun MainWorkoutCardPremium(
    data: StatusUiData,
    onStartWorkout: () -> Unit
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
                .blur(60.dp)
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
                        text = "ОСНОВНИЙ КВЕСТ",
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
                    Text(
                        text = "🔥",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 20.sp
                    )
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
                                text = task.name,
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
                            text = "+ ще ${mainQuest.tasks.size - 5} вправ",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(start = 18.dp)
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
                    text = mainQuest.subtitle,
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
                        text = if (mainQuest.isCompleted) "ЗАВЕРШЕНО ✓" else "РОЗПОЧАТИ",
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ЩОДЕННІ ЦІЛІ",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )
            Text(
                text = "${quests.flatMap { it.tasks }.count { it.isCompleted }}/${quests.flatMap { it.tasks }.size}",
                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF00F0FF))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        quests.forEach { quest ->
            quest.tasks.forEachIndexed { index, task ->
                TaskItemPremium(
                    task = task,
                    onToggle = { onTaskToggled(task, quest.id) },
                    onDelete = { onRemoveTask(task.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Add Task Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAddTask(quest.id) }
                    .drawBehind {
                        // Manual dashed border drawing if needed
                    }
                    .background(Color.White.copy(alpha = 0.02f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ДОДАТИ НОВУ ЦІЛЬ", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
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
            text = task.name,
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
