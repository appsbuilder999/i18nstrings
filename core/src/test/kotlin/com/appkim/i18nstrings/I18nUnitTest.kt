package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.GoogleTranslateService
import com.appkim.i18nstrings.translate.TranslationManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalTime::class)
class I18nUnitTest {

    //region Initialization Variables and Generic Functions for Test Classes
    private val mgr = TranslationManager(GoogleTranslateService())
    private val parser = AndroidParser()
    private val helper = I18nStringsHelper()
    private val testDefaultFile = File("test_data/strings.xml".replace("/", File.separator))
    private val testStringFormatDir = "test_data/trans_01_for_string_format"
    private val testReverseTransDir = "test_data/reverse_01_trans_02"
    private val testTransPercentDir = "test_data/trans_02"
    private val testTrans03PercentDir = "test_data/trans_03"
    private val testTrans03ReversePercentDir = "test_data/trans_03_reverse".replace("/", File.separator)
    private val testQualityRawDir = "test_data/quality/raw".replace("/", File.separator)

    private fun getDefaultItems(): List<TranslationManager.LocalizedItem> {
        val text = testDefaultFile.readBytes().toString(Charsets.UTF_8)
        return helper.getLocalizedItems(text)
    }

    private fun getDefaultString(): String {
        return testDefaultFile.readBytes().toString(Charsets.UTF_8)
    }

    //endregion

    @Test
    fun updateAndroidStrings() = runTest(timeout = Duration.INFINITE) {
        val resourcesDir = "test_data/method_updateAndroidStrings"
        parser.updateResForTest(resourcesDir, CodeInfo.fromCode("zh-CN"))
    }

    //region Integration of some test code, generally used to verify that the code refactoring or adjustment, the overall data is normal


    /**
     * Smoke test, used to check the code as a whole after a code change is made
     */
    @Test
    fun smokeTest() = runTest(timeout = Duration.INFINITE) {
        val ret = parser.translate(getDefaultString(), CodeInfo.fromCode("zh-CN"), CodeInfo.default)

        println(ret)

        val srcItems = getDefaultItems()
        val dstItems = helper.getLocalizedItems(ret)

        val percent = dstItems.size.toFloat() / srcItems.size.toFloat()

        // In the case of Chinese, the translation rate is 94.84%, srcItems size: 660 dstItems size: 626
        println("percent: $percent srcItems size: ${srcItems.size} dstItems size: ${dstItems.size}")

        assert(percent > 0.9f)
    }


    /**
     * Output translation rate per language (translated texts / all texts)
     */
    @Test
    fun qualityTranslatePercent() = runTest(timeout = Duration.INFINITE) {
        val srcItems = getDefaultItems()
        println("srcItems size: ${srcItems.size}")
        CodeInfo.codeInfos.forEach {
            val langName = it.name

            val dstItems = helper.getLocalizedItems(testTrans03PercentDir, it)
            val reverseItems= helper.getLocalizedItems(testTrans03ReversePercentDir, it)
            val refItems = helper.getLocalizedItems(testQualityRawDir, it)

            if (dstItems.isEmpty()) {
                return@forEach
            }

            val percent = mgr.calTranslatePercent(dstItems, srcItems) / 100.0f
            val reversePercent = mgr.calSimilarityPercent(reverseItems, srcItems) / 100.0f
            val refPercent = mgr.calSimilarityPercent(refItems, dstItems) / 100.0f
            //if (percent < 90) {
            //    println("lang: $langName code: ${it.code} percent: $percent reversePercent: $reversePercent refPercent: $refPercent refCountPercent: ${(refItems.size.toFloat() / srcItems.size.toFloat() * 100).toInt()}")
            //}
            println("${it.code}\t$langName\t$percent\t$reversePercent\t$refPercent\t${refItems.size.toFloat() / srcItems.size.toFloat()}")
        }
    }

