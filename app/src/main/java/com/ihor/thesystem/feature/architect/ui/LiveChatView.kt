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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.data.local.room.entity.ChatMessageEntity
import kotlinx.coroutines.launch

@Composable
fun LiveChatView(
    history: List<ChatMessageEntity>,
    sessionId: Long,
    onSendMessage: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(history.size - 1)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(history) { msg ->
                ChatBubble(msg)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Запитайте тренера...", color = Color.Gray, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(sessionId, messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send, 
                    contentDescription = "Send", 
                    tint = if (messageText.isNotBlank()) NeonCyan else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessageEntity) {
    val isUser = msg.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isUser) Color(0xFF004D40) else Color(0xFF212121)
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = shape,
            modifier = Modifier.padding(horizontal = 4.dp).widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.message,
                color = Color.White,
                modifier = Modifier.padding(12.dp),
                fontFamily = RajdhaniFamily,
                fontSize = 14.sp
            )
        }
    }
}
