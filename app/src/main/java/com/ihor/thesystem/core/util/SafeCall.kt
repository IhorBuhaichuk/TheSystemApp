package com.ihor.thesystem.core.util

import com.ihor.thesystem.domain.model.AppErrorType
import com.ihor.thesystem.domain.model.DomainError
import kotlinx.coroutines.CancellationException

/**
 * Універсальна обгортка для безпечного виконання корутин з деталізацією помилок.
 */
/**
 * Спеціалізована функція для перехоплення помилок у саспенд-корутинах.
 * Важливо: вона явно прокидає CancellationException, щоб не порушувати 
 * механізм скасування корутин в Android (наприклад, при ViewModel.onCleared).
 */
inline fun <T> runSuspendCatching(block: () -> T): kotlin.Result<T> {
    return try {
        kotlin.Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        kotlin.Result.failure(e)
    }
}
