package com.appkim.i18nstrings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import androidx.core.content.ContextCompat
import com.appkim.i18nstrings.translate.GoogleTranslateService
import com.appkim.i18nstrings.ui.R
import java.io.File
import java.io.IOException

class I18nStringsAppModel(context: Context) {

    companion object {
        private const val SP_NAME = "i18n_strings"
        private const val KEY_SELECTED_TYPE = "selected_type"
        private const val KEY_SELECTED_CODE = "selected_code"
        private const val KEY_DISCLAIMER_AGREE = "disclaimer_agree"

        const val TYPE_NONE = 0
        const val TYPE_RELEASE = 1
        const val TYPE_BETA = 2
        const val TYPE_ONLINE_CACHE = 3
        const val TYPE_LOCAL = 4
        const val TYPE_ONLINE = 5

        private var typeNameNone = "Follow System"
        private var typeNameRelease = "Release"
        private var typeNameBeta = "Beta"
        private var typeNameOnlineCache = "Online Cache"
        private var typeNameLocal = "Local"
        private var typeNameOnline = "Online"

    }

    data class LanguageItem(val codeInfo: CodeInfo, var type: Int) {
        val name: String
            get(){

                val name = when (type) {
                    TYPE_NONE -> typeNameNone
                    TYPE_RELEASE -> codeInfo.name + "(${codeInfo.code})" + " (${typeNameRelease})"
                    TYPE_BETA -> codeInfo.name + "(${codeInfo.code})" + " (${typeNameBeta})"
                    TYPE_ONLINE_CACHE -> codeInfo.name + "(${codeInfo.code})" + " (${typeNameOnlineCache})"
                    TYPE_LOCAL -> codeInfo.name + "(${codeInfo.code})" + " (${typeNameLocal})"
                    else -> codeInfo.name + "(${codeInfo.code})" + " (${typeNameOnline})"
                }

                return name
            }
    }

    var cacheDir: String

    var selectedType = 0
    var selectedCode: String? = null
    var items = mutableListOf<LanguageItem>()
    var isConfigFileExist = false


    init {
        cacheDir = context.cacheDir.absolutePath
        isConfigFileExist = readFileExist(context)

        val releaseCodes = readReleaseCodes(context)
        val betaCodes = readBetaCodes(context)
        val localCodes = readLocalCodes(context)
        val cacheCodes = readCacheCodes()
        val onlineCodes = readOnlineCodes()

        val languageTagMap = mutableSetOf<String>()

        items.add(LanguageItem(CodeInfo.fromCode("None"), TYPE_NONE))

        releaseCodes.forEach {
            items.add(LanguageItem(it, TYPE_RELEASE))
            languageTagMap.add(it.code)
        }
        betaCodes.forEach {
            if(!languageTagMap.contains(it.code)) {
                items.add(LanguageItem(it, TYPE_BETA))
                languageTagMap.add(it.code)
            }
        }
        if(I18nStringsApp.supportedLocalTranslateFile) {
            localCodes.forEach {
                if(!languageTagMap.contains(it.code)) {
                    items.add(LanguageItem(it, TYPE_LOCAL))
                    languageTagMap.add(it.code)
                }
            }
        }
        if(I18nStringsApp.supportedRealtimeTranslate) {
            cacheCodes.forEach {
                if(!languageTagMap.contains(it.code)) {
                    items.add(LanguageItem(it, TYPE_ONLINE_CACHE))
                    languageTagMap.add(it.code)
                }
            }
            onlineCodes.forEach {
                if(!languageTagMap.contains(it.code)) {
                    items.add(LanguageItem(it, TYPE_ONLINE))
                    languageTagMap.add(it.code)
                }
            }

        }

        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        selectedType = sp.getInt(KEY_SELECTED_TYPE, 0)
        selectedCode = sp.getString(KEY_SELECTED_CODE, "")

        items.sortWith(compareBy({ item ->
            if(item.type == selectedType && selectedType == 0) {
                0
            } else {
                if(item.type == selectedType && item.codeInfo.code == selectedCode) {
                    0
                } else {
                    item.type + 1
                }
            }

        }, { it.codeInfo.name }))
    }

    fun initTypeName(context: Context) {
        typeNameNone = context.getString(R.string.i18n_type_none)
        typeNameRelease = context.getString(R.string.i18n_type_release)
        typeNameBeta = context.getString(R.string.i18n_type_beta)
        typeNameOnlineCache = context.getString(R.string.i18n_type_online_cache)
        typeNameLocal = context.getString(R.string.i18n_type_local)
        typeNameOnline = context.getString(R.string.i18n_type_online)
    }

