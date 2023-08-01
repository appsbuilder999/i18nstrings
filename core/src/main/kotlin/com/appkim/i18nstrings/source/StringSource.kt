package com.appkim.i18nstrings.source

import com.appkim.i18nstrings.CodeInfo

abstract class StringSource {

    abstract fun contains(codeInfo: CodeInfo, key: String, rawText: String): Boolean

    abstract fun getString(codeInfo: CodeInfo, key: String, rawText: String): String?
}