package com.ihor.thesystem.core.ui

import com.ihor.thesystem.R
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.AppErrorType // Corrected import
import com.ihor.thesystem.domain.model.DataError // Corrected import

fun DomainError.asUiText(): UiText {
    return when (this) {
        DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
        DataError.Local.NOT_FOUND -> UiText.StringResource(R.string.error_unknown)
        DataError.Local.SQLITE_EXCEPTION -> UiText.StringResource(R.string.error_operation_failed)
        DataError.Local.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
        
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_timeout)
        DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_unknown) 
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server_error)
        DataError.Network.SERIALIZATION -> UiText.StringResource(R.string.error_operation_failed)
        DataError.Network.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
        
        AppErrorType.Unknown -> UiText.StringResource(R.string.error_unknown)
        AppErrorType.AiParsingError -> UiText.StringResource(R.string.error_ai_parsing)
        is AppErrorType.Message -> UiText.DynamicString(this.message ?: "")
        
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
