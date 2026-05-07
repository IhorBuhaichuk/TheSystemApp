package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ExerciseWeightType
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.RankProgressionPolicy
import com.ihor.thesystem.domain.model.ReferenceMatrix
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgressionMatrixRepositoryImpl @Inject constructor(
    private val matrixDao:    ProgressionMatrixDao,
    private val analyticsDao: WorkoutAnalyticsDao,
    private val transactionProvider: TransactionProvider,
    private val clock: AppClock
) : ProgressionMatrixRepository {

    override fun getAllEntries(): Flow<List<ProgressionMatrixEntry>> =
        matrixDao.getAllEntriesWithNames().map { list ->
            list.map { item ->
                item.entity.toDomain(item.exerciseName, item.exerciseNameUk, matrixWeeks = 48)
            }
        }

    override suspend fun getEntrySync(exerciseId: Int): ProgressionMatrixEntry? {
        val item = matrixDao.getEntryWithExerciseNameSync(exerciseId) ?: return null
        return item.entity.toDomain(item.exerciseName, item.exerciseNameUk, matrixWeeks = 48)
    }

    override suspend fun updateCurrentWeight(exerciseId: Int, newWeight: Float) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId)
        if (existing != null) {
            matrixDao.update(existing.copy(currentWeight = newWeight))
        } else {
            matrixDao.insert(ProgressionMatrixEntity(
                exerciseId = exerciseId,
                startWeight = 0f,
                targetWeight = 0f,
                currentWeight = newWeight
            ))
        }
    }

    override suspend fun updateMatrixGoals(exerciseId: Int, startWeight: Float, targetWeight: Float) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId)
        if (existing != null) {
            matrixDao.update(existing.copy(
                startWeight = startWeight,
                targetWeight = targetWeight,
                currentWeight = startWeight
            ))
        } else {
            matrixDao.insert(ProgressionMatrixEntity(
                exerciseId = exerciseId,
                startWeight = startWeight,
                targetWeight = targetWeight,
                currentWeight = startWeight
            ))
        }
    }

    override suspend fun saveAnnualProgressionPlan(
        exerciseId: Int,
        startWeight: Float,
        targetWeight: Float,
        targetWeightNote: String?
    ) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId)
        if (existing != null) {
            matrixDao.update(
                existing.copy(
                    startWeight = startWeight,
                    targetWeight = targetWeight,
                    currentWeight = startWeight,
                    targetWeightNote = targetWeightNote
                )
            )
        } else {
            matrixDao.insert(
                ProgressionMatrixEntity(
                    exerciseId = exerciseId,
                    startWeight = startWeight,
                    targetWeight = targetWeight,
                    currentWeight = startWeight,
                    targetWeightNote = targetWeightNote
                )
            )
        }
    }

    override suspend fun saveExerciseSets(exerciseId: Int, sets: List<ActiveSetInput>) {
        saveExerciseSetsWithDate(exerciseId, sets, clock.now())
    }

    override suspend fun saveExerciseSetsWithDate(
        exerciseId: Int,
        sets: List<ActiveSetInput>,
        timestamp: Long,
        userFeedback: String?
    ) {
        val parsedSets = sets.mapNotNull { it.toValidLoggedSet() }
        if (parsedSets.isEmpty()) return

        val zoneId = clock.zoneId()
        val date = java.time.Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val weightedSets = parsedSets.filter { it.weight > TECHNICAL_BODYWEIGHT_LOAD }
        val totalTonnage = weightedSets.sumOf { it.weight * it.reps }

        transactionProvider.runInTransaction {
            val existingLogs = analyticsDao.getLogsForExerciseOnDate(exerciseId, startOfDay, endOfDay)

            if (existingLogs.isNotEmpty()) {
                val sessionId = existingLogs.first().sessionId

                analyticsDao.insertSessionLog(
                    WorkoutSessionLogEntity(
                        sessionId = sessionId,
                        questId = 0,
                        timestamp = timestamp,
                        totalTonnage = totalTonnage,
                        cycleDay = 0,
                        durationMinutes = 0
                    )
                )

                analyticsDao.deleteSetsBySession(sessionId)
                analyticsDao.insertSetLogs(
                    parsedSets.map { input ->
                        input.toEntity(
                            sessionId = sessionId,
                            exerciseId = exerciseId,
                            userFeedback = userFeedback
                        )
                    }
                )
            } else {
                val sessionLog = WorkoutSessionLogEntity(
                    questId = 0,
                    timestamp = timestamp,
                    totalTonnage = totalTonnage,
                    cycleDay = 0,
                    durationMinutes = 0
                )

                analyticsDao.saveFullSessionLog(
                    sessionLog,
                    parsedSets.map { input ->
                        input.toEntity(
                            sessionId = 0,
                            exerciseId = exerciseId,
                            userFeedback = userFeedback
                        )
                    }
                )
            }

            val maxWeightedLoad = weightedSets.maxOfOrNull { it.weight }?.toFloat()
                ?: return@runInTransaction

            matrixDao.getEntryForExerciseSync(exerciseId)?.let { existing ->
                matrixDao.update(existing.copy(currentWeight = maxWeightedLoad))
            } ?: matrixDao.insert(
                ProgressionMatrixEntity(
                    exerciseId = exerciseId,
                    startWeight = 0f,
                    targetWeight = 0f,
                    currentWeight = maxWeightedLoad
                )
            )
        }
    }

    override suspend fun getReferenceForExercise(id: Int): ReferenceMatrix? {
        return matrixDao.getReferenceById(id)?.toDomain()
    }

    override suspend fun getReferenceForExercise(name: String): ReferenceMatrix? {
        return matrixDao.getReferenceByName(name)?.toDomain()
    }

    override fun getAllReferences(): Flow<List<ReferenceMatrix>> {
        return matrixDao.getAllReferences().map { references ->
            references.map { it.toDomain() }
        }
    }

    override suspend fun completeCycle(exerciseId: Int) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        val nextCycles = existing.completedCycles + 1
        
        matrixDao.update(existing.copy(
            completedCycles = nextCycles,
            isPromotionPending = true
        ))
    }

    override suspend fun promoteRank(exerciseId: Int) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        val nextRank = RankProgressionPolicy.nextRank(existing.currentRank)
        matrixDao.update(existing.copy(
            isPromotionPending = false,
            currentRank = nextRank
        ))
    }

    override suspend fun updateRank(exerciseId: Int, newRank: Rank) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        if (existing.currentRank != newRank) {
            matrixDao.update(existing.copy(currentRank = newRank))
        }
    }

    override suspend fun setPromotionPending(exerciseId: Int, pending: Boolean) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        matrixDao.update(existing.copy(isPromotionPending = pending))
    }

    override suspend fun getExerciseIdsByCategory(category: ExerciseCategory): List<Int> {
        return matrixDao.getExerciseIdsByCategory(category)
    }

    override suspend fun updateTarget(
        exerciseId: Int,
        weight: Double,
        sets: Int,
        reps: String,
        aiFeedback: String?,
        timestamp: Long
    ) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        matrixDao.update(existing.copy(
            nextRecommendedWeight = weight,
            nextRecommendedSets = sets,
            nextRecommendedReps = reps,
            lastAiFeedback = aiFeedback,
            lastAnalyzedTimestamp = timestamp
        ))
    }
}

