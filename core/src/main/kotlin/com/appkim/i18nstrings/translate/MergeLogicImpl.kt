package com.appkim.i18nstrings.translate

import com.appkim.i18nstrings.utils.Log
import java.util.HashMap

class MergeLogicImpl : MergeLogic() {

    private val head = "div72"
    private val end = "div24"
    private val itemStart = "div86"
    private val itemEnd = "div18"

    private val tagHead = "<$head>"
    private val tagEnd = "<$end>"
    private val tagItemStart = "<$itemStart>"
    private val tagItemEnd = "<$itemEnd>"

    private val textExtra = "<div33>hell, world</div63>"


    private fun getReplaceTempMap(): Map<String, List<String>> {
        val map = HashMap<String, List<String>>()

        map[tagHead] = listOf(" $tagHead", "$tagHead ")
        map[tagEnd] = listOf(" $tagEnd", "$tagEnd ")
        map[tagItemStart] = listOf(" $tagItemStart", "$tagItemStart ")
        map[tagItemEnd] = listOf(" $tagItemEnd", "$tagItemEnd ")

        map[tagItemEnd + tagItemStart] =
            listOf("$tagItemEnd $itemStart>", "$tagItemEnd$itemStart>")

        return map
    }


    /**
     * Remove spaces in div
     */
    fun replaceDivAndNumbers(input: String): String {
        val pattern = """<\s*div[^>]*\d+[^>]*>""".toRegex(
            setOf(
                RegexOption.IGNORE_CASE,
                RegexOption.DOT_MATCHES_ALL
            )
        )
        val replaced = input.replace(pattern) {
            it.value.replace(" ", "")
        }
        return replaced
    }


    /**
     * Exceptions that are prone to occur after translation are handled, after which they will be processed twice at the time of cutting
     */
    override fun preprocessText(text: String): String {

        var str = replaceDivAndNumbers(text)

        val map = getReplaceTempMap()

        map.forEach {
            val key = it.key
            val list = it.value
            list.forEach{ item ->
                str = str.replace(item, key)
            }
        }

        return str
    }

    /**
     * Handle the various exception displays for the separator. The logic for processing is to test each exception logic once, and if the test passes, then no other exception logic is processed
     */
    override fun splitText(text: String, hopeSize: Int): List<String> {

        var split = text.split(tagItemEnd + tagItemStart)
        if (split.size == hopeSize) return split


//        Log.d("1. split.size: ${split.size} hopeSize: $hopeSize")
//        Log.d(text)

        val divider = "[split98]"
        var temp = text.replace(tagItemEnd + tagItemStart, divider)

        //<div1><div86>One of them is complete, the other is missing numbers 其中一个完整，另一个缺数字
        val regex1 = ("<div\\d+>$tagItemStart").toRegex()
        val regex2 = ("$tagItemEnd<div\\d+>").toRegex()
        temp = regex1.replace(temp, divider)
        temp = regex2.replace(temp, divider)
        split = temp.split(divider)
        if (split.size == hopeSize) return split

//        Log.d("2. split.size: ${split.size} hopeSize: $hopeSize")
//        Log.d(temp)

//        val logStr = temp

        //<div18S><div86>, <div18><. div86> <div18div86>
        val regex3 = ("<.{0,2}$itemEnd.{0,6}$itemStart.{0,2}>").toRegex()
        temp = regex3.replace(temp, divider)
        val regex4 = Regex("(?<!<)$itemEnd><$itemStart>")
        temp = regex4.replace(temp, divider)
        val regex5 = Regex("<$itemEnd><$itemStart(?!>)")
        temp = regex5.replace(temp, divider)

        /**
         * There are various other exceptions that can only be hard-coded partially
         */
        temp = temp.replace("<di ><div86>", divider)
            .replace("<div18><di >", divider)
            .replace("<div1div86>", divider)
            .replace("><di<div86>", divider)
            .replace("<div18di><v >", divider)
            .replace("<div18><div8O", divider)
            .replace("<div18>", divider)
            .replace("div86>", divider)


        //Log.d("temp: $temp")

        split = temp.split(divider)
 //       if (split.size != hopeSize) {
            //Log.d("3. split.size: ${split.size} hopeSize: $hopeSize")
            //Log.d(temp)
//            Log.d(text)
//            Log.d(logStr)
            //split.forEach(::println)
//        }

        return split
    }

    /**
     * Translation of the returned results, occasionally the last tag is converted to an incorrect format, such as "MD5 <870530> <6032 42 >", suspected that google did some tricks,
     * So deliberately add some content at the end and then remove it.
     */
    override fun removeExtraText(text: String): String {
        if (textExtra.isEmpty()) return text

        var str = text
        /**
         * The ending div is wrong, use div33 to mark the replacement
         * <div72><div86>Exciting<div86>Gee Toast<div18><86>Gee India<div18><div4><div33>hel, wire</div62>
         */
        if (!str.contains(tagEnd)) {
            val targetIndex = str.indexOf("<div33>")
            val lastOpenBracketIndexBeforeTarget = str.lastIndexOf('<', targetIndex - 1)
            if (lastOpenBracketIndexBeforeTarget >= 0) {
                str = str.substring(0, lastOpenBracketIndexBeforeTarget) + tagEnd
            }
        }

        return if (str.contains(tagEnd)) {
            str = str.substringBefore(tagEnd) + tagEnd

            str
        } else {

            Log.d("error end: $text")
            //Returns an error result and the upper cut string will fail, leading to retranslation
            "error end"
        }
    }

    override fun getTagHead(): String {
        return tagHead
    }

    override fun getTagEnd(): String {
        return tagEnd
    }

    override fun getTagItemStart(): String {
        return tagItemStart
    }

    override fun getTagItemEnd(): String {
        return tagItemEnd
    }

    override fun getTextExtra(): String {
        return textExtra
    }

    override fun removeHeadEnd(text: String): String {
        var str = text.trim()
        //If no header or tail is set, it is returned directly, at which point the cut is made, and the first and last words will carry STRING_QUOTE_HEAD and STRING_QUOTE_END
        str = if (tagHead.isNotEmpty()) {
            str.replace(tagHead + tagItemStart, "")
                .replace(tagItemEnd + tagEnd, "")
        } else {
            str.substring(tagItemStart.length, str.length - tagItemEnd.length)
        }
        return str
    }

}