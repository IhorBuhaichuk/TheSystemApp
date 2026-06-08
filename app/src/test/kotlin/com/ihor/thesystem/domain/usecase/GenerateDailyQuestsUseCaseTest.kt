package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseRecommendation
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.SystemWorkoutTemplateType
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import com.ihor.thesystem.domain.repository.WorkoutRepository
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GenerateDailyQuestsUseCaseTest {

    private val questRepository: QuestRepository = mockk(relaxed = true)
    private val scheduleRepository: ScheduleRepository = mockk()
    private val configRepository: SystemConfigRepository = mockk()
    private val playerRepository: PlayerRepository = mockk()
    private val matrixRepository: ProgressionMatrixRepository = mockk()
    private val calculateRecommendation: CalculateRecommendedSetUseCase = mockk()
    private val decideTodayWorkout: DecideTodayWorkoutUseCase = mockk()
    private val equipmentProfileRepository: EquipmentProfileRepository = mockk()
    private val workoutRepository: WorkoutRepository = mockk()
    private val clock = FixedClock(TODAY)

    private var createdTitle: String? = null
    private var createdExercises: List<ExerciseRecommendation>? = null
    private var createdPromotionExerciseId: Int? = null
    private var createdPromotionTitle: String? = null
    private var createdPromotionDescription: String? = null
    private var createdPromotionTargetWeight: Double? = null
    private var createdPromotionTargetReps: Int? = null

    @Test
    fun `no excuse decision creates bodyweight protocol main quest`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            decision = decision(TodayTrainingDecisionType.NO_EXCUSE)
        )

        useCase()()

        assertEquals(SystemWorkoutTemplateType.NO_EXCUSE.questTitle, createdTitle)
        val exercises = requireNotNull(createdExercises)
        assertTrue(exercises.size in 2..4)
        assertTrue(exercises.all { it.exerciseId < 0 })
        assertTrue(exercises.all { it.weight == 0.0 })
    }

    @Test
    fun `active recovery decision creates recovery protocol even on rest day`() = runTest {
        arrange(
            schedule = restSchedule(),
            decision = decision(TodayTrainingDecisionType.ACTIVE_RECOVERY)
        )

        useCase()()

        assertEquals(SystemWorkoutTemplateType.ACTIVE_RECOVERY.questTitle, createdTitle)
        val exercises = requireNotNull(createdExercises)
        assertTrue(exercises.isNotEmpty())
        assertTrue(exercises.all { it.exerciseId < 0 })
        assertTrue(exercises.all { it.weight == 0.0 })
    }

    @Test
    fun `deload decision creates reduced targets without raising weight`() = runTest {
        arrange(
            schedule = workoutSchedule(exercises = listOf(exercise())),
            decision = decision(
                type = TodayTrainingDecisionType.DELOAD,
                loadMultiplier = 0.8f,
                volumeMultiplier = 0.6f
            )
        )
        coEvery { calculateRecommendation(EXERCISE_ID, any()) } returns SetRecommendation(
            exerciseId = EXERCISE_ID,
            weight = 100.0,
            sets = 4,
            reps = 10,
            isProgression = false
        )

        useCase()()

        assertEquals(SystemWorkoutTemplateType.DELOAD.questTitle, createdTitle)
        val exercise = requireNotNull(createdExercises).single()
        assertEquals(EXERCISE_ID, exercise.exerciseId)
        assertTrue(exercise.weight <= 100.0)
        assertEquals(80.0, exercise.weight, 0.001)
        assertTrue(exercise.sets <= 4)
    }

    @Test
    fun `unavailable scheduled exercise without substitution does not create main quest`() = runTest {
        val unavailableExercise = exercise()
        arrange(
            schedule = workoutSchedule(exercises = listOf(unavailableExercise)),
            decision = decision(
                type = TodayTrainingDecisionType.STANDARD_TRAINING,
                loadMultiplier = 1f,
                volumeMultiplier = 1f
            ),
            equipmentProfile = EquipmentProfile(
                trainsAtGym = false,
                availableEquipment = setOf(EquipmentType.BODY_ONLY)
            ),
            allExercises = listOf(unavailableExercise)
        )

        useCase()()

        assertNull(createdTitle)
        assertNull(createdExercises)
    }

    @Test
    fun `pending matrix entry creates boss fight quest`() = runTest {
        arrange(
            schedule = restSchedule(),
            decision = decision(TodayTrainingDecisionType.REST),
            matrixEntries = listOf(
                progressionEntry(
                    isPromotionPending = true,
                    exerciseTrackingMode = ExerciseTrackingMode.WEIGHT_REPS.name,
                    targetWeight = 100f,
                    nextRecommendedWeight = 102.5
                )
            )
        )

        useCase()()

        assertEquals(EXERCISE_ID, createdPromotionExerciseId)
        assertTrue(requireNotNull(createdPromotionTitle).startsWith("Контрольний норматив"))
        assertTrue(requireNotNull(createdPromotionDescription).startsWith("Умова:"))
        assertEquals(102.5, requireNotNull(createdPromotionTargetWeight), 0.001)
        assertNull(createdPromotionTargetReps)
    }

    private fun arrange(
        schedule: ScheduleDay,
        decision: TodayTrainingDecision,
        matrixEntries: List<ProgressionMatrixEntry> = emptyList(),
        equipmentProfile: EquipmentProfile = EquipmentProfile(
            trainsAtGym = true,
            availableEquipment = setOf(EquipmentType.BODY_ONLY, EquipmentType.BARBELL, EquipmentType.BENCH),
            barbellAvailable = true,
            benchAvailable = true
        ),
        allExercises: List<ExerciseDetails> = schedule.exercises
    ) {
        createdTitle = null
        createdExercises = null
        createdPromotionExerciseId = null
        createdPromotionTitle = null
        createdPromotionDescription = null
        createdPromotionTargetWeight = null
        createdPromotionTargetReps = null

        every { configRepository.getConfigFlow() } returns flowOf(
            SystemConfig(
                cycleAnchorDateTimestamp = TODAY.toEpochDay(),
                cycleAnchorDay = 1,
                cycleDaysPerMicrocycle = 4
            )
        )
        every { playerRepository.getPlayer() } returns flowOf(null)
        every { scheduleRepository.getScheduleForDay(1) } returns flowOf(schedule)
        every { questRepository.getDailyQuestsForDate(any()) } returns flowOf(emptyList())
        every { matrixRepository.getAllEntries() } returns flowOf(matrixEntries)
        every { questRepository.getActiveQuests() } returns flowOf(emptyList())
        coEvery { equipmentProfileRepository.getProfileSnapshot() } returns equipmentProfile
        coEvery { workoutRepository.getAllExercisesSync() } returns allExercises
        coEvery { decideTodayWorkout(any()) } returns decision
        coEvery { questRepository.createMainQuest(any(), any(), any()) } answers {
            createdTitle = firstArg()
            createdExercises = secondArg()
            Unit
        }
        coEvery { questRepository.createPromotionQuest(any(), any(), any(), any(), any(), any()) } answers {
            createdPromotionExerciseId = firstArg()
            createdPromotionTitle = secondArg()
            createdPromotionDescription = thirdArg()
            createdPromotionTargetWeight = arg(3)
            createdPromotionTargetReps = arg(4)
            Unit
        }
    }

    private fun useCase(): GenerateDailyQuestsUseCase =
        GenerateDailyQuestsUseCase(
            questRepo = questRepository,
            scheduleRepo = scheduleRepository,
            configRepo = configRepository,
            playerRepo = playerRepository,
            matrixRepo = matrixRepository,
            calculateRecommendation = calculateRecommendation,
            adjustRecommendation = AdjustWorkoutRecommendationUseCase(),
            decideTodayWorkout = decideTodayWorkout,
            equipmentProfileRepository = equipmentProfileRepository,
            findExerciseSubstitutions = FindExerciseSubstitutionsUseCase(
                workoutRepository = workoutRepository,
                equipmentProfileRepository = equipmentProfileRepository
            ),
            resolveTrainingCycleDay = ResolveTrainingCycleDayUseCase(
                calculateCycleDay = CalculateCycleDayForDateUseCase(),
                clock = clock
            ),
            clock = clock
        )

    private fun decision(
        type: TodayTrainingDecisionType,
        loadMultiplier: Float = 0f,
        volumeMultiplier: Float = 0.45f
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = TODAY.toEpochDay(),
            cycleDay = 1,
            workoutName = "Upper Day",
            readinessScore = 58,
            readinessLevel = ReadinessLevel.REDUCED,
            recoveryDebt = RecoveryDebt(
                value = 20,
                level = RecoveryDebtLevel.LOW,
                reasons = emptyList()
            ),
            decisionType = type,
            loadMultiplier = loadMultiplier,
            volumeMultiplier = volumeMultiplier,
            reason = "Test decision",
            warnings = emptyList(),
            selectedWorkoutTemplateId = 10,
            isTrainingAllowed = type != TodayTrainingDecisionType.ACTIVE_RECOVERY
        )

    private fun workoutSchedule(
        exercises: List<ExerciseDetails> = emptyList()
    ): ScheduleDay =
        ScheduleDay(
            id = 1,
            cycleDay = 1,
            workoutTemplateId = 10,
            workoutTemplateName = "Upper Day",
            dailyTaskNames = emptyList(),
            exercises = exercises
        )

    private fun restSchedule(): ScheduleDay =
        ScheduleDay(
            id = 1,
            cycleDay = 1,
            workoutTemplateId = null,
            workoutTemplateName = null,
            dailyTaskNames = emptyList(),
            exercises = emptyList()
        )

    private fun exercise(): ExerciseDetails =
        ExerciseDetails(
            id = EXERCISE_ID,
            name = "Bench Press",
            equipment = "barbell"
        )

    private fun progressionEntry(
        isPromotionPending: Boolean,
        exerciseTrackingMode: String?,
        targetWeight: Float = 0f,
        nextRecommendedWeight: Double? = null,
        nextRecommendedReps: String? = null
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = EXERCISE_ID,
            exerciseName = "Bench Press",
            exerciseNameUk = null,
            exerciseTrackingMode = exerciseTrackingMode,
            startWeight = 80f,
            targetWeight = targetWeight,
            currentWeight = 95f,
            targetWeightNote = null,
            weeklyStep = 2.5f,
            progressPercent = 100f,
            currentRank = Rank.E,
            isPromotionPending = isPromotionPending,
            nextRecommendedWeight = nextRecommendedWeight,
            nextRecommendedReps = nextRecommendedReps
        )

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private companion object {
        const val EXERCISE_ID = 101
        val TODAY: LocalDate = LocalDate.of(2026, 6, 7)
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
    }
}
