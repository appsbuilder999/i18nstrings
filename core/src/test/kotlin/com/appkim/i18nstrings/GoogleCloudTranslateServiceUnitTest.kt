package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.GoogleCloudTranslateService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GoogleCloudTranslateServiceUnitTest {

    @Test
    fun updateAndroidStrings() = runTest(timeout = Duration.INFINITE) {
        val service = GoogleCloudTranslateService("/Users/petyrzhan/Desktop/subtle-sublime-390803-89081a28c480.json")
        val result = service.translateSingle("hello world", CodeInfo.fromCode("zh-CN"), CodeInfo.default, 1)
        println("result: $result")
    }
}