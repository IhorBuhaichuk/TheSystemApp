package com.ihor.thesystem.domain.util

import com.ihor.thesystem.domain.model.MuscleGroup

object MuscleGroupMapper {
    private val CHEST_EXERCISES = setOf("Жим від підлоги", "Жим гантелей (кут)", "Жим гантелей під кутом")
    private val BACK_EXERCISES = setOf("Тяга в нахилі", "Підтягування", "Face Pulls")
    private val SHOULDER_EXERCISES = setOf("Жим гантелей сидячи", "Face Pulls", "Жим гантелей (кут)", "Махи гантелями", "Махи гантелями в сторони", "Жим гантелей під кутом")
    private val QUAD_EXERCISES = setOf("Присідання", "Болгарські присідання", "Присідання зі штангою")
    private val HAMSTRING_GLUTE_EXERCISES = setOf("Присідання", "Румунська тяга", "Болгарські присідання", "Присідання зі штангою", "Сідничний місток")
    private val ARM_EXERCISES = setOf("Жим від підлоги", "Жим гантелей сидячи", "Тяга в нахилі", "Підтягування", "Згинання біцепс (EZ)", "Французький жим (EZ)")

    fun getMuscleGroupsForExercise(exerciseName: String): List<MuscleGroup> {
        val groups = mutableListOf<MuscleGroup>()
        
        if (exerciseName in CHEST_EXERCISES) groups.add(MuscleGroup.CHEST)
        if (exerciseName in BACK_EXERCISES) groups.add(MuscleGroup.BACK)
        if (exerciseName in SHOULDER_EXERCISES) groups.add(MuscleGroup.SHOULDERS)
        if (exerciseName in QUAD_EXERCISES) groups.add(MuscleGroup.QUADS)
        if (exerciseName in HAMSTRING_GLUTE_EXERCISES) groups.add(MuscleGroup.HAMSTRINGS_GLUTES)
        if (exerciseName in ARM_EXERCISES) groups.add(MuscleGroup.ARMS)
        
        return groups
    }
}