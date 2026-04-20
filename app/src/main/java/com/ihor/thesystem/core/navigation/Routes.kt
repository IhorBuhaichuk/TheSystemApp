package com.ihor.thesystem.core.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Status : Routes
    
    @Serializable
    data object Statistics : Routes
    
    @Serializable
    data object Architect : Routes
    
    @Serializable
    data object Calendar : Routes
    
    @Serializable
    data object AnnualMatrix : Routes
}
