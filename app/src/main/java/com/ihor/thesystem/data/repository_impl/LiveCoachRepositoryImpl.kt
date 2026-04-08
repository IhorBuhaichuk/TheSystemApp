package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import javax.inject.Inject
import javax.inject.Named

class LiveCoachRepositoryImpl @Inject constructor(
    @Named("LiveCoachModel") private val generativeModel: GenerativeModel
) : LiveCoachRepository {

    override suspend fun sendMessage(history: List<Content>, newMessage: String): String {
        val chat = generativeModel.startChat(history)
        val response = chat.sendMessage(newMessage)
        return response.text ?: "Помилка зв'язку з тренером."
    }
}
