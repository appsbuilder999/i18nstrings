package com.appkim.i18nstrings.utils

import com.appkim.i18nstrings.CodeInfo

object CostTime {

    private const val averageTime = 194

    private val timeMap = mutableMapOf<String, Int>()

    fun getTimeText(codeInfo: CodeInfo, size: Int): String {
        val time = timeMap[codeInfo.code]?: averageTime
        val totalTime = Math.max(time * size / 1000, 1) + 10
        //cover to 1min 10s format
        val min = totalTime / 60
        val sec = totalTime % 60
        if(min > 0) {
            return "${min}min ${sec}s"
        } else {
            return "${sec}s"
        }
    }

    fun getTime(codeInfo: CodeInfo, size: Int): Int {
        val time = timeMap[codeInfo.code]?: averageTime
        val totalTime = Math.max(time * size / 1000, 1) + 10
        return totalTime
    }

    init {
        timeMap.apply {
            put("hr",47)
            put("af",49)
            put("nl",52)
            put("vi",50)
            put("bg",51)
            put("da",54)
            put("id",53)
            put("ca",52)
            put("fy",55)
            put("sv",53)
            put("cs",52)
            put("he",52)
            put("kri",53)
            put("hu",53)
            put("pt",54)
            put("is",54)
            put("",54)
            put("sl",54)
            put("no",55)
            put("lo",55)
            put("su",57)
            put("gom",54)
            put("ts",55)
            put("fr",56)
            put("ro",59)
            put("ay",57)
            put("ht",59)
            put("de",61)
            put("bho",56)
            put("zh-TW",58)
            put("be",59)
            put("bm",63)
            put("mk",60)
            put("gl",63)
            put("ilo",63)
            put("",63)
            put("sk",63)
            put("el",64)
            put("it",67)
            put("eo",64)
            put("es",66)
            put("sq",68)
            put("tr",66)
            put("lv",67)
            put("jv",71)
            put("ckb",67)
            put("bs",72)
            put("sw",69)
            put("nso",69)
            put("kk",70)
            put("ln",73)
            put("uk",72)
            put("ru",75)
            put("eu",75)
            put("lb",85)
            put("hy",77)
            put("qu",77)
            put("ko",77)
            put("zh-CN",78)
            put("ti",78)
            put("fi",92)
            put("mt",89)
            put("pl",83)
            put("mi",82)
            put("mk",83)
            put("tl",96)
            put("mn",85)
            put("ee",84)
            put("ak",89)
            put("az",90)
            put("as",87)
            put("zu",93)
            put("ja",91)
            put("mt",101)
            put("ky",95)
            put("fi",96)
            put("tg",95)
            put("gn",97)
            put("te",96)
            put("doi",95)
            put("et",98)
            put("haw",101)
            put("dv",96)
            put("mni-Mtei",96)
            put("bn",100)
            put("cy",100)
            put("ceb",119)
            put("ku",102)
            put("mi",101)
            put("th",102)
            put("st",112)
            put("uz",108)
            put("lb",124)
            put("mg",121)
            put("om",113)
            put("gd",119)
            put("my",125)
            put("yi",118)
            put("pa",121)
            put("lg",121)
            put("lt",123)
            put("mg",140)
            put("co",151)
            put("ar",138)
            put("mr",132)
            put("gu",135)
            put("ny",147)
            put("fa",142)
            put("ga",142)
            put("ne",145)
            put("sm",148)
            put("yo",154)
            put("hi",163)
            put("am",167)
            put("ka",171)
            put("sa",175)
            put("ml",184)
            put("ur",195)
            put("kn",189)
            put("ml",205)
            put("km",217)
            put("ta",223)
            put("ps",227)
            put("si",350)
            put("sn",454)
            put("xh",430)
            put("ig",458)
            put("mai",422)
            put("ha",497)
            put("mai",458)
            put("sd",554)
            put("so",709)
            put("la",999)
            put("tt",734)
            put("la",1145)
            put("or",904)
            put("tk",958)
            put("hmn",1326)
            put("rw",1229)
            put("ug",1329)
            put("lus",1287)
            put("rw",1793)
        }
    }


}