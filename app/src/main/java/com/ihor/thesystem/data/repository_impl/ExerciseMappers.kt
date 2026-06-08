package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.domain.model.ExerciseDetails

internal fun ExerciseEntity.toExerciseDetails(): ExerciseDetails =
    ExerciseDetails(
        id = id,
        name = name,
        nameUk = nameUk,
        category = category,
        muscleGroups = muscleGroups,
        equipment = equipment,
        level = level,
        mechanic = mechanic,
        force = force,
        gifUrl = gifUrl,
        externalId = externalId,
        trackingMode = trackingMode,
        isCoreSystemExercise = isCoreSystemExercise,
        movementPattern = movementPattern,
        techniqueTips = techniqueTips,
        commonMistakes = commonMistakes,
        substitutionExternalIds = substitutionExternalIds
    )
