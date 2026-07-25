package com.ihor.thesystem.core.ui

import java.util.Locale

/**
 * Maps only known system template names to user-facing Ukrainian copy.
 *
 * Persisted names and user-created workout titles stay unchanged.
 */
fun String.toUserFacingWorkoutName(): String =
    when (trim().uppercase(Locale.ROOT)) {
        "WORKOUT A" -> "Тренування А"
        "WORKOUT B" -> "Тренування Б"
        "FULL BODY" -> "Усе тіло"
        "FULL BODY / WORKOUT A-B" -> "Усе тіло · тренування А–Б"
        "NO EXCUSE PROTOCOL" -> "Коротке тренування"
        "RECOVERY PROTOCOL" -> "Активне відновлення"
        "DELOAD SESSION" -> "Полегшене тренування"
        else -> this
    }
