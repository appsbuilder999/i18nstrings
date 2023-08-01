package com.appkim.i18nstrings.translate

import com.appkim.i18nstrings.CodeInfo
import com.appkim.i18nstrings.I18nStrings
import com.appkim.i18nstrings.utils.Log
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.io.ByteArrayInputStream
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

class TranslationManager(val translateService: TranslateService = I18nStrings.translationService) {

    data class LocalizedItem(
        val name: String,
        var content: String
    )

    companion object {
        // not support sr, it will translate any tag <div> \n -> \н %s -> %с
        val unsupportedBatchCodes = setOf("sr")
    }

    private suspend fun translateWithoutBatch(
        srcItems: List<LocalizedItem>,
        toLanguage: CodeInfo,
        language: CodeInfo
    ): List<LocalizedItem> {

        var list = srcItems.mapIndexed { index, item ->
            //translation of text
            Log.d("index: $index, content: ${item.content}")
            val text = translateService.translateSingle(item.content, toLanguage, language)
            LocalizedItem(item.name, text)
        }

        //Handles translated formatting exceptions such as % s, % d, % 1 $ s, etc.
        list = checkStringFormat(list, srcItems)

        //Handling of abnormal line breaks after translation, e.g. \ n
        list = checkLineBreakCount(list, srcItems)

        //Do the final check, mark the abnormal copy for output to facilitate subsequent processing, and at the same time copy the source copy into the translated copy to avoid abnormal program operation crash
        list = checkStringFormatAndLineBreakFinal(list, srcItems, toLanguage)

        //If there are other exceptions to the string, you can do the processing in this method
        list = checkOtherStringException(list)

        return list
    }

    suspend fun translate(
        srcItems: List<LocalizedItem>,
        dstCode: CodeInfo,
        srcCode: CodeInfo
    ): List<LocalizedItem> {

        val transItems = if (translateService.isSupportedBatch(dstCode)) {
            translateBatch(srcItems, dstCode, srcCode)
        } else {
            translateWithoutBatch(srcItems, dstCode, srcCode)
        }

        return transItems
    }

    /**
     * This method mainly handles some abnormal cases of translation
     * 1. translated content, there are parts of the content is still not translated, collect these contents, re-execute the translation process once again
     * 2. Handle abnormal formatting content after translation, such as % s, % d, % 1 $ s, etc. * 3.
     * 3. Handle html content that has been translated abnormally, such as \ n, etc.
     */
    private suspend fun translateBatch(
        srcItems: List<LocalizedItem>,
        dstCode: CodeInfo,
        srcCode: CodeInfo, isReverse: Boolean = false
    ): List<LocalizedItem> {

        val contents = srcItems.map { it.content }

        //translation of text
        var list = translateService.translateBatch(contents, dstCode, srcCode)
            .mapIndexed { index, item -> LocalizedItem(srcItems[index].name, item) }

        //Checking for untranslated copy, doing second translations, and increasing the translation rate
        list = checkTranslationContentsAndRetry(list, srcItems, dstCode, srcCode, isReverse)

        //Both cases are already filtered ahead of time when back-translating to correct the translation, so there is no need to perform these methods
        if (!isReverse) {
            //Handles translated formatting exceptions such as % s, % d, % 1 $ s, etc.
            list = checkStringFormat(list, srcItems)

            //Handling of abnormal line breaks after translation, e.g. \ n
            list = checkLineBreakCount(list, srcItems)

            //After the above processing, there are still abnormal formatting content, or abnormal line breaks, here again for translation, each time only one translation, not translation merge
            list = checkStringExceptionAndReTranslation(list, srcItems, dstCode, srcCode)

            //Do a final check to replace the abnormal copy with the source copy to avoid crashing the program running abnormally
            list = checkStringFormatAndLineBreakFinal(list, srcItems, dstCode)
        }

        //If there are other exceptions to the string, you can do the processing in this method
        list = checkOtherStringException(list)

        //If it is currently translating back, it no longer enters the translation back logic
        if (!isReverse && dstCode != CodeInfo.default) {
            //Back-translate translations, check for accuracy, and optimize translations
            list = checkReverseTranslation(list, srcItems, dstCode, srcCode)
        }

        return list
    }

