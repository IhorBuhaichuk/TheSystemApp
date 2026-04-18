package com.ihor.thesystem.domain.model

interface AppError {
    val message: String?
}

sealed interface DomainError : AppError {
    override val message: String?
        get() = when (this) {
            is UnknownError -> "An unknown error occurred."
            is DatabaseError -> throwable?.localizedMessage ?: "A database error occurred."
            is NetworkError -> message ?: "A network error occurred."
            is Message -> message
        }

    object UnknownError : DomainError
    data class DatabaseError(val throwable: Throwable? = null) : DomainError
    data class NetworkError(val code: Int? = null, override val message: String? = null) : DomainError
    data class Message(override val message: String) : DomainError
}

interface ExceptionMapper {
    fun map(e: Throwable): DomainError
}