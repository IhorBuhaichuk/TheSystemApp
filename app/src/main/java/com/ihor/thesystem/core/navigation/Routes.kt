package com.ihor.thesystem.core.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    companion object {
        const val PICKER_SOURCE_CYCLE = "cycle"
        const val PICKER_SOURCE_ANNUAL = "annual"
        const val PICKER_RESULT_EXERCISE_ID = "picker_result_exercise_id"
    }

    @Serializable
    data object Status : Routes

    @Serializable
    data object Cycle : Routes
    
    @Serializable
    data object Statistics : Routes
    
    @Serializable
    data object Architect : Routes
    
    @Serializable
    data object Calendar : Routes

    @Serializable
    data object Profile : Routes

    @Serializable
    data object CalendarSettings : Routes
     
    @Serializable
    data object AnnualProgressionPlan : Routes

    @Serializable
    data object AnnualProgressionDetails : Routes

    @Serializable
    data class WorkoutAnalysis(val sessionId: Long = 0L) : Routes

    @Serializable
    data class ExercisePicker(
        val source: String,
        val cycleDay: Int = -1
    ) : Routes
}