    /**
     * The text is checked by the back-translation method, and if the back-translation is less than 40% similar to the source text, the translation is considered inaccurate, and the text is re-translated line by line.
     */
    private suspend fun checkReverseTranslation(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>,
        dstCode: CodeInfo,
        srcCode: CodeInfo
    ): List<LocalizedItem> {

        //Getting text that needs to be back-translated
        val needReverseItems = getNeedReverseItems(dstItems, srcItems)

        //translated text
        val reversedItems = translateBatch(needReverseItems, srcCode, dstCode, true)

        //Evaluate back-translated text and return content that needs to be re-translated
        val needTransItems = getNeedTranslateItems(reversedItems, srcItems)

        //Line-by-line translation of text to be reTranslated and returned
        val retItems = reTranslateItems(
            dstItems,
            srcItems,
            needTransItems,
            reversedItems,
            dstCode,
            srcCode
        )

        return retItems
    }

    /**
     * Output how much the translation is improved with the addition of back-translation.
     * There is a strange problem with the data. Take the sm language for example
     * 1. without adding back-translation method to optimize the translation, the back-translation rate is 74%, the number of articles is 583, and the list of texts to be back-translated is defined as reversedItems.
     * 2. For the texts filtered out by the back-translation method that need to be re-translated, translate them one by one. And then batch translation of the article-by-article translation of the text to update the reversedItems, the rate of translation is 86%, the number of articles is 583
     * 3. write back the text translated line by line to the original translated text, and then carry out fullReversedItems, the rate of translation is 80%, and the number of articles is 577.
     * For the above problem, the back translation rate is 6% less, about 35 texts.
     * At first I thought it was a bug in my code, but after locating the testReversePerformanceSaveRawData and testReversePerformanceSaveDebug methods, I confirmed that it was not a bug
     * Detecting fullReversedItems added 55 pieces of data that need to be re-translated, only 2-3 are valuable, the rest do not need to be re-translated
     *
     */
//    suspend fun testReversePerformanceSaveRawData(
//        dstItems: List<LocalizedItem>,
//        srcItems: List<LocalizedItem>,
//        needTransItems: List<LocalizedItem>,
//        reversedItems: List<LocalizedItem>,
//        dstCode: CodeInfo,
//        srcCode: CodeInfo
//    ) {
//
//        val dstMap = HashMap<String, LocalizedItem>()
//        dstItems.forEach {
//            dstMap[it.name] = it
//        }
//        val smallNeedReverseItems = needTransItems.map {
//            val text = dstMap[it.name]!!.content
//            LocalizedItem(it.name, text)
//        }
//        val smallReversedItems =
//            translateBatch(smallNeedReverseItems, srcCode, dstCode, true)
//
//        val smallReversedMap = HashMap<String, LocalizedItem>()
//        smallReversedItems.forEach {
//            smallReversedMap[it.name] = it
//        }
//
//        val newReversedItems = reversedItems.map {
//            if (smallReversedMap.contains(it.name)) {
//                val text = smallReversedMap[it.name]!!.content
//                LocalizedItem(it.name, text)
//            } else {
//                it.copy()
//            }
//        }
//
//        val fullNeedReverseItems = getNeedReverseItems(dstItems, srcItems)
//        val fullReversedItems = translateBatch(fullNeedReverseItems, srcCode, dstCode, true)
//
//        val dir = "test_data/method_debugReversePerformance_batch"
//
//        val parser = AndroidParser()
//
//        parser.writeToJsonFile(srcItems, "srcItems.json", dir)
//        parser.writeToJsonFile(reversedItems, "reversedItems.json", dir)
//        parser.writeToJsonFile(newReversedItems, "newReversedItems.json", dir)
//        parser.writeToJsonFile(fullReversedItems, "fullReversedItems.json", dir)
//
//    }


    fun getNeedTranslateItems(
        reversedItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>
    ): List<LocalizedItem> {

        val map = HashMap<String, LocalizedItem>()
        srcItems.forEach {
            map[it.name] = it
        }

        val needTransItems = ArrayList<LocalizedItem>()
        reversedItems.forEach {
            val srcItem = map[it.name]!!

            if (needReTranslateBySimilarity(srcItem.content, it.content)) {
                needTransItems.add(srcItem)
            }
        }

        return needTransItems
    }

