package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.MuscleGroup
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import com.ihor.thesystem.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EquipmentProfileUseCasesTest {

    @Test
    fun `raw equipment strings map to equipment types`() {
        assertEquals(setOf(EquipmentType.BODY_ONLY), EquipmentType.fromRawEquipment("body only"))
        assertEquals(setOf(EquipmentType.DUMBBELL), EquipmentType.fromRawEquipment("dumbbell"))
        assertEquals(setOf(EquipmentType.BARBELL), EquipmentType.fromRawEquipment("barbell"))
        assertEquals(setOf(EquipmentType.MACHINE), EquipmentType.fromRawEquipment("machine"))
        assertEquals(setOf(EquipmentType.CABLE), EquipmentType.fromRawEquipment("cable"))
        assertEquals(setOf(EquipmentType.BANDS), EquipmentType.fromRawEquipment("bands"))
    }

    @Test
    fun `filter removes exercises that require unavailable equipment`() {
        val profile = EquipmentProfile(
            availableEquipment = setOf(EquipmentType.BODY_ONLY),
            barbellAvailable = false,
            benchAvailable = false
        )
        val useCase = FilterExercisesByEquipmentUseCase()

        val result = useCase(
            exercises = listOf(
                exercise(id = 1, name = "Push-up", equipment = "body only"),
                exercise(id = 2, name = "Barbell Bench Press", equipment = "barbell"),
                exercise(id = 3, name = "Band Pull Apart", equipment = "bands")
            ),
            profile = profile
        )

        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun `substitutions prioritize same muscle group and available equipment`() = runTest {
        val workoutRepository: WorkoutRepository = mockk()
        val equipmentProfileRepository: EquipmentProfileRepository = mockk()
        val source = exercise(
            id = 1,
            name = "Barbell Bench Press",
            equipment = "barbell",
            muscleGroups = listOf(MuscleGroup.CHEST)
        )
        val availableChest = listOf(
            exercise(id = 2, name = "Push-up", equipment = "body only", muscleGroups = listOf(MuscleGroup.CHEST)),
            exercise(id = 3, name = "Knee Push-up", equipment = "body only", muscleGroups = listOf(MuscleGroup.CHEST)),
            exercise(id = 4, name = "Chest Stretch Hold", equipment = "body only", muscleGroups = listOf(MuscleGroup.CHEST))
        )
        val unavailable = exercise(
            id = 5,
            name = "Dumbbell Fly",
            equipment = "dumbbell",
            muscleGroups = listOf(MuscleGroup.CHEST)
        )
        val unrelated = exercise(
            id = 6,
            name = "Bodyweight Squat",
            equipment = "body only",
            muscleGroups = listOf(MuscleGroup.QUADS)
        )

        coEvery { workoutRepository.getAllExercisesSync() } returns listOf(source) + availableChest + unavailable + unrelated
        coEvery { equipmentProfileRepository.getProfileSnapshot() } returns EquipmentProfile(
            availableEquipment = setOf(EquipmentType.BODY_ONLY)
        )
        every { equipmentProfileRepository.getProfile() } returns flowOf(EquipmentProfile())

        val result = FindExerciseSubstitutionsUseCase(
            workoutRepository = workoutRepository,
            equipmentProfileRepository = equipmentProfileRepository
        )(source.id)

        assertTrue(result.size in 1..5)
        assertTrue(result.all { it.equipment == "body only" })
        assertTrue(result.all { MuscleGroup.CHEST in it.muscleGroups })
        assertFalse(result.any { it.id == unavailable.id })
        assertFalse(result.any { it.id == unrelated.id })
    }

    @Test
    fun `substitutions prefer explicit core metadata replacements`() = runTest {
        val workoutRepository: WorkoutRepository = mockk()
        val equipmentProfileRepository: EquipmentProfileRepository = mockk()
        val source = exercise(
            id = 1,
            name = "Barbell Bench Press",
            equipment = "barbell",
            muscleGroups = listOf(MuscleGroup.CHEST),
            externalId = "Barbell_Bench_Press",
            substitutionExternalIds = listOf("Push_Up")
        )
        val explicit = exercise(
            id = 2,
            name = "Push-up",
            equipment = "body only",
            muscleGroups = listOf(MuscleGroup.CHEST),
            externalId = "Push_Up"
        )
        val generic = exercise(
            id = 3,
            name = "Chest Press",
            equipment = "body only",
            muscleGroups = listOf(MuscleGroup.CHEST),
            externalId = "Chest_Press"
        )

        coEvery { workoutRepository.getAllExercisesSync() } returns listOf(source, generic, explicit)
        coEvery { equipmentProfileRepository.getProfileSnapshot() } returns EquipmentProfile(
            availableEquipment = setOf(EquipmentType.BODY_ONLY)
        )
        every { equipmentProfileRepository.getProfile() } returns flowOf(EquipmentProfile())

        val result = FindExerciseSubstitutionsUseCase(
            workoutRepository = workoutRepository,
            equipmentProfileRepository = equipmentProfileRepository
        )(source.id)

        assertEquals(explicit.id, result.first().id)
    }

    private fun exercise(
        id: Int,
        name: String,
        equipment: String?,
        muscleGroups: List<MuscleGroup> = listOf(MuscleGroup.CHEST),
        externalId: String? = null,
        substitutionExternalIds: List<String> = emptyList()
    ): ExerciseDetails =
        ExerciseDetails(
            id = id,
            name = name,
            category = ExerciseCategory.STRENGTH,
            muscleGroups = muscleGroups,
            equipment = equipment,
            externalId = externalId,
            substitutionExternalIds = substitutionExternalIds
        )
}
