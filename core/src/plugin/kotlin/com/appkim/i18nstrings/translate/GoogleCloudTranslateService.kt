package com.appkim.i18nstrings.translate

import com.appkim.i18nstrings.CodeInfo
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import java.io.File
import java.io.FileInputStream


/**
 * How to use Google Translate API in Android Studio projects
 * https://medium.com/@yeksancansu/how-to-use-google-translate-api-in-android-studio-projects-7f09cae320c7
 */
class GoogleCloudTranslateService(private val credentialsFilePath: String = "subtle-sublime-390803-89081a28c480.json") : BaseTranslateService(0) {

    private var translate: Translate

    companion object {
        private val codes = listOf("af","sq","am","ar","hy","as","ay","az","bm","eu","be","bn","bho","bs","bg","ca","ceb","zh-CN","zh-TW","co","hr","cs","da","dv","doi","nl","en","eo","et","ee","fil","fi","fr","fy","gl","ka","de","el","gn","gu","ht","ha","haw","he","iw","hi","hmn","hu","is","ig","ilo","id","ga","it","ja","jv","jw","kn","kk","km","rw","gom","ko","kri","ku","ckb","ky","lo","la","lv","ln","lt","lg","lb","mk","mai","mg","ms","ml","mt","mi","mr","mni-Mtei","lus","mn","my","ne","no","ny","or","om","ps","fa","pl","pt","pa","qu","ro","ru","sm","sa","gd","nso","sr","st","sn","sd","si","sk","sl","so","es","su","sw","sv","tl","tg","ta","tt","te","th","ti","ts","tr","tk","ak","uk","ur","ug","uz","vi","cy","xh","yi","yo","zu")
        private val codesMap = mutableMapOf<String, String>().apply {
            codes.forEach { code ->
                put(code.lowercase(), code) }
        }

        private val hardCodeMap = mutableMapOf<String, String>().apply {
            val tmp = mutableMapOf<String, String>()
            tmp["auto"] = "auto"

            //Android
            tmp["mni-IN"] = "mni-Mtei"

            //special region
            tmp["zh-HK"] = "zh-TW"

            tmp.forEach { (key, value) ->
                put(key.lowercase(), value.lowercase()) }
        }
    }


    init {
        val file = File(credentialsFilePath)

        //Get credentials:
        val myCredentials = GoogleCredentials.fromStream(
            FileInputStream(file))

        //Set credentials and get translate service:
        val translateOptions =
            TranslateOptions.newBuilder().setCredentials(myCredentials).build()
        translate = translateOptions.service
    }

    override suspend fun translateSingle(
        strText: String,
        dstLangCode: CodeInfo,
        srcLangCode: CodeInfo,
        retries: Int
    ): String {

        if (strText.length > getMaxStringCount(srcLangCode)) return strText

        val dstCode = getGoogleSupportedCode(dstLangCode)
        val srcCode = getGoogleSupportedCode(srcLangCode)

        val translation: Translation = translate.translate(
            strText,
            Translate.TranslateOption.sourceLanguage(srcCode),
            Translate.TranslateOption.targetLanguage(dstCode),
            Translate.TranslateOption.model("base")
        )

        return translation.translatedText
    }

    override fun isSupportedLanguage(codeInfo: CodeInfo): Boolean {
        val googleCode = getGoogleSupportedCode(codeInfo)
        return googleCode != null
    }


    private fun getGoogleSupportedCode(code: CodeInfo):String? {
        val language = code.language
        val region = code.region
        val codeText = if(region.isEmpty()) language.lowercase() else "$language-$region".lowercase()

        val googleCode: String? = if(hardCodeMap.contains(codeText)) {
            hardCodeMap[codeText]
        } else {
            if(codesMap.contains(codeText)) {
                codesMap[codeText]
            } else {
                codesMap[language.lowercase()]
            }
        }

        return googleCode
    }

//    override suspend fun translateBatch(
//        srcList: List<String>,
//        dstLangCode: String,
//        srcLangCode: String
//    ): List<String> {
//
//        val dstCode =
//            if (codeInGoogle.contains(dstLangCode)) codeInGoogle[dstLangCode] else dstLangCode
//        val srcCode =
//            if (codeInGoogle.contains(srcLangCode)) codeInGoogle[srcLangCode] else srcLangCode
//
//        val translations = translate.translate(
//            srcList,
//            Translate.TranslateOption.sourceLanguage(srcCode),
//            Translate.TranslateOption.targetLanguage(dstCode),
//            Translate.TranslateOption.model("base")
//        )
//
//        return translations.map { it.translatedText }
//    }
}