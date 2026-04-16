package com.ihor.thesystem.core.util

import timber.log.Timber
import javax.inject.Inject

class TimberLoggerImpl @Inject constructor() : AppLogger {
    override fun d(message: String, tag: String?) {
        if (tag != null) Timber.tag(tag).d(message) else Timber.d(message)
    }

    override fun i(message: String, tag: String?) {
        if (tag != null) Timber.tag(tag).i(message) else Timber.i(message)
    }

    override fun w(message: String, tag: String?) {
        if (tag != null) Timber.tag(tag).w(message) else Timber.w(message)
    }

    override fun e(throwable: Throwable?, message: String, tag: String?) {
        val t = if (tag != null) Timber.tag(tag) else Timber
        if (throwable != null) {
            t.e(throwable, message)
        } else {
            t.e(message)
        }
    }
}
