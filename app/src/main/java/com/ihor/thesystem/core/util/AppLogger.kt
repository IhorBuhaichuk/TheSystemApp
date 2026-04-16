package com.ihor.thesystem.core.util

interface AppLogger {
    fun d(message: String, tag: String? = null)
    fun i(message: String, tag: String? = null)
    fun w(message: String, tag: String? = null)
    fun e(throwable: Throwable? = null, message: String, tag: String? = null)
}
