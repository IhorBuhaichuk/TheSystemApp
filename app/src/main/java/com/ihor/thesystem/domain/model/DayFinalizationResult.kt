package com.ihor.thesystem.domain.model

sealed interface DayFinalizationResult {
    data object Success : DayFinalizationResult
    data object LevelUp : DayFinalizationResult
    data object PenaltyZoneEntered : DayFinalizationResult
    data object None : DayFinalizationResult
}
