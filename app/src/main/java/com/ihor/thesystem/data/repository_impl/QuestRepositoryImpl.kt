package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.QuestDao
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.QuestWithTasks
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao
) : QuestRepository {

    override fun getActiveDailyQuest(): Flow<Quest?> =
        questDao.getActiveQuestByType(EntityQuestType.DAILY).map { it?.toDomain() }

    override fun getActiveMainQuest(): Flow<Quest?> =
        questDao.getActiveQuestByType(EntityQuestType.MAIN).map { it?.toDomain() }

    override fun getActivePromotionQuest(): Flow<Quest?> =
        questDao.getActiveQuestByType(EntityQuestType.PROMOTION).map { it?.toDomain() }

    override fun getActiveQuests(): Flow<List<Quest>> =
        questDao.getActiveQuestsWithTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun hasActiveQuests(): Boolean =
        questDao.getActiveQuestCount() > 0

    override suspend fun toggleTaskCompletion(taskId: Int, questId: Int, isCompleted: Boolean) {
        questDao.setTaskCompletion(taskId, isCompleted)
        val allTasks = questDao.getTasksForQuestSync(questId)
        if (allTasks.isNotEmpty() && allTasks.all { it.isCompleted }) {
            questDao.updateQuestStatus(questId, EntityQuestStatus.COMPLETED)
        } else {
            questDao.updateQuestStatus(questId, EntityQuestStatus.ACTIVE)
        }
    }

    override suspend fun updateQuestStatus(questId: Int, status: DomainQuestStatus) =
        questDao.updateQuestStatus(questId, status.toEntity())

    override suspend fun createDailyQuest(
        title: String, tasks: List<String>, scheduleId: Int?
    ) {
        val questId = questDao.insertQuest(
            QuestEntity(title = title, type = EntityQuestType.DAILY, scheduleId = scheduleId)
        ).toInt()
        tasks.forEach { taskName ->
            questDao.insertQuestTask(QuestTaskEntity(questId = questId, name = taskName))
        }
    }

    override suspend fun createMainQuest(
        title: String, exercises: List<ExerciseRecommendation>, scheduleId: Int?
    ) {
        val questId = questDao.insertQuest(
            QuestEntity(title = title, type = EntityQuestType.MAIN, scheduleId = scheduleId)
        ).toInt()
        exercises.forEach { rec ->
            questDao.insertQuestTask(
                QuestTaskEntity(
                    questId = questId, 
                    name = rec.exerciseName,
                    exerciseId = rec.exerciseId,
                    targetWeight = rec.weight,
                    targetSets = rec.sets,
                    targetReps = rec.reps
                )
            )
        }
    }

    override suspend fun createPromotionQuest(exerciseId: Int, title: String, description: String) {
        val questId = questDao.insertQuest(
            QuestEntity(title = title, type = EntityQuestType.PROMOTION, scheduleId = null)
        ).toInt()
        questDao.insertQuestTask(
            QuestTaskEntity(
                questId = questId,
                name = description,
                exerciseId = exerciseId,
                isCompleted = false
            )
        )
    }

    override suspend fun addTaskToQuest(questId: Int, taskName: String) {
        questDao.insertQuestTask(QuestTaskEntity(questId = questId, name = taskName))
    }

    override suspend fun removeTask(taskId: Int) {
        questDao.deleteTask(taskId)
    }

    override suspend fun archiveActiveQuests() {
        questDao.archiveActiveQuests()
    }

    override suspend fun getLastTwoMainQuestsStatus(): List<DomainQuestStatus> =
        questDao.getLastTwoMainQuests().map { it.status.toDomain() }

    override fun getQuestsByDate(dateMillis: Long): Flow<List<Quest>> =
        questDao.getQuestsByDate(dateMillis).map { list -> list.map { it.toDomain() } }
}

private typealias EntityQuestType   = com.ihor.thesystem.data.local.room.entity.QuestType
private typealias EntityQuestStatus = com.ihor.thesystem.data.local.room.entity.QuestStatus

private fun QuestWithTasks.toDomain() = Quest(
    id     = quest.id,
    title  = quest.title,
    type   = quest.type.toDomain(),
    date   = quest.date,
    status = quest.status.toDomain(),
    tasks  = tasks.map { 
        QuestTask(
            id = it.id, 
            questId = it.questId, 
            name = it.name, 
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
}

private fun DomainQuestStatus.toEntity() = when (this) {
    DomainQuestStatus.ACTIVE    -> EntityQuestStatus.ACTIVE
    DomainQuestStatus.COMPLETED -> EntityQuestStatus.COMPLETED
    DomainQuestStatus.FAILED    -> EntityQuestStatus.FAILED
}
