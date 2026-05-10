package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AnnualProgressionAdjustment
import com.ihor.thesystem.domain.model.AnnualProgressionExercisePlan
import com.ihor.thesystem.domain.model.AnnualProgressionPlan
import com.ihor.thesystem.domain.model.AnnualProgressionPlanStatus
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import java.util.Locale
import javax.inject.Inject

class SaveAnnualProgressionPlanUseCase @Inject constructor(
    private val progressionMatrixRepository: ProgressionMatrixRepository
) {
    suspend operator fun invoke(plan: AnnualProgressionPlan) {
        plan.exercises
            .filter { it.status == AnnualProgressionPlanStatus.Ready }
            .forEach { exercisePlan ->
                progressionMatrixRepository.saveAnnualProgressionPlan(
                    exerciseId = exercisePlan.exerciseId,
                    startWeight = exercisePlan.currentWeight.toFloat(),
                    targetWeight = exercisePlan.targetWeight.toFloat(),
                    targetWeightNote = exercisePlan.toPlanNote(plan)
                )
            }
    }

    private fun AnnualProgressionExercisePlan.toPlanNote(plan: AnnualProgressionPlan): String {
        val monthly = monthlyTargets.joinToString(separator = ";") { target ->
            "M${target.monthIndex}:${target.weight.formatWeight()}:${target.adjustment.code()}"
        }
        return "annual_step_loading|start=${plan.startDate}|adaptationEnd=${plan.adaptationEndsOn}|step=${inventoryStep.formatWeight()}|$monthly"
    }

    private fun AnnualProgressionAdjustment.code(): String =
        when (this) {
            AnnualProgressionAdjustment.Baseline -> "base"
            AnnualProgressionAdjustment.StandardStep -> "step"
            AnnualProgressionAdjustment.Plateau -> "plateau"
            AnnualProgressionAdjustment.ForcedJump -> "jump"
        }

    private fun Double.formatWeight(): String =
        if (this % 1.0 == 0.0) {
            toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
        }
}
