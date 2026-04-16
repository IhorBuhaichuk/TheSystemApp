package com.ihor.thesystem.domain.model

sealed interface DomainError

sealed interface DataError : DomainError {
    enum class Local : DataError {
        DISK_FULL,
        SQLITE_EXCEPTION,
        NOT_FOUND,
        UNKNOWN
    }
    
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN
    }
}

sealed interface AppError : DomainError {
    object Unknown : AppError
    data class Message(val text: String) : AppError
}
