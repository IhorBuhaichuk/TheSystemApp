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
import kotlinx.serialization.json.Json
import timber.log.Timber

object DatabasePopulator {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun populate(context: Context, db: AppDatabase) {
        val workoutDao = db.workoutDao()
        
        // 1. Перевірка через DAO, чи є записи
        if (workoutDao.getAllExercisesSync().isNotEmpty()) return

        Timber.d("Database is empty. Starting population from JSON...")

        withContext(Dispatchers.IO) {
            // 2. Зчитування та парсинг JSON з assets
            val exercisesJson = try {
                context.assets.open("exercises_ua.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Timber.e(e, "Failed to read exercises_ua.json")
                return@withContext
            }

            val exerciseDtos = try {
                json.decodeFromString<List<ExerciseDto>>(exercisesJson)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse exercises JSON")
                return@withContext
            }

            db.withTransaction {
                // Базова конфігурація
                db.playerDao().insertOrUpdate(PlayerEntity())
                db.systemConfigDao().insertOrUpdate(SystemConfigEntity())

                // 3. Мапінг ExerciseDto -> ExerciseEntity та збереження
                exerciseDtos.forEach { dto ->
                    val entity = ExerciseEntity(
                        externalId = dto.id,
                        name = dto.name,
                        category = mapCategory(dto.category),
                        muscleGroups = mapMuscles(dto.primaryMuscles + dto.secondaryMuscles),
                        equipment = dto.equipment,
                        instructions = dto.instructions.joinToString("\n"),
                        gifUrl = dto.gifUrl
                    )
                    workoutDao.insertExercise(entity)
                }
                Timber.d("Inserted ${exerciseDtos.size} exercises")
            }
        }
    }

    private fun mapCategory(cat: String?): ExerciseCategory = when (cat?.lowercase()) {
        "strength" -> ExerciseCategory.STRENGTH
        "stretching" -> ExerciseCategory.FLEXIBILITY
        "plyometrics" -> ExerciseCategory.ENDURANCE
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
                else -> null
            }
        }.distinct()
    }
}
