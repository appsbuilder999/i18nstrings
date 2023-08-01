package com.appkim.i18nstrings

import com.appkim.i18nstrings.source.StringSource
import com.appkim.i18nstrings.translate.GoogleTranslateService
import com.appkim.i18nstrings.translate.TranslateService

object I18nStrings {
    var translationService: TranslateService = GoogleTranslateService()
    var extraStringsSource: StringSource? = null
}