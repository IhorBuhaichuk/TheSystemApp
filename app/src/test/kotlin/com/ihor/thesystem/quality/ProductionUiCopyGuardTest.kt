package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProductionUiCopyGuardTest {

    @Test
    fun `production ui does not expose in development placeholders`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val mainSource = projectRoot.resolve("src/main")
        val forbidden = listOf(
            "В розробці",
            "в розробці",
            "Coming soon",
            "coming soon"
        )

        val offenders = mainSource.walkTopDown()
            .filter { file -> file.isFile && file.extension in setOf("kt", "xml") }
            .flatMap { file ->
                val text = file.readText()
                forbidden.mapNotNull { marker ->
                    if (marker in text) {
                        "${file.relativeTo(projectRoot).invariantSeparatorsPath}: $marker"
                    } else {
                        null
                    }
                }
            }
            .toList()

        assertTrue(
            "Production UI must use honest empty/unavailable states instead of placeholders: $offenders",
            offenders.isEmpty()
        )
    }
}
