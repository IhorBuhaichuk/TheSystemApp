package com.ihor.thesystem.quality

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DependencyCatalogGuardTest {

    @Test
    fun `unused chart library is not kept in the dependency graph`() {
        val versionCatalog = rootProject()
            .resolve("gradle/libs.versions.toml")
            .readText()
        val appBuild = appProject()
            .resolve("build.gradle.kts")
            .readText()

        assertFalse("Vico is not used by the current custom Canvas charts", "vico" in versionCatalog.lowercase())
        assertFalse("Do not add Vico dependencies unless chart code imports Vico APIs", "libs.vico" in appBuild)
    }

    @Test
    fun `compiler stack compatibility flags are documented until coordinated upgrade`() {
        val gradleProperties = rootProject()
            .resolve("gradle.properties")
            .readText()

        assertTrue("Document why AGP compatibility flags are still present", "coordinated Kotlin/AGP upgrade" in gradleProperties)
        assertTrue("Current AGP/Kotlin stack still requires built-in Kotlin to be disabled", "android.builtInKotlin=false" in gradleProperties)
        assertTrue("Current AGP/Kotlin stack still requires the legacy AGP DSL", "android.newDsl=false" in gradleProperties)
    }

    private fun appProject(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile

    private fun rootProject(): File =
        requireNotNull(appProject().parentFile)
}
