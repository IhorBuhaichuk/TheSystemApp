package com.ihor.thesystem.domain.model

data class NutritionEntry(
    val dateEpochDay: Long,
    val proteinHit: Boolean,
    val waterHit: Boolean,
    val mealsQuality: MealsQuality = MealsQuality.NORMAL,
    val bodyWeight: Float? = null,
    val goalMode: NutritionGoalMode = NutritionGoalMode.MAINTENANCE,
    val note: String? = null
) {
    init {
        require(bodyWeight == null || bodyWeight > 0f) { "Body weight must be positive." }
    }
}

enum class MealsQuality {
    LOW,
    NORMAL,
    GOOD
}

enum class NutritionGoalMode {
    DEFICIT,
    MAINTENANCE,
    GAIN
}

enum class NutritionFloorTargetStatus {
    HIT,
    MISSED,
    UNKNOWN
}

enum class WeightTrend {
    DOWN,
    STABLE,
    UP
}

data class NutritionFloorStatus(
    val proteinStatus: NutritionFloorTargetStatus = NutritionFloorTargetStatus.UNKNOWN,
    val hydrationStatus: NutritionFloorTargetStatus = NutritionFloorTargetStatus.UNKNOWN,
    val weeklyWeightAverage: Float? = null,
    val trend: WeightTrend = WeightTrend.STABLE,
    val goalMode: NutritionGoalMode = NutritionGoalMode.MAINTENANCE,
    val recommendation: String = "Даних по нутриціології поки недостатньо."
)
