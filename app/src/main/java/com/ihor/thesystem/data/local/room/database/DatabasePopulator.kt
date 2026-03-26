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

        // ── Exercises (IDs 1–17) ─────────────────────────────────────
        val exercises = listOf(
            ExerciseEntity(1,  "Сідничний місток"),
            ExerciseEntity(2,  "Мертвий жук"),
            ExerciseEntity(3,  "Scapular Push-ups"),
            ExerciseEntity(4,  "Wall Slides"),
            ExerciseEntity(5,  "Підтягування"),
            ExerciseEntity(6,  "Присідання"),
            ExerciseEntity(7,  "Болгарські спліт-присідання"),
            ExerciseEntity(8,  "Жим штанги з підлоги"),
            ExerciseEntity(9,  "Жим гантелей сидячи"),
            ExerciseEntity(10, "Face Pulls"),
            ExerciseEntity(11, "Жим гантелей під кутом"),
            ExerciseEntity(12, "Румунська тяга"),
            ExerciseEntity(13, "Тяга штанги в нахилі"),
            ExerciseEntity(14, "Махи гантелями в сторони"),
            ExerciseEntity(15, "Біцепс EZ"),
            ExerciseEntity(16, "Французький жим EZ"),
            ExerciseEntity(17, "Планка")
        )
        exercises.forEach { workoutDao.insertExercise(it) }

        // ── Workout Templates ─────────────────────────────────────────
        workoutDao.insertTemplate(WorkoutTemplateEntity(1, "Пре-квест"))
        workoutDao.insertTemplate(WorkoutTemplateEntity(2, "Комплекс Б"))
        workoutDao.insertTemplate(WorkoutTemplateEntity(3, "Комплекс А"))

        // Пре-квест: 1,2,3,4
        listOf(1,2,3,4).forEachIndexed { i, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(1, exId, i))
        }
        // Комплекс Б: 12,11,13,14,15,16,17
        listOf(12,11,13,14,15,16,17).forEachIndexed { i, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(2, exId, i))
        }
        // Комплекс А: 5,6,7,8,9,10,2
        listOf(5,6,7,8,9,10,2).forEachIndexed { i, exId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(3, exId, i))
        }

        // ── Debuff Configs ────────────────────────────────────────────
        debuffConfigDao.insert(DebuffConfigEntity(1, "СЛАБКІСТЬ",  "Дебаф: зниження продуктивності", 0))
        debuffConfigDao.insert(DebuffConfigEntity(2, "ЦНС",        "Дебаф: перевтома нервової системи", 0))
        debuffConfigDao.insert(DebuffConfigEntity(3, "ХВОРОБА",    "Штраф: активна хвороба", 20))

        // ── Schedule (4 дні) ──────────────────────────────────────────
        scheduleDao.insertSchedule(ScheduleEntity(1, cycleDay=1, workoutTemplateId=null, debuffConfigId=1))
        scheduleDao.insertSchedule(ScheduleEntity(2, cycleDay=2, workoutTemplateId=2,    debuffConfigId=2))
        scheduleDao.insertSchedule(ScheduleEntity(3, cycleDay=3, workoutTemplateId=null, debuffConfigId=null))
        scheduleDao.insertSchedule(ScheduleEntity(4, cycleDay=4, workoutTemplateId=3,    debuffConfigId=null))

        // ── Daily Task Templates (IDs 1–8) ────────────────────────────
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

        // День 1: Омега-3, Креатин, Декомпресія, Магній
        listOf(1,2,3,4).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(1, it)) }
        // День 2: Омега-3, Сон
        listOf(1,5).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(2, it)) }
        // День 3: Магній, D3, Омега-3, Креатин, Постава
        listOf(4,6,1,2,7).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(3, it)) }
        // День 4: Омега-3, Розтягнення, Креатин, Магній
        listOf(1,8,2,4).forEach { scheduleDao.insertScheduleTaskCrossRef(ScheduleTaskCrossRef(4, it)) }

        // ── Progression Matrix ────────────────────────────────────────
        val matrix = listOf(
            ProgressionMatrixEntity(0, 8,  69.0f,  87.5f,  69.0f),
            ProgressionMatrixEntity(0, 12, 80.0f,  107.5f, 80.0f),
            ProgressionMatrixEntity(0, 6,  62.5f,  90.0f,  62.5f),
            ProgressionMatrixEntity(0, 13, 54.0f,  72.5f,  54.0f),
            ProgressionMatrixEntity(0, 5,  26.0f,  -1.0f,  26.0f, "BW+10"),
            ProgressionMatrixEntity(0, 11, 22.0f,  30.0f,  22.0f),
            ProgressionMatrixEntity(0, 9,  14.5f,  22.5f,  14.5f),
            ProgressionMatrixEntity(0, 7,  7.0f,   16.0f,  7.0f),
            ProgressionMatrixEntity(0, 15, 33.0f,  42.0f,  33.0f),
            ProgressionMatrixEntity(0, 16, 33.0f,  42.0f,  33.0f),
            ProgressionMatrixEntity(0, 10, 11.5f,  20.0f,  11.5f),
            ProgressionMatrixEntity(0, 14, 6.0f,   11.0f,  6.0f)
        )
        matrix.forEach { progressionMatrixDao.insert(it) }
    }
}