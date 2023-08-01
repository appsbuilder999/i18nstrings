package com.appkim.i18nstrings.translate

import com.appkim.i18nstrings.CodeInfo
import com.appkim.i18nstrings.utils.Log
import kotlinx.coroutines.delay
import org.apache.commons.text.StringEscapeUtils
import java.net.URLEncoder

abstract class BaseTranslateService(private val delayTime: Long) : TranslateService() {

    companion object {
        var MAX_LEN = 4000

        // sr, all kinds of separators tried, all get translated, affects character cutting <div> \n -> \н %s -> %с
        // TODO： Another way to think about it, for that language, is to use the translated separator directly 另一种思路，针对该语言，直接使用翻译后的分隔符
        // ug TODO Verify it later
        // lus Less than 40% translation rate for batch translation
        // hmn Less than 40% translation rate for batch translation
        val unsupportedBatchCodes = setOf("sr", "ug", "lus", "hmn")
        val secondBatchLogicCodes = setOf<String>()

    }

    private var mergeLogic: MergeLogic = MergeLogicImpl()

    override suspend fun translateBatch(
        srcList: List<String>,
        dstLangCode: CodeInfo,
        srcLangCode: CodeInfo
    ): List<String> {

        val retList = ArrayList<String>(srcList)

        val textList = ArrayList<String>()
        val indexList = ArrayList<Int>()

        retList.forEachIndexed { index, it ->
            if (it.length < getMaxStringCount(srcLangCode)) {
                textList.add(it)
                indexList.add(index)
            }
        }

        mergeLogic = if (!secondBatchLogicCodes.contains(dstLangCode.code)) {
            MergeLogicImpl()
        } else {
            MergeLogicImpl2()
        }

        val transList = translateBatchInternal(textList, dstLangCode, srcLangCode)

        transList.forEachIndexed { index, it ->
            retList[indexList[index]] = it
        }

        return retList
    }

    override fun isSupportedBatch(codeInfo: CodeInfo): Boolean {
        return !unsupportedBatchCodes.contains(codeInfo.code)
    }

    /**
     * Different languages accept different lengths, so here's a simple mapping
     */
    fun getMaxStringCount(langCode: CodeInfo): Int {
        return if (langCode == CodeInfo.default) {
            MAX_LEN
        } else {
            MAX_LEN / 2
        }
    }

    private suspend fun translateBatchInternal(
        srcList: List<String>,
        dstLangCode: CodeInfo,
        srcLangCode: CodeInfo
    ): List<String> {

        val translatedList = ArrayList<String>()
        var startIndex = 0
        val maxLen = getMaxStringCount(srcLangCode)

        // Translate up to MAX_LEN characters at one time.
        while (startIndex < srcList.size) {
            val mergeResult = mergeStrings(srcList, startIndex, maxLen)
            val endIndex = mergeResult.first
            val mergeText = mergeResult.second
            val size = endIndex - startIndex + 1

            Log.d("TranslateService total size: ${srcList.size} startIndex: $startIndex endIndex: $endIndex size: $size")

            val transText = translateSingle(mergeText, dstLangCode, srcLangCode)
            val transList = splitStrings(transText, size)

//            if(size != transList.size) {
//                Log.d(mergeText)
//            }

//            Log.d("mergeText: $mergeText")
//            Log.d("transText: $transText")


            if (delayTime > 0) {
                delay(delayTime)
            }

            //Log.d("transList.size: ${transList.size} size: $size")

            if (transList.size != size) {
                //Translation failed, enter retry logic
                retry(startIndex, endIndex) { start, end ->

                    val retryMergeText = mergeStringsWithRange(srcList, start, end)
                    val retryTrans = translateSingle(retryMergeText, dstLangCode, srcLangCode)
                    val retrySize = end - start + 1
                    val retryTransList = splitStrings(retryTrans, retrySize)

//                    Log.d("hopeSize: $retrySize splitSize: ${retryTransList.size}")
//                    if(retrySize != retryTransList.size) {
//                        Log.d(retryMergeText)
//                    }

                    var ret = true
                    if (retryTransList.size == retrySize) {
                        translatedList.addAll(retryTransList)
                    } else {
                        ret = false
                    }
                    ret
                }

                startIndex = endIndex + 1
            } else {
                startIndex = endIndex + 1
                translatedList.addAll(transList)
            }
        }

        return translatedList
    }

    /**
     * Retry mechanism
     * @param start Start Position
     * @param end End Position
     * @param runMethod Retry content
     * @return
     * @throws Exception
     * Specify a range (start - end), split the range into two halves, retry to execute runMethod, if runMethod returns true, then execute the other half, otherwise continue to split in half retrying
     * throws an exception when it can no
     */
    private suspend fun retry(start: Int, end: Int, runMethod: suspend (Int, Int) -> Boolean) {
        var len = (end - start + 1) / 2
        var l = start
        var r = start + len - 1

        var retryCount = 0
        while (l <= end) {
            if (delayTime > 0)
                delay(delayTime)

            val ret = runMethod(l, r)
            Log.d("ret: $ret start: $l end: $r size: ${r - l + 1}")
            if (ret) {
                if (retryCount > 0) {
                    len *= 2
                    retryCount--
                }
                l = r + 1
                r = Math.min(l + len - 1, end)
            } else {
                len /= 2
                r = Math.min(l + len - 1, end)
                retryCount++
                if (len == 0) throw Exception("translate error")
            }
        }
    }

