package com.ihor.thesystem.feature.status.viewmodel

sealed class StatusOneOffEvent {
    data class ShowLevelUp(val newClass: String, val newMonth: Int) : StatusOneOffEvent()
    object ShowPenaltyActivated                                     : StatusOneOffEvent()
    object ShowPenaltyDeactivated                                   : StatusOneOffEvent()
}