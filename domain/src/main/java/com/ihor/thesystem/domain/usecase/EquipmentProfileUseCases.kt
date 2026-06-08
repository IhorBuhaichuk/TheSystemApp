package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import com.ihor.thesystem.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEquipmentProfileUseCase @Inject constructor(
    private val repository: EquipmentProfileRepository
) {
    operator fun invoke(): Flow<EquipmentProfile> =
        repository.getProfile()
}

class SaveEquipmentProfileUseCase @Inject constructor(
    private val repository: EquipmentProfileRepository
) {
    suspend operator fun invoke(profile: EquipmentProfile) {
        repository.saveProfile(profile)
    }
}

class FilterExercisesByEquipmentUseCase @Inject constructor() {
    operator fun invoke(
        exercises: List<ExerciseDetails>,
        profile: EquipmentProfile
    ): List<ExerciseDetails> =
        exercises.filter { exercise -> profile.allows(exercise) }
}

class FindExerciseSubstitutionsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val equipmentProfileRepository: EquipmentProfileRepository
) {
    suspend operator fun invoke(exerciseId: Int): List<ExerciseDetails> {
        val exercises = workoutRepository.getAllExercisesSync()
        val source = exercises.firstOrNull { it.id == exerciseId } ?: return emptyList()
        val profile = equipmentProfileRepository.getProfileSnapshot()
        val sourceTrackingMode = ExerciseTrackingModeResolver.resolve(source)
        val sourceEquipment = EquipmentType.fromRawEquipment(source.equipment)
        val explicitSubstitutions = source.substitutionExternalIds.toSet()

        val explicitMatches = exercises
            .asSequence()
            .filter { candidate -> candidate.id != source.id }
            .filter { candidate -> candidate.externalId in explicitSubstitutions }
            .filter { candidate -> profile.allows(candidate) }
            .toList()

        val scoredMatches = exercises
            .asSequence()
            .filter { candidate -> candidate.id != source.id }
            .filter { candidate -> candidate.externalId !in explicitSubstitutions }
            .filter { candidate -> profile.allows(candidate) }
            .filter { candidate -> candidate.muscleGroups.intersect(source.muscleGroups.toSet()).isNotEmpty() }
            .map { candidate ->
                candidate to candidate.substitutionScore(
                    source = source,
                    sourceTrackingMode = sourceTrackingMode,
                    sourceEquipment = sourceEquipment
                )
            }
            .filter { (_, score) -> score > 0 }
            .sortedByDescending { (_, score) -> score }
            .map { (candidate, _) -> candidate }
            .toList()

        return (explicitMatches + scoredMatches)
            .distinctBy { it.id }
            .take(MAX_SUBSTITUTIONS)
    }

    private fun ExerciseDetails.substitutionScore(
        source: ExerciseDetails,
        sourceTrackingMode: com.ihor.thesystem.domain.model.ExerciseTrackingMode,
        sourceEquipment: Set<EquipmentType>
    ): Int {
        val candidateTrackingMode = ExerciseTrackingModeResolver.resolve(this)
        val candidateEquipment = EquipmentType.fromRawEquipment(equipment)
        val muscleOverlap = muscleGroups.intersect(source.muscleGroups.toSet()).size

        return (muscleOverlap * MUSCLE_MATCH_SCORE) +
            (if (muscleGroups.firstOrNull() == source.muscleGroups.firstOrNull()) PRIMARY_MUSCLE_SCORE else 0) +
            (if (category == source.category) CATEGORY_SCORE else 0) +
            (if (candidateTrackingMode == sourceTrackingMode) TRACKING_MODE_SCORE else 0) +
            (if (candidateEquipment.intersect(sourceEquipment).isNotEmpty()) EQUIPMENT_SIMILARITY_SCORE else 0) +
            (if (mechanic != null && mechanic == source.mechanic) MECHANIC_SCORE else 0)
    }

    private companion object {
        const val MAX_SUBSTITUTIONS = 5
        const val MUSCLE_MATCH_SCORE = 20
        const val PRIMARY_MUSCLE_SCORE = 12
        const val CATEGORY_SCORE = 8
        const val TRACKING_MODE_SCORE = 6
        const val EQUIPMENT_SIMILARITY_SCORE = 4
        const val MECHANIC_SCORE = 3
    }
}
