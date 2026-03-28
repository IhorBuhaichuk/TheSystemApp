package com.ihor.thesystem.data.local.room.database

import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*

object DatabasePopulator {

    suspend fun populate(
        playerDao: PlayerDao,
        systemConfigDao: SystemConfigDao,
        workoutDao: WorkoutDao,
        scheduleDao: ScheduleDao,
        progressionMatrixDao: ProgressionMatrixDao,
        debuffConfigDao: DebuffConfigDao
    ) {
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
            ExerciseEntity(13, "Тяга штанги в нахилі"),
            ExerciseEntity(14, "Махи гантелями"),
            ExerciseEntity(15, "Згинання біцепс (EZ)"),
            ExerciseEntity(16, "Французький жим (EZ)"),
            ExerciseEntity(17, "Планка")
        )
        exercises.forEach { workoutDao.insertExercise(it) }

        // ── Reference Matrix ────────────────────────────────
        val referenceMatrix = listOf(
            ReferenceMatrixEntity(
                "pushups_weighted", "Жим від підлоги", WeightType.ABSOLUTE, 2.5,
                mapOf("M0" to 60.0, "M1" to 62.5, "M2" to 65.0, "M3" to 67.5, "M4" to 70.0, "M5" to 72.5, "M6" to 75.0, "M7" to 77.5, "M8" to 79.0, "M9" to 80.0, "M10" to 82.5, "M11" to 84.0, "M12" to 85.0)
            ),
            ReferenceMatrixEntity(
                "squats", "Присідання", WeightType.ABSOLUTE, 5.0,
                mapOf("M0" to 55.0, "M1" to 60.0, "M2" to 65.0, "M3" to 70.0, "M4" to 75.0, "M5" to 77.5, "M6" to 80.0, "M7" to 100.0, "M8" to 105.0, "M9" to 110.0, "M10" to 115.0, "M11" to 117.5, "M12" to 120.0)
            ),
            ReferenceMatrixEntity(
                "romanian_deadlift", "Румунська тяга", WeightType.ABSOLUTE, 5.0,
                mapOf("M0" to 60.0, "M1" to 70.0, "M2" to 75.0, "M3" to 80.0, "M4" to 85.0, "M5" to 87.5, "M6" to 90.0, "M7" to 120.0, "M8" to 125.0, "M9" to 127.5, "M10" to 130.0, "M11" to 132.5, "M12" to 135.0)
            ),
            ReferenceMatrixEntity(
                "db_press_seated", "Жим гантелей сидячи", WeightType.ABSOLUTE, 2.5,
                mapOf("M0" to 32.5, "M1" to 35.0, "M2" to 37.5, "M3" to 40.0, "M4" to 42.5, "M5" to 45.0, "M6" to 47.0, "M7" to 49.0, "M8" to 50.0, "M9" to 51.5, "M10" to 52.5, "M11" to 54.0, "M12" to 55.0)
            ),
            ReferenceMatrixEntity(
                "bent_over_row", "Тяга в нахилі", WeightType.ABSOLUTE, 2.5,
                mapOf("M0" to 45.0, "M1" to 50.0, "M2" to 55.0, "M3" to 60.0, "M4" to 65.0, "M5" to 70.0, "M6" to 72.5, "M7" to 75.0, "M8" to 77.5, "M9" to 80.0, "M10" to 82.5, "M11" to 84.0, "M12" to 85.0)
            ),
            ReferenceMatrixEntity(
                "pullups", "Підтягування", WeightType.ADDED_WEIGHT, 2.5,
                mapOf("M0" to 0.0, "M1" to 0.0, "M2" to 0.0, "M3" to 0.0, "M4" to 0.0, "M5" to 2.5, "M6" to 5.0, "M7" to 5.0, "M8" to 7.5, "M9" to 7.5, "M10" to 10.0, "M11" to 10.0, "M12" to 10.0),
                mapOf("M0" to 21, "M1" to 24, "M2" to 27, "M3" to 30, "M4" to 36)
            ),
            ReferenceMatrixEntity(
                "face_pulls", "Face Pulls", WeightType.ABSOLUTE, 1.0,
                mapOf("M0" to 8.5, "M1" to 9.5, "M2" to 11.0, "M3" to 12.5, "M4" to 13.5, "M5" to 15.0, "M6" to 16.0, "M7" to 17.0, "M8" to 18.0, "M9" to 18.5, "M10" to 19.0, "M11" to 19.5, "M12" to 20.0)
            ),
            ReferenceMatrixEntity(
                "bulgarian_splits", "Болгарські присідання", WeightType.ADDED_WEIGHT, 2.0,
                mapOf("M0" to 0.0, "M1" to 0.0, "M2" to 2.0, "M3" to 4.0, "M4" to 6.0, "M5" to 8.0, "M6" to 10.0, "M7" to 12.0, "M8" to 14.0, "M9" to 16.0, "M10" to 17.5, "M11" to 19.0, "M12" to 20.0)
            ),
            ReferenceMatrixEntity(
                "db_press_incline", "Жим гантелей під кутом", WeightType.ABSOLUTE, 1.0,
                mapOf("M0" to 16.0, "M1" to 18.5, "M2" to 20.0, "M3" to 21.0, "M4" to 22.0, "M5" to 23.0, "M6" to 24.0, "M7" to 28.0, "M8" to 29.0, "M9" to 30.0, "M10" to 31.0, "M11" to 32.0, "M12" to 32.5)
            ),
            ReferenceMatrixEntity(
                "bicep_curl_ez", "Біцепс EZ", WeightType.ABSOLUTE, 1.0,
                mapOf("M0" to 32.0, "M1" to 33.0, "M2" to 34.0, "M3" to 35.0, "M4" to 36.0, "M5" to 37.5, "M6" to 38.5, "M7" to 39.0, "M8" to 40.0, "M9" to 40.5, "M10" to 41.5, "M11" to 42.0, "M12" to 42.5)
            ),
            ReferenceMatrixEntity(
                "french_press_ez", "Французький жим EZ", WeightType.ABSOLUTE, 1.0,
                mapOf("M0" to 22.0, "M1" to 27.0, "M2" to 29.0, "M3" to 30.0, "M4" to 31.0, "M5" to 32.5, "M6" to 34.0, "M7" to 35.5, "M8" to 37.0, "M9" to 38.0, "M10" to 39.0, "M11" to 39.5, "M12" to 40.0)
            ),
            ReferenceMatrixEntity(
                "lateral_raises", "Махи гантелями в сторони", WeightType.ABSOLUTE, 0.5,
                mapOf("M0" to 6.0, "M1" to 6.5, "M2" to 7.0, "M3" to 7.5, "M4" to 8.0, "M5" to 8.5, "M6" to 10.5, "M7" to 11.0, "M8" to 11.5, "M9" to 12.0, "M10" to 13.0, "M11" to 14.0, "M12" to 15.0)
            )
        )
        referenceMatrix.forEach { progressionMatrixDao.insertReference(it) }

