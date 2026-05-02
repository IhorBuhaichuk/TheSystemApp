package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AnnualProgressionAdjustment
import com.ihor.thesystem.domain.model.AnnualProgressionExercisePlan
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyTarget
import com.ihor.thesystem.domain.model.AnnualProgressionPlan
import com.ihor.thesystem.domain.model.AnnualProgressionPlanInput
import com.ihor.thesystem.domain.model.AnnualProgressionPlanStatus
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class GenerateAnnualProgressionPlanUseCase @Inject constructor() {
    operator fun invoke(
        startDate: LocalDate,
        inputs: List<AnnualProgressionPlanInput>
    ): AnnualProgressionPlan {
        val adaptationEndsOn = startDate.plusDays(ADAPTATION_DAYS.toLong())
        return AnnualProgressionPlan(
            startDate = startDate,
            adaptationEndsOn = adaptationEndsOn,
            exercises = inputs.map { input ->
                generateExercisePlan(input)
            }
        )
    }

    private fun generateExercisePlan(input: AnnualProgressionPlanInput): AnnualProgressionExercisePlan {
        val delta = input.targetWeight - input.currentWeight
        if (input.currentWeight <= 0.0) {
            return emptyPlan(input, AnnualProgressionPlanStatus.NeedsCurrentWeight)
        }
        if (delta <= 0.0 || input.inventoryStep <= 0.0) {
            return emptyPlan(input, AnnualProgressionPlanStatus.NeedsTargetWeight)
        }

        val idealMonthlyStep = delta / MONTHS_IN_PLAN
        val monthlyTargets = (0..MONTHS_IN_PLAN).map { month ->
            val weight = when (month) {
                0 -> input.currentWeight
                MONTHS_IN_PLAN -> input.targetWeight
                else -> quantizeFromOrigin(
                    value = input.currentWeight + idealMonthlyStep * month,
                    origin = input.currentWeight,
                    step = input.inventoryStep
                ).coerceIn(input.currentWeight, input.targetWeight)
            }
            val previous = if (month == 0) null else {
                val previousWeight = when (month - 1) {
                    0 -> input.currentWeight
                    else -> quantizeFromOrigin(
                        value = input.currentWeight + idealMonthlyStep * (month - 1),
                        origin = input.currentWeight,
                        step = input.inventoryStep
                    ).coerceIn(input.currentWeight, input.targetWeight)
                }
                if (month == MONTHS_IN_PLAN) {
                    quantizeFromOrigin(
                        value = input.currentWeight + idealMonthlyStep * (month - 1),
                        origin = input.currentWeight,
                        step = input.inventoryStep
                    ).coerceIn(input.currentWeight, input.targetWeight)
                } else {
                    previousWeight
                }
            }
            AnnualProgressionMonthlyTarget(
                monthIndex = month,
                weight = weight,
                adjustment = resolveAdjustment(
                    month = month,
                    previousWeight = previous,
                    currentWeight = weight,
                    inventoryStep = input.inventoryStep
                )
            )
        }

        return AnnualProgressionExercisePlan(
            exerciseId = input.exerciseId,
            exerciseName = input.exerciseName,
            currentWeight = input.currentWeight,
            targetWeight = input.targetWeight,
            inventoryStep = input.inventoryStep,
            idealMonthlyStep = idealMonthlyStep,
            monthlyTargets = monthlyTargets,
            status = AnnualProgressionPlanStatus.Ready
        )
    }

    private fun emptyPlan(
        input: AnnualProgressionPlanInput,
        status: AnnualProgressionPlanStatus
    ): AnnualProgressionExercisePlan =
        AnnualProgressionExercisePlan(
            exerciseId = input.exerciseId,
            exerciseName = input.exerciseName,
            currentWeight = input.currentWeight,
            targetWeight = input.targetWeight,
            inventoryStep = input.inventoryStep,
            idealMonthlyStep = 0.0,
            monthlyTargets = emptyList(),
            status = status
        )

    private fun quantizeFromOrigin(value: Double, origin: Double, step: Double): Double {
        val stepsFromOrigin = ((value - origin) / step).roundToInt()
        return origin + stepsFromOrigin * step
    }

    private fun resolveAdjustment(
        month: Int,
        previousWeight: Double?,
        currentWeight: Double,
        inventoryStep: Double
    ): AnnualProgressionAdjustment {
        if (month == 0 || previousWeight == null) return AnnualProgressionAdjustment.Baseline
        val change = currentWeight - previousWeight
        return when {
            abs(change) < WEIGHT_EPSILON -> AnnualProgressionAdjustment.Plateau
            change > inventoryStep * FORCED_JUMP_THRESHOLD -> AnnualProgressionAdjustment.ForcedJump
            else -> AnnualProgressionAdjustment.StandardStep
        }
    }
}

const val ANNUAL_PROGRESSION_ADAPTATION_DAYS = 14
private const val ADAPTATION_DAYS = ANNUAL_PROGRESSION_ADAPTATION_DAYS
private const val MONTHS_IN_PLAN = 12
private const val FORCED_JUMP_THRESHOLD = 1.25
private const val WEIGHT_EPSILON = 0.001
