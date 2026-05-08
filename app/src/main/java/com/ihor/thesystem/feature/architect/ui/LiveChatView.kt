package com.ihor.thesystem.feature.architect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.AccentAi
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.asString
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole

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
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            if (history.isEmpty()) {
                item {
                    EmptyChatMessage()
                }
            } else {
                items(history, key = { it.id }) { message ->
                    LiveChatBubble(message = message)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Запит до тренера",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary.copy(alpha = 0.45f),
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentPrimary,
                    focusedContainerColor = Color.White.copy(alpha = 0.025f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(sessionId, messageText.trim())
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (messageText.isNotBlank()) AccentPrimary.copy(alpha = 0.11f) else Color.White.copy(alpha = 0.035f)
                    )
                    .border(
                        1.dp,
                        if (messageText.isNotBlank()) AccentPrimary.copy(alpha = 0.32f) else BorderSubtle,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (messageText.isNotBlank()) AccentPrimary else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val accent = if (message.role == ChatRole.AI) AccentAi else AccentPrimary
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = when (message.role) {
                    ChatRole.USER -> "Ти"
                    ChatRole.AI -> "Тренер"
                    ChatRole.SYSTEM -> "Система"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isUser) TextMuted else accent,
                    fontWeight = FontWeight.Bold
                )
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clip(shape)
                    .background(if (isUser) Color.White.copy(alpha = 0.045f) else accent.copy(alpha = 0.07f))
                    .border(1.dp, if (isUser) BorderSubtle else accent.copy(alpha = 0.2f), shape)
                    .padding(13.dp)
            ) {
                Text(
                    text = message.text.asString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyChatMessage() {
    Text(
        text = "Чат порожній",
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.026f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(13.dp))
            .padding(14.dp),
        style = MaterialTheme.typography.bodySmall.copy(
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    )
}
