package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiReleaseConfigurationGuardTest {

    @Test
    fun `release build does not expose client side Gemini as configured`() {
        val buildFile = findProjectRoot().resolve("app/build.gradle.kts").readText()
        val releaseBlock = Regex("""release\s*\{([\s\S]*?)\n\s*}""")
            .find(buildFile)
            ?.groupValues
            ?.get(1)
            ?: error("release build type not found")

        assertTrue(
            "Release must not embed a client-side Gemini API key.",
            """buildConfigField("String", "GEMINI_API_KEY", "\"\"")""" in releaseBlock
        )
        assertTrue(
            "Release must report client AI as unconfigured unless a backend/proxy is added.",
            """buildConfigField("boolean", "GEMINI_CLIENT_AI_ENABLED", "false")""" in releaseBlock
        )
    }

    private fun findProjectRoot(): File =
        generateSequence(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .firstOrNull { it.resolve("settings.gradle.kts").exists() }
            ?: error("Project root not found")
}
