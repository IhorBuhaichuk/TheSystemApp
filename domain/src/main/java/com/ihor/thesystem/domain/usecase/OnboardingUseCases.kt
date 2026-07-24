package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AppErrorType
import com.ihor.thesystem.domain.model.AppStartDestination
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.OnboardingAnswers
import com.ihor.thesystem.domain.model.OnboardingExperience
import com.ihor.thesystem.domain.model.OnboardingGoal
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerProfileValidationPolicy
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import com.ihor.thesystem.domain.repository.OnboardingRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAppStartDestinationUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) {
    operator fun invoke(): Flow<AppStartDestination> =
        onboardingRepository.isOnboardingCompleted().map { completed ->
            if (completed) AppStartDestination.STATUS else AppStartDestination.ONBOARDING
        }
}

class CompleteOnboardingUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val playerRepository: PlayerRepository,
    private val systemConfigRepository: SystemConfigRepository,
    private val equipmentProfileRepository: EquipmentProfileRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(answers: OnboardingAnswers): Result<Unit, DomainError> {
        val name = answers.name.trim()
        PlayerProfileValidationPolicy.validateName(name)?.let { error ->
            return Result.Error(error)
        }

        return try {
            when (val playerResult = playerRepository.updatePlayer(answers.toPlayer(name))) {
                is Result.Error -> return Result.Error(playerResult.error)
                is Result.Success -> Unit
            }

            equipmentProfileRepository.saveProfile(answers.toEquipmentProfile())
            systemConfigRepository.updateConfig(answers.toSystemConfig())
            onboardingRepository.setOnboardingCompleted(true)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(AppErrorType.Message(e.message ?: DataError.Local.UNKNOWN.message.orEmpty()))
        }
    }

    private suspend fun OnboardingAnswers.toPlayer(name: String): Player {
        val existing = playerRepository.getPlayerSnapshot()
        val level = experience.startLevel()
        val xpTotal = (level - 1).coerceAtLeast(0) * XP_PER_LEVEL

        return (existing ?: Player(
            id = 1,
            name = name,
            level = level,
            playerClass = PlayerRank.NOVICE,
            height = 0f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = 1,
            xpTotal = xpTotal
        )).copy(
            name = name,
            level = level,
            playerClass = PlayerRank.NOVICE,
            currentMonth = existing?.currentMonth ?: 1,
            currentWeek = existing?.currentWeek ?: 1,
            currentCycleDay = 1,
            xpTotal = xpTotal,
            xpThisWeek = 0
        )
    }

    private suspend fun OnboardingAnswers.toSystemConfig(): SystemConfig {
        val todayEpochDay = todayEpochDay()
        val current = systemConfigRepository.getConfigFlow().firstOrNull() ?: SystemConfig()
        val target = goal.trainingTarget()

        return current.copy(
            targetSets = target.sets,
            targetReps = target.reps,
            cycleAnchorDateTimestamp = todayEpochDay,
            cycleAnchorDay = 1,
            cycleDaysPerMicrocycle = cyclePreset.cycleDays,
            microCyclesPerMonth = cyclePreset.microCyclesPerMonth,
            needsDailyInit = true
        )
    }

    private fun OnboardingAnswers.toEquipmentProfile(): EquipmentProfile {
        val selected = (equipment + EquipmentType.BODY_ONLY).ifEmpty { setOf(EquipmentType.BODY_ONLY) }
        return EquipmentProfile(
            trainsAtGym = selected.any { it in GYM_EQUIPMENT },
            availableEquipment = selected,
            dumbbellMaxKg = if (EquipmentType.DUMBBELL in selected) DEFAULT_DUMBBELL_MAX_KG else null,
            barbellAvailable = EquipmentType.BARBELL in selected,
            benchAvailable = EquipmentType.BENCH in selected,
            pullUpBarAvailable = EquipmentType.PULL_UP_BAR in selected,
            dipBarsAvailable = EquipmentType.DIP_BARS in selected,
            bandsAvailable = EquipmentType.BANDS in selected,
            machinesAvailable = selected.any { it == EquipmentType.MACHINE || it == EquipmentType.CABLE }
        )
    }

    private fun todayEpochDay(): Long =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()

    private data class TrainingTarget(val sets: Int, val reps: Int)

    private fun OnboardingGoal.trainingTarget(): TrainingTarget =
        when (this) {
            OnboardingGoal.BUILD_STRENGTH -> TrainingTarget(sets = 5, reps = 5)
            OnboardingGoal.BUILD_MUSCLE -> TrainingTarget(sets = 4, reps = 10)
            OnboardingGoal.LOSE_WEIGHT -> TrainingTarget(sets = 3, reps = 15)
            OnboardingGoal.BUILD_HABIT -> TrainingTarget(sets = 3, reps = 8)
        }

    private fun OnboardingExperience.startLevel(): Int =
        when (this) {
            OnboardingExperience.BEGINNER -> 1
            OnboardingExperience.RETURNING -> 2
            OnboardingExperience.INTERMEDIATE -> 3
        }

    private companion object {
        const val XP_PER_LEVEL = 1000
        const val DEFAULT_DUMBBELL_MAX_KG = 24f

        val GYM_EQUIPMENT = setOf(
            EquipmentType.BARBELL,
            EquipmentType.MACHINE,
            EquipmentType.CABLE,
            EquipmentType.BENCH,
            EquipmentType.PULL_UP_BAR,
            EquipmentType.DIP_BARS
        )
    }
}
