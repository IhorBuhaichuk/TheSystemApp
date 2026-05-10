package com.ihor.thesystem.domain.model

sealed interface DomainError {
    val message: String?
}

sealed interface DataError : DomainError {
    enum class Local : DataError {
        DISK_FULL,
        SQLITE_EXCEPTION,
        NOT_FOUND,
        UNKNOWN;

        override val message: String?
            get() = when (this) {
                DISK_FULL -> "Disk full error"
                SQLITE_EXCEPTION -> "Database error"
                NOT_FOUND -> "Not found"
                UNKNOWN -> "Unknown local error"
            }
    }

    enum class Network : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN;

        override val message: String?
            get() = when (this) {
                REQUEST_TIMEOUT -> "Request timeout"
                TOO_MANY_REQUESTS -> "Too many requests"
                NO_INTERNET -> "No internet connection"
                SERVER_ERROR -> "Server error"
                SERIALIZATION -> "Serialization error"
                UNKNOWN -> "Unknown network error"
            }
    }
}

sealed interface AppErrorType : DomainError {
    object Unknown : AppErrorType {
        override val message: String? = "An unknown app error occurred."
    }
    object AiParsingError : AppErrorType {
        override val message: String? = "AI parsing error"
    }
    data class Message(val text: String) : AppErrorType {
        override val message: String? = text
    }
}

interface ExceptionMapper {
    fun map(e: Throwable): DomainError
}

enum class ValidationError : DomainError {
    INVALID_PLAYER_NAME,
    INVALID_WEIGHT,
    INVALID_HEIGHT,
    INVALID_AGE;

    override val message: String?
        get() = name
}
