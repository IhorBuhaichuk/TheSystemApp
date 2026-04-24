package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.PlayerRank

sealed class StatusOneOffEvent {
    data class ShowLevelUp(val newClass: PlayerRank, val newMonth: Int) : StatusOneOffEvent()
}