    /**
     * Compare the reference translated text with the text translated by the plug-in and output the differences between them
     * lang: Chinese (Simplified) code: zh-CN similarity percent: 86 translate percent: 81
     * The items with large similarity differences were exported, totaling 56 items, which were imported into chatgpt for detection, of which 20 were inaccurately translated and 36 were accurately translated, totaling 545 items. 3.66% error rate.
     */
    @Test
    fun qualityCompareToReference() = runTest(timeout = Duration.INFINITE) {
        val srcItems = getDefaultItems()
        val codes = listOf<CodeInfo>(CodeInfo.fromCode("zh-CN"))
        codes.forEach {
            val langName = it.name

            val dstItems = helper.getLocalizedItems(testTrans03PercentDir, it)
            val rawItems = helper.getLocalizedItems(testQualityRawDir, it)

            if (dstItems.isEmpty() || rawItems.isEmpty()) {
                return@forEach
            }

            val similarityPercent = mgr.calSimilarityPercent(rawItems, dstItems)
            val translatePercent = mgr.calTranslatePercent(rawItems, srcItems)
            if(translatePercent > 20)
                println("lang: $langName code: ${it.code} size:${rawItems.size} similarity percent: $similarityPercent translate percent: $translatePercent")

            //println("lang: $langName code: ${it.code} dst percent: $dstPercent raw percent: $rawPercent")

        }
    }

    //endregion

    //region Back-translation related tests

    @Test
    fun checkReverseTranslation() = runTest(timeout = Duration.INFINITE) {

        val code = CodeInfo.fromCode("sm")

        val retText = parser.translate(getDefaultString(), code, CodeInfo.default)
        parser.writeToRes(code, retText, "test_data/improve_reverse_02/trans")

//        val reverseText = mgr.translateForAndroid(retText, CodeInfo.default, code)
//        parser.saveFile(code, reverseText, "test_data/improve_reverse_02/reverse")
//
//        val srcItems = getDefaultItems()
//        val reverseItem = helper.getLocalizedItems("test_data/improve_reverse_02/reverse", code)
//
//        val filterReverseItems = reverseItem.filter { mgr.getFormatArgs(it.content).size == 0
//                && mgr.getLineBreakCount(it.content) == 0 }
//
//        val percent = mgr.calSimilarityPercent(filterReverseItems, srcItems)
//        println("percent: $percent")

    }


    @Test
    fun qualityReversePercent() = runTest(timeout = Duration.INFINITE) {
        val defaultList = getDefaultItems()

        val codes = listOf("zh-CN")
        //val reverseDir = testReverseTransDir

        codes.forEach {
            val dstItems = helper.getLocalizedItems(testTrans03ReversePercentDir, CodeInfo.fromCode(it))

            if (dstItems.isEmpty()) {
                return@forEach
            }

            val reversePercent = mgr.calSimilarityPercent(dstItems, defaultList)
            val transPercent = dstItems.size.toFloat() / defaultList.size.toFloat() * 100
            val percent = reversePercent.toFloat() / 100 * transPercent

            println("$it: ${percent}% reversePercent: $reversePercent% transPercent: $transPercent% size: ${dstItems.size}")

        }

    }

