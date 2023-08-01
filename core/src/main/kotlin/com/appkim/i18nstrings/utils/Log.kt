package com.appkim.i18nstrings.utils

object Log {

    var i18nLogger: I18nLogger? = null

    fun d(message: String) {
        i18nLogger?.debug(message)
    }

    fun i(message: String) {
        i18nLogger?.info(message)
    }

    fun w(message: String) {
        i18nLogger?.warn(message)
    }

    fun e(message: String) {
        i18nLogger?.error(message)
    }

    fun e(e: Exception) {
        i18nLogger?.error(e)
    }
}

abstract class I18nLogger {
    abstract fun debug(message: String)

    abstract fun info(message: String)

    abstract fun warn(message: String)

    abstract fun error(message: String)

    abstract fun error(e: Exception)
}