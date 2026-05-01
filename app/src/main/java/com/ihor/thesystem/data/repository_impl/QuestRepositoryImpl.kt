package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.QuestDao
import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.QuestWithTasks
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao,
    private val questLogDao: QuestLogDao,
    private val transactionProvider: TransactionProvider,
    private val clock: AppClock
) : QuestRepository {

    override fun getActiveDailyQuest(): Flow<Quest?> =
        questDao.getActiveQuestByType(EntityQuestType.DAILY, EntityQuestStatus.ACTIVE).map { it?.toDomain() }

    override fun getActiveMainQuest(): Flow<Quest?> =
        questDao.getActiveQuestByType(EntityQuestType.MAIN, EntityQuestStatus.ACTIVE).map { it?.toDomain() }

    override fun getActivePromotionQuest(): Flow<Quest?> =
        questDao.getActiveQuestByType(EntityQuestType.PROMOTION, EntityQuestStatus.ACTIVE).map { it?.toDomain() }

    override fun getActiveQuests(): Flow<List<Quest>> =
        questDao.getActiveQuestsWithTasks(EntityQuestStatus.ACTIVE).map { list -> list.map { it.toDomain() } }

    override suspend fun hasActiveQuests(): Boolean =
        questDao.getActiveQuestCount(EntityQuestStatus.ACTIVE) > 0

    override suspend fun toggleTaskCompletion(taskId: Int, questId: Int, isCompleted: Boolean) =
        transactionProvider.runInTransaction {
            questDao.setTaskCompletion(taskId, isCompleted)
            val allTasks = questDao.getTasksForQuestSync(questId)
            if (allTasks.isNotEmpty() && allTasks.all { it.isCompleted }) {
                questDao.updateQuestStatus(questId, EntityQuestStatus.COMPLETED)
            } else {
                questDao.updateQuestStatus(questId, EntityQuestStatus.ACTIVE)
            }
        }

    override suspend fun completeQuestTasksForExercises(questId: Int, exerciseIds: Set<Int>) =
        transactionProvider.runInTransaction {
            if (exerciseIds.isEmpty()) return@runInTransaction
            val tasks = questDao.getTasksForQuestSync(questId)
            tasks
                .filter { task -> task.exerciseId != null && task.exerciseId in exerciseIds && !task.isCompleted }
                .forEach { task ->
                    questDao.setTaskCompletion(task.id, true)
                }

            val updatedTasks = questDao.getTasksForQuestSync(questId)
            if (updatedTasks.isNotEmpty() && updatedTasks.all { it.isCompleted }) {
                questDao.updateQuestStatus(questId, EntityQuestStatus.COMPLETED)
            }
        }

    override suspend fun updateQuestStatus(questId: Int, status: DomainQuestStatus) =
        questDao.updateQuestStatus(questId, status.toEntity())

    override suspend fun createDailyQuest(
        title: String, tasks: List<String>, scheduleId: Int?
    ) = transactionProvider.runInTransaction {
        val questId = questDao.insertQuest(
            QuestEntity(
                title = title, 
                type = EntityQuestType.DAILY, 
                scheduleId = scheduleId, 
                status = EntityQuestStatus.ACTIVE,
                date = clock.now()
            )
        ).toInt()
        tasks.forEach { taskName ->
            questDao.insertQuestTask(QuestTaskEntity(questId = questId, name = taskName))
        }
    }

    override suspend fun createMainQuest(
        title: String, exercises: List<ExerciseRecommendation>, scheduleId: Int?
    ) = transactionProvider.runInTransaction {
        val questId = questDao.insertQuest(
            QuestEntity(
                title = title, 
                type = EntityQuestType.MAIN, 
                scheduleId = scheduleId, 
                status = EntityQuestStatus.ACTIVE,
                date = clock.now()
            )
        ).toInt()
        exercises.forEach { rec ->
            questDao.insertQuestTask(
                QuestTaskEntity(
                    questId = questId, 
                    name = rec.exerciseName,
                    nameUk = rec.exerciseNameUk,
                    exerciseId = rec.exerciseId,
                    targetWeight = rec.weight,
                    targetSets = rec.sets,
                    targetReps = rec.reps
                )
            )
        }
    }

    override suspend fun createPromotionQuest(
        exerciseId: Int, 
        title: String, 
        description: String,
        targetWeight: Double?,
        targetReps: Int?,
        exerciseNameUk: String?
    ) = transactionProvider.runInTransaction {
        val questId = questDao.insertQuest(
            QuestEntity(
                title = title, 
                type = EntityQuestType.PROMOTION, 
                scheduleId = null, // Раніше тут зберігався exerciseId
                targetExerciseId = exerciseId, // Тепер використовуємо спеціальне поле
                status = EntityQuestStatus.ACTIVE,
                date = clock.now()
            )
        ).toInt()
        questDao.insertQuestTask(
            QuestTaskEntity(
                questId = questId,
                name = description,
                nameUk = exerciseNameUk?.let { "ЕКЗАМЕН: ${it.uppercase()}" },
                exerciseId = exerciseId,
                isCompleted = false,
                targetWeight = targetWeight,
                targetReps = targetReps,
                targetSets = 1
            )
        )
        Unit
    }

    override suspend fun addTaskToQuest(questId: Int, taskName: String) {
        questDao.insertQuestTask(QuestTaskEntity(questId = questId, name = taskName))
    }

    override suspend fun removeTask(taskId: Int) {
        questDao.deleteTask(taskId)
    }

    override suspend fun archiveActiveQuests() = transactionProvider.runInTransaction {
        // Успішно виконані квести переносимо в архів (LOCKED)
        questDao.updateStatusForQuests(EntityQuestStatus.COMPLETED, EntityQuestStatus.LOCKED)
        // Провалені квести також архівуємо (вони вже мають статус FAILED)
        questDao.updateStatusForQuests(EntityQuestStatus.FAILED, EntityQuestStatus.LOCKED)
    }

    override suspend fun getLastTwoMainQuestsStatus(): List<DomainQuestStatus> =
        questDao.getLastQuestsByType(type = EntityQuestType.MAIN, limit = 2).map { it.status.toDomain() }

    override fun getQuestsByDate(dateMillis: Long): Flow<List<Quest>> {
        val (start, end) = getDayRange(dateMillis)
        return questDao.getQuestsByDateRange(start, end).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getQuestById(questId: Int): Quest? =
        questDao.getQuestWithTasksById(questId)?.toDomain()

    override suspend fun deleteQuestWithTasks(questId: Int) {
        questDao.deleteQuestWithTasks(questId)
    }

    override fun getDailyQuestsForDate(dateMillis: Long): Flow<List<Quest>> {
        val (start, end) = getDayRange(dateMillis)
        return questDao.getDailyQuestsForDateRange(start, end, EntityQuestType.DAILY, EntityQuestType.MAIN)
            .map { list -> list.map { it.toDomain() } }
    }

    private fun getDayRange(millis: Long): Pair<Long, Long> {
        val instant = java.time.Instant.ofEpochMilli(millis)
        val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
        
        val logicalDate = zonedDateTime.toLocalDate()
        
        val start = logicalDate.atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            
        val end = logicalDate.atTime(java.time.LocalTime.MAX)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return start to end
    }

    override fun getPendingPromotionQuests(): Flow<List<Quest>> =
        questDao.getPendingPromotionQuests(EntityQuestType.PROMOTION, EntityQuestStatus.COMPLETED).map { list -> list.map { it.toDomain() } }

    override fun getActivePromotionQuests(): Flow<List<Quest>> =
        questDao.getActiveQuestsWithTasks(EntityQuestStatus.ACTIVE).map { list ->
            list.filter { it.quest.type == EntityQuestType.PROMOTION }.map { it.toDomain() } 
        }

    override suspend fun logQuestResult(
        questId: Int,
        questType: DomainQuestType,
        wasSuccessful: Boolean
    ) {
        questLogDao.insert(
            QuestLogEntity(
                questId = questId,
                questType = questType.toEntity(),
                wasSuccessful = wasSuccessful,
                completedAt = clock.now()
            )
        )
    }

    override fun getSuccessfulQuestCount(
        questType: DomainQuestType,
        startMillis: Long,
        endMillis: Long
    ): Flow<Int> =
        questLogDao.getSuccessfulQuestCount(
            type = questType.toEntity(),
            startMillis = startMillis,
            endMillis = endMillis
        )

    override suspend fun getDailyTasksWithCompletionForDate(dateMillis: Long): List<Pair<String, Boolean>> {
        val (start, end) = getDayRange(dateMillis)
        return questDao.getDailyTasksForDateSync(start, end, EntityQuestType.DAILY).map {
            it.name to it.isCompleted
        }
    }
}

private typealias EntityQuestType   = com.ihor.thesystem.data.local.room.entity.QuestType
private typealias EntityQuestStatus = com.ihor.thesystem.data.local.room.entity.QuestStatus

private fun QuestWithTasks.toDomain() = Quest(
    id     = quest.id,
    title  = quest.title,
    type   = quest.type.toDomain(),
    date   = quest.date,
    status = quest.status.toDomain(),
    scheduleId = quest.scheduleId,
    targetExerciseId = quest.targetExerciseId,
    tasks  = tasks.map { 
        QuestTask(
            id = it.id, 
            questId = it.questId, 
            name = it.name, 
            nameUk = it.nameUk,
            isCompleted = it.isCompleted, 
            exerciseId = it.exerciseId,
            recommendedWeight = it.targetWeight,
            recommendedSets = it.targetSets,
            recommendedReps = it.targetReps
        ) 
    }
)

private fun EntityQuestType.toDomain() = when (this) {
    EntityQuestType.DAILY -> DomainQuestType.DAILY
    EntityQuestType.MAIN  -> DomainQuestType.MAIN
    EntityQuestType.PROMOTION -> DomainQuestType.PROMOTION
}

private fun EntityQuestStatus.toDomain() = when (this) {
    EntityQuestStatus.ACTIVE    -> DomainQuestStatus.ACTIVE
    EntityQuestStatus.COMPLETED -> DomainQuestStatus.COMPLETED
    EntityQuestStatus.FAILED    -> DomainQuestStatus.FAILED
    EntityQuestStatus.LOCKED    -> DomainQuestStatus.LOCKED
}

private fun DomainQuestType.toEntity() = when (this) {
    DomainQuestType.DAILY -> EntityQuestType.DAILY
    DomainQuestType.MAIN -> EntityQuestType.MAIN
    DomainQuestType.PROMOTION -> EntityQuestType.PROMOTION
}

private fun DomainQuestStatus.toEntity() = when (this) {
    DomainQuestStatus.ACTIVE    -> EntityQuestStatus.ACTIVE
    DomainQuestStatus.COMPLETED -> EntityQuestStatus.COMPLETED
    DomainQuestStatus.FAILED    -> EntityQuestStatus.FAILED
    DomainQuestStatus.LOCKED    -> EntityQuestStatus.LOCKED
}
