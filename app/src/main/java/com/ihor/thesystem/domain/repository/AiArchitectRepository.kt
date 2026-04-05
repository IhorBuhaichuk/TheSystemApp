package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.ChatMessage

interface AiArchitectRepository {
    /**
     * Відправляє текстовий запит до Gemini і повертає відповідь у вигляді повідомлення чату.
     */
    suspend fun getChatResponse(prompt: String): ChatMessage
}