    /**
     * Through debugging and analysis, the back-translation optimization can increase the correct back-translation rate from 78% to 87% for language sm.
     * Checked the content of the back-translation replacement, 48 pieces of data, there are 30 corrected obvious errors, as follows
     * apps srcText: [App Manager] firstReverseText: [Edit Edit] secondReverseText: [App Manager]
     * Conclusion: from the point of view of translation result optimization, the back-translation method is feasible, but it will increase the translation time-consuming, and we need to think of a solution.
     */
    @Test
    fun debugReverseTrans() {

        val dir = "test_data/method_debugReverseTrans"
        val code = CodeInfo.fromCode("sm")

        val srcItems = getDefaultItems()
//        val firstTransItem = helper.getLocalizedItems("$dir/trans_02", code)
        val firstReverseItem = helper.getLocalizedItems("$dir/reverse_01_trans_02", code)
        val secondTransItem = helper.getLocalizedItems("$dir/improve_reverse_01/trans", code)
        val secondReverseItem = helper.getLocalizedItems("$dir/improve_reverse_01/reverse", code)

        val srcMap = getMap(srcItems)
//        val firstTransMap = getMap(firstTransItem)
        val firstReverseMap = getMap(firstReverseItem)
        val secondTransMap = getMap(secondTransItem)
        val secondReverseMap = getMap(secondReverseItem)

        println(
            "first: ${
                mgr.calSimilarityPercent(
                    firstReverseItem,
                    srcItems
                )
            }% second: ${mgr.calSimilarityPercent(secondReverseItem, srcItems)}%"
        )

        val needTransItems = mgr.getNeedTranslateItems(firstReverseItem, srcItems)

        val newFirstReverseItems = firstReverseItem.map { it.copy() }
        val newFirstReverseItemsMap = getMap(newFirstReverseItems)
        var newCount = 0
        needTransItems.forEach {
            val name = it.name
            if (secondReverseMap.contains(name)) {
                newCount++
                newFirstReverseItemsMap[name]!!.content = secondReverseMap[name]!!.content
            }
        }

