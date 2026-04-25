package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.model.AnnualMatrixProvider
import com.ihor.thesystem.domain.model.MatrixRow
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.AnnualMatrixRepository
import javax.inject.Inject

class AnnualMatrixRepositoryImpl @Inject constructor() : AnnualMatrixRepository {
    override fun getMatrix(): List<MatrixRow> = AnnualMatrixProvider.getMatrix()
    
    override fun getExerciseRankById(exerciseId: Int, current1RM: Double, playerWeight: Double): Rank {
        return AnnualMatrixProvider.getExerciseRankById(exerciseId, current1RM, playerWeight)
    }
}