    /**
     * Detecting translation results that need to be optimized and optimizing them through back-translation methods
     *
     * After back-translation optimization, the correct rate of back-translation can be increased from 78% to 87% for language sm
     * Checked the content of back-translation replacement, 48 pieces of data, there are 30 corrected obvious errors, as follows
     * apps srcText: [App Manager] firstReverseText: [Edit Edit] secondReverseText: [App Manager]
     * Conclusion: from the point of view of translation result optimization, the back-translation method is feasible, but it will increase the translation time-consuming, and we need to think of a solution.
     */
    suspend fun reTranslateItems(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>,
        needTransItems: List<LocalizedItem>,
        reversedItems: List<LocalizedItem>,
        dstCode: CodeInfo,
        srcCode: CodeInfo
    ): List<LocalizedItem> {


        Log.d("needTransList: ${needTransItems.size}")

        // The original is a piece-by-piece translation, by comparing the quality of piece-by-piece translation and batch translation, there is no obvious gap, changed to batch translation to increase speed
        // details：debugReverseTrans()/debugReversePerformanceBatch()
        val reTransList = translateBatch(needTransItems, dstCode, srcCode, true)

        //Replacing translated content with original listings
        val retItems = dstItems.map { it.copy() }
        val retMap = HashMap<String, LocalizedItem>()
        retItems.forEach {
            retMap[it.name] = it
        }

        val srcMap = HashMap<String, LocalizedItem>()
        srcItems.forEach {
            srcMap[it.name] = it
        }

        reTransList.forEach {
            val retItem = retMap[it.name]!!
            val srcItem = srcMap[it.name]!!

            //To cope with a situation, the re-translated content may have some untranslated parts, then face a dilemma, whether to keep the original translation or keep the re-translated content.
            //Conclusion: keep the re-translated content, because the original translated content is supposed to filter out the options that have similarity problems, and from the accuracy point of view, it's better to display the original English content rather than the incorrect original translated content.
            //So the following logic can be directly written as retItem.content = it.content, considering that this part of the code mainly has little impact on the performance, it is kept for easy understanding.
            if (srcItem.content != retItem.content && srcItem.content == it.content) {
                retItem.content = it.content
            } else {
                retItem.content = it.content
            }
        }

        //testReversePerformanceSaveRawData(retItems, srcItems, needTransItems, reversedItems, dstCode, srcCode)

        return retItems
    }

    private fun getNeedReverseItems(
        dstList: List<LocalizedItem>,
        srcItems: List<LocalizedItem>
    ): List<LocalizedItem> {
        //Filter the text that needs to be back-translated, if the text contains formatting characters or \n, it has already been translated in the checkStringFormatAndLineBreakFinal method, so it will not be back-translated here.

        val needReverseItems = ArrayList<LocalizedItem>()
        srcItems.forEachIndexed { index, item ->
            if (getFormatArgs(item.content).isEmpty()
                && getLineBreakCount(item.content) == 0
                && dstList[index].content.lowercase() != item.content.lowercase()
            ) {
                needReverseItems.add(LocalizedItem(item.name, dstList[index].content))
            }
        }


        return needReverseItems
    }

    fun checkStringFormatAndLineBreakFinal(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>,
        toLanguage: CodeInfo
    ): List<LocalizedItem> {
        val retItems = dstItems.map { it.copy() }


        for (i in dstItems.indices) {

            val srcText = srcItems[i].content
            val dstText = dstItems[i].content

            var needReplace = false

            if (!isFormatArgsEquals(srcText, dstText)) {
                needReplace = true
            }

            if (!isLineBreakEquals(srcText, dstText)) {
                needReplace = true
            }

            if (needReplace) {
                retItems[i].content = srcText
                Log.d("language: ${toLanguage.code}, name: ${srcItems[i].name} srcText: $srcText, dstText: $dstText")
            }
        }

        return retItems
    }

    private fun checkOtherStringException(
        dstItems: List<LocalizedItem>
    ): List<LocalizedItem> {
        val retItems = dstItems.map { it.copy() }

        //There are parts of the content that \'become' after translation, causing android studio to not be able to parse it
        //trans <string name="reopen_from_source">Can't, please reopen from last app</string>
        //en <string name="reopen_from_source">Can\'t, please reopen from last app</string>
        retItems.forEach {
            it.content = it.content.replace("\\'", "'").replace("'", "\\'")
        }

        retItems.forEach {
            it.content = preprocessAngleBrackets(it.content)
        }

        return retItems
    }

    /**
     * Handling asymmetric <> symbols in strings
     */
    fun preprocessAngleBrackets(input: String): String {

        val text = "<div>$input</div>"

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val inputStream = ByteArrayInputStream(text.toByteArray())
        try {
            docBuilder.parse(inputStream)
        } catch (e: Exception) {
            Log.d("preprocessAngleBrackets: $input")
            val doc = Jsoup.parse(input)
            return Jsoup.clean(doc.html(), Safelist.none()).replace("&gt;", "")
                .replace("&lt;", "")
                .trim()
        }

        return input
    }

