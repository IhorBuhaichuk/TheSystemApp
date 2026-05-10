package com.ihor.thesystem.domain.model

/**
 * Shared validation rules for editable player profile fields.
 *
 * Units are explicit because the UI and persistence layer both store metric values.
 */
object PlayerProfileValidationPolicy {
    const val MAX_NAME_LENGTH = 50
    const val MIN_WEIGHT_KG = 20f
    const val MAX_WEIGHT_KG = 500f
    const val MIN_HEIGHT_CM = 50f
    const val MAX_HEIGHT_CM = 300f
    const val MIN_AGE = 1
    const val MAX_AGE = 120

    fun validateName(name: String): ValidationError? =
        if (name.isBlank() || name.length > MAX_NAME_LENGTH) {
            ValidationError.INVALID_PLAYER_NAME
        } else {
            null
        }

    fun validateWeight(weight: Float): ValidationError? =
        if (weight !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
            ValidationError.INVALID_WEIGHT
        } else {
            null
        }

    fun validateHeight(height: Float): ValidationError? =
        if (height !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
            ValidationError.INVALID_HEIGHT
        } else {
            null
        }

    fun validateAge(age: Int): ValidationError? =
        if (age !in MIN_AGE..MAX_AGE) {
            ValidationError.INVALID_AGE
        } else {
            null
        }
}
