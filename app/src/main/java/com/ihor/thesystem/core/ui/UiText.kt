package com.ihor.thesystem.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.model.MessageTextKey

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args.toTypedArray())
        }
    }

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args.toTypedArray())
        }
    }
}

fun MessageText.asString(context: Context): String =
    when (this) {
        is MessageText.DynamicString -> value
        is MessageText.Resource -> context.getString(key.stringResId(), *args.toTypedArray())
    }

@Composable
fun MessageText.asString(): String =
    when (this) {
        is MessageText.DynamicString -> value
        is MessageText.Resource -> stringResource(key.stringResId(), *args.toTypedArray())
    }

@StringRes
private fun MessageTextKey.stringResId(): Int =
    when (this) {
        MessageTextKey.ERROR_AI_UNCONFIGURED -> R.string.error_ai_unconfigured
        MessageTextKey.ERROR_AI_GENERIC -> R.string.error_ai_generic
        MessageTextKey.ERROR_AI_PARSING -> R.string.error_ai_parsing
        MessageTextKey.ERROR_AI_RATE_LIMIT -> R.string.error_ai_rate_limit
        MessageTextKey.ERROR_AI_OVERLOADED -> R.string.error_ai_overloaded
        MessageTextKey.AI_ANALYSIS_COMPLETE -> R.string.ai_analysis_complete
        MessageTextKey.AI_FALLBACK_ACTIVATED -> R.string.ai_fallback_activated
        MessageTextKey.ARCHITECT_INITIAL_MESSAGE -> R.string.architect_initial_message
        MessageTextKey.ARCHITECT_NO_DATA -> R.string.architect_no_data
        MessageTextKey.ARCHITECT_SEND_ANALYSIS -> R.string.architect_btn_send_analysis
        MessageTextKey.ERROR_NETWORK_ARCHITECT -> R.string.error_network_architect
        MessageTextKey.ARCHITECT_DIRECTIVES_APPLIED -> R.string.architect_directives_applied
    }
