package com.ihor.thesystem.data.local.room.database

import android.content.Context
import androidx.room.withTransaction
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.PlayerEntity
import com.ihor.thesystem.data.local.room.entity.SystemConfigEntity
import com.ihor.thesystem.data.remote.dto.ExerciseDto
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MuscleGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import kotlin.system.measureTimeMillis

object DatabasePopulator {

    private val json = Json { ignoreUnknownKeys = true }

    class DatabasePopulationException(
        message: String,
        cause: Throwable? = null
    ) : IllegalStateException(message, cause)

    @Serializable
    private data class ExerciseTranslationDto(
        val id: String,
        val name_uk: String
    )

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    suspend fun populate(
        context: Context,
        db: AppDatabase,
        ioDispatcher: CoroutineDispatcher
    ) {
        val workoutDao = db.workoutDao()

        withContext(ioDispatcher) {
            val totalTime = measureTimeMillis {
                val existingExerciseCount = db.withTransaction {
                    ensureRequiredSingletonRows(db)
                    workoutDao.getExerciseCount()
                }

                if (existingExerciseCount > 0) {
                    Timber.d("Database already has $existingExerciseCount exercises. Skipping exercise seed.")
                    return@measureTimeMillis
                }

                Timber.d("Database has no exercises. Starting exercise seed.")

                val exerciseDtos = readRequiredExerciseSeed(context)
                val translations = readOptionalTranslations(context)

                db.withTransaction {
                    ensureRequiredSingletonRows(db)

                    if (workoutDao.getExerciseCount() > 0) {
                        Timber.d("Exercises were seeded by another population pass. Skipping duplicate insert.")
                        return@withTransaction
                    }

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

                    Timber.d("Inserted ${entities.size} exercises.")
                }
            }

            Timber.d("Database population check/completion took ${totalTime}ms.")
        }
    }

    private suspend fun ensureRequiredSingletonRows(db: AppDatabase) {
        if (db.playerDao().getPlayerSync() == null) {
            db.playerDao().insertOrUpdate(PlayerEntity())
            Timber.d("Initial player created.")
        }

        if (db.systemConfigDao().getConfigSync() == null) {
            db.systemConfigDao().insertOrUpdate(SystemConfigEntity())
            Timber.d("Initial system config created.")
        }
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    private fun readRequiredExerciseSeed(context: Context): List<ExerciseDto> {
        val exercises = try {
            context.assets.open("exercises_ua.json").use { inputStream ->
                json.decodeFromStream<List<ExerciseDto>>(inputStream)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw DatabasePopulationException(
                "Failed to read or parse required seed asset exercises_ua.json.",
                e
            )
        }

        if (exercises.isEmpty()) {
            throw DatabasePopulationException("Required seed asset exercises_ua.json is empty.")
        }

        return exercises
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    private fun readOptionalTranslations(context: Context): Map<String, String> {
        return try {
            context.assets.open("exercises_uk.json").use { inputStream ->
                json.decodeFromStream<List<ExerciseTranslationDto>>(inputStream)
                    .associate { it.id to it.name_uk }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to read or parse optional seed asset exercises_uk.json.")
            emptyMap()
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
