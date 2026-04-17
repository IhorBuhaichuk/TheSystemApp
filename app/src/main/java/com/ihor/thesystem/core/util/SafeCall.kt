package com.ihor.thesystem.core.util

import com.ihor.thesystem.domain.model.AppError
import com.ihor.thesystem.domain.model.DomainError
import kotlinx.coroutines.CancellationException

/**
 * Універсальна обгортка для безпечного виконання корутин.
 * Перехоплює всі Exception, повертаючи Result.Error(AppError.Message),
 * але коректно прокидає CancellationException для правильної роботи Coroutines.
 */
suspend fun <T> safeCall(action: suspend () -> Result<T, DomainError>): Result<T, DomainError> {
    return try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(AppError.Message(e.localizedMessage ?: "Unknown Error"))
    }
}