    suspend fun checkStringExceptionAndReTranslation(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>,
        toLanguage: CodeInfo,
        language: CodeInfo = CodeInfo.default
    ): List<LocalizedItem> {
        val retItems = dstItems.map { it.copy() }

        val transIndex = ArrayList<Int>()
        val transValue = ArrayList<String>()

        for (i in dstItems.indices) {
            val strItem = srcItems[i]

            val srcText = strItem.content
            val dstText = dstItems[i].content

            var needTrans = false

            if (!isFormatArgsEquals(srcText, dstText)) {
                needTrans = true
            }

            if (!isLineBreakEquals(srcText, dstText)) {
                needTrans = true
            }

            if (needTrans) {
                //Log.d("1. name: ${strItem.name} srcText: $srcText dstText: $dstText")
                transIndex.add(i)
                transValue.add(srcText)
            }
        }

        Log.d("checkStringExceptionAndRetranslation sum: " + transIndex.size)

        transValue.forEachIndexed { i, s ->
            var text = translateService.translateSingle(s, toLanguage, language)
            val index = transIndex[i]
            var dstArgsSize = getFormatArgs(text).size
            val srcArgsSize = getFormatArgs(srcItems[index].content).size
            var dstLineCount = getLineBreakCount(text)
            val srcLineCount = getLineBreakCount(srcItems[index].content)
            if (dstArgsSize == srcArgsSize && dstLineCount == srcLineCount) {
                retItems[index].content = text
            } else {
                text = correctStringFormat(text)
                text = correctLineBreak(text)

                dstArgsSize = getFormatArgs(text).size
                dstLineCount = getLineBreakCount(text)
                if (dstArgsSize == srcArgsSize && dstLineCount == srcLineCount) {
                    retItems[index].content = text
                } else {
                    //All processing did not work, none of the original text should be used, and the final hand-off to a unified method of processing
                    //retItems[index] = srcItem[index].content
                    Log.d("s: $s")
                    Log.d("text: $text")
                    Log.d("test srcArgsSize: $srcArgsSize dstArgsSize: $dstArgsSize srcLineCount: $srcLineCount dstLineCount: $dstLineCount")
                }
            }
        }

        return retItems
    }

    /**
     * Check if the number of newlines in the list matches the number of newlines in the original data srcItems
     * 1. If consistent, do not process
     * 2. if inconsistent, check whether there is \n in the translated text, if so, replace it with \n
     * 3. check again after the replacement, if still inconsistent, output the contents of the text without processing (due to machine translation, there is a translation of the data are misplaced, the original data does not have line breaks, but the translated data have line breaks)
     */
    fun checkLineBreakCount(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>
    ): List<LocalizedItem> {
        val retItems = dstItems.map { it.copy() }

        for (i in dstItems.indices) {
            val srcItem = srcItems[i]

            val srcCount = getLineBreakCount(srcItem.content)
            val dstCount = getLineBreakCount(dstItems[i].content)
            if (srcCount == 0 && dstCount == 0) continue

            if (srcCount != getLineBreakCount(dstItems[i].content)) {
                val text = correctLineBreak(dstItems[i].content)
                if (srcCount != getLineBreakCount(text)) {
                    retItems[i].content = srcItem.content
                } else {
                    //Don't set up a direct translation of the source file for incorrect content, as this will cause subsequent checks to pass straight through, masking many problems
                    //Log.d("translation data error. name: ${srcItem.name} src: ${srcItem.content} dst: $text")
                }
            }
        }

        return retItems
    }

    fun checkStringFormat(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>
    ): List<LocalizedItem> {
        val retItems = dstItems.map { it.copy() }.toMutableList()

        for (i in dstItems.indices) {
            val strItem = srcItems[i]

            val srcText = strItem.content
            val dstText = dstItems[i].content

            val srcArgs = getFormatArgs(srcText)
            val dstArgs = getFormatArgs(dstText)
            if (srcArgs.isEmpty() && dstArgs.isEmpty()) continue

            if (srcArgs.size != dstArgs.size) {
                retItems[i].content = correctStringFormat(dstText)
            } else {
                for (j in srcArgs.indices) {
                    if (srcArgs[j] != dstArgs[j]) {
                        Log.d("name: ${strItem.name} srcText: $srcText dstText: $dstText")
                        retItems[i].content = correctStringFormat(dstText)
                        break
                    }
                }
            }
        }

        return retItems
    }