        // ── Workout Templates ─────────────────────────────────────────
        workoutDao.insertTemplate(WorkoutTemplateEntity(1, "Пре-квест"))
        workoutDao.insertTemplate(WorkoutTemplateEntity(2, "Тренування Б"))
        workoutDao.insertTemplate(WorkoutTemplateEntity(3, "Тренування А"))

        // Пре-квест
        listOf(1,2,3,4).forEachIndexed { i, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(1, exId, i))
        }
        // Комплекс Б
        listOf(12,11,13,14,15,16,17).forEachIndexed { i, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(2, exId, i))
        }
        // Комплекс А
        listOf(5,6,7,8,9,10,2).forEachIndexed { i, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(3, exId, i))
        }

        // ── Debuff Configs ────────────────────────────────────────────
        debuffConfigDao.insert(DebuffConfigEntity(1, "СЛАБКІСТЬ",  "Дебаф: зниження продуктивності", 0))
        debuffConfigDao.insert(DebuffConfigEntity(2, "ЦНС",        "Дебаф: перевтома нервової системи", 0))
        debuffConfigDao.insert(DebuffConfigEntity(3, "ХВОРОБА",    "Штраф: активна хвороба", 20))

        // ── Schedule ──────────────────────────────────────────
        scheduleDao.insertSchedule(ScheduleEntity(1, cycleDay=1, workoutTemplateId=null, debuffConfigId=1))
        scheduleDao.insertSchedule(ScheduleEntity(2, cycleDay=2, workoutTemplateId=2,    debuffConfigId=2))
        scheduleDao.insertSchedule(ScheduleEntity(3, cycleDay=3, workoutTemplateId=null, debuffConfigId=null))
        scheduleDao.insertSchedule(ScheduleEntity(4, cycleDay=4, workoutTemplateId=3,    debuffConfigId=null))

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

        // ── Initial Progression Matrix ────────────────────────────────
        val matrixData = listOf(
            Triple(8,  "Жим від підлоги",  69.0f),
            Triple(12, "Румунська тяга", 80.0f),
            Triple(6,  "Присідання", 62.5f),
            Triple(13, "Тяга штанги в нахилі", 54.0f),
            Triple(5,  "Підтягування", 26.0f),
            Triple(11, "Жим гантелей під кутом", 22.0f),
            Triple(9,  "Жим гантелей сидячи", 14.5f),
            Triple(7,  "Болгарські присідання", 7.0f),
            Triple(15, "Біцепс EZ", 33.0f),
            Triple(16, "Французький жим EZ", 33.0f),
            Triple(10, "Face Pulls", 11.5f),
            Triple(14, "Махи гантелями в сторони", 6.0f)
        )
        
        matrixData.forEach { (exId, name, current) ->
            val ref = referenceMatrix.find { it.exerciseName == name }
            val start = ref?.milestones?.get("M0")?.toFloat() ?: current
            val target = ref?.milestones?.get("M12")?.toFloat() ?: -1.0f
            progressionMatrixDao.insert(ProgressionMatrixEntity(exerciseId = exId, startWeight = start, targetWeight = target, currentWeight = current))
        }
    }
}
