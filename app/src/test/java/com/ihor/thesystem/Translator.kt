package com.ihor.thesystem

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Test
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class Translator {

    private val jsonFormat = Json { prettyPrint = true }

    @Test
    fun translate() {
        val inputFilePath = "C:\\Users\\gesha\\AndroidStudioProjects\\TheSystem-master\\app\\src\\main\\assets\\exercises.json"
        val outputFilePath = "C:\\Users\\gesha\\AndroidStudioProjects\\TheSystem-master\\app\\src\\main\\assets\\exercises_ua.json"
        val apiKey = "AIzaSyCr0u0gPOs_x6VUmmg204GiiIDPrfiY8i0" // ВСТАВ КЛЮЧ ТІЛЬКИ СЮДИ

        val file = File(inputFilePath)
        if (!file.exists()) {
            println("Файл не знайдено: $inputFilePath")
            return
        }

        val originalText = file.readText()
        val jsonArray = Json.parseToJsonElement(originalText).jsonArray
        val resultList = mutableListOf<JsonElement>()
        val batchSize = 40

        val chunks = jsonArray.chunked(batchSize)
        println("Знайдено ${jsonArray.size} вправ. Розбито на ${chunks.size} батчів.")

        val systemPrompt = """
            Ти професійний тренер. Твоє завдання: перекласти українською значення ключів "name" та масиву "instructions" у наданому JSON.
            Інші ключі (id, level, mechanic, equipment, category тощо) ЗАЛИШАЙ БЕЗ ЗМІН англійською.
            Структура об'єктів має залишитися ідентичною.
            Поверни ВИКЛЮЧНО валідний JSON-масив без додаткового тексту.
            
            Термінологія:
            - Flyes = Розведення
            - Row = Тяга (наприклад, Barbell Row = Тяга штанги в нахилі)
            - Deadlift = Станова тяга
            - Crunch = Скручування
            - Band = Еспандер (або гумова стрічка)
            - Calf / Calves = Литкові м'язи
            - Clean = Взяття на груди
            - Shrug = Шраги
            - Dumbbell = Гантель
            - Barbell = Штанга
            - Bodyweight = З власною вагою
            - Cable = У кросовері / На нижньому/верхньому блоці
            - Curl = Згинання рук
            - Extension = Розгинання
            - Bear Crawl = Ведмежа хода
        """.trimIndent()

        chunks.forEachIndexed { index, chunk ->
            println("Обробка батчу ${index + 1} з ${chunks.size}...")

            val payload = buildJsonObject {
                put("system_instruction", buildJsonObject {
                    put("parts", buildJsonObject { put("text", systemPrompt) })
                })
                put("contents", buildJsonArray {
                    add(buildJsonObject {
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", Json.encodeToString(JsonArray(chunk)))
                            })
                        })
                    })
                })
                put("generationConfig", buildJsonObject {
                    put("response_mime_type", "application/json")
                })
            }

            val responseText = sendGeminiRequest(payload.toString(), apiKey)

            if (responseText.isNotEmpty()) {
                try {
                    val cleanJson = if (responseText.contains("```json")) {
                        responseText.substringAfter("```json").substringBeforeLast("```").trim()
                    } else {
                        responseText.trim()
                    }
                    val translatedArray = Json.parseToJsonElement(cleanJson).jsonArray
                    resultList.addAll(translatedArray)
                } catch (e: Exception) {
                    println("Помилка парсингу батчу ${index + 1}: ${e.message}")
                }
            }

            Thread.sleep(5000)
        }

        val finalJson = jsonFormat.encodeToString(JsonArray(resultList))
        File(outputFilePath).writeText(finalJson)
        println("Готово. Збережено у $outputFilePath")
    }

    private fun sendGeminiRequest(payload: String, apiKey: String): String {
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(payload) }

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = Json.parseToJsonElement(response).jsonObject
                val candidates = jsonResponse["candidates"]?.jsonArray
                return candidates?.get(0)?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""
            } else {
                println("Помилка API: ${connection.responseCode} - ${connection.errorStream.bufferedReader().use { it.readText() }}")
                return ""
            }
        } finally {
            connection.disconnect()
        }
    }
}