package com.ihor.thesystem.data.local.room.database

import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*

object DatabasePopulator {

    suspend fun populate(db: AppDatabase) {
        val workoutDao = db.workoutDao()
        if (workoutDao.getAllExercisesSync().isNotEmpty()) return

        // 1. Гравець та конфігурація
        db.playerDao().insertOrUpdate(PlayerEntity())
        db.systemConfigDao().insertOrUpdate(SystemConfigEntity())

        // 2. Вправи
        val exercises = listOf(
            ExerciseEntity(1,  "Сідничний місток"),
            ExerciseEntity(2,  "Мертвий жук"),
            ExerciseEntity(3,  "Scapular Push-ups"),
            ExerciseEntity(4,  "Wall Slides"),
            ExerciseEntity(5,  "Підтягування"),
            ExerciseEntity(6,  "Присідання зі штангою"),
            ExerciseEntity(7,  "Болгарські присідання"),
            ExerciseEntity(8,  "Жим від підлоги"),
            ExerciseEntity(9,  "Жим гантелей сидячи"),
            ExerciseEntity(10, "Face Pulls"),
            ExerciseEntity(11, "Жим гантелей під кутом"),
            ExerciseEntity(12, "Румунська тяга"),
            ExerciseEntity(13, "Тяга в нахилі"),
            ExerciseEntity(14, "Махи гантелями в сторони"),
            ExerciseEntity(15, "Згинання біцепс (EZ)"),
            ExerciseEntity(16, "Французький жим (EZ)"),
            ExerciseEntity(17, "Планка")
        )
        exercises.forEach { workoutDao.insertExercise(it) }

        // 3. Шаблони тренувань (Templates)
        // ТРЕНУВАННЯ А (id = 2)
        workoutDao.insertTemplate(WorkoutTemplateEntity(id = 2, name = "Тренування А"))
        listOf(6, 8, 13, 9, 10, 15, 17).forEachIndexed { index, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(2, exId, index))
        }

        // ТРЕНУВАННЯ Б (id = 3)
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
        db.scheduleDao().insertSchedule(ScheduleEntity(1, 1, null)) // ДЕНЬ 1: ДЕНЬ -> ВІДПОЧИНОК
        db.scheduleDao().insertSchedule(ScheduleEntity(2, 2, 3))    // ДЕНЬ 2: НІЧ -> ТРЕНУВАННЯ Б
        db.scheduleDao().insertSchedule(ScheduleEntity(3, 3, null)) // ДЕНЬ 3: ПІСЛЯ НОЧІ -> ВІДПОЧИНОК
        db.scheduleDao().insertSchedule(ScheduleEntity(4, 4, 2))    // ДЕНЬ 4: ВИХІДНИЙ -> ТРЕНУВАННЯ А
    }
}
