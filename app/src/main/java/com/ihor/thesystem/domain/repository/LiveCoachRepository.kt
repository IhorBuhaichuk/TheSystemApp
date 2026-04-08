package com.ihor.thesystem.domain.repository

import com.google.ai.client.generativeai.type.Content

interface LiveCoachRepository {
    /**
     * Відправляє нове повідомлення в контексті історії чату.
     */
    suspend fun sendMessage(history: List<Content>, newMessage: String): String
}