private fun ProgressionMatrixEntity.toDomain(
    exerciseName: String,
    exerciseNameUk: String?,
    matrixWeeks: Int
): ProgressionMatrixEntry {
    val weeklyStep = if (targetWeight > 0f && matrixWeeks > 0)
        (targetWeight - startWeight) / matrixWeeks else 0f

    val range    = if (targetWeight > 0f) targetWeight - startWeight else 1f
    val progress = if (range > 0f)
        ((currentWeight - startWeight) / range).coerceIn(0f, 1f) else 0f

    return ProgressionMatrixEntry(
        id                = this.exerciseId,
        exerciseId        = this.exerciseId,
        exerciseName      = exerciseName,
        exerciseNameUk    = exerciseNameUk,
        startWeight       = startWeight,
        targetWeight      = targetWeight,
        currentWeight     = currentWeight,
        targetWeightNote  = targetWeightNote,
        weeklyStep        = weeklyStep,
        progressPercent   = progress,
        currentRank       = currentRank,
        completedCycles   = completedCycles,
        isPromotionPending = isPromotionPending,
        nextRecommendedWeight = nextRecommendedWeight,
        nextRecommendedSets = nextRecommendedSets,
        nextRecommendedReps = nextRecommendedReps,
        lastAiFeedback = lastAiFeedback,
        lastAnalyzedTimestamp = lastAnalyzedTimestamp
    )
}

private data class LoggedSetInput(
    val weight: Double,
    val reps: Int
)

private const val TECHNICAL_BODYWEIGHT_LOAD = 1.0

private fun ActiveSetInput.toValidLoggedSet(): LoggedSetInput? {
    val parsedWeight = weight.replace(',', '.').toDoubleOrNull()
        ?.takeIf { it > 0.0 }
        ?: return null
    val parsedReps = reps.toIntOrNull()
        ?.takeIf { it > 0 }
        ?: return null

    return LoggedSetInput(
        weight = parsedWeight,
        reps = parsedReps
    )
}

private fun LoggedSetInput.toEntity(
    sessionId: Long,
    exerciseId: Int,
    userFeedback: String?
) = ExerciseSetLogEntity(
    sessionId = sessionId,
    exerciseId = exerciseId,
    weight = weight,
    reps = reps,
    isCompleted = true,
    userFeedback = userFeedback
)

private fun ReferenceMatrixEntity.toDomain() = ReferenceMatrix(
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    weightType = weightType.toDomain(),
    progressionStep = progressionStep,
    milestones = milestones,
    repsMilestones = repsMilestones
)

private fun WeightType.toDomain() = when (this) {
    WeightType.ABSOLUTE -> ExerciseWeightType.ABSOLUTE
    WeightType.BODY_WEIGHT -> ExerciseWeightType.BODY_WEIGHT
    WeightType.ADDED_WEIGHT -> ExerciseWeightType.ADDED_WEIGHT
}
