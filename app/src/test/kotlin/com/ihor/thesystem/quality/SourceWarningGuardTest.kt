package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SourceWarningGuardTest {

    @Test
    fun `source avoids warning-prone APIs covered by release readiness`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val sourceRoot = projectRoot.resolve("src/main")
        val deprecatedDividerCall = Regex("""\bDivider\(""")

        val offenders = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val path = file.relativeTo(projectRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    when {
                        deprecatedDividerCall.containsMatchIn(line) -> "$path:${index + 1} uses deprecated Divider"
                        "Icons.Filled.Assignment" in line -> "$path:${index + 1} uses deprecated Assignment icon"
                        "String.format(\"" in line -> "$path:${index + 1} uses String.format without Locale"
                        "androidx.hilt.navigation.compose.hiltViewModel" in line -> "$path:${index + 1} uses moved hiltViewModel import"
                        "Locale(\"uk\")" in line -> "$path:${index + 1} uses deprecated Locale constructor"
                        ".asFrameworkPaint()" in line -> "$path:${index + 1} uses deprecated Compose asFrameworkPaint"
                        else -> null
                    }
                }
            }
            .toList()

        assertTrue(
            "Use current Compose/Hilt/Locale APIs and String.format(Locale, ...): $offenders",
            offenders.isEmpty()
        )
    }
}
