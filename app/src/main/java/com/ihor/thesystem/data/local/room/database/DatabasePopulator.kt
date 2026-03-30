package com.ihor.thesystem.data.local.room.database

import androidx.room.withTransaction
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*

object DatabasePopulator {

    suspend fun populate(db: AppDatabase) {
        val workoutDao = db.workoutDao()
        
        // ПЕРЕВІРКА: якщо в базі вже є вправи, значить вона заповнена
        if (workoutDao.getAllExercisesSync().isNotEmpty()) return

        db.withTransaction {
            val playerDao = db.playerDao()
            val systemConfigDao = db.systemConfigDao()
            val scheduleDao = db.scheduleDao()
            val progressionMatrixDao = db.progressionMatrixDao()
            val debuffConfigDao = db.debuffConfigDao()

            // ── Player ──────────────────────────────────────────────────
            playerDao.insertOrUpdate(PlayerEntity())

            // ── System Config ────────────────────────────────────────────
            systemConfigDao.insertOrUpdate(SystemConfigEntity())

            // ── Exercises (17 вправ) ─────────────────────────────────────
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

            // ── Reference Matrix (Нормативи для 12 основних вправ) ──────
            val referenceMatrix = listOf(
                ReferenceMatrixEntity("8",  "Жим від підлоги", WeightType.ABSOLUTE, 2.5, mapOf("M0" to 60.0, "M12" to 85.0)),
                ReferenceMatrixEntity("6",  "Присідання зі штангою", WeightType.ABSOLUTE, 5.0, mapOf("M0" to 55.0, "M12" to 120.0)),
                ReferenceMatrixEntity("12", "Румунська тяга", WeightType.ABSOLUTE, 5.0, mapOf("M0" to 60.0, "M12" to 135.0)),
                ReferenceMatrixEntity("9",  "Жим гантелей сидячи", WeightType.ABSOLUTE, 2.5, mapOf("M0" to 32.5, "M12" to 55.0)),
                ReferenceMatrixEntity("13", "Тяга в нахилі", WeightType.ABSOLUTE, 2.5, mapOf("M0" to 45.0, "M12" to 85.0)),
                ReferenceMatrixEntity("5",  "Підтягування", WeightType.ADDED_WEIGHT, 2.5, mapOf("M0" to 0.0, "M12" to 10.0)),
                ReferenceMatrixEntity("10", "Face Pulls", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 8.5, "M12" to 20.0)),
                ReferenceMatrixEntity("7",  "Болгарські присідання", WeightType.ADDED_WEIGHT, 2.0, mapOf("M0" to 0.0, "M12" to 20.0)),
                ReferenceMatrixEntity("11", "Жим гантелей під кутом", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 16.0, "M12" to 32.5)),
                ReferenceMatrixEntity("15", "Згинання біцепс (EZ)", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 32.0, "M12" to 42.5)),
                ReferenceMatrixEntity("16", "Французький жим (EZ)", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 22.0, "M12" to 40.0)),
                ReferenceMatrixEntity("14", "Махи гантелями в сторони", WeightType.ABSOLUTE, 0.5, mapOf("M0" to 6.0, "M12" to 15.0))
            )
            referenceMatrix.forEach { progressionMatrixDao.insertReference(it) }

            // ── Progression Matrix (Поточний прогрес для 12 вправ) ──────
            listOf(
                ProgressionMatrixEntity(8,  60f, 85f,  69f),   // Жим від підлоги
                ProgressionMatrixEntity(6,  55f, 120f, 62.5f), // Присідання
                ProgressionMatrixEntity(12, 60f, 135f, 80f),   // Румунська тяга
                ProgressionMatrixEntity(9,  32.5f, 55f, 35f),  // Жим гантелей сидячи
                ProgressionMatrixEntity(13, 45f, 85f, 54f),    // Тяга в нахилі
                ProgressionMatrixEntity(5,  0f, 10f, 0f),      // Підтягування
                ProgressionMatrixEntity(10, 8.5f, 20f, 11.5f), // Face Pulls
                ProgressionMatrixEntity(7,  0f, 20f, 7f),      // Болгарські присідання
                ProgressionMatrixEntity(11, 16f, 32.5f, 22f),  // Жим гантелей під кутом
                ProgressionMatrixEntity(15, 32f, 42.5f, 33f),  // Біцепс
                ProgressionMatrixEntity(16, 22f, 40f, 33f),    // Французький жим
                ProgressionMatrixEntity(14, 6f, 15f, 6f)       // Махи
            ).forEach { progressionMatrixDao.insert(it) }

            // ── Workout Templates ─────────────────────────────────────────
            workoutDao.insertTemplate(WorkoutTemplateEntity(1, "Пре-квест"))
            workoutDao.insertTemplate(WorkoutTemplateEntity(2, "Тренування Б"))
            workoutDao.insertTemplate(WorkoutTemplateEntity(3, "Тренування А"))

            // Мапінг вправ (CrossRef)
            listOf(1,2,3,4).forEachIndexed { i, id -> workoutDao.insertCrossRef(WorkoutExerciseCrossRef(1, id, i)) }
            listOf(12,11,13,14,15,16,17).forEachIndexed { i, id -> workoutDao.insertCrossRef(WorkoutExerciseCrossRef(2, id, i)) }
            listOf(5,6,7,8,9,10,2).forEachIndexed { i, id -> workoutDao.insertCrossRef(WorkoutExerciseCrossRef(3, id, i)) }

            // ── Debuff Configs ────────────────────────────────────────────
            debuffConfigDao.insert(DebuffConfigEntity(1, "СЛАБКІСТЬ",  "Дебаф: зниження продуктивності", 0))
            debuffConfigDao.insert(DebuffConfigEntity(2, "ЦНС",        "Дебаф: перевтома нервової системи", 0))
            debuffConfigDao.insert(DebuffConfigEntity(3, "ХВОРОБА",    "Штраф: активна хвороба", 20))

            // ── Schedule ──────────────────────────────────────────
            scheduleDao.insertSchedule(ScheduleEntity(1, 1, null))
            scheduleDao.insertSchedule(ScheduleEntity(2, 2, 2))
            scheduleDao.insertSchedule(ScheduleEntity(3, 3, null))
            scheduleDao.insertSchedule(ScheduleEntity(4, 4, 3))

            // ── Daily Task Templates ────────────────────────────
            val tasks = listOf("Омега-3", "Креатин", "Декомпресія", "Магній", "Сон", "D3", "Постава", "Розтягнення")
            tasks.forEachIndexed { i, name -> scheduleDao.insertDailyTaskTemplate(DailyTaskTemplateEntity(i + 1, name)) }

            // Мапінг завдань до днів
            listOf(1,2,3,4).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(1, it)) }
            listOf(1,5).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(2, it)) }
            listOf(4,6,1,2,7).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(3, it)) }
            listOf(1,8,2,4).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(4, it)) }
        }
    }
}
