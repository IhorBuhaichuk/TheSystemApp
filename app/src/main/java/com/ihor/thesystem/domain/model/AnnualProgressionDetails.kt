package com.ihor.thesystem.domain.model

import java.time.LocalDate

data class AnnualProgressionDetailsData(
    val exercises: List<AnnualProgressionExerciseDetails> = emptyList()
)

data class AnnualProgressionExerciseDetails(
    val exerciseId: Int,
    val exerciseName: String,
    val startDate: LocalDate,
    val adaptationEndDate: LocalDate?,
    val inventoryStep: Double?,
    val monthlyProgress: List<AnnualProgressionMonthlyProgress>,
    val currentStatus: AnnualProgressionDetailStatus,
    val recommendation: String?
)

data class AnnualProgressionMonthlyProgress(
    val monthIndex: Int,
    val planWeight: Double,
    val actualWeight: Double?,
    val status: AnnualProgressionDetailStatus
)

enum class AnnualProgressionDetailStatus {
    OnPlan,
    SlightlyBelow,
    AbovePlan,
    NoFact
}
