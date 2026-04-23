package com.ihor.thesystem.core.util

import com.ihor.thesystem.domain.model.AppErrorType
import com.ihor.thesystem.domain.model.DomainError
import kotlinx.coroutines.CancellationException

/**
 * Універсальна обгортка для безпечного виконання корутин з деталізацією помилок.
 */
suspend /**
 * Аналог runCatching для корутин, який не поглинає CancellationException.
 * Використовує стандартний kotlin.Result.
 */
inline fun <R> runSuspendCatching(block: () -> R): kotlin.Result<R> {
    return try {
        kotlin.Result.success(block())
    } catch (c: kotlinx.coroutines.CancellationException) {
        throw c
    } catch (e: Throwable) {
        kotlin.Result.failure(e)
    }
}
