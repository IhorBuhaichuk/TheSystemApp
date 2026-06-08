package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DirectiveValidationAudit
import com.ihor.thesystem.domain.model.DirectiveValidationResult
import com.ihor.thesystem.domain.model.DirectiveValidationStatus
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.SystemDecisionValidationContext
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.util.Result
import javax.inject.Inject
import kotlin.math.max

class ValidateDirectivesUseCase @Inject constructor() {

    operator fun invoke(
        directives: List<WorkoutDirective>,
        matrix: List<ProgressionMatrixEntry>
    ): Result<List<WorkoutDirective>, DomainError> =
        when (val result = invoke(directives, matrix, SystemDecisionValidationContext())) {
            is Result.Success -> Result.Success(result.data.validatedDirectives)
            is Result.Error -> result
        }

    operator fun invoke(
        directives: List<WorkoutDirective>,
        matrix: List<ProgressionMatrixEntry>,
        context: SystemDecisionValidationContext
    ): Result<DirectiveValidationResult, DomainError> {
        return try {
            val matrixByExercise = matrix.associateBy { it.exerciseId }
            val audits = mutableListOf<DirectiveValidationAudit>()
            val validated = directives.mapNotNull { directive ->
                val matrixEntry = matrixByExercise[directive.exerciseId]
                val validation = validateDirective(
                    directive = directive,
                    matrixEntry = matrixEntry,
                    context = context
                )
                audits += validation.audit
                validation.validated
            }

            Result.Success(
                DirectiveValidationResult(
                    validatedDirectives = validated,
                    audits = audits
                )
            )
        } catch (e: Exception) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }

    private fun validateDirective(
        directive: WorkoutDirective,
        matrixEntry: ProgressionMatrixEntry?,
        context: SystemDecisionValidationContext
    ): SingleDirectiveValidation {
        val trackingMode = matrixEntry?.let { entry ->
            ExerciseTrackingModeResolver.resolve(
                trackingModeOverride = entry.exerciseTrackingMode,
                name = entry.exerciseName,
                nameUk = entry.exerciseNameUk
            )
        }

        if (trackingMode != null && !trackingMode.usesWeightInput) {
            return directive.rejected("Exercise tracking mode does not accept kg targets.")
        }

        val systemBlockReason = context.blockReason()
        if (systemBlockReason != null) {
            return directive.rejected(systemBlockReason)
        }

        val initialWeight = if (directive.targetWeight <= 0.0 && matrixEntry != null) {
            matrixEntry.currentWeight.toDouble()
        } else {
            directive.targetWeight
        }

        val finalWeight = if (matrixEntry != null) {
            initialWeight.coerceIn(
                minimumValue = matrixEntry.minimumAllowedWeight(),
                maximumValue = matrixEntry.maximumAllowedWeight()
            )
        } else {
            initialWeight.coerceAtLeast(0.0)
        }

        val sanitized = directive.copy(
            targetWeight = finalWeight,
            targetSets = directive.targetSets.coerceIn(1, 10),
            targetReps = validateReps(directive.targetReps)
        )

        val status = if (sanitized == directive) {
            DirectiveValidationStatus.ACCEPTED
        } else {
            DirectiveValidationStatus.CLAMPED
        }

        return SingleDirectiveValidation(
            validated = sanitized,
            audit = DirectiveValidationAudit(
                exerciseId = directive.exerciseId,
                original = directive,
                validated = sanitized,
                status = status,
                reason = if (status == DirectiveValidationStatus.ACCEPTED) {
                    "Accepted by system validation."
                } else {
                    "Clamped by progression matrix, target cap, allowed step, sets or reps policy."
                }
            )
        )
    }

    private fun SystemDecisionValidationContext.blockReason(): String? {
        val decision = todayDecision ?: return if (lastWorkoutFailed) {
            "Last workout failed; AI progression is blocked until the system sees a stable session."
        } else {
            null
        }

        return when {
            decision.readinessLevel == ReadinessLevel.RECOVERY ||
                decision.readinessLevel == ReadinessLevel.REDUCED ||
                decision.readinessScore < STANDARD_READINESS_SCORE ->
                "Readiness is below standard; AI recommendation cannot update progression targets."
            decision.recoveryDebt.level == RecoveryDebtLevel.HIGH ||
                decision.recoveryDebt.level == RecoveryDebtLevel.CRITICAL ->
                "Recovery debt is high or critical; AI recommendation cannot update progression targets."
            decision.decisionType in BLOCKED_DECISION_TYPES ->
                "Today decision is ${decision.decisionType}; AI recommendation cannot update progression targets."
            lastWorkoutFailed ->
                "Last workout failed; AI progression is blocked until the system sees a stable session."
            else -> null
        }
    }

    private fun ProgressionMatrixEntry.minimumAllowedWeight(): Double =
        minOf(currentWeight.toDouble(), targetWeight.toDouble())
            .coerceAtLeast(0.0)

    private fun ProgressionMatrixEntry.maximumAllowedWeight(): Double {
        val current = currentWeight.toDouble().coerceAtLeast(0.0)
        val matrixCap = max(current, targetWeight.toDouble().coerceAtLeast(0.0))
        val stepCap = current + allowedStep()
        return minOf(matrixCap, stepCap).coerceAtLeast(minimumAllowedWeight())
    }

    private fun ProgressionMatrixEntry.allowedStep(): Double =
        weeklyStep
            .takeIf { it > 0f }
            ?.toDouble()
            ?: max(DEFAULT_ALLOWED_STEP_KG, currentWeight.toDouble() * DEFAULT_ALLOWED_STEP_PERCENT)

    private fun WorkoutDirective.rejected(reason: String): SingleDirectiveValidation =
        SingleDirectiveValidation(
            validated = null,
            audit = DirectiveValidationAudit(
                exerciseId = exerciseId,
                original = this,
                validated = null,
                status = DirectiveValidationStatus.REJECTED,
                reason = reason
            )
        )

    private fun validateReps(reps: String): String {
        return if (reps.contains("-")) {
            val parts = reps.split("-")
            val minRaw = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val maxRaw = parts.getOrNull(1)?.toIntOrNull() ?: 8
            val min = minRaw.coerceIn(1, 30)
            val max = maxRaw.coerceIn(1, 30)
            val actualMin = minOf(min, max)
            val actualMax = maxOf(min, max)
            "$actualMin-$actualMax"
        } else {
            reps.toIntOrNull()?.coerceIn(1, 30)?.toString() ?: "8"
        }
    }

    private data class SingleDirectiveValidation(
        val validated: WorkoutDirective?,
        val audit: DirectiveValidationAudit
    )

    private companion object {
        const val STANDARD_READINESS_SCORE = 65
        const val DEFAULT_ALLOWED_STEP_KG = 2.5
        const val DEFAULT_ALLOWED_STEP_PERCENT = 0.025
        val BLOCKED_DECISION_TYPES = setOf(
            TodayTrainingDecisionType.ACTIVE_RECOVERY,
            TodayTrainingDecisionType.NO_EXCUSE,
            TodayTrainingDecisionType.DELOAD,
            TodayTrainingDecisionType.REST
        )
    }
}
