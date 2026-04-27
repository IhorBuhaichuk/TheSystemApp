package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchExercisesUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(
        query: String?,
        muscles: List<String> = emptyList(),
        equipment: List<String> = emptyList(),
        levels: List<String> = emptyList(),
        mechanics: List<String> = emptyList(),
        forces: List<String> = emptyList()
    ): Flow<List<ExerciseDetails>> {
        return repository.searchExercises(query, muscles, equipment, levels, mechanics, forces)
    }
}
