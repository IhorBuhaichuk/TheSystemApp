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
                val match = mojibakeRules.asSequence()
                    .mapNotNull { rule ->
                        rule.regex.find(text)?.let { result -> rule to result.value }
                    }
                    .firstOrNull()
                when {
                    match != null -> {
                        val (rule, value) = match
                        "$relativePath contains mojibake marker '$value' (${rule.description})"
                    }
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

        private data class MojibakeRule(
            val description: String,
            val regex: Regex
        )

        private val windows1251SecondByteChars =
            "[ЂЃ‚ѓ„…†‡€‰Љ‹ЊЌЋЏђ‘’“”•–—™љ›њќћџЎўЈ¤Ґ¦§Ё©Є«¬®Ї°±Ііґµ¶·ё№є»јЅѕї]"
        private val windows1251MojibakeChunk = "[РС]$windows1251SecondByteChars"

        private val mojibakeRules = listOf(
            MojibakeRule(
                description = "UTF-8 Cyrillic decoded as Windows-1252 or Latin-1",
                regex = Regex("[ÐÑÂÃ]")
            ),
            MojibakeRule(
                description = "UTF-8 punctuation decoded as Windows-1252 or Windows-1251",
                regex = Regex("â€|â„|â€¦|вЂ|вњ")
            ),
            MojibakeRule(
                description = "UTF-8 Cyrillic decoded as Windows-1251",
                regex = Regex("Р[Ўџ†]|С[Њѓ]")
            ),
            MojibakeRule(
                description = "ambiguous Рі marker next to another decoded UTF-8 chunk",
                regex = Regex("(?:$windows1251MojibakeChunk)Рі|Рі(?:$windows1251MojibakeChunk)")
            ),
            MojibakeRule(
                description = "double-decoded Cyrillic text",
                regex = Regex("Гђ|Г‘|Г‚|Гѓ|Гўв‚¬|ГўвЂћ|Гўв‚¬В¦|РІР‚|РІСљ")
            )
        )
    }
}
