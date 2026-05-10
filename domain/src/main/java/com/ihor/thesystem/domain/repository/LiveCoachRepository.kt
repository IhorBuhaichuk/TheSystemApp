package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.AiConversationMessage

interface LiveCoachRepository {
    /**
     * Відправляє нове повідомлення в контексті історії чату.
     */
    suspend fun sendMessage(history: List<AiConversationMessage>, newMessage: String): String
}
