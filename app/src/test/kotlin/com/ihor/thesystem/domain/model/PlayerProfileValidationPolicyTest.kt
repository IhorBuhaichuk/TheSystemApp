package com.ihor.thesystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerProfileValidationPolicyTest {

    @Test
    fun `valid profile values pass validation`() {
        assertNull(PlayerProfileValidationPolicy.validateName("Hunter"))
        assertNull(PlayerProfileValidationPolicy.validateWeight(82f))
        assertNull(PlayerProfileValidationPolicy.validateHeight(180f))
        assertNull(PlayerProfileValidationPolicy.validateAge(35))
    }

    @Test
    fun `invalid profile values return specific validation errors`() {
        assertEquals(
            ValidationError.INVALID_PLAYER_NAME,
            PlayerProfileValidationPolicy.validateName("")
        )
        assertEquals(
            ValidationError.INVALID_WEIGHT,
            PlayerProfileValidationPolicy.validateWeight(PlayerProfileValidationPolicy.MIN_WEIGHT_KG - 0.1f)
        )
        assertEquals(
            ValidationError.INVALID_HEIGHT,
            PlayerProfileValidationPolicy.validateHeight(PlayerProfileValidationPolicy.MAX_HEIGHT_CM + 0.1f)
        )
        assertEquals(
            ValidationError.INVALID_AGE,
            PlayerProfileValidationPolicy.validateAge(PlayerProfileValidationPolicy.MAX_AGE + 1)
        )
    }

    @Test
    fun `inclusive profile boundaries are valid`() {
        assertNull(PlayerProfileValidationPolicy.validateName("A".repeat(PlayerProfileValidationPolicy.MAX_NAME_LENGTH)))
        assertNull(PlayerProfileValidationPolicy.validateWeight(PlayerProfileValidationPolicy.MIN_WEIGHT_KG))
        assertNull(PlayerProfileValidationPolicy.validateWeight(PlayerProfileValidationPolicy.MAX_WEIGHT_KG))
        assertNull(PlayerProfileValidationPolicy.validateHeight(PlayerProfileValidationPolicy.MIN_HEIGHT_CM))
        assertNull(PlayerProfileValidationPolicy.validateHeight(PlayerProfileValidationPolicy.MAX_HEIGHT_CM))
        assertNull(PlayerProfileValidationPolicy.validateAge(PlayerProfileValidationPolicy.MIN_AGE))
        assertNull(PlayerProfileValidationPolicy.validateAge(PlayerProfileValidationPolicy.MAX_AGE))
    }
}
