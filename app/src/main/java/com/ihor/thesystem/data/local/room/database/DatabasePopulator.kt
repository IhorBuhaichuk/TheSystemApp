package com.ihor.thesystem.data.local.room.database

import android.content.Context
import androidx.room.withTransaction
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.remote.dto.ExerciseDto
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MuscleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import kotlin.system.measureTimeMillis

object DatabasePopulator {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class ExerciseTranslationDto(
        val id: String,
        val name_uk: String
    )

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    suspend fun populate(context: Context, db: AppDatabase) {
        val workoutDao = db.workoutDao()

        withContext(Dispatchers.IO) {
            val totalTime = measureTimeMillis {
                val exerciseDtos = try {
                    context.assets.open("exercises_ua.json").use { inputStream ->
                        json.decodeFromStream<List<ExerciseDto>>(inputStream)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to read or parse exercises_ua.json")
                    return@withContext
                }

                val translations = try {
                    context.assets.open("exercises_uk.json").use { inputStream ->
                        json.decodeFromStream<List<ExerciseTranslationDto>>(inputStream)
                            .associate { it.id to it.name_uk }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to read or parse exercises_uk.json")
                    emptyMap()
                }

                db.withTransaction {
                    // 1. Атомарна перевірка: якщо вправи вже є, нічого не робимо
                    if (workoutDao.getAllExercisesSync().isNotEmpty()) {
                        Timber.d("Database already has exercise data. Skipping population.")
                        return@withTransaction
                    }

                    Timber.d("Database is empty. Starting population...")

                    // 2. Ініціалізуємо гравця ТІЛЬКИ якщо його ще немає (id=1)
                    if (db.playerDao().getPlayerSync() == null) {
                        db.playerDao().insertOrUpdate(PlayerEntity())
                        Timber.d("Initial player created")
                    }

                    // 3. Ініціалізуємо конфігурацію ТІЛЬКИ якщо її немає (id=1)
                    if (db.systemConfigDao().getConfigSync() == null) {
                        db.systemConfigDao().insertOrUpdate(SystemConfigEntity())
                        Timber.d("Initial system config created")
                    }

                    // 4. Мапінг ExerciseDto -> ExerciseEntity та пакетне збереження
                    val entities = exerciseDtos.map { dto ->
                        ExerciseEntity(
                            externalId = dto.id,
                            name = dto.name,
                            nameUk = translations[dto.id],
                            category = mapCategory(dto.category),
                            muscleGroups = mapMuscles(dto.primaryMuscles + dto.secondaryMuscles),
                            equipment = dto.equipment,
                            level = dto.level,
                            mechanic = dto.mechanic,
                            force = dto.force,
                            instructions = dto.instructions.joinToString("\n"),
                            gifUrl = dto.gifUrl
                        )
                    }

                    entities.chunked(200).forEach { chunk ->
                        workoutDao.insertExercises(chunk)
                    }
                    
                    Timber.d("Inserted ${entities.size} exercises")
                }
            }
            Timber.d("Database population check/completion took ${totalTime}ms")
        }
    }

    private fun mapCategory(cat: String?): ExerciseCategory = when (cat?.lowercase()) {
        "strength", "powerlifting", "olympic weightlifting", "strongman" -> ExerciseCategory.STRENGTH
        "stretching" -> ExerciseCategory.FLEXIBILITY
        "plyometrics", "cardio" -> ExerciseCategory.ENDURANCE
        else -> ExerciseCategory.UNKNOWN
    }

    private fun mapMuscles(muscles: List<String>): List<MuscleGroup> {
        return muscles.mapNotNull { muscle ->
            when (muscle.lowercase()) {
                "chest" -> MuscleGroup.CHEST
                "lats", "middle back", "lower back", "traps" -> MuscleGroup.BACK
                "shoulders" -> MuscleGroup.SHOULDERS
                "quadriceps" -> MuscleGroup.QUADS
                "hamstrings", "glutes" -> MuscleGroup.HAMSTRINGS_GLUTES
                "biceps", "triceps", "forearms" -> MuscleGroup.ARMS
                "abdominals" -> MuscleGroup.ABS
                "calves", "abductors", "adductors" -> MuscleGroup.LEGS
                "neck", "hip flexors" -> MuscleGroup.CORE
                else -> null
            }
        }.distinct()
    }
}
