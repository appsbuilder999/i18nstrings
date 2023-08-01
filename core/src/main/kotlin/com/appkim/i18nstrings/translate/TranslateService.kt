package com.appkim.i18nstrings.translate

import com.appkim.i18nstrings.CodeInfo

abstract class TranslateService {
    /**
     * Translate a single string
     */
    abstract suspend fun translateSingle(
        strText: String,
        dstLangCode: CodeInfo,
        srcLangCode: CodeInfo,
        retries: Int = 3  // This is the default value for the number of retries
    ): String

    /**
     * Batch translation of string lists
     */
    abstract suspend fun translateBatch(
        srcList: List<String>,
        dstLangCode: CodeInfo,
        srcLangCode: CodeInfo
    ): List<String>

    /**
     * Does the language support batch translation
     */
    abstract fun isSupportedBatch(codeInfo: CodeInfo): Boolean

    abstract fun isSupportedLanguage(codeInfo: CodeInfo): Boolean
}