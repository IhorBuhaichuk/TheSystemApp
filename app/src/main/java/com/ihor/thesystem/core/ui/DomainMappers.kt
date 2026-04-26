package com.ihor.thesystem.core.ui

import com.ihor.thesystem.R
import com.ihor.thesystem.domain.model.MuscleGroup
import com.ihor.thesystem.domain.model.PlayerRank

fun MuscleGroup.asUiText(): UiText {
    return when (this) {
        MuscleGroup.CHEST -> UiText.StringResource(R.string.muscle_group_chest)
        MuscleGroup.BACK -> UiText.StringResource(R.string.muscle_group_back)
        MuscleGroup.SHOULDERS -> UiText.StringResource(R.string.muscle_group_shoulders)
        MuscleGroup.QUADS -> UiText.StringResource(R.string.muscle_group_quads)
        MuscleGroup.HAMSTRINGS_GLUTES -> UiText.StringResource(R.string.muscle_group_hamstrings_glutes)
        MuscleGroup.ARMS -> UiText.StringResource(R.string.muscle_group_arms)
        MuscleGroup.ABS -> UiText.StringResource(R.string.muscle_group_abs)
        MuscleGroup.LEGS -> UiText.StringResource(R.string.muscle_group_legs)
        MuscleGroup.CORE -> UiText.StringResource(R.string.muscle_group_core)
    }
}

fun PlayerRank.asUiText(): UiText {
    return when (this) {
        PlayerRank.NOVICE -> UiText.StringResource(R.string.rank_novice)
        PlayerRank.APPRENTICE -> UiText.StringResource(R.string.rank_apprentice)
        PlayerRank.ADEPT -> UiText.StringResource(R.string.rank_adept)
        PlayerRank.EXPERT -> UiText.StringResource(R.string.rank_expert)
        PlayerRank.MASTER -> UiText.StringResource(R.string.rank_master)
        PlayerRank.THE_SYSTEM -> UiText.StringResource(R.string.rank_the_system)
    }
}
