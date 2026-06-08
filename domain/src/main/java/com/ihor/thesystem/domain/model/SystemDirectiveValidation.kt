package com.ihor.thesystem.domain.model

enum class DirectiveValidationStatus {
    ACCEPTED,
    CLAMPED,
    REJECTED
}

data class SystemDecisionValidationContext(
    val todayDecision: TodayTrainingDecision? = null,
    val lastWorkoutFailed: Boolean = false
)

data class DirectiveValidationAudit(
    val exerciseId: Int,
    val original: WorkoutDirective,
    val validated: WorkoutDirective?,
    val status: DirectiveValidationStatus,
    val reason: String
)

data class DirectiveValidationResult(
    val validatedDirectives: List<WorkoutDirective>,
    val audits: List<DirectiveValidationAudit>
) {
    val hasSystemCorrections: Boolean
        get() = audits.any { it.status != DirectiveValidationStatus.ACCEPTED }
}

data class AiRecommendationApplicationResult(
    val acceptedCount: Int,
    val clampedCount: Int,
    val rejectedCount: Int,
    val audits: List<DirectiveValidationAudit>
) {
    val hasSystemCorrections: Boolean
        get() = clampedCount > 0 || rejectedCount > 0

    companion object {
        val Empty = AiRecommendationApplicationResult(
            acceptedCount = 0,
            clampedCount = 0,
            rejectedCount = 0,
            audits = emptyList()
        )

        fun from(validationResult: DirectiveValidationResult): AiRecommendationApplicationResult =
            AiRecommendationApplicationResult(
                acceptedCount = validationResult.audits.count { it.status == DirectiveValidationStatus.ACCEPTED },
                clampedCount = validationResult.audits.count { it.status == DirectiveValidationStatus.CLAMPED },
                rejectedCount = validationResult.audits.count { it.status == DirectiveValidationStatus.REJECTED },
                audits = validationResult.audits
            )
    }
}
