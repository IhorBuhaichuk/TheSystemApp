package com.ihor.thesystem.feature.architect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.data.local.room.entity.ChatMessageEntity
import kotlinx.coroutines.launch

/**
 * Екран Живого Чату з ШІ-тренером.
 * Оптимізовано для UX: підтримка клавіатури, автоматичний скрол та збереження стану.
 */
@Composable
fun LiveChatView(
    history: List<ChatMessageEntity>,
    sessionId: Long,
    onSendMessage: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Завдання 1: Збереження стану тексту при рекомпозиції чи повороті екрану
    var messageText by rememberSaveable { mutableStateOf("") }
    
    // Завдання 2: Стан списку для керування скролом
    val listState = rememberLazyListState()

    // Завдання 2: Автоматичний плавний скрол до останнього повідомлення при зміні історії
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    // Завдання 1: imePadding() гарантує, що поле вводу підніметься над клавіатурою
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() 
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(history, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }

        // Рядок вводу повідомлення
        Surface(
            color = BackgroundDeep,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text("Запитайте тренера...", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = PanelBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFamily, fontSize = 15.sp)
                )
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(sessionId, messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (messageText.isNotBlank()) NeonCyan.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send, 
                        contentDescription = "Send", 
                        tint = if (messageText.isNotBlank()) NeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessageEntity) {
    val isUser = msg.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    
    // Кольорова схема у стилі Sci-Fi
    val bgColor = if (isUser) NeonCyan.copy(alpha = 0.15f) else PanelSurface
    val borderColor = if (isUser) NeonCyan.copy(alpha = 0.4f) else PanelBorder
    
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(), 
        contentAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = msg.message,
                color = TextPrimary,
                modifier = Modifier.padding(12.dp),
                fontFamily = RajdhaniFamily,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}
