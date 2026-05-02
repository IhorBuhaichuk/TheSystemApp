package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalizedResourceCoverageTest {

    @Test
    fun `localized string resource files cover all default strings`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val resRoot = projectRoot.resolve("src/main/res")
        val defaultStrings = resRoot.resolve("values/strings.xml")

        val defaultNames = defaultStrings.extractTranslatableNames()
        val incompleteLocales = resRoot.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .mapNotNull { valuesDir ->
                val stringsFile = valuesDir.resolve("strings.xml")
                if (!stringsFile.exists()) return@mapNotNull null

                val missingNames = defaultNames - stringsFile.extractTranslatableNames()
                valuesDir.name.takeIf { missingNames.isNotEmpty() }?.let { locale ->
                    "$locale is missing ${missingNames.size} strings: ${missingNames.take(10)}"
                }
            }

        assertTrue(
            "Locale-specific strings.xml files must be complete because Ukrainian is the default resource language. $incompleteLocales",
            incompleteLocales.isEmpty()
        )
    }

    private fun File.extractTranslatableNames(): Set<String> {
        val xml = readText()
        val resourceRegex = Regex("<(string|plurals)\\s+name=\"([^\"]+)\"")
        val nonTranslatableRegex = Regex("<(string|plurals)\\s+name=\"([^\"]+)\"[^>]*translatable=\"false\"")
        val nonTranslatable = nonTranslatableRegex.findAll(xml).map { it.groupValues[2] }.toSet()

        return resourceRegex.findAll(xml)
            .map { it.groupValues[2] }
            .filterNot { it in nonTranslatable }
            .toSet()
    }
}