        println(
            "newFirst: ${
                mgr.calSimilarityPercent(
                    newFirstReverseItems,
                    srcItems
                )
            }% newCount: $newCount"
        )


        val firstList = ArrayList<TranslationManager.LocalizedItem>()
        val secondList = ArrayList<TranslationManager.LocalizedItem>()

        needTransItems.forEach {
            val name = it.name
            val srcText = srcMap[name]!!.content
//            val firstTransText = firstTransMap[name]!!.content
            val firstReverseText = firstReverseMap[name]!!.content

            firstList.add(firstReverseMap[name]!!)

            if (secondReverseMap.contains(name) && secondTransMap.contains(name)) {
//                val secondTransText = secondTransMap[name]!!.content
                val secondReverseText = secondReverseMap[name]!!.content

                secondList.add(secondReverseMap[name]!!)

                if (mgr.needReTranslateBySimilarity(secondReverseText, srcText)) {
                    //println("name: $name srcText: [$srcText] firstReverseText: [$firstReverseText] secondReverseText: [$secondReverseText]")
                } else {
                    //Test results. 30 out of 48 were completely wrong and were corrected.
                    //apps srcText: [App Manager] firstReverseText: [Edit Edit] secondReverseText: [App Manager]
                    println("name: $name srcText: [$srcText] firstReverseText: [$firstReverseText] secondReverseText: [$secondReverseText]")

                }

                //println("name: $name srcText: [$srcText] firstReverseText: [$firstReverseText] secondReverseText: [$secondReverseText]")
            } else {

                //println("name: $name srcText: [$srcText] firstReverseText: [$firstReverseText]")
            }
        }

        val firstPercent = mgr.calSimilarityPercent(firstList, srcItems)
        val secondPercent = mgr.calSimilarityPercent(secondList, srcItems)

        println("firstPercent: $firstPercent secondPercent: $secondPercent")

    }

    /**
     * Attempted to apply the back-translation method to batch translation, checked the content of the back-translation replacement, 61 data, 31 corrected obvious errors
     * Conclusion: Batch translation can still be used after the back translation method, and the quality of translation has not been significantly reduced compared with that of line-by-line translation
     */
    @Test
    fun debugReversePerformanceBatch() {

        val dir = "test_data/method_debugReversePerformance_batch"
        val srcItems = helper.readFromJsonFile("srcItems.json", dir)
        val reversedItems = helper.readFromJsonFile("reversedItems.json", dir)
        val newReversedItems = helper.readFromJsonFile("newReversedItems.json", dir)
        val fullReversedItems = helper.readFromJsonFile("fullReversedItems.json", dir)


        val percent = mgr.calSimilarityPercent(reversedItems, srcItems)
        val newPercent = mgr.calSimilarityPercent(newReversedItems, srcItems)
        val fullPercent = mgr.calSimilarityPercent(fullReversedItems, srcItems)

        //sm percent: 74, newPercent: 86 //Test Recorded Data
        println("percent: $percent size: ${reversedItems.size}")
        println("newPercent: $newPercent size: ${newReversedItems.size}")
        println("fullPercent: $fullPercent size: ${fullReversedItems.size}")

        val srcMap = HashMap<String, TranslationManager.LocalizedItem>()
        srcItems.forEach {
            srcMap[it.name] = it
        }
        val reversedMap = HashMap<String, TranslationManager.LocalizedItem>()
        reversedItems.forEach {
            reversedMap[it.name] = it
        }
        val newReversedMap = HashMap<String, TranslationManager.LocalizedItem>()
        newReversedItems.forEach {
            newReversedMap[it.name] = it
        }
        val fullReversedMap = HashMap<String, TranslationManager.LocalizedItem>()
        fullReversedItems.forEach {
            fullReversedMap[it.name] = it
        }


        val reversedItemsSet = mutableSetOf<String>()
        reversedItems.forEach {
            if (!reversedItemsSet.contains(it.name)) {
                reversedItemsSet.add(it.name)
            }
        }
        newReversedItems.forEach {
            if (!reversedItemsSet.contains(it.name)) {
                reversedItemsSet.add(it.name)
            }
        }
        fullReversedItems.forEach {
            if (!reversedItemsSet.contains(it.name)) {
                reversedItemsSet.add(it.name)
            }
        }

        println("reversedItemsSet size: ${reversedItemsSet.size}")

        reversedItems.forEachIndexed { index, item ->

            val srcItem = srcMap[item.name]!!.content
            val reversedItem = item.content
            val newReversedItem = newReversedItems[index].content


            if (newReversedItem != reversedItem) {

                val needReTrans = mgr.needReTranslateBySimilarity(srcItem, reversedItem)
                val newNeedReTrans = mgr.needReTranslateBySimilarity(srcItem, newReversedItem)

                if (needReTrans && !newNeedReTrans) {
                    println("name: ${item.name} src: [$srcItem] reversedItem: [$reversedItem] newReversed: [$newReversedItem]")
                }
            }
        }

//        var newCount = 0
//        var newList = ArrayList<String>()
//        var fullCount = 0
//        var fullList = ArrayList<String>()
//
//        var allCount = 0
//
//        reversedItemsSet.forEach {
//            val srcItem = srcMap[it]!!.content
//            val reversedItem = if(reversedMap.contains(it)) reversedMap[it]!!.content else ""
//            val newReversedItem = if(newReversedMap.contains(it)) newReversedMap[it]!!.content else ""
//            val fullReversedItem = if(fullReversedMap.contains(it)) fullReversedMap[it]!!.content else ""
//
//            val newNeedReTrans = mgr.needReTranslateBySimilarity(srcItem, newReversedItem)
//            val fullNeedReTrans = mgr.needReTranslateBySimilarity(srcItem, fullReversedItem)
//
//
//            if(newNeedReTrans && !fullNeedReTrans) {
//                newList.add("name: $it src: [$srcItem] newReversed: [$newReversedItem] fullReversed: $fullReversedItem")
//                newCount++
//            }
//
//            if(!newNeedReTrans && fullNeedReTrans) {
//                fullList.add("name: $it src: [$srcItem] newReversed: $newReversedItem fullReversed: [$fullReversedItem]")
//                fullCount++
//            }
//
//            if(newNeedReTrans && fullNeedReTrans) {
//                println("name: $it src: [$srcItem] newReversed: [$newReversedItem] fullReversed: [$fullReversedItem]")
//                allCount++
//            }
//        }
//
//        println("allCount: $allCount")
//
//        newList.forEach(::println)
//
//        println("newCount: $newCount")
//
//        fullList.forEach(::println)
//
//        println("fullCount: $fullCount")
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
     */
    @Test
    fun debugReversePerformance() {

        val dir = "test_data/method_debugReversePerformance"
        val srcItems = helper.readFromJsonFile("srcItems.json", dir)
        val reversedItems = helper.readFromJsonFile("reversedItems.json", dir)
        val newReversedItems = helper.readFromJsonFile("newReversedItems.json", dir)
        val fullReversedItems = helper.readFromJsonFile("fullReversedItems.json", dir)


        val percent = mgr.calSimilarityPercent(reversedItems, srcItems)
        val newPercent = mgr.calSimilarityPercent(newReversedItems, srcItems)
        val fullPercent = mgr.calSimilarityPercent(fullReversedItems, srcItems)

        //sm percent: 74, newPercent: 86 //Test Recorded Data
        println("percent: $percent size: ${reversedItems.size}")
        println("newPercent: $newPercent size: ${newReversedItems.size}")
        println("fullPercent: $fullPercent size: ${fullReversedItems.size}")

        val srcMap = HashMap<String, TranslationManager.LocalizedItem>()
        srcItems.forEach {
            srcMap[it.name] = it
        }
        val reversedMap = HashMap<String, TranslationManager.LocalizedItem>()
        reversedItems.forEach {
            reversedMap[it.name] = it
        }
        val newReversedMap = HashMap<String, TranslationManager.LocalizedItem>()
        newReversedItems.forEach {
            newReversedMap[it.name] = it
        }
        val fullReversedMap = HashMap<String, TranslationManager.LocalizedItem>()
        fullReversedItems.forEach {
            fullReversedMap[it.name] = it
        }


        val reversedItemsSet = mutableSetOf<String>()
        reversedItems.forEach {
            if (!reversedItemsSet.contains(it.name)) {
                reversedItemsSet.add(it.name)
            }
        }
        newReversedItems.forEach {
            if (!reversedItemsSet.contains(it.name)) {
                reversedItemsSet.add(it.name)
            }
        }
        fullReversedItems.forEach {
            if (!reversedItemsSet.contains(it.name)) {
                reversedItemsSet.add(it.name)
            }
        }

        println("reversedItemsSet size: ${reversedItemsSet.size}")

        var newCount = 0
        val newList = ArrayList<String>()
        var fullCount = 0
        val fullList = ArrayList<String>()

        var allCount = 0

        reversedItemsSet.forEach {
            val srcItem = srcMap[it]!!.content
//            val reversedItem = if (reversedMap.contains(it)) reversedMap[it]!!.content else ""
            val newReversedItem =
                if (newReversedMap.contains(it)) newReversedMap[it]!!.content else ""
            val fullReversedItem =
                if (fullReversedMap.contains(it)) fullReversedMap[it]!!.content else ""

            val newNeedReTrans = mgr.needReTranslateBySimilarity(srcItem, newReversedItem)
            val fullNeedReTrans = mgr.needReTranslateBySimilarity(srcItem, fullReversedItem)


            if (newNeedReTrans && !fullNeedReTrans) {
                newList.add("name: $it src: [$srcItem] newReversed: [$newReversedItem] fullReversed: $fullReversedItem")
                newCount++
            }

            if (!newNeedReTrans && fullNeedReTrans) {
                fullList.add("name: $it src: [$srcItem] newReversed: $newReversedItem fullReversed: [$fullReversedItem]")
                fullCount++
            }

            if (newNeedReTrans && fullNeedReTrans) {
                println("name: $it src: [$srcItem] newReversed: [$newReversedItem] fullReversed: [$fullReversedItem]")
                allCount++
            }

//
//
//            if(needReTranslateBySimilarity(srcItem, newReversedItem)
//                || needReTranslateBySimilarity(srcItem, fullReversedItem)) {
//                println("name: $it src: [$srcItem] newReversed: [$newReversedItem] fullReversed: [$fullReversedItem]")
//            }
        }

        println("allCount: $allCount")

        newList.forEach(::println)

        println("newCount: $newCount")

        fullList.forEach(::println)

        println("fullCount: $fullCount")
    }

    private fun getMap(items: List<TranslationManager.LocalizedItem>): HashMap<String, TranslationManager.LocalizedItem> {
        val map = HashMap<String, TranslationManager.LocalizedItem>()
        items.forEach {
            map[it.name] = it
        }
        return map
    }

    /**
     * Compare the back-translated files, filter out the contents that need to be re-translated, and re-write the files after translating them line by line.
     */
    @Test
    fun reTranslateItems_isCorrect() = runTest(timeout = Duration.INFINITE) {
        val text = testDefaultFile.readBytes().toString(Charsets.UTF_8)
        val items = parser.parseXml(text)
        val srcItems = items.map { TranslationManager.LocalizedItem(it.name, it.content) }

        val code = CodeInfo.fromCode("sm")

        val smItems = helper.getLocalizedItems(testTransPercentDir, code)
        val smMap = getMap(smItems)

        val dstItems = srcItems.map {
            if (smMap.contains(it.name)) {
                TranslationManager.LocalizedItem(it.name, smMap[it.name]!!.content)
            } else {
                it.copy()
            }
        }

        //Getting text that needs to be back-translated
        //val needReverseItems = mgr.getNeedReverseItems(dstItems, srcItems)

        //translated text
        //val reversedItems = mgr.translateBatch(needReverseItems, srcLangCode, dstLangCode, true)
        val reversedItems = helper.getLocalizedItems(testReverseTransDir, code)

        //Evaluate back-translated text and return content that needs to be re-translated
        val needTransItems = mgr.getNeedTranslateItems(reversedItems, srcItems)

        println("needTransItems size: ${needTransItems.size}")

        //Line-by-line translation of text to be retranslated and returned
        val retItems =
            mgr.reTranslateItems(dstItems, srcItems, needTransItems, reversedItems, code, CodeInfo.default)

        val itemsMap = HashMap<String, AndroidParser.Item>()
        items.forEachIndexed { index, item ->
            itemsMap[item.name] =
                AndroidParser.Item(item.name, retItems[index].content, item.isCDATA)
        }

        val retText = parser.toXml(itemsMap, text, code)

        parser.writeToRes(code, retText)
    }

    @Test
    fun improveReversePercent() = runTest(timeout = Duration.INFINITE) {
        val code = CodeInfo.fromCode("sm")

        println("lang code: code")

        val file = File("test_data/improve_reverse/values-${code.resCode}/strings.xml")

        val srcText = file.readBytes().toString(Charsets.UTF_8)

        val dstText = parser.translate(srcText, CodeInfo.default, code)

        parser.writeToRes(code, dstText)
    }


    /**
     * Back-translating all languages yields a generally acceptable time spent, with the highest, la, taking only 1 minute and 48 seconds without special processing.
     * "mg", "haw", "hmn", "ig", "km", "la", "mn", "st", "so", "uz" which take longer compared to other languages.
     */
    @Test
    fun saveToFileReverseTranslate() = runTest(timeout = Duration.INFINITE) {

        val transDirPath = "test_data/trans_03"
        val reverseDirPath = "test_data/trans_03_reverse"

        val dirFile = File(reverseDirPath)
        if (!dirFile.exists()) dirFile.mkdirs()
        val logFile = File("$reverseDirPath/summary.txt")
        if (!logFile.exists()) logFile.createNewFile()

        val needReverseCodes = CodeInfo.codeInfos
        val unSupportCodes = setOf<CodeInfo>()
        needReverseCodes.forEach {
            if (unSupportCodes.contains(it)) {
                return@forEach
            }

            println("lang code: $it")

            val dstFile = File(reverseDirPath + "/values-${it.resCode}/strings.xml")
            if (dstFile.exists()) {
                return@forEach
            }

            val file = File(transDirPath + "/values-${it.resCode}/strings.xml")

            if (!file.exists()) {
                println("lang: $it file not exists")
                return@forEach
            }

            val start = System.currentTimeMillis()

            val srcText = file.readBytes().toString(Charsets.UTF_8)

            val dstText = parser.translate(srcText, CodeInfo.default, it)

            parser.writeToRes(it, dstText, reverseDirPath)

            val end = System.currentTimeMillis()

            val logText = "lang summary: $it cost time: ${end - start} ms"

            println(logText)

            logFile.appendText(logText + "\n")
        }
    }


    /**
     * Take the translation, translate it back, and see if it agrees with the original at any rate
     */
    @Test
    fun qualityReverseAllLanguagePercent() {

        //var reverseDir = testReverseTransDir
        val reverseDir = "test_data/trans_03_reverse"

        val defaultList = getDefaultItems()

        //val codes = listOf<String>("sm")
        val codes = CodeInfo.codeInfos

        var maxPercent = 0
        var maxCode: CodeInfo? = null
        codes.forEach {
            val dstItems = helper.getLocalizedItems(reverseDir, it)

            if (dstItems.isEmpty()) {
                return@forEach
            }

            val reversePercent = mgr.calSimilarityPercent(dstItems, defaultList)
            val transPercent = dstItems.size.toFloat() / defaultList.size.toFloat()
            val percent = reversePercent.toFloat() / 100 * transPercent * 100

            if (percent > maxPercent) {
                maxPercent = percent.toInt()
                maxCode = it
            }

            println("${it.code} : ${percent.toInt()}% reversePercent: $reversePercent% transPercent: ${(transPercent * 100).toInt()}% size: ${dstItems.size}")


//            if(percent > 20)
//                println("code: $it name: ${I18nManager.lang_map[it]} percent: $percent% count: $count size: ${dstItems.size}")
        }

        println("max code: $maxCode percent: $maxPercent%")

    }

    //endregion

    /**
     * Translate for all languages and then save to file
     */
    @Test
    fun saveToFileAllLanguage_isCorrect() = runTest(timeout = Duration.INFINITE) {

        val dirPath = "test_data/trans_03"

        val srcText = getDefaultString()

        val dirFile = File(dirPath)
        if (!dirFile.exists()) dirFile.mkdirs()
        val logFile = File("$dirPath/summary.txt")
        if (!logFile.exists()) logFile.createNewFile()

        val codes = CodeInfo.codeInfos

        for (i in codes.indices) {
            println("lang code: ${codes[i]}")

            val dir = File("$dirPath/values-${codes[i].resCode}")
            if (dir.exists()) {
                continue
            }

            if (codes[i] != CodeInfo.default
                && !TranslationManager.unsupportedBatchCodes.contains(codes[i].code)
            ) {

                //calculate cost time and print
                val start = System.currentTimeMillis()
                val retText = parser.translate(srcText, codes[i], CodeInfo.default)
                val end = System.currentTimeMillis()

                parser.writeToRes(codes[i], retText, dirPath)

                val srcItems = helper.getLocalizedItems(srcText)
                val dstItems = helper.getLocalizedItems(retText)

                val percent = dstItems.size.toFloat() / srcItems.size.toFloat()

                //In the case of sr, the translation rate is 94.09%, line by line. srcItems size: 660 dstItems size: 621
                val logText =
                    "lang summary: ${codes[i].code} ${codes[i].name} cost time: ${end - start} ms, percent: $percent, srcItems size: ${srcItems.size} dstItems size: ${dstItems.size}"

                println(logText)

                logFile.appendText(logText + "\n")
            }
        }
    }

    //region Some special languages, written tests Case

    /**
     * In some languages, the code used for translation is inconsistent with the standard code, and when written to android, the corresponding code is also inconsistent
     */
    @Test
    fun specialLangCodeTest() = runTest(timeout = Duration.INFINITE) {
        val testCode = listOf("he", "fil").map { CodeInfo.fromCode(it) }

        val text =
            File("$testStringFormatDir/values/strings.xml").readBytes().toString(Charsets.UTF_8)

        testCode.forEach {
            println("lang code: $it")
            val startTime = System.currentTimeMillis()
            val ret = parser.translate(text, it, CodeInfo.default)
            val endTime = System.currentTimeMillis()

            println("cost time: ${endTime - startTime} ms")
            //println(ret)

            parser.writeToRes(it, ret)

            assert(endTime - startTime < 60000)
        }
    }

    @Test
    fun translateWithoutMerge_isCorrect() = runTest(timeout = Duration.INFINITE) {
        //println(I18nManager.lang_map["fil"])
        val text =
            File("$testStringFormatDir/values/strings.xml").readBytes().toString(Charsets.UTF_8)
        val ret = parser.translate(text, CodeInfo.fromCode("he"), CodeInfo.default)

        val srcItems = helper.getLocalizedItems(text)
        val dstItems = helper.getLocalizedItems(ret)

        val percent = dstItems.size.toFloat() / srcItems.size.toFloat()

        // The sr case is translated line by line with a translation rate of a 94.09% srcItems size: 660 dstItems size: 621
        println("percent: $percent srcItems size: ${srcItems.size} dstItems size: ${dstItems.size}")
        parser.writeToRes(CodeInfo.fromCode("he"), ret)
        assert(percent > 0.93f)
    }

    //endregion

    //region Checking for exceptions when cutting strings

    @Test
    fun checkStringExceptionAndReTranslation_isCorrect() = runTest(timeout = Duration.INFINITE) {
        val srcMap = helper.getDefaultLocalizedItemsMap(testStringFormatDir)
        val dstMap = helper.getAllLocalizeItemsMapFromResource(testStringFormatDir)

        val keyList = dstMap.keys.toList()

        var dstSum = 0
        var retSum = 0
        for (i in keyList.indices) {

            var dstItemSum = 0
            var retItemSum = 0

            var dstItems = dstMap[keyList[i]]!!

            val srcItems = ArrayList<TranslationManager.LocalizedItem>()
            dstItems.forEach { srcItems.add(srcMap[it.name]!!) }

            val tmpList = mgr.checkStringFormat(dstItems, srcItems)

            dstItems = mgr.checkLineBreakCount(tmpList, srcItems)
            val retItems = mgr.checkStringExceptionAndReTranslation(dstItems, srcItems, keyList[i])

            for (j in retItems.indices) {
                val srcArgsCount = mgr.getFormatArgs(srcItems[j].content).size
                val dstArgsCount = mgr.getFormatArgs(dstItems[j].content).size
                val retArgsCount = mgr.getFormatArgs(retItems[j].content).size

                val srcLineCount = mgr.getLineBreakCount(srcItems[j].content)
                val dstLineCount = mgr.getLineBreakCount(dstItems[j].content)
                val retLineCount = mgr.getLineBreakCount(retItems[j].content)

                if (dstArgsCount != srcArgsCount || dstLineCount != srcLineCount) {
                    dstSum++
                    dstItemSum++
                }

                if (retArgsCount != srcArgsCount || retLineCount != srcLineCount) {
                    retSum++
                    retItemSum++

                }

            }

            println("dstItemSum: $dstItemSum retItemSum: $retItemSum")
            if (retItemSum != 0) {
                println("checkStringExceptionAndReTranslation error in lang code: ${keyList[i]}, name: ${keyList[i].name}")
                //throw Exception("checkStringExceptionAndReTranslation error in lang code: ${keyList[i]}")
            }
        }

        println("dstSum: $dstSum retSum: $retSum")
        assert(retSum == 25)
    }


    //endregion
}

