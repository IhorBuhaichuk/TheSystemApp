package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

class SourceEncodingGuardTest {

    @Test
    fun `main source text files are valid utf8 without mojibake markers`() {
        val appRoot = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        val repoRoot = requireNotNull(appRoot.parentFile)
        val roots = listOf(
            repoRoot.resolve("app/src/main"),
            repoRoot.resolve("domain/src/main")
        )
        val checkedExtensions = setOf("kt", "kts", "xml", "json", "csv", "txt")
        val offenders = roots
            .flatMap { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension in checkedExtensions }
                    .toList()
            }
            .mapNotNull { file ->
                val relativePath = file.relativeTo(repoRoot).invariantSeparatorsPath
                val bytes = file.readBytes()
                val text = runCatching { decodeStrictUtf8(bytes) }
                    .getOrElse { return@mapNotNull "$relativePath is not valid UTF-8" }
                val marker = mojibakeMarkers.firstOrNull { it in text }
                when {
                    marker != null -> "$relativePath contains mojibake marker '$marker'"
                    replacementCharacter in text -> "$relativePath contains Unicode replacement character"
                    else -> null
                }
            }

        assertTrue(
            "Source files must stay UTF-8 and must not contain mojibake: $offenders",
            offenders.isEmpty()
        )
    }

    private fun decodeStrictUtf8(bytes: ByteArray): String =
        StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()

    private companion object {
        private const val replacementCharacter = '\uFFFD'
        private val mojibakeMarkers = listOf(
            "Ð",
            "Ñ",
            "Â",
            "Ã",
            "â€",
            "â„",
            "â€¦",
            "вЂ",
            "вњ"
        )
    }
}