    /**
     * Check whether the translated content contains untranslated content and reTranslate it
     */
    private suspend fun checkTranslationContentsAndRetry(
        transItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>,
        dstCode: CodeInfo,
        srcCode: CodeInfo,
        isReverse: Boolean = false
    ): List<LocalizedItem> {
        val srcList = srcItems.map { it.content }
        val retItems = transItems.map { it.copy() }

        var firstRun = true

        var count = 0

        for (i in retItems.indices) {
            //Determine whether the content is the same as before translation, if it is the same, it means that the translation has failed and needs to be re-translated.
            if (retItems[i].content.lowercase() == srcList[i].lowercase()) {
                count++
            }
        }

        //1. In the case of Luxembourgish, there is no need to check the number of failed translations. This is because Luxembourgish translation services are unstable and often have translation failures
        //2. in the case of content that needs to be re-translated after back-translation detection, it is itself prone to a large number of untranslations
        //3. if the translation is small, it is easy for half of the content to be untranslated, and there is no need to check the number of failed translations.
        if (dstCode.code == "lus" || isReverse || transItems.size < 15) {
            //In the case of Luxembourgish, there is no need to check the number of failed translations. This is because Luxembourgish translation services are unstable and often have translation failures
            //660 texts, only 304 translated, a translation rate of less than 50 per cent
        } else {
            if (count.toFloat() / transItems.size.toFloat() > 0.4f) {
                Log.e("The number of failed translations is more than 40%. Please check whether the translation content is correct or whether there are any special conditions for the current language.count: $count total: ${transItems.size}")
            }
        }

        var retryTime = Math.max(1, count / 20) //proportional to the number of translation failures

        while (retryTime > 0) {
            val indexList = ArrayList<Int>()
            val needTransList = ArrayList<String>()

            for (i in retItems.indices) {
                val text = retItems[i].content
                //Determine whether the content is the same as before translation, if it is the same, it means that the translation has failed and needs to be re-translated.
                if (text.lowercase() == srcList[i].lowercase()) {
                    indexList.add(i)
                    needTransList.add(srcList[i])
                }
            }

            //In a language like Luxembourg, where the translation service is unstable, there are more than 300 texts, which have to be retried 15 times, checking each time how many are left and minimizing the number of retries as much as possible
            retryTime = Math.min(retryTime, indexList.size / 20 + 1)

            if (indexList.size > 0 && (firstRun || indexList.size > 5)) {
                if (firstRun) firstRun = false
                val resultList = translateService.translateBatch(needTransList, dstCode, srcCode)
                for (i in 0 until indexList.size) {
                    retItems[indexList[i]].content = resultList[i]
                }
            } else {
                break
            }

            retryTime--
        }

        return retItems
    }

    private fun correctLineBreak(text: String): String {
        return text.replace("\\ n", "\\n")
    }

    /**
     * Correct errors in formatting strings and transferring characters after translation
     */
    private fun correctStringFormat(text: String): String {
        return text.replace("٪ 1 \$ d", "%1\$d")
            .replace("٪ 2 \$ d", "%2\$d")
            .replace("٪ 3 \$ d", "%3\$d")
            .replace("٪ 4 \$ d", "%4\$d")
            .replace("٪ 1 \$ s", "%1\$s")
            .replace("٪ 2 \$ s", "%2\$s")
            .replace("٪ 3 \$ s", "%3\$s")
            .replace("٪ 4 \$ s", "%4\$s")
            .replace("% 1 \$ d", "%1\$d")
            .replace("% 2 \$ d", "%2\$d")
            .replace("% 3 \$ d", "%3\$d")
            .replace("% 4 \$ d", "%4\$d")
            .replace("% 1 \$ s", "%1\$s")
            .replace("% 2 \$ s", "%2\$s")
            .replace("% 3 \$ s", "%3\$s")
            .replace("% 4 \$ s", "%4\$s")
            .replace("%1\$ ", "%1\$s")
            .replace("٪ d", "%d")
            .replace("٪ s", "%s")
            .replace("% s", "%s")
            .replace("% d", "%d")

    }

    /**
     * Check that line breaks are consistent across languages
     */
    private fun isLineBreakEquals(srcText: String, dstText: String): Boolean {
        return getLineBreakCount(srcText) == getLineBreakCount(dstText)
    }

