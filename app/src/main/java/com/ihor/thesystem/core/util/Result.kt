package com.ihor.thesystem.core.util

import com.ihor.thesystem.domain.model.DomainError

sealed interface Result<out D, out E : DomainError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : DomainError>(val error: E) : Result<Nothing, E>
}

inline fun <D, E : DomainError, T> Result<D, E>.map(transform: (D) -> T): Result<T, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(transform(data))
    }
}

fun <D, E : DomainError> Result<D, E>.asEmptyResult(): Result<Unit, E> {
    return map { }
}

fun <D, E : DomainError> Result<D, E>.getOrNull(): D? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }
}
