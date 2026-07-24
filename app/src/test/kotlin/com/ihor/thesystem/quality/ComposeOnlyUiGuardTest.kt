package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ComposeOnlyUiGuardTest {

    @Test
    fun `web ui source files are not introduced into app sources`() {
        val repoRoot = repoRoot()
        val sourceRoots = listOf(
            repoRoot.resolve("app/src"),
            repoRoot.resolve("domain/src")
        ).filter { it.exists() }
        val forbiddenExtensions = setOf(
            "css",
            "scss",
            "sass",
            "html",
            "htm",
            "jsx",
            "tsx",
            "vue",
            "svelte"
        )

        val offenders = sourceRoots
            .flatMap { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension.lowercase() in forbiddenExtensions }
                    .map { it.relativeTo(repoRoot).invariantSeparatorsPath }
                    .toList()
            }

        assertTrue(
            "THE SYSTEM UI must stay native Kotlin/Jetpack Compose; remove web UI source files: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `web ui package manifests are not introduced`() {
        val repoRoot = repoRoot()
        val forbiddenFileNames = setOf(
            "package.json",
            "package-lock.json",
            "pnpm-lock.yaml",
            "yarn.lock",
            "bun.lockb",
            "vite.config.js",
            "vite.config.ts",
            "next.config.js",
            "next.config.ts",
            "tailwind.config.js",
            "tailwind.config.ts"
        )
        val ignoredDirectories = setOf(
            ".git",
            ".gradle",
            ".idea",
            "build",
            "node_modules"
        )

        val offenders = repoRoot.walkTopDown()
            .onEnter { directory -> directory.name !in ignoredDirectories }
            .filter { it.isFile && it.name.lowercase() in forbiddenFileNames }
            .map { it.relativeTo(repoRoot).invariantSeparatorsPath }
            .toList()

        assertTrue(
            "Do not add web package-manager or web framework config files to this Android project: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `gradle configuration does not reference web ui libraries`() {
        val repoRoot = repoRoot()
        val searchedFiles = listOf(
            repoRoot.resolve("build.gradle.kts"),
            repoRoot.resolve("settings.gradle.kts"),
            repoRoot.resolve("app/build.gradle.kts"),
            repoRoot.resolve("domain/build.gradle.kts"),
            repoRoot.resolve("gradle/libs.versions.toml")
        ).filter { it.exists() }
        val forbiddenMarkers = listOf(
            "react",
            "tailwind",
            "framer-motion",
            "21st.dev",
            "@vitejs",
            "vite",
            "nextjs",
            "next.js",
            "webpack"
        )

        val offenders = searchedFiles.flatMap { file ->
            val text = file.readText().lowercase()
            forbiddenMarkers.mapNotNull { marker ->
                if (marker in text) {
                    "${file.relativeTo(repoRoot).invariantSeparatorsPath} contains '$marker'"
                } else {
                    null
                }
            }
        }

        assertTrue(
            "Android UI dependencies must stay native Compose, not web UI libraries: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `compose only rule is documented for future agents`() {
        val repoRoot = repoRoot()
        val agents = repoRoot.resolve("AGENTS.md").readText()
        val guidelines = repoRoot.resolve("UI_UX_GUIDELINES.md").readText()

        assertTrue("AGENTS.md must point UI tasks to Jetpack Compose", "Jetpack Compose" in agents)
        assertTrue("UI_UX_GUIDELINES.md must point UI tasks to Jetpack Compose", "Jetpack Compose" in guidelines)
        assertTrue("AGENTS.md must explicitly forbid React", "React" in agents)
        assertTrue("UI_UX_GUIDELINES.md must explicitly forbid React", "React" in guidelines)
        assertTrue("UI_UX_GUIDELINES.md must explicitly forbid Tailwind", "Tailwind" in guidelines)
    }

    private fun repoRoot(): File {
        val appRoot = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        return requireNotNull(appRoot.parentFile)
    }
}
