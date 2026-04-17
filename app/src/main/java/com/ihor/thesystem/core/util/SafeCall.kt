package com.ihor.thesystem.core.util

import com.ihor.thesystem.domain.model.AppError
import com.ihor.thesystem.domain.model.DomainError
import kotlinx.coroutines.CancellationException

/**
 * Універсальна обгортка для безпечного виконання корутин з деталізацією помилок.
 *
 * @param errorMapper Функція для перетворення Exception у специфічний DomainError.
 * @param action Основна дія, що повертає Result.
 */
suspend fun <T> safeCall(
    errorMapper: (Exception) -> DomainError = { 
        // За замовчуванням намагаємося розпізнати SQLiteException через ім'я класу,
        // щоб не залежати від Android SDK у домені.
        if (it.javaClass.simpleName.contains("SQLiteException")) {
            com.ihor.thesystem.domain.model.DataError.Local.SQLITE_EXCEPTION
        } else {
            AppError.Message(it.localizedMessage ?: "Unknown Error")
        }
    },
    action: suspend () -> Result<T, DomainError>
): Result<T, DomainError> {
    return try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(errorMapper(e))
    }
}
