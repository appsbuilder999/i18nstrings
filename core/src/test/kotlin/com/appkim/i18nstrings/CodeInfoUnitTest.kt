package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.GoogleCloudTranslateService
import com.appkim.i18nstrings.translate.GoogleTranslateService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CodeInfoUnitTest {


    @Test
    fun isSupportedLanguage() {
        val set = mutableSetOf<CodeInfo>()

        val result1 = set.add(CodeInfo.fromResCode("zh-rCN"))
        val result2 = set.add(CodeInfo.fromResCode("zh-rCN"))
        assert(result1 == true)
        assert(result2 == false)
    }


}