    /**
     * Merge multiple strings into one string, merge multiple strings into one string to submit the translation for the following reasons:
     * 1. the actual project has more texts, usually more than 500, for each text to call the translation interface, query time-consuming, if you use the free interface, but also easy to seal the number
     * 2. The most important one! The translated text is often associated with several nearby texts, now most of the translations use ai, the merged text is conducive to contextual analysis, to provide a more accurate translation!
     */
    private fun mergeStrings(toTranslate: List<String>, startIndex: Int, maxLen: Int): Pair<Int, String> {
        val stringBuilder = StringBuilder()

        val tagHead = mergeLogic.getTagHead()
        val tagEnd = mergeLogic.getTagEnd()
        val tagItemStart = mergeLogic.getTagItemStart()
        val tagItemEnd = mergeLogic.getTagItemEnd()
        val textExtra = mergeLogic.getTextExtra()

        stringBuilder.append(tagHead)

        var endIndex = toTranslate.size - 1

        val fixedStringLen =
            tagItemStart.length + tagItemEnd.length + tagEnd.length + textExtra.length

        for (i in startIndex until toTranslate.size) {
            val preLen = stringBuilder.length + toTranslate[i].length + fixedStringLen

            if (preLen > maxLen) {
                stringBuilder.append(tagEnd)
                endIndex = i - 1
                break
            }

            stringBuilder.append(tagItemStart)
            stringBuilder.append(toTranslate[i])
            stringBuilder.append(tagItemEnd)

            if (i == (toTranslate.size - 1)) {
                stringBuilder.append(tagEnd)
                endIndex = i
                break
            }
        }

        val text = stringBuilder.toString() + textExtra

        return Pair(endIndex, text)
    }

    /**
     * This method has already determined that the merged string is less than maxLen, so it does not need to determine
     */
    private fun mergeStringsWithRange(
        toTranslate: List<String>,
        startIndex: Int,
        endIndex: Int
    ): String {
        val stringBuilder = StringBuilder()

        val tagHead = mergeLogic.getTagHead()
        val tagEnd = mergeLogic.getTagEnd()
        val tagItemStart = mergeLogic.getTagItemStart()
        val tagItemEnd = mergeLogic.getTagItemEnd()
        val textExtra = mergeLogic.getTextExtra()

        stringBuilder.append(tagHead)

        for (i in startIndex..endIndex) {
            stringBuilder.append(tagItemStart)
            stringBuilder.append(toTranslate[i])
            stringBuilder.append(tagItemEnd)
        }

        stringBuilder.append(tagEnd)

        return stringBuilder.toString() + textExtra
    }

    private fun splitStrings(text: String, hopeSize: Int): List<String> {

        //Filtering and correcting copy as a whole
        var str = mergeLogic.preprocessText(text)

        //Remove the extra string added at the end
        str = mergeLogic.removeExtraText(str)

        str = mergeLogic.removeHeadEnd(str)

        //Cutting up the content
        return mergeLogic.splitText(str, hopeSize)
    }

    fun serializeText(text: String): String {
        return URLEncoder.encode(text, "UTF-8")
    }

    fun deserializeText(text: String): String {
        return StringEscapeUtils.unescapeHtml4(text)
    }


    //Record the methods that were tried and failed, in case anyone wants to try them
    //code: ug text: <size of folder><Enter Zip Name><No App found to open this file><Copying><Moving><File><Folder><New Folder><Enter Name><Extracting><Stopping><Open Parent Directory>
    //Return result：https://translate.google.com/m?sl=en&tl=ug&hl=en&q=%3Csize+of+folder%3E%3CEnter+Zip+Name%3E%3CNo+App+found+to+open+this+file%3E%3CCopying%3E%3CMoving%3E%3CFile%3E%3CFolder%3E%3CNew+Folder%3E%3CEnter+Name%3E%3CExtracting%3E%3CStopping%3E%3COpen+Parent+Directory%3E
    //hopeSize: 12 splitSize: 3
    class MergeLogicImpl2 : MergeLogic() {
        override fun getTagHead(): String {
            return ""
        }

        override fun getTagEnd(): String {
            return ""
        }

        override fun getTagItemStart(): String {
            return "["
        }

        override fun getTagItemEnd(): String {
            return "]"
        }

        override fun getTextExtra(): String {
            return ""
        }

        override fun preprocessText(text: String): String {
            return text.replace("] [", "][")
        }

        override fun removeExtraText(text: String): String {
            return text
        }

        override fun removeHeadEnd(text: String): String {
            return text.trim().substring(1, text.length - 1)
        }

        override fun splitText(text: String, hopeSize: Int): List<String> {
            return text.split("][")
            //if (split.size == hopeSize)
        }

    }


}