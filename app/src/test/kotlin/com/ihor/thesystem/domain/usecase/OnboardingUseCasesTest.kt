package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AppStartDestination
import com.ihor.thesystem.domain.model.BodyWeightLog
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.OnboardingAnswers
import com.ihor.thesystem.domain.model.OnboardingCyclePreset
import com.ihor.thesystem.domain.model.OnboardingExperience
import com.ihor.thesystem.domain.model.OnboardingGoal
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import com.ihor.thesystem.domain.repository.OnboardingRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingUseCasesTest {

    @Test
    fun `first launch route decision opens onboarding`() = runTest {
        val repository = FakeOnboardingRepository(completed = false)
        val destination = ObserveAppStartDestinationUseCase(repository).invoke().first()

        assertEquals(AppStartDestination.ONBOARDING, destination)
    }

    @Test
    fun `completed onboarding does not show again`() = runTest {
        val repository = FakeOnboardingRepository(completed = true)
        val destination = ObserveAppStartDestinationUseCase(repository).invoke().first()

        assertEquals(AppStartDestination.STATUS, destination)
    }

    @Test
    fun `completion creates default player equipment and config`() = runTest {
        val onboardingRepository = FakeOnboardingRepository(completed = false)
        val playerRepository = FakePlayerRepository()
        val configRepository = FakeSystemConfigRepository()
        val equipmentRepository = FakeEquipmentProfileRepository()
        val useCase = CompleteOnboardingUseCase(
            onboardingRepository = onboardingRepository,
            playerRepository = playerRepository,
            systemConfigRepository = configRepository,
            equipmentProfileRepository = equipmentRepository,
            clock = FixedClock(LocalDate.of(2026, 7, 24))
        )

        val result = useCase(
            OnboardingAnswers(
                name = " Ігор ",
                goal = OnboardingGoal.BUILD_STRENGTH,
                equipment = setOf(EquipmentType.DUMBBELL, EquipmentType.BENCH),
                experience = OnboardingExperience.INTERMEDIATE,
                cyclePreset = OnboardingCyclePreset.FIVE_DAY
            )
        )

        assertTrue(result is Result.Success)
        assertEquals(true, onboardingRepository.completed.value)

        val player = playerRepository.player.value
        assertNotNull(player)
        assertEquals("Ігор", player?.name)
        assertEquals(3, player?.level)
        assertEquals(2_000, player?.xpTotal)
        assertEquals(1, player?.currentCycleDay)

        val profile = equipmentRepository.profile.value
        assertEquals(setOf(EquipmentType.BODY_ONLY, EquipmentType.DUMBBELL, EquipmentType.BENCH), profile.availableEquipment)
        assertEquals(24f, profile.dumbbellMaxKg)
        assertEquals(true, profile.benchAvailable)

        val config = configRepository.config.value
        assertNotNull(config)
        assertEquals(5, config?.targetSets)
        assertEquals(5, config?.targetReps)
        assertEquals(5, config?.cycleDaysPerMicrocycle)
        assertEquals(4, config?.microCyclesPerMonth)
        assertEquals(LocalDate.of(2026, 7, 24).toEpochDay(), config?.cycleAnchorDateTimestamp)
        assertEquals(1, config?.cycleAnchorDay)
        assertEquals(true, config?.needsDailyInit)
    }
}

private class FakeOnboardingRepository(
    completed: Boolean
) : OnboardingRepository {
    val completed = MutableStateFlow(completed)

    override fun isOnboardingCompleted(): Flow<Boolean> = completed

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        this.completed.value = completed
    }
}

private class FakePlayerRepository : PlayerRepository {
    val player = MutableStateFlow<Player?>(null)

    override fun getPlayer(): Flow<Player?> = player
    override suspend fun getPlayerSnapshot(): Player? = player.value
    override fun getLatestWeight(): Flow<Float?> = MutableStateFlow(null)
    override fun getWeightHistory(limit: Int): Flow<List<BodyWeightLog>> = MutableStateFlow(emptyList())
    override suspend fun updatePlayer(player: Player): Result<Unit, DataError.Local> {
        this.player.value = player
        return Result.Success(Unit)
    }
    override suspend fun logWeight(weight: Float): Result<Unit, DataError.Local> = Result.Success(Unit)
    override suspend fun updateHeight(height: Float): Result<Unit, DataError.Local> = Result.Success(Unit)
    override suspend fun updateAge(age: Int): Result<Unit, DataError.Local> = Result.Success(Unit)
    override suspend fun updateCurrentCycleDay(day: Int): Result<Unit, DataError.Local> {
        player.value = player.value?.copy(currentCycleDay = day)
        return Result.Success(Unit)
    }
    override suspend fun getWeightByDate(dateMillis: Long): Result<Float?, DataError.Local> = Result.Success(null)
    override suspend fun getWeightAtOrBefore(timestamp: Long): Result<Float?, DataError.Local> = Result.Success(null)
}

private class FakeSystemConfigRepository : SystemConfigRepository {
    val config = MutableStateFlow<SystemConfig?>(null)

    override fun getConfigFlow(): Flow<SystemConfig?> = config
    override suspend fun updateConfig(config: SystemConfig) {
        this.config.value = config
    }
    override suspend fun setNeedsDailyInit(needed: Boolean) {
        config.value = (config.value ?: SystemConfig()).copy(needsDailyInit = needed)
    }
    override suspend fun saveLastInitDate(epochDay: Long) {
        config.value = (config.value ?: SystemConfig()).copy(lastInitEpochDay = epochDay)
    }
}

private class FakeEquipmentProfileRepository : EquipmentProfileRepository {
    val profile = MutableStateFlow(EquipmentProfile())

    override fun getProfile(): Flow<EquipmentProfile> = profile
    override suspend fun getProfileSnapshot(): EquipmentProfile = profile.value
    override suspend fun saveProfile(profile: EquipmentProfile) {
        this.profile.value = profile
    }
}

private class FixedClock(
    private val date: LocalDate
) : AppClock {
    override fun now(): Long =
        date.atStartOfDay(zoneId()).toInstant().toEpochMilli()

    override fun zoneId(): ZoneId = ZoneId.of("Europe/Kyiv")
}
