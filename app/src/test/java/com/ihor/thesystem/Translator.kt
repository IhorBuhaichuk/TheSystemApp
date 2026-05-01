package com.ihor.thesystem

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

class Translator {

    private val jsonFormat = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Ignore("Manual localization utility. Requires GEMINI_API_KEY and rewrites asset files.")
    @Test
    fun generateLocalization() {
        val inputFile = projectFile("app/src/main/assets/exercises_ua.json", "src/main/assets/exercises_ua.json")
        val outputFile = projectFile("app/src/main/assets/exercises_uk.json", "src/main/assets/exercises_uk.json")
        val apiKey = readApiKey()

        val originalText = inputFile.readText()
        val jsonArray = Json.parseToJsonElement(originalText).jsonArray

        val simplifiedList = jsonArray.map { element ->
            val obj = element.jsonObject
            buildJsonObject {
                put("id", obj["id"] ?: JsonNull)
                put("name", obj["name"] ?: JsonNull)
            }
        }

        val resultList = mutableListOf<JsonElement>()
        val batchSize = 60

        val chunks = simplifiedList.chunked(batchSize)
        println("Знайдено ${simplifiedList.size} назв. Розбито на ${chunks.size} батчів.")

        val systemPrompt = """
            Ти професійний перекладач спортивної термінології. 
            Твоє завдання: отримати JSON-масив з об'єктами (ключі "id" та "name").
            Поверни новий JSON-масив, де "id" залишається без змін, а замість ключа "name" використовується ключ "name_uk" з перекладеною українською назвою.
            
            Термінологія:
            - Flyes = Розведення
            - Row = Тяга
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
            - Pulldown = Тяга зверху
            - Pushdown = Розгинання на блоці
            
            Приклад вихідного об'єкта: {"id": "Barbell_Squat", "name_uk": "Присідання зі штангою"}
            
            Поверни ВИКЛЮЧНО валідний JSON-масив без додаткового тексту.
        """.trimIndent()

        chunks.forEachIndexed { index, chunk ->
            println("\nОбробка батчу ${index + 1} з ${chunks.size}...")

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

            var success = false
            var attempts = 0
            val maxAttempts = 5

            while (!success && attempts < maxAttempts) {
                try {
                    attempts++
                    val responseText = sendGeminiRequest(payload.toString(), apiKey)

                    val cleanJson = if (responseText.contains("```json")) {
                        responseText.substringAfter("```json").substringBeforeLast("```").trim()
                    } else {
                        responseText.trim()
                    }

                    val translatedArray = Json.parseToJsonElement(cleanJson).jsonArray
                    resultList.addAll(translatedArray)
                    success = true

                    println("Батч ${index + 1} успішно оброблено.")
                    Thread.sleep(4500) // Пауза після успішного запиту

                } catch (e: Exception) {
                    println("Помилка (Спроба $attempts з $maxAttempts): ${e.message}")
                    if (attempts < maxAttempts) {
                        val waitTime = 10000L * attempts // Затримка: 10с, 20с, 30с...
                        println("Очікування ${waitTime / 1000} секунд перед повторною спробою...")
                        Thread.sleep(waitTime)
                    } else {
                        println("КРИТИЧНА ПОМИЛКА: Не вдалося обробити батч ${index + 1} після $maxAttempts спроб. Скрипт зупинено.")
                        return // Зупиняємо виконання, щоб не перезаписати файл неповними даними
                    }
                }
            }
        }

        val finalJson = jsonFormat.encodeToString(JsonArray(resultList))
        outputFile.writeText(finalJson)
        println("\nГотово. Збережено у ${outputFile.path}. Всього перекладено об'єктів: ${resultList.size}")
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
                val errorMsg = connection.errorStream.bufferedReader().use { it.readText() }
                throw RuntimeException("HTTP ${connection.responseCode} - $errorMsg")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun projectFile(vararg candidates: String): File {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        val roots = sequenceOf(
            File(userDir),
            File(userDir).parentFile
        ).filterNotNull()

        return roots
            .flatMap { root -> candidates.asSequence().map { root.resolve(it) } }
            .firstOrNull { it.exists() }
            ?: error("Не знайдено файл. Перевірені шляхи: ${candidates.joinToString()}")
    }

    private fun readApiKey(): String {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        System.getenv("GEMINI_API_KEY")
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val localProperties = sequenceOf(
            File(userDir).resolve("local.properties"),
            File(userDir).resolve("../local.properties")
        ).firstOrNull { it.exists() }

        val keyFromLocalProperties = localProperties?.inputStream()?.use { stream ->
            Properties().apply { load(stream) }.getProperty("GEMINI_API_KEY")
        }

        return keyFromLocalProperties
            ?.takeIf { it.isNotBlank() }
            ?: error("GEMINI_API_KEY is required for this manual utility.")
    }
}
