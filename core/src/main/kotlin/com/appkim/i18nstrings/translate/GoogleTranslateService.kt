package com.appkim.i18nstrings.translate

import com.appkim.i18nstrings.CodeInfo
import com.appkim.i18nstrings.utils.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

class GoogleTranslateService : BaseTranslateService(500) {
    companion object {
        private val codes = listOf("af","sq","am","ar","hy","as","ay","az","bm","eu","be","bn","bho","bs","bg","ca","ceb","ny","zh-CN","zh-TW","co","hr","cs","da","dv","doi","nl","en","eo","et","ee","tl","fi","fr","fy","gl","ka","de","el","gn","gu","ht","ha","haw","iw","hi","hmn","hu","is","ig","ilo","id","ga","it","ja","jw","kn","kk","km","rw","gom","ko","kri","ku","ckb","ky","lo","la","lv","ln","lt","lg","lb","mk","mai","mg","ms","ml","mt","mi","mr","mni-Mtei","lus","mn","my","ne","no","or","om","ps","fa","pl","pt","pa","qu","ro","ru","sm","sa","gd","nso","sr","st","sn","sd","si","sk","sl","so","es","su","sw","sv","tg","ta","tt","te","th","ti","ts","tr","tk","ak","uk","ur","ug","uz","vi","cy","xh","yi","yo","zu")

        val codesMap = mutableMapOf<String, String>().apply {
            codes.forEach { code ->
                put(code.lowercase(), code) }
        }

        private val hardCodeMap = mutableMapOf<String, String>().apply {
            put("auto", "auto")

            //Android
            put("mni-IN", "mni-Mtei")

            //google cloud API
            // Hebrew Language new code is he, but the old code is iw, and google translate use iw
            put("he", "iw")
            put("fil", "tl")
            put("jv", "jw")

            //special region
            put("zh-HK", "zh-TW")
        }
    }

    fun getGoogleSupportedCode(code: CodeInfo):String? {
        val language = code.language
        val region = code.region
        val codeText = if(region.isEmpty()) language else "$language-$region"

        val googleCode: String? = if(hardCodeMap.contains(codeText)) {
            hardCodeMap[codeText]
        } else {
            if(codesMap.contains(codeText.lowercase())) {
                codesMap[codeText.lowercase()]
            } else {
                codesMap[language.lowercase()]
            }
        }

        return googleCode
    }

    override suspend fun translateSingle(
        strText: String,
        dstLangCode: CodeInfo,
        srcLangCode: CodeInfo,
        retries: Int
    ): String {

        if (strText.length > getMaxStringCount(srcLangCode)) return strText

        val dstCode = getGoogleSupportedCode(dstLangCode)
        if(dstCode!!.lowercase() != dstLangCode.code.lowercase() && dstCode != "en") {
            Log.w("Google Translate does not support the language ${dstLangCode.code}, use $dstCode instead")
        }
        val srcCode = getGoogleSupportedCode(srcLangCode)

        val serializedText = serializeText(strText)
        //there are a lot of translate projects in github but false."https://translate.google.com/translate_a/single"
        val url = "https://translate.google.com/m?sl=$srcCode&tl=$dstCode&q=$serializedText"
        val beforeTrans = "class=\"result-container\">"
        val afterTrans = "</div>"

        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

        repeat(retries) {
            try {
                val response: HttpResponse = client.get(url)
                val htmlResponse = response.bodyAsText()
                val translationStart = htmlResponse.indexOf(beforeTrans) + beforeTrans.length
                val translationEnd = htmlResponse.indexOf(afterTrans, translationStart)
                return deserializeText(
                    htmlResponse.substring(translationStart, translationEnd).trim()
                )
            } catch (e: Exception) {
                if (it == retries - 1) {  // last retry, throw exception
                    Log.e("Google Translate fails, make sure your network can visit https://translate.google.com")
                    throw e
                }
            }
        }

        //To ensure that all paths have returns or throw exceptions, add a RuntimeException that is impossible to reach. under normal circumstances, this exception will not be thrown.
        throw RuntimeException("Unreachable")
    }

    override fun isSupportedLanguage(codeInfo: CodeInfo): Boolean {
        val googleCode = getGoogleSupportedCode(codeInfo)
        return googleCode != null
    }
}