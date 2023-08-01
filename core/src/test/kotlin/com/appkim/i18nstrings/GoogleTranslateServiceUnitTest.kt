package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.GoogleCloudTranslateService
import com.appkim.i18nstrings.translate.GoogleTranslateService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GoogleTranslateServiceUnitTest {

    @Test
    fun getGoogleSupportedCode() {
        val service = GoogleTranslateService()
        val code = service.getGoogleSupportedCode(CodeInfo.fromCode("zh-HK"))
        println(code)
        assert(code == "zh-TW")
    }

    @Test
    fun isSupportedLanguage() {
        val service = GoogleTranslateService()
        assert(service.isSupportedLanguage(CodeInfo.fromCode("zh-CN")))
    }


}