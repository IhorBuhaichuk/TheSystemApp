package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.MatrixRow
import com.ihor.thesystem.domain.model.Rank

interface AnnualMatrixRepository {
    fun getMatrix(): List<MatrixRow>
    fun getExerciseRankById(exerciseId: Int, current1RM: Double, playerWeight: Double): Rank
}
