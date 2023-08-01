package com.appkim.i18nstrings

import java.util.Locale

class CodeInfo private constructor(val locale: Locale, val isBCP47: Boolean = false) {

    val language: String
        get() = locale.language!!

    val region: String
        get() {
            return locale.country ?:""
        }

    val code: String
        get() = locale.toLanguageTag()!!

    val resCode: String
        get() {
            val map = mutableMapOf<String, String>().apply {
                put("he", "iw")
                put("mni-Mtei".lowercase(), "mni-rIN")
            }
            if(map.contains(code)) {
                return map[code]!!
            } else {
                if(isBCP47) {
                    return "b+" + locale.toLanguageTag().replace("-", "+")
                } else {
                    return if(region.isEmpty()) language else "${language.lowercase()}-r${region.uppercase()}"
                }

            }
        }

    val name: String
        get() {
            return locale.displayName
        }

    companion object {

        val codes = listOf("af","sq","am","ar","hy","as","ay","az","bm","eu","be","bn","bho","bs","bg","ca","ceb","zh-CN","zh-TW","co","hr","cs","da","dv","doi","nl","en","eo","et","ee","fil","fi","fr","fy","gl","ka","de","el","gn","gu","ht","ha","haw","he","iw","hi","hmn","hu","is","ig","ilo","id","ga","it","ja","jv","jw","kn","kk","km","rw","gom","ko","kri","ku","ckb","ky","lo","la","lv","ln","lt","lg","lb","mk","mai","mg","ms","ml","mt","mi","mr","mni-Mtei","lus","mn","my","ne","no","ny","or","om","ps","fa","pl","pt","pa","qu","ro","ru","sm","sa","gd","nso","sr","st","sn","sd","si","sk","sl","so","es","su","sw","sv","tl","tg","ta","tt","te","th","ti","ts","tr","tk","ak","uk","ur","ug","uz","vi","cy","xh","yi","yo","zu")
        val names = listOf("Afrikaans","Albanian","Amharic","Arabic","Armenian","Assamese","Aymara","Azerbaijani","Bambara","Basque","Belarusian","Bengali","Bhojpuri","Bosnian","Bulgarian","Catalan","Cebuano","Chinese (Simplified)","Chinese (Traditional)","Corsican","Croatian","Czech","Danish","Dhivehi","Dogri","Dutch","English","Esperanto","Estonian","Ewe","Filipino (Tagalog)","Finnish","French","Frisian","Galician","Georgian","German","Greek","Guarani","Gujarati","Haitian Creole","Hausa","Hawaiian","Hebrew","Hebrew","Hindi","Hmong","Hungarian","Icelandic","Igbo","Ilocano","Indonesian","Irish","Italian","Japanese","Javanese","Javanese","Kannada","Kazakh","Khmer","Kinyarwanda","Konkani","Korean","Krio","Kurdish","Kurdish (Sorani)","Kyrgyz","Lao","Latin","Latvian","Lingala","Lithuanian","Luganda","Luxembourgish","Macedonian","Maithili","Malagasy","Malay","Malayalam","Maltese","Maori","Marathi","Meiteilon (Manipuri)","Mizo","Mongolian","Myanmar (Burmese)","Nepali","Norwegian","Nyanja (Chichewa)","Odia (Oriya)","Oromo","Pashto","Persian","Polish","Portuguese (Portugal, Brazil)","Punjabi","Quechua","Romanian","Russian","Samoan","Sanskrit","Scots Gaelic","Sepedi","Serbian","Sesotho","Shona","Sindhi","Sinhala (Sinhalese)","Slovak","Slovenian","Somali","Spanish","Sundanese","Swahili","Swedish","Tagalog (Filipino)","Tajik","Tamil","Tatar","Telugu","Thai","Tigrinya","Tsonga","Turkish","Turkmen","Twi (Akan)","Ukrainian","Urdu","Uyghur","Uzbek","Vietnamese","Welsh","Xhosa","Yiddish","Yoruba","Zulu")

        private val codeMap = mutableMapOf<String, CodeInfo>()

        fun fromCode(code: String): CodeInfo {
            if(codeMap.contains(code)) return codeMap[code]!!

            val local = Locale.forLanguageTag(code)

            val codeInfo = CodeInfo(local)
            codeMap[codeInfo.code] = codeInfo
            return codeInfo
        }

        val default = fromCode("en-US")
        val auto = fromCode("auto")
        val codeInfos = mutableListOf<CodeInfo>().apply {
            codes.forEach {
                add(CodeInfo.fromCode(it))
            }
        }

        fun from(language: String, region: String): CodeInfo {
            val locale = if(region.isEmpty()) {
                Locale(language)
            } else {
                Locale(language, region)
            }

            if(codeMap.contains(locale.toLanguageTag())) return codeMap[locale.toLanguageTag()]!!

            val codeInfo = CodeInfo(locale)
            codeMap[codeInfo.code] = codeInfo
            return codeInfo
        }

        fun fromResCode(resCode: String): CodeInfo {

            var tagPart = resCode
            if (tagPart.startsWith("b+")) {
                tagPart = tagPart.substring(2).replace('+', '-')
            } else {
                val dashIndex = tagPart.indexOf('-')
                if (dashIndex != -1) {
                    tagPart = tagPart.substring(0, dashIndex) + "-" + tagPart.substring(dashIndex + 2)
                }
            }

            val locale =  Locale.forLanguageTag(tagPart)

            if(codeMap.contains(locale.toLanguageTag())) return codeMap[locale.toLanguageTag()]!!

            val codeInfo = CodeInfo(locale, resCode.contains("b+"))
            codeMap[codeInfo.code] = codeInfo
            return codeInfo
        }

        private val availableLocales = Locale.getAvailableLocales().map {
            it.toLanguageTag()
        }.toHashSet()
    }

    fun isDefault(): Boolean {
        return this == default
    }

    override fun toString(): String {
        return code
     }

    fun isResCodeAvailable(): Boolean {
        return locale.toLanguageTag() != "und" && availableLocales.contains(locale.toLanguageTag())
    }
}