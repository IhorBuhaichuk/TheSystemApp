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
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import kotlin.system.measureTimeMillis

object DatabasePopulator {

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    suspend fun populate(context: Context, db: AppDatabase) {
        val workoutDao = db.workoutDao()

        withContext(Dispatchers.IO) {
            val totalTime = measureTimeMillis {
                if (workoutDao.getAllExercisesSync().isNotEmpty()) {
                    Timber.d("Database already has exercise data. Skipping population.")
                    return@withContext
                }

                Timber.d("Database is empty. Starting population from JSON...")

                val exerciseDtos = try {
                    context.assets.open("exercises_ua.json").use { inputStream ->
                        json.decodeFromStream<List<ExerciseDto>>(inputStream)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to read or parse exercises_ua.json")
                    return@withContext
                }

                db.withTransaction {
                    // Базова конфігурація
                    db.playerDao().insertOrUpdate(PlayerEntity())
                    db.systemConfigDao().insertOrUpdate(SystemConfigEntity())

                    // 3. Мапінг ExerciseDto -> ExerciseEntity та пакетне збереження
                    val entities = exerciseDtos.map { dto ->
                        ExerciseEntity(
                            externalId = dto.id,
                            name = dto.name,
                            category = mapCategory(dto.category),
                            muscleGroups = mapMuscles(dto.primaryMuscles + dto.secondaryMuscles),
                            equipment = dto.equipment,
                            instructions = dto.instructions.joinToString("\n"),
                            gifUrl = dto.gifUrl
                        )
                    }

                    // Insert in chunks to avoid large transaction overhead if needed, 
                    // though 873 is manageable in one go.
                    entities.chunked(200).forEach { chunk ->
                        workoutDao.insertExercises(chunk)
                    }
                    
                    Timber.d("Inserted ${entities.size} exercises")
                }
            }
            Timber.d("Database population completed in ${totalTime}ms")
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
