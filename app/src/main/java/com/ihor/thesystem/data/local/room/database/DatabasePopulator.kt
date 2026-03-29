package com.ihor.thesystem.data.local.room.database

import androidx.room.withTransaction
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*

object DatabasePopulator {

    suspend fun populate(
        db: AppDatabase
    ) {
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

            // ── Exercises ─────────────────────────────────────
            val exercises = listOf(
                ExerciseEntity(1,  "Сідничний місток"),
                ExerciseEntity(2,  "Мертвий жук"),
                ExerciseEntity(3,  "Scapular Push-ups"),
                ExerciseEntity(4,  "Wall Slides"),
                ExerciseEntity(5,  "Підтягування"),
                ExerciseEntity(6,  "Присідання"),
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

            // ── Reference Matrix ────────────────────────────────
            val referenceMatrix = listOf(
                ReferenceMatrixEntity("pushups_weighted", "Жим від підлоги", WeightType.ABSOLUTE, 2.5, mapOf("M0" to 60.0, "M12" to 85.0)),
                ReferenceMatrixEntity("squats", "Присідання", WeightType.ABSOLUTE, 5.0, mapOf("M0" to 55.0, "M12" to 120.0)),
                ReferenceMatrixEntity("romanian_deadlift", "Румунська тяга", WeightType.ABSOLUTE, 5.0, mapOf("M0" to 60.0, "M12" to 135.0)),
                ReferenceMatrixEntity("db_press_seated", "Жим гантелей сидячи", WeightType.ABSOLUTE, 2.5, mapOf("M0" to 32.5, "M12" to 55.0)),
                ReferenceMatrixEntity("bent_over_row", "Тяга в нахилі", WeightType.ABSOLUTE, 2.5, mapOf("M0" to 45.0, "M12" to 85.0)),
                ReferenceMatrixEntity("pullups", "Підтягування", WeightType.ADDED_WEIGHT, 2.5, mapOf("M0" to 0.0, "M12" to 10.0)),
                ReferenceMatrixEntity("face_pulls", "Face Pulls", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 8.5, "M12" to 20.0)),
                ReferenceMatrixEntity("bulgarian_splits", "Болгарські присідання", WeightType.ADDED_WEIGHT, 2.0, mapOf("M0" to 0.0, "M12" to 20.0)),
                ReferenceMatrixEntity("db_press_incline", "Жим гантелей під кутом", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 16.0, "M12" to 32.5)),
                ReferenceMatrixEntity("bicep_curl_ez", "Біцепс EZ", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 32.0, "M12" to 42.5)),
                ReferenceMatrixEntity("french_press_ez", "Французький жим EZ", WeightType.ABSOLUTE, 1.0, mapOf("M0" to 22.0, "M12" to 40.0)),
                ReferenceMatrixEntity("lateral_raises", "Махи гантелями в сторони", WeightType.ABSOLUTE, 0.5, mapOf("M0" to 6.0, "M12" to 15.0))
            )
            referenceMatrix.forEach { progressionMatrixDao.insertReference(it) }

            // ── Workout Templates ─────────────────────────────────────────
            workoutDao.insertTemplate(WorkoutTemplateEntity(1, "Пре-квест"))
            workoutDao.insertTemplate(WorkoutTemplateEntity(2, "Тренування Б"))
            workoutDao.insertTemplate(WorkoutTemplateEntity(3, "Тренування А"))

            // Мапінг вправ
            listOf(1,2,3,4).forEachIndexed { i, exId -> workoutDao.insertCrossRef(WorkoutExerciseCrossRef(1, exId, i)) }
            listOf(12,11,13,14,15,16,17).forEachIndexed { i, exId -> workoutDao.insertCrossRef(WorkoutExerciseCrossRef(2, exId, i)) }
            listOf(5,6,7,8,9,10,2).forEachIndexed { i, exId -> workoutDao.insertCrossRef(WorkoutExerciseCrossRef(3, exId, i)) }

            // ── Debuff Configs ────────────────────────────────────────────
            debuffConfigDao.insert(DebuffConfigEntity(1, "СЛАБКІСТЬ",  "Дебаф: зниження продуктивності", 0))
            debuffConfigDao.insert(DebuffConfigEntity(2, "ЦНС",        "Дебаф: перевтома нервової системи", 0))
            debuffConfigDao.insert(DebuffConfigEntity(3, "ХВОРОБА",    "Штраф: активна хвороба", 20))

            // ── Schedule ──────────────────────────────────────────
            scheduleDao.insertSchedule(ScheduleEntity(1, cycleDay=1, workoutTemplateId=null))
            scheduleDao.insertSchedule(ScheduleEntity(2, cycleDay=2, workoutTemplateId=2))
            scheduleDao.insertSchedule(ScheduleEntity(3, cycleDay=3, workoutTemplateId=null))
            scheduleDao.insertSchedule(ScheduleEntity(4, cycleDay=4, workoutTemplateId=3))

            // ── Daily Task Templates ────────────────────────────
            val tasks = listOf(
                DailyTaskTemplateEntity(1, "Омега-3"),
                DailyTaskTemplateEntity(2, "Креатин"),
                DailyTaskTemplateEntity(3, "Декомпресія"),
                DailyTaskTemplateEntity(4, "Магній"),
                DailyTaskTemplateEntity(5, "Сон"),
                DailyTaskTemplateEntity(6, "D3"),
                DailyTaskTemplateEntity(7, "Постава"),
                DailyTaskTemplateEntity(8, "Розтягнення")
            )
            tasks.forEach { scheduleDao.insertDailyTaskTemplate(it) }

            // Task mapping
            listOf(1,2,3,4).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(1, it)) }
            listOf(1,5).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(2, it)) }
            listOf(4,6,1,2,7).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(3, it)) }
            listOf(1,8,2,4).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(4, it)) }
            
            // ── Initial Matrix ──────────────────────────────────
            listOf(
                ProgressionMatrixEntity(exerciseId = 8, startWeight = 60f, targetWeight = 85f, currentWeight = 69f),
                ProgressionMatrixEntity(exerciseId = 12, startWeight = 60f, targetWeight = 135f, currentWeight = 80f),
                ProgressionMatrixEntity(exerciseId = 6, startWeight = 55f, targetWeight = 120f, currentWeight = 62.5f)
            ).forEach { progressionMatrixDao.insert(it) }
        }
    }
}
