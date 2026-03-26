package com.ihor.thesystem.core.navigation

sealed class Routes(val route: String) {
    object Status     : Routes("status")
    object Mode       : Routes("mode")
    object Statistics : Routes("statistics")
    object Architect  : Routes("architect")
}