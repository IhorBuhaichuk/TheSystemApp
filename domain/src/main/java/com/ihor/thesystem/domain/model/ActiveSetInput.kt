package com.ihor.thesystem.domain.model

data class ActiveSetInput(
    val id: Long = System.nanoTime(),
    val weight: String = "",
    val reps: String = "",
    val isCompleted: Boolean = false
)