    /**
     * Check the consistency of formatting strings %s, %d, etc. in different languages.
     */
    private fun isFormatArgsEquals(srcText: String, dstText: String): Boolean {
        val srcArgs = getFormatArgs(srcText)
        val dstArgs = getFormatArgs(dstText)

        var isEquals = true

        if (srcArgs.size != dstArgs.size) {
            isEquals = false
        } else {
            for (j in srcArgs.indices) {
                if (srcArgs[j] != dstArgs[j]) {
                    isEquals = false
                    break
                }
            }
        }

        return isEquals
    }

    /**
     * Get an array of parameters for a formatted string
     */
    fun getFormatArgs(text: String): List<String> {
        val p: Pattern = Pattern.compile("%[\\d\\.]*[dfs]|%\\d+\\$[sdf]")
        val m: Matcher = p.matcher(text)

        val matches: MutableList<String> = ArrayList()
        while (m.find()) {
            matches.add(m.group())
        }

        return matches
    }

    /**
     * Get the number of \n contained in the string
     */
    fun getLineBreakCount(text: String): Int {
        val p: Pattern = Pattern.compile("\\\\n")
        val m: Matcher = p.matcher(text)

        var count = 0
        while (m.find()) {
            count++
        }

        return count
    }

    //region Calculate the function associated with the indicator

    fun needReTranslateBySimilarity(dstText: String, srcText: String): Boolean {
        val similarity = calSimilarityPercent(dstText, srcText)
        return needReTranslateBySimilarity(similarity)
    }

    private fun needReTranslateBySimilarity(similarity: Int): Boolean {
        return similarity < 40
    }

    /**
     * Calculation of the percentage similarity value (0-100) for the list of back-translated texts
     * Calculated by this method and concluded that by resubmitting texts for translation after back-translation comparisons, it was possible to increase the similarity of the returned texts from 65% to 72%
     */
    fun calSimilarityPercent(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>
    ): Int {

        val map = HashMap<String, String>()
        srcItems.forEach {
            map[it.name] = it.content
        }

        var count = 0

        val countList = Array(11) { 0 }

        dstItems.forEach { item ->

            if(map.contains(item.name)) {
                val srcText = map[item.name]!!

                val similarity = calSimilarityPercent(item.content, srcText)

                if (needReTranslateBySimilarity(similarity)) {
                    //println("${item.name} : ${item.content} : $srcText")
                    count++
                }

                val index = similarity / 10
                countList[index] = countList[index] + 1
            } else {
                count++
            }
        }

        val percent = (1 - count.toFloat() / dstItems.size) * 100

//        countList.forEachIndexed{ index, item ->
//            print("[$index : $item], ")
//        }
//        Log.d("")
//        Log.d("dstItems.size: ${dstItems.size} count: $count, percent: $percent")

        return percent.toInt()

    }

    /**
     * Calculate the similarity percentage value of two strings (0-100)
     */
    private fun calSimilarityPercent(lhs: String, rhs: String): Int {
        val distance = calLevenshteinDistance(lhs, rhs)
        val max = maxOf(lhs.length, rhs.length)

        //For edit distances less than 5, it is considered to be consistent 对于编辑距离小于5的，认为是一致的
        return if (distance > 5) {
            val percent = distance.toFloat() / max * 100

            100 - percent.toInt()
        } else {
            100
        }
    }

    /**
     * Calculate the edit distance between two strings, calculate the similarity by edit distance.
     */
    private fun calLevenshteinDistance(lhs: String, rhs: String): Int {
        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = IntArray(lhsLength)
        var newCost = IntArray(lhsLength)

        for (i in 0 until lhsLength) {
            cost[i] = i
        }

        for (j in 1 until rhsLength) {
            newCost[0] = j

            for (i in 1 until lhsLength) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1

                val costReplace = cost[i - 1] + match
                val costInsert = cost[i] + 1
                val costDelete = newCost[i - 1] + 1

                newCost[i] = minOf(costInsert, costDelete, costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhs.length]
    }

    /**
     * To calculate the translation rate, the text in the target language is de-emphasized
     */
    fun calTranslatePercent(
        dstItems: List<LocalizedItem>,
        srcItems: List<LocalizedItem>
    ): Int {

        var count = 0
        val map = HashMap<String, String>()
        srcItems.forEach {
            map[it.name] = it.content
        }

        dstItems.forEach {
            val srcText = map[it.name]
            if (srcText != null) {
                if (it.content != srcText) {
                    count++
                }
            }
        }

        val percent = count / srcItems.size.toFloat() * 100

        return percent.toInt()
    }

    //endregion

}