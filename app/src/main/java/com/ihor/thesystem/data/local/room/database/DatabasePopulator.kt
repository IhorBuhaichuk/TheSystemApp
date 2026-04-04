package com.ihor.thesystem.data.local.room.database

import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*

object DatabasePopulator {

    suspend fun populate(db: AppDatabase) {
        val workoutDao = db.workoutDao()
        if (workoutDao.getAllExercisesSync().isNotEmpty()) return

        // Виконуємо вставки окремими кроками без однієї гігантської транзакції,
        // щоб не блокувати базу надовго при старті.
        
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

        // 3. Матриця прогресії (12 основних вправ)
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

        // 4. Розклад та дебафи
        db.debuffConfigDao().insert(DebuffConfigEntity(1, "СЛАБКІСТЬ", "Дебаф: зниження продуктивності", 0))
        db.debuffConfigDao().insert(DebuffConfigEntity(2, "ЦНС", "Дебаф: перевтома системи", 0))
        
        db.scheduleDao().insertSchedule(ScheduleEntity(1, 1, null))
        db.scheduleDao().insertSchedule(ScheduleEntity(2, 2, 2))
        db.scheduleDao().insertSchedule(ScheduleEntity(3, 3, null))
        db.scheduleDao().insertSchedule(ScheduleEntity(4, 4, 3))
    }
}
