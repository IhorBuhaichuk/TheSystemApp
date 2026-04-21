package com.ihor.thesystem.domain.util

import com.ihor.thesystem.domain.model.MuscleGroup

object MuscleGroupMapper {
    fun getMuscleGroupsForExercise(exerciseName: String): List<MuscleGroup> {
        val groups = mutableListOf<MuscleGroup>()
        
        if (exerciseName in listOf("Жим від підлоги", "Жим гантелей (кут)")) groups.add(MuscleGroup.CHEST)
        if (exerciseName in listOf("Тяга в нахилі", "Підтягування", "Face Pulls")) groups.add(MuscleGroup.BACK)
        if (exerciseName in listOf("Жим гантелей сидячи", "Face Pulls", "Жим гантелей (кут)", "Махи гантелями")) groups.add(MuscleGroup.SHOULDERS)
        if (exerciseName in listOf("Присідання", "Болгарські присідання")) groups.add(MuscleGroup.QUADS)
        if (exerciseName in listOf("Присідання", "Румунська тяга", "Болгарські присідання")) groups.add(MuscleGroup.HAMSTRINGS_GLUTES)
        if (exerciseName in listOf("Жим від підлоги", "Жим гантелей сидячи", "Тяга в нахилі", "Підтягування", "Згинання біцепс (EZ)", "Французький жим (EZ)")) groups.add(MuscleGroup.ARMS)
        
        return groups
    }
}