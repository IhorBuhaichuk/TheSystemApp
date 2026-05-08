package com.ihor.thesystem.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiDomainTextBoundaryGuardTest {

    @Test
    fun `ai chat domain models do not depend on ui text`() {
        val source = File(requireNotNull(System.getProperty("user.dir")))
            .absoluteFile
            .resolve("src/main/java/com/ihor/thesystem/domain/model/AiDomainModels.kt")
            .readText()

        assertFalse(
            "AI domain models must not import presentation UiText.",
            "core.ui.UiText" in source
        )
        assertTrue(
            "ChatMessage and AiArchitectReport must use the domain MessageText abstraction.",
            "val text: MessageText" in source &&
                "val architectFeedback: MessageText" in source
        )
    }
}
