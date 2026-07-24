package com.ihor.thesystem.domain.model

enum class OnboardingGoal {
    BUILD_STRENGTH,
    BUILD_MUSCLE,
    LOSE_WEIGHT,
    BUILD_HABIT
}

enum class OnboardingExperience {
    BEGINNER,
    RETURNING,
    INTERMEDIATE
}

enum class OnboardingCyclePreset(
    val cycleDays: Int,
    val microCyclesPerMonth: Int
) {
    THREE_DAY(cycleDays = 3, microCyclesPerMonth = 4),
    FOUR_DAY(cycleDays = 4, microCyclesPerMonth = 4),
    FIVE_DAY(cycleDays = 5, microCyclesPerMonth = 4)
}

enum class AppStartDestination {
    ONBOARDING,
    STATUS
}

data class OnboardingAnswers(
    val name: String,
    val goal: OnboardingGoal,
    val equipment: Set<EquipmentType>,
    val experience: OnboardingExperience,
    val cyclePreset: OnboardingCyclePreset
)
