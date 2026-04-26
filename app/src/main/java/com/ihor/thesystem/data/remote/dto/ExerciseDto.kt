package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val category: String? = null,
    val images: List<String> = emptyList()
) {
    val gifUrl: String?
        get() = images.firstOrNull()?.let { imagePath ->
            val pathWithoutExtension = imagePath.substringBeforeLast(".")
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/$pathWithoutExtension.gif"
        }
}
