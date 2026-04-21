package com.ihor.thesystem.domain.model

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText

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
    object AiParsingError : AppError
    data class Message(val text: String) : AppError
}

fun DomainError.asUiText(): UiText {
    return when (this) {
        DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
        DataError.Local.NOT_FOUND -> UiText.StringResource(R.string.error_unknown)
        DataError.Local.SQLITE_EXCEPTION -> UiText.StringResource(R.string.error_operation_failed)
        DataError.Local.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
        
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_timeout)
        DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_operation_failed)
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server_error)
        DataError.Network.SERIALIZATION -> UiText.StringResource(R.string.error_operation_failed)
        DataError.Network.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
        
        AppError.Unknown -> UiText.StringResource(R.string.error_unknown)
        AppError.AiParsingError -> UiText.DynamicString("Помилка генерації AI, спробуйте ще раз")
        is AppError.Message -> UiText.DynamicString(this.text)
        
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
