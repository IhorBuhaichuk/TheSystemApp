package com.ihor.thesystem.domain.util

/**
 * Санітизує рядок для безпечного вставлення в AI промпт.
 * Видаляє символи, що можуть порушити структуру JSON або промпту (", ', {, }),
 * замінює переноси рядків на пробіли та обмежує довжину.
 */
fun String.sanitizeForPrompt(): String {
    return this.replace("\"", "")
        .replace("'", "")
        .replace("{", "[")
        .replace("}", "]")
        .replace("\n", " ")
        .replace("\r", " ")
        .trim()
        .let { if (it.length > 500) it.take(500) else it }
}
