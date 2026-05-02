package com.ihor.thesystem.security

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SensitiveDataTest {

    @Test
    fun `source files do not contain hardcoded Google api keys`() {
        val projectRoot = findProjectRoot()
        val filesToScan = listOf(
            projectRoot.resolve("app/src"),
            projectRoot.resolve("domain/src"),
            projectRoot.resolve("build.gradle.kts"),
            projectRoot.resolve("settings.gradle.kts"),
            projectRoot.resolve("gradle.properties"),
            projectRoot.resolve("gradle/libs.versions.toml")
        ).flatMap { it.scanFiles() }

        val googleApiKeyPattern = Regex("AI" + "za[0-9A-Za-z_-]{20,}")
        val findings = filesToScan.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                if (googleApiKeyPattern.containsMatchIn(line)) {
                    "${file.relativeTo(projectRoot).invariantSeparatorsPath}:${index + 1}"
                } else {
                    null
                }
            }
        }

        assertTrue(
            "Hardcoded Google API keys found:\n${findings.joinToString(separator = "\n")}",
            findings.isEmpty()
        )
    }

    private fun findProjectRoot(): File =
        generateSequence(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .firstOrNull { it.resolve("settings.gradle.kts").exists() }
            ?: error("Project root not found")

    private fun File.scanFiles(): List<File> {
        if (!exists()) return emptyList()
        if (isFile) return listOf(this).filter { it.isScannableTextFile() }

        return walkTopDown()
            .onEnter { directory -> directory.name !in ignoredDirectories }
            .filter { it.isFile && it.isScannableTextFile() }
            .toList()
    }

    private fun File.isScannableTextFile(): Boolean =
        extension in setOf("kt", "kts", "java", "xml", "properties", "toml", "json", "gradle")

    private companion object {
        val ignoredDirectories = setOf(".git", ".gradle", ".idea", ".kotlin", "build")
    }
}
