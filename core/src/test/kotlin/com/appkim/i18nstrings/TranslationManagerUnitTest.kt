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
class TranslationManagerUnitTest {

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

    /**
     * Check the generated multilanguage, if it contains the same content as the default language (the code should not if it runs properly)
     */
    @Test
    fun checkXmlContainSameItem() = runTest(timeout = Duration.INFINITE) {
        val srcItems = getDefaultItems()
        val map = HashMap<String, TranslationManager.LocalizedItem>()
        srcItems.forEach {
            map[it.name] = it
        }

        //var ret = mgr.convertXmlString(getDefaultString(), "sm")

        //var dstItems = parser.convertToItems(ret)
        val dstItems = helper.getLocalizedItems(testTransPercentDir, CodeInfo.fromCode("sm"))

        var sameCount = 0
        dstItems.forEach {
            if (map[it.name]!!.content.lowercase() == it.content.lowercase()) {
                println(it.name)
                sameCount++
            }
        }
        println(sameCount)
        assert(sameCount == 0)
    }

    //endregion


    //region Checking for exceptions when cutting strings
    @Test
    fun checkFinal_isCorrect() = runTest(timeout = Duration.INFINITE) {
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

            //This step has to pull the network, which can lead to very slow execution and is not necessary
            //这一步要拉取网络，会导致执行很慢，没必要
            //val dstItems = mgr.checkStringExceptionAndReTranslation(tmpList, srcItems, keyList[i])
            val retItems = mgr.checkStringFormatAndLineBreakFinal(tmpList, srcItems, keyList[i])

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
        assert(retSum == 0)
    }

    @Test
    fun checkStringFormat_isCorrect() {
        val srcMap = helper.getDefaultLocalizedItemsMap(testStringFormatDir)
        val dstMap = helper.getAllLocalizeItemsMapFromResource(testStringFormatDir)

        val keyList = dstMap.keys.toList()

        var dstSum = 0
        var retSum = 0
        for (i in keyList.indices) {

            val dstItems = dstMap[keyList[i]]!!

            val srcItems = ArrayList<TranslationManager.LocalizedItem>()
            dstItems.forEach { srcItems.add(srcMap[it.name]!!) }

            val retItems = mgr.checkStringFormat(dstItems, srcItems)

            for (j in retItems.indices) {
                val srcCount = mgr.getFormatArgs(srcItems[j].content).size
                val dstCount = mgr.getFormatArgs(dstItems[j].content).size
                val retCount = mgr.getFormatArgs(retItems[j].content).size

                if (dstCount != srcCount) {
                    dstSum++
                }

                if (retCount != srcCount) {
                    retSum++

                    println("TODO handle string format error")
                    println("lang code: ${keyList[i]} name: ${srcItems[j].name}")
                    println("srcCount: $srcCount retCount: $retCount")
                    println("src: ${srcItems[j].content} dst: ${dstItems[j].content} ret: ${retItems[j]}")
                    println("src: ${srcItems[j].name} dst: ${dstItems[j].name}")
                }

            }
        }

        println("dstSum: $dstSum retSum: $retSum")
        assert(dstSum == 341 && retSum == 64)
    }

    @Test
    fun getFormatN_isCorrect() {
        val text = "\\n  \n \\n \\\\n \\ \n"
        val count = TranslationManager().getLineBreakCount(text)
        Assertions.assertEquals(3, count)
    }

    @Test
    fun checkLineBreakCount_isCorrect() {

        val srcMap = helper.getDefaultLocalizedItemsMap(testStringFormatDir)
        val dstMap = helper.getAllLocalizeItemsMapFromResource(testStringFormatDir)

        val keyList = dstMap.keys.toList()

        var dstSum = 0
        var retSum = 0
        for (i in keyList.indices) {

            val dstItems = dstMap[keyList[i]]!!

            val srcItems = ArrayList<TranslationManager.LocalizedItem>()
            dstItems.forEach { srcItems.add(srcMap[it.name]!!) }

            val retItems = mgr.checkLineBreakCount(dstItems, srcItems)

            for (j in retItems.indices) {
                val srcCount = mgr.getLineBreakCount(srcItems[j].content)
                val retCount = mgr.getLineBreakCount(retItems[j].content)
                val dstCount = mgr.getLineBreakCount(dstItems[j].content)
                if (dstCount != srcCount) {
                    dstSum++
                }

                if (retCount != srcCount) {
                    retSum++

                    println("TODO handle string format error")
                    println("lang code: ${keyList[i]} name: ${srcItems[j].name}")
                    println("srcCount: $srcCount retCount: $retCount")
                    println("src: ${srcItems[j].content} dst: ${dstItems[j].content} ret: ${retItems[j]}")
                    println("src: ${srcItems[j].name} dst: ${dstItems[j].name}")
                }
            }
        }

        println("dstSum: $dstSum retSum: $retSum")
        assert(dstSum == 313 && retSum == 225)
    }

    //endregion
}

