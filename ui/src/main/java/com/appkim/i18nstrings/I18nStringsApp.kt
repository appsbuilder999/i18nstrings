package com.appkim.i18nstrings

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import com.appkim.i18nstrings.utils.Log
import com.appkim.i18nstrings.utils.I18nLogger
import java.io.PrintWriter
import java.io.StringWriter


object I18nStringsApp {

    var supportedRealtimeTranslate: Boolean = true
    var supportedLocalTranslateFile: Boolean = true
    var localTranslationDir: String = ""


    private lateinit var appHelper: I18nStringsAppHelper
    lateinit var model: I18nStringsAppModel

    fun init(context: Context) {

        Log.i18nLogger = I18nStringsAppHelper.Logcat()

        model = I18nStringsAppModel(context)
        appHelper = I18nStringsAppHelper(model)
        appHelper.initData(context)
    }

    fun wrapResources(applicationContext: Context, resources: Resources): Resources {
        return RestringHelper.wrapResources(applicationContext, resources)
    }

    fun getAppCompatDelegate(context: Context, delegate: AppCompatDelegate): AppCompatDelegate {
        return RestringHelper.getAppCompatDelegate(context, delegate)
    }

    fun getEstimatedTime(context: Context, codeInfo: CodeInfo): Pair<Int, Int> {
        return appHelper.getEstimatedTime(context, codeInfo)
    }

    suspend fun loadOnlineLanguage(context: Context, codeInfo: CodeInfo) {
        appHelper.loadOnlineLanguage(context, codeInfo)
    }

}

