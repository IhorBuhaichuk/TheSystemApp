package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NoPrintStackTraceTest {

    @Test
    fun `production code does not call printStackTrace`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val sourceRoot = projectRoot.resolve("src/main")

        val offenders = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "java") }
            .filter { file -> "printStackTrace(" in file.readText() }
            .map { it.relativeTo(projectRoot).invariantSeparatorsPath }
            .toList()

        assertTrue(
            "Use structured logging instead of printStackTrace(): $offenders",
            offenders.isEmpty()
        )
    }
}
