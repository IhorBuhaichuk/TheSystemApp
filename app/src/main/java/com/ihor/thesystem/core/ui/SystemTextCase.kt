package com.ihor.thesystem.core.ui

import java.util.Locale

internal val UkrainianLocale: Locale = Locale.forLanguageTag("uk-UA")

/**
 * Display casing for product labels: the first letter is uppercase and
 * subsequent letters are lowercase. Leading numbers and punctuation are kept.
 */
fun String.toSystemSentenceCase(locale: Locale = Locale.getDefault()): String {
    if (isBlank()) return this
    val lowercase = lowercase(locale)
    val firstLetterIndex = lowercase.indexOfFirst(Char::isLetter)
    if (firstLetterIndex < 0) return lowercase
    return buildString(lowercase.length) {
        append(lowercase, 0, firstLetterIndex)
        append(
            lowercase.substring(firstLetterIndex, firstLetterIndex + 1)
                .uppercase(locale)
        )
        append(lowercase, firstLetterIndex + 1, lowercase.length)
    }
}
