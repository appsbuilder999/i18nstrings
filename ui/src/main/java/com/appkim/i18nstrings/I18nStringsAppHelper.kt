package com.appkim.i18nstrings

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.appkim.i18nstrings.utils.CostTime
import com.appkim.i18nstrings.utils.I18nLogger
import com.appkim.i18nstrings.utils.Log
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.restring.PluralKeyword
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.restring.StringRepository
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale

class I18nStringsAppHelper(private val model: I18nStringsAppModel) {

    fun initData(context: Context) {

        val type = model.selectedType
        val code = model.selectedCode
        if(type != I18nStringsAppModel.TYPE_NONE && code != null) {

            RestringHelper.initRestring(context)

            val codeInfo = CodeInfo.fromCode(code)
            val locale = codeInfo.locale

            if(type == I18nStringsAppModel.TYPE_RELEASE) {
                AppLocale.desiredLocale = locale
            } else {

                if(Restring.stringRepository.supportedLocales.contains(locale)) {
                    AppLocale.desiredLocale = locale
                } else {
                    val map = when(type) {
                        I18nStringsAppModel.TYPE_BETA -> getStringsMapFromAssets(context, codeInfo)
                        I18nStringsAppModel.TYPE_ONLINE_CACHE -> getStringsMapFromDir(codeInfo, "${model.cacheDir}/${AndroidParser.betaFolderName}")
                        I18nStringsAppModel.TYPE_LOCAL -> getStringsMapFromLocal(codeInfo)
                        else -> mapOf()
                    }
                    if(map.isNotEmpty()) {
                        Restring.putStrings(locale, map)
                        AppLocale.desiredLocale = locale
                    }
                }
            }

        } else {
            RestringHelper.initRestring(context, true)
        }

        model.initTypeName(context)
    }

    private fun getStringsMapFromAssets(context: Context, codeInfo: CodeInfo): Map<String, CharSequence> {
        try {
            val inputStream =
                context.resources.assets.open(AndroidParser.betaFolderName + "/strings-${codeInfo.resCode}.xml")
            val reader = inputStream.bufferedReader()
            val text = reader.readText().trim()

            return AndroidParser().parseXmlToMap(text)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return emptyMap()
    }

    private fun getStringsMapFromLocal(codeInfo: CodeInfo): Map<String, CharSequence> {
        return getStringsMapFromDir(codeInfo, "${I18nStringsApp.localTranslationDir}/${AndroidParser.betaFolderName}")
    }

    private fun getStringsMapFromDir(codeInfo: CodeInfo, dir: String): Map<String, CharSequence> {
        try {
            val inputStream = FileInputStream(File("$dir/strings-${codeInfo.resCode}.xml"))
            val reader = inputStream.bufferedReader()
            val text = reader.readText().trim()

            return AndroidParser().parseXmlToMap(text)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return emptyMap()
    }

    fun getEstimatedTime(context: Context, codeInfo: CodeInfo): Pair<Int, Int> {

        val strings = model.readStrings(context)

        val totalTime = CostTime.getTime(codeInfo, strings.size)

        return Pair(strings.size, totalTime)
    }

    suspend fun loadOnlineLanguage(context: Context, codeInfo: CodeInfo) {

        val items = mutableListOf<AndroidParser.Item>()

        val strings = mutableListOf<StringItem>()

        val stringsName = model.readStrings(context)
        val resources = context.resources
        stringsName.forEach {
            val stringId = resources.getIdentifier(it, "string", context.packageName)
            strings.add(StringItem(it, stringId, ""))
        }

        val retStrings = getLocaleStringResource(context, Locale.ENGLISH, strings)


        if(retStrings.isNotEmpty()) {
            items.addAll(retStrings.map { AndroidParser.Item(it.name, it.value, false) })
            val parser = AndroidParser()
            parser.updateDir(items, model.cacheDir, codeInfo)
        }
    }

    data class StringItem(val name: String, val id: Int, val value: String)

    private fun getLocaleStringResource(
        context: Context,
        requestedLocale: Locale,
        strings: List<StringItem>
    ): List<StringItem> {
        val retList = mutableListOf<StringItem>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // use latest api
            val config = Configuration(context.resources.configuration)
            config.setLocale(requestedLocale)
            val enContext = context.createConfigurationContext(config)

            strings.forEach {
                val value = enContext.resources.getString(it.id)
                retList.add(StringItem(it.name, it.id, value))
            }

        } else { // support older android versions
            val res = context.resources
            val conf: Configuration = res.configuration
            val savedLocale: Locale = conf.locale
            conf.locale = requestedLocale
            res.updateConfiguration(conf, null)

            // retrieve resources from desired locale

            strings.forEach {
                val value = res.getString(it.id)
                retList.add(StringItem(it.name, it.id, value))
            }

            // restore original locale
            conf.locale = savedLocale
            res.updateConfiguration(conf, null)
        }
        return retList
    }


    class Logcat: I18nLogger() {
        override fun debug(message: String) {
            android.util.Log.d("i18nString", message)
        }

        override fun info(message: String) {
            android.util.Log.i("i18nString", message)
        }

        override fun warn(message: String) {
            android.util.Log.w("i18nString", message)
        }

        override fun error(message: String) {
            android.util.Log.e("i18nString", message)
        }

        override fun error(e: Exception) {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            val exceptionAsString = sw.toString()
            Log.e(exceptionAsString)
            error(exceptionAsString)
        }
    }
}