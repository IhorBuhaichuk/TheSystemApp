package com.ihor.thesystem.core.ui

import com.ihor.thesystem.R
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.feature.exercise_search.ui.toEquipmentUiText
import com.ihor.thesystem.feature.exercise_search.ui.toForceUiText
import com.ihor.thesystem.feature.exercise_search.ui.toLevelUiText
import com.ihor.thesystem.feature.exercise_search.ui.toMechanicUiText
import com.ihor.thesystem.feature.exercise_search.ui.toMuscleUiText
import com.ihor.thesystem.feature.exercise_search.ui.toUiText
import org.junit.Assert.assertEquals
import org.junit.Test

class UserFacingTextMappersTest {

    @Test
    fun `player ranks use Ukrainian display resources`() {
        assertStringResource(PlayerRank.NOVICE.asUiText(), R.string.rank_novice)
        assertStringResource(PlayerRank.THE_SYSTEM.asUiText(), R.string.rank_the_system)
    }

    @Test
    fun `unknown exercise metadata does not expose internal English values`() {
        assertStringResource(ExerciseCategory.UNKNOWN.toUiText(), R.string.filter_category_other)
        assertStringResource("UNMAPPED_MUSCLE".toMuscleUiText(), R.string.filter_muscle_other)
        assertStringResource("UNMAPPED_EQUIPMENT".toEquipmentUiText(), R.string.filter_equipment_other)
        assertStringResource("UNMAPPED_LEVEL".toLevelUiText(), R.string.filter_value_not_specified)
        assertStringResource("UNMAPPED_MECHANIC".toMechanicUiText(), R.string.filter_mechanic_na)
        assertStringResource("UNMAPPED_FORCE".toForceUiText(), R.string.filter_value_not_specified)
    }

    private fun assertStringResource(actual: UiText, expected: Int) {
        val resource = actual as UiText.StringResource
        assertEquals(expected, resource.resId)
    }
}
