package com.ihor.thesystem.quality

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReleaseLintGuardTest {

    @Test
    fun `gradle dependency constraints use reduced project import overhead`() {
        val gradleProperties = rootProject()
            .resolve("gradle.properties")
            .readText()

        assertTrue(
            "Disable Android dependency constraints to avoid unnecessary library component constraints",
            "android.dependency.useConstraints=false" in gradleProperties
        )
    }

    @Test
    fun `main activity does not declare redundant orientation lock`() {
        val manifest = appProject()
            .resolve("src/main/AndroidManifest.xml")
            .readText()

        assertFalse("MainActivity must not declare android:screenOrientation", "android:screenOrientation" in manifest)
    }

    @Test
    fun `resources avoid lint typo and ellipsis false positives`() {
        val strings = appProject()
            .resolve("src/main/res/values/strings.xml")
            .readText()

        assertFalse(
            "Use &#8230; instead of literal three-dot ellipsis",
            ">[^<]*\\.\\.\\.[^<]*<".toRegex().containsMatchIn(strings)
        )
        assertTrue(
            "Google font certificate arrays must suppress spellcheck false positives",
            """name="com_google_android_gms_fonts_certs_dev" translatable="false" tools:ignore="Typos"""" in strings &&
                """name="com_google_android_gms_fonts_certs_prod" translatable="false" tools:ignore="Typos"""" in strings
        )
        assertTrue(
            "The default string catalog is intentionally kept for Ukrainian-first localization",
            """tools:ignore="UnusedResources"""" in strings
        )
    }

    @Test
    fun `launcher icons use adaptive resources without legacy bitmap duplicates`() {
        val res = appProject().resolve("src/main/res")
        val legacyBitmapIcons = res
            .listFiles { file -> file.isDirectory && file.name.matches(Regex("""mipmap-(m|h|x|xx|xxx)hdpi""")) }
            .orEmpty()
            .flatMap { it.listFiles().orEmpty().toList() }
            .filter { it.name.startsWith("ic_launcher") }

        assertFalse("Adaptive launcher XML must stay in the v26 qualifier", res.resolve("mipmap-anydpi/ic_launcher.xml").exists())
        assertFalse("Adaptive round launcher XML must stay in the v26 qualifier", res.resolve("mipmap-anydpi/ic_launcher_round.xml").exists())
        assertTrue("Adaptive launcher XML is required", res.resolve("mipmap-anydpi-v26/ic_launcher.xml").exists())
        assertTrue("Adaptive round launcher XML is required", res.resolve("mipmap-anydpi-v26/ic_launcher_round.xml").exists())
        assertTrue("Remove legacy launcher bitmap duplicates: $legacyBitmapIcons", legacyBitmapIcons.isEmpty())
    }

    @Test
    fun `adaptive launcher sdk qualifier has a targeted lint suppression`() {
        val lintConfig = appProject()
            .resolve("lint.xml")
            .readText()

        assertTrue("ObsoleteSdkInt suppression must be scoped to adaptive launcher icons", """id="ObsoleteSdkInt"""" in lintConfig)
        assertTrue("ObsoleteSdkInt suppression must not be global", """path="src/main/res/mipmap-anydpi-v26"""" in lintConfig)
    }

    @Test
    fun `intentional visual resources are kept explicitly`() {
        val keepFile = appProject()
            .resolve("src/main/res/values/resource_keep.xml")
            .readText()

        val keptResources = listOf(
            "@drawable/daily_quest_scroll",
            "@drawable/status_rpg_hero_bg",
            "@drawable/rank_badge_a",
            "@drawable/rank_badge_b",
            "@drawable/rank_badge_c",
            "@drawable/rank_badge_d",
            "@drawable/rank_badge_e",
            "@drawable/rank_badge_s"
        )

        assertTrue("Intentional visual resources must use tools:keep", """tools:keep=""" in keepFile)
        assertTrue(
            "Missing tools:keep entries: ${keptResources.filterNot { it in keepFile }}",
            keptResources.all { it in keepFile }
        )
    }

    @Test
    fun `template resources that are not wired into the app are absent`() {
        val res = appProject().resolve("src/main/res")

        assertFalse("Remove unused template colors.xml", res.resolve("values/colors.xml").exists())
        assertFalse("Remove unused sample backup_rules.xml", res.resolve("xml/backup_rules.xml").exists())
    }

    private fun appProject(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile

    private fun rootProject(): File =
        requireNotNull(appProject().parentFile)
}
