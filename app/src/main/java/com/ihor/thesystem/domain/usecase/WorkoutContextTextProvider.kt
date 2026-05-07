package com.ihor.thesystem.domain.usecase

interface WorkoutContextTextProvider {
    fun workoutResultsHeader(totalDayTonnage: Int): String
    fun workoutResultsItem(exerciseId: Int, name: String, weight: Double, reps: Int): String
    fun exerciseLabel(exerciseId: Int): String
}
