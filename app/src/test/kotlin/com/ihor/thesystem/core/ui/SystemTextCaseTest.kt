package com.ihor.thesystem.core.ui

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemTextCaseTest {

    @Test
    fun convertsLatinDisplayLabelToSentenceCase() {
        assertEquals(
            "Training and calendar settings",
            "TRAINING AND CALENDAR SETTINGS".toSystemSentenceCase(Locale.ENGLISH)
        )
    }

    @Test
    fun convertsUkrainianDisplayLabelToSentenceCase() {
        assertEquals(
            "Рекомендація системи",
            "РЕКОМЕНДАЦІЯ СИСТЕМИ".toSystemSentenceCase(Locale.forLanguageTag("uk-UA"))
        )
    }

    @Test
    fun keepsLeadingNumberAndCapitalizesFirstLetter() {
        assertEquals(
            "4. Statistics",
            "4. STATISTICS".toSystemSentenceCase(Locale.ENGLISH)
        )
    }
}