    private fun readBetaCodes(context: Context): List<CodeInfo> {
        val codes = mutableListOf<CodeInfo>()
        try {
            val assets = context.assets.list(AndroidParser.betaFolderName)
            if (assets != null) {
                for (asset in assets) {
                    try {
                        val pattern = Regex("strings-(.*).xml")
                        val matchResult = pattern.find(asset)
                        val androidCode = matchResult?.groupValues?.get(1)
                        if(androidCode != null) {
                            val codeInfo = CodeInfo.fromResCode(androidCode)
                            //Since Locale.getAvailableLocales() returns the language supported by the system, the return is inconsistent under different java support, resulting in some languages can not be read properly.
                            //zh-HK
                            //if(codeInfo.isResCodeAvailable())
                            codes.add(codeInfo)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return codes
    }

    private fun readCacheCodes(): List<CodeInfo> {
        return readCodes("$cacheDir/${AndroidParser.betaFolderName}")
    }

    private fun readLocalCodes(context: Context): List<CodeInfo> {
        if(hasReadWriteStoragePermission(context)) return listOf()

        if(I18nStringsApp.localTranslationDir.isEmpty() || !File(I18nStringsApp.localTranslationDir).exists()) return listOf()

        return readCodes(I18nStringsApp.localTranslationDir)
    }

    private fun hasReadWriteStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun readCodes(path: String): List<CodeInfo> {
        val codes = mutableListOf<CodeInfo>()
        try {
            val files = File(path).list()
            if (files != null) {
                for (file in files) {
                    try {
                        Log.i("Asset: ", file)
                        val pattern = Regex("strings-(.*).xml")
                        val matchResult = pattern.find(file)
                        val androidCode = matchResult?.groupValues?.get(1)
                        Log.i("androidCode: ", "$androidCode")
                        if(androidCode != null) {
                            val codeInfo = CodeInfo.fromResCode(androidCode)
                            codes.add(codeInfo)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return codes
    }

    /**
     * 读取系统支持的语言列表，并且根据Google翻译的支持情况进行过滤
     * 不同方法获取的语言列表区分如下：
     * 1. Locals.getAvailableLocales()方法只能获取到Java本身支持的语言列表，不是系统的语言支持列表
     * 2. Resources.getSystem().getConfiguration().locales 获取的是Setting界面中用户添加的语言列表
     * 3. Resources.getSystem().assets.locales 获取的是系统支持的语言列表
     * 注意：通过#3获取的语言，往往比设置界面能添加的语言要多，原因是厂商对部分语言做了屏蔽，用户无法添加，但系统本身是支持的
     */
    private fun readOnlineCodes(): List<CodeInfo> {
        val locales = Resources.getSystem().assets.locales
        val codeInfos = mutableListOf<CodeInfo>()

        for(local in locales) {

            val codeInfo = CodeInfo.fromCode(local)
            if(local == "ja-JP") {
                println(local)
            }
            if(GoogleTranslateService.codesMap.contains(codeInfo.code.lowercase())) {
                if(local == "ja-JP") {
                    println(local)
                }
                codeInfos.add(codeInfo)
            }
        }

        return codeInfos
    }

    private fun readFileExist(context: Context): Boolean {
        return try {
            val inputStream1 = context.assets.open(AndroidParser.betaFolderName + "/" + AndroidParser.resourcesConfigFileName)
            inputStream1.close() // Don't forget to close InputStream
            val inputStream2 = context.assets.open(AndroidParser.betaFolderName + "/" + AndroidParser.stringsConfigFileName)
            inputStream2.close() // Don't forget to close InputStream
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Android的API没有提供获取app的语言的方法，所以只能通过读取assets下自己写入的release_codes.config文件来获取
     */
    private fun readReleaseCodes(context: Context): List<CodeInfo> {
        val codes = mutableListOf<CodeInfo>()

        try {
            val inputStream = context.assets.open(AndroidParser.betaFolderName + "/" + AndroidParser.resourcesConfigFileName)
            val reader = inputStream.bufferedReader()
            val text = reader.readText().trim()

            codes.addAll(text.split(",").map { CodeInfo.fromCode(it.trim()) })
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return codes
    }

    /**
     * 如果直接通过R文件获取，获取到的strings数量会比实际的多（举例：文案实际有600多条，但R有3000多条），原因是R文件中包含了系统的strings和用户定义的一些不需要翻译的字符串
     */
    fun readStrings(context: Context): List<String> {
        val strings = mutableListOf<String>()

        try {
            val inputStream = context.assets.open(AndroidParser.betaFolderName + "/" + AndroidParser.stringsConfigFileName)
            val reader = inputStream.bufferedReader()
            val text = reader.readText().trim()

            strings.addAll(text.split(","))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return strings
    }

    fun setSelectedLanguageItems(context: Context, item: LanguageItem) {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_SELECTED_CODE, item.codeInfo.code).commit()
        sp.edit().putInt(KEY_SELECTED_TYPE, item.type).commit()
    }

    fun setDisclaimerAgree(context: Context, agree: Boolean) {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_DISCLAIMER_AGREE, agree).commit()
    }

    fun getDisclaimerAgree(context: Context): Boolean {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_DISCLAIMER_AGREE, false)
    }
}