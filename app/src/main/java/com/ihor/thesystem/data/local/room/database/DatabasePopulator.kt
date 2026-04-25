package com.ihor.thesystem.data.local.room.database

import androidx.room.withTransaction
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.domain.model.MuscleGroup
import timber.log.Timber

object DatabasePopulator {

    suspend fun populate(db: AppDatabase) {
        val workoutDao = db.workoutDao()
        if (workoutDao.getAllExercisesSync().isNotEmpty()) return

        Timber.d("Starting database population...")

        db.withTransaction {
            // 1. Гравець та конфігурація
            db.playerDao().insertOrUpdate(PlayerEntity())
            db.systemConfigDao().insertOrUpdate(SystemConfigEntity())
            Timber.d("Player and Config inserted")

            // 2. Вправи з групами м'язів
            val exercises = listOf(
                ExerciseEntity(1,  "Сідничний місток", muscleGroups = listOf(MuscleGroup.HAMSTRINGS_GLUTES)),
                ExerciseEntity(2,  "Мертвий жук", muscleGroups = emptyList()),
                ExerciseEntity(3,  "Scapular Push-ups", muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)),
                ExerciseEntity(4,  "Wall Slides", muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)),
                ExerciseEntity(5,  "Підтягування", muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.ARMS)),
                ExerciseEntity(6,  "Присідання зі штангою", muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS_GLUTES)),
                ExerciseEntity(7,  "Болгарські присідання", muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS_GLUTES)),
                ExerciseEntity(8,  "Жим від підлоги", muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS)),
                ExerciseEntity(9,  "Жим гантелей сидячи", muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.ARMS)),
                ExerciseEntity(10, "Face Pulls", muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)),
                ExerciseEntity(11, "Жим гантелей під кутом", muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)),
                ExerciseEntity(12, "Румунська тяга", muscleGroups = listOf(MuscleGroup.HAMSTRINGS_GLUTES, MuscleGroup.BACK)),
                ExerciseEntity(13, "Тяга в нахилі", muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.ARMS)),
                ExerciseEntity(14, "Махи гантелями в сторони", muscleGroups = listOf(MuscleGroup.SHOULDERS)),
                ExerciseEntity(15, "Згинання біцепс (EZ)", muscleGroups = listOf(MuscleGroup.ARMS)),
                ExerciseEntity(16, "Французький жим (EZ)", muscleGroups = listOf(MuscleGroup.ARMS)),
                ExerciseEntity(17, "Планка", muscleGroups = emptyList())
            )
            exercises.forEach { workoutDao.insertExercise(it) }

            // 3. Шаблони тренувань (Templates)
            workoutDao.insertTemplate(WorkoutTemplateEntity(id = 2, name = "Тренування А"))
            listOf(6, 8, 13, 9, 10, 15, 17).forEachIndexed { index, exId ->
                workoutDao.insertCrossRef(WorkoutExerciseCrossRef(2, exId, index))
            }

            workoutDao.insertTemplate(WorkoutTemplateEntity(id = 3, name = "Тренування Б"))
            listOf(12, 5, 11, 7, 14, 16, 17).forEachIndexed { index, exId ->
                workoutDao.insertCrossRef(WorkoutExerciseCrossRef(3, exId, index))
            }

            // 4. Матриця прогресії
            val matrixData = listOf(
                ProgressionMatrixEntity(8,  60f, 85f,  69f),
                ProgressionMatrixEntity(6,  55f, 120f, 62.5f),
                ProgressionMatrixEntity(12, 60f, 135f, 80f),
                ProgressionMatrixEntity(9,  32.5f, 55f, 35f),
                ProgressionMatrixEntity(13, 45f, 85f, 54f),
                ProgressionMatrixEntity(5,  0f, 10f, 0f),
                ProgressionMatrixEntity(10, 8.5f, 20f, 11.5f),
                ProgressionMatrixEntity(7,  0f, 20f, 7f),
                ProgressionMatrixEntity(11, 16f, 32.5f, 22f),
                ProgressionMatrixEntity(15, 32f, 42.5f, 33f),
                ProgressionMatrixEntity(16, 22f, 40f, 33f),
                ProgressionMatrixEntity(14, 6f, 15f, 6f)
            )
            matrixData.forEach { db.progressionMatrixDao().insert(it) }

            // 5. Розклад
            db.scheduleDao().insertSchedule(ScheduleEntity(1, 1, null))
            db.scheduleDao().insertSchedule(ScheduleEntity(2, 2, 3))
            db.scheduleDao().insertSchedule(ScheduleEntity(3, 3, null))
            db.scheduleDao().insertSchedule(ScheduleEntity(4, 4, 2))
        }

        Timber.d("Database population completed successfully.")
    }
}
