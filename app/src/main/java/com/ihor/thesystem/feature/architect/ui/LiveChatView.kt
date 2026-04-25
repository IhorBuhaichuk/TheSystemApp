package com.ihor.thesystem.feature.architect.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import kotlinx.coroutines.launch

/**
 * Екран Живого Чату з ШІ-тренером.
 * Оптимізовано для UX: підтримка клавіатури, автоматичний скрол та збереження стану.
 */
@Composable
fun LiveChatView(
    history: List<ChatMessage>,
    sessionId: Long,
    onSendMessage: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() 
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(history, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }

        // Рядок вводу повідомлення
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text("ВВЕДІТЬ ЗАПИТ...", color = Color.White.copy(alpha = 0.2f), fontSize = 13.sp, letterSpacing = 1.sp) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonCyan,
                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    shape = RoundedCornerShape(20.dp),
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
                        .size(52.dp)
                        .background(
                            color = if (messageText.isNotBlank()) NeonCyan.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                            shape = CircleShape
                        )
                        .border(1.dp, if (messageText.isNotBlank()) NeonCyan.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send, 
                        contentDescription = "Send", 
                        tint = if (messageText.isNotBlank()) NeonCyan else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == ChatRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    
    val bgColor = if (isUser) Color.White.copy(alpha = 0.05f) else StatusError.copy(alpha = 0.03f)
    val borderColor = if (isUser) Color.White.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.2f)
    
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(), 
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Text(
                text = if (isUser) "ВИ" else "ТРЕНЕР",
                fontFamily = TekoFamily,
                fontSize = 10.sp,
                color = if (isUser) OnSurfaceVariant else StatusError,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp, end = 8.dp)
            )
            Surface(
                color = bgColor,
                shape = shape,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                ChatMessageItem(text = msg.text.asString())
            }
        }
    }
}

@Composable
private fun ChatMessageItem(text: String) {
    val paragraphs = remember(text) { text.split("\n\n") }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        paragraphs.forEach { paragraph ->
            val isHeader = paragraph.trimEnd().endsWith(":")
            Text(
                text = paragraph,
                color = Color.White,
                fontFamily = RajdhaniFamily,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                modifier = if (isHeader) Modifier.padding(bottom = 6.dp) else Modifier
            )
        }
    }
}
