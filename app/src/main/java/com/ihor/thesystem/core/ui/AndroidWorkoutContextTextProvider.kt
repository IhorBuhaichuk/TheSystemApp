package com.ihor.thesystem.core.ui

import android.content.Context
import com.ihor.thesystem.R
import com.ihor.thesystem.domain.usecase.WorkoutContextTextProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidWorkoutContextTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : WorkoutContextTextProvider {

    override fun workoutResultsHeader(totalDayTonnage: Int): String =
        context.getString(R.string.text_workout_results_header, totalDayTonnage)

    override fun workoutResultsItem(exerciseId: Int, name: String, weight: Double, reps: Int): String =
        context.getString(R.string.text_workout_results_item, exerciseId, name, weight, reps)

    override fun exerciseLabel(exerciseId: Int): String =
        context.getString(R.string.text_exercise_label, exerciseId)
}
