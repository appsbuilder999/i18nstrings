package com.appkim.i18nstrings

import com.appkim.i18nstrings.source.LocalStringSource
import com.appkim.i18nstrings.source.MutipleStringSource
import com.appkim.i18nstrings.source.StringSource
import com.appkim.i18nstrings.translate.TranslationManager
import com.appkim.i18nstrings.utils.CostTime
import com.appkim.i18nstrings.utils.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.CDataNode
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.io.File
import java.io.IOException
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


/**
 * Encapsulate string file parsing and conversion functions here to facilitate subsequent code separation
 */
class AndroidParser {

    private val mgr = TranslationManager()

    companion object {
        const val betaFolderName = "i18nStrings"
        const val resourcesConfigFileName = "resources_codes.config"
        const val stringsConfigFileName = "strings.config"
        const val defaultStringsFileName = "strings.xml"

        var onlySupportedStringsXml: Boolean = true
        var excludeXmls: List<String> = listOf()
    }

    //region Item：Parses each item of the xml file
    class Item(
        val name: String,
        var content: String,
        var isCDATA: Boolean = false
    ) {

        fun copy(newContent: String): Item {
            return Item(name, newContent, isCDATA)
        }

        /**
         * Regular expressions match English characters (including letters, numbers, English punctuation and special characters)
         */
        fun isEnglishOnly(): Boolean {
            val regex = Regex("^[\\x00-\\x7F]+$")
            return regex.matches(content)
        }

        fun restoreSentenceCasing(): Item {
            val newContent = if (content.split(" ").size == 2) {
                capitalizeWords(content)
            } else {
                capitalizeSentence(content)
            }

            return copy(newContent)
        }

        private fun capitalizeWords(input: String): String {
            return input.split(" ").joinToString(" ") {
                val firstChar = it.first().uppercaseChar()
                val restOfString = it.substring(1).lowercase()
                firstChar + restOfString
            }
        }

        private fun capitalizeSentence(sentence: String): String {
            if (sentence.isEmpty()) {
                return sentence
            }

            val firstChar = sentence.first().uppercaseChar()
            val restOfString = sentence.substring(1).lowercase()

            return firstChar + restOfString
        }
    }
    //endregion

    //region Public Functions

    suspend fun updateResForTest(resourceDir: String, code: CodeInfo) {
        val srcFile = getAndroidFile(resourceDir)
        val dstFile = getAndroidFile(resourceDir, code)

        if (!srcFile.exists()) return

        val srcText = srcFile.readBytes().toString(Charsets.UTF_8)
        val dstText = if (dstFile.exists()) dstFile.readBytes().toString(Charsets.UTF_8) else ""

        val text = translate(srcText, code, CodeInfo.default, dstText)

        dstFile.parentFile.mkdirs()
        dstFile.createNewFile()
        dstFile.printWriter().use { out ->
            out.println(text)
        }
    }

    suspend fun updateDir(srcItems: List<Item>, dirPath: String, codeInfo: CodeInfo) {
        val map = if(codeInfo.code != "en") {
            translateToMap(srcItems, codeInfo)
        } else {
            translateToMap(srcItems, codeInfo, CodeInfo.auto)
        }

        writeToDir(map, codeInfo, srcItems, dirPath)
    }

    suspend fun updateProject(
        releaseCodes: List<CodeInfo>,
        betaCodes: List<CodeInfo>,
        appModuleName: String,
        moduleResMap: Map<String, List<String>>,
        assetsPath: String,
        betaPath: String
    ) {

        Log.i("  translation prepare...")

        var checkPass = true
        val codes = mutableListOf<CodeInfo>()
        codes.addAll(releaseCodes)
        codes.addAll(betaCodes)
        for (codeInfo in codes) {
            if(!mgr.translateService.isSupportedLanguage(codeInfo)) {
                Log.e("    ${codeInfo.code} is not supported for ${mgr.translateService.javaClass.simpleName}")
                checkPass = false
                break
            }
        }

        if(!checkPass) {
            Log.e("  translation failed")
            return
        }

        Log.i("    releaseCodes = $releaseCodes, betaCodes = $betaCodes, appModuleName = $appModuleName")
        moduleResMap.forEach { Log.i("    ${it.key} = ${it.value}") }

        Log.i("  translating...")

        val mutipleStringSource = MutipleStringSource()
        val localStringSource = LocalStringSource(releaseCodes, betaCodes, appModuleName, moduleResMap, betaPath)
        mutipleStringSource.addSource(localStringSource)
        if(I18nStrings.extraStringsSource != null) mutipleStringSource.addSource(I18nStrings.extraStringsSource!!)

        translateDefault(appModuleName, moduleResMap, mutipleStringSource)

        localStringSource.refreshDefault(appModuleName, moduleResMap)

        val srcResItems = parseXmlFromRes(appModuleName, moduleResMap, CodeInfo.default)

        Log.i("    total strings count: ${srcResItems.size}")
        Log.i("    release")
        for (codeInfo in releaseCodes) {
            val itemsMap = translate(srcResItems, codeInfo, mutipleStringSource)

            writeToRes(itemsMap, codeInfo, moduleResMap)
        }

        Log.i("    beta")
        for (codeInfo in betaCodes) {
            val itemsMap = translate(srcResItems, codeInfo, mutipleStringSource)

            writeToDir(itemsMap, codeInfo, srcResItems, betaPath)
        }

        removeDuplicateFolders(releaseCodes, betaCodes, moduleResMap, betaPath)

        saveAppSupportedLanguage(moduleResMap[appModuleName]!!, assetsPath)

        saveAppSupportedStrings(srcResItems, assetsPath)

        Log.i("  translating done")
    }

    private fun saveAppSupportedLanguage(resDirPaths: List<String>, assetsPath: String) {

        val dir = getBetaDir(assetsPath)
        if (!dir.exists()) dir.mkdirs()

        val codes = getCodeInfosFromRes(resDirPaths)

        val file = getBetaFile(assetsPath, resourcesConfigFileName)
        if (file.exists()) file.delete()

        Log.i("    app resources supported languages: $codes")

        if (codes.isNotEmpty()) {
            file.createNewFile()
            file.printWriter().use { out ->
                out.println(codes.joinToString(",") { it.code })
            }
        }
    }

    private fun saveAppSupportedStrings(srcResItems: List<Item>, assetsPath: String) {

        val dir = getBetaDir(assetsPath)
        if (!dir.exists()) dir.mkdirs()

        val strings = srcResItems.map { it.name }

        val file = getBetaFile(assetsPath, stringsConfigFileName)
        if (file.exists()) file.delete()


        file.createNewFile()
        file.printWriter().use { out ->
            out.println(strings.joinToString(","))
        }
    }

    /**
     * When a language is moved from release to beta, or beta to release, the old content remains and the duplicate folders need to be removed
     */
    private fun removeDuplicateFolders(
        releaseCodes: List<CodeInfo>,
        betaCodes: List<CodeInfo>,
        moduleMap: Map<String, List<String>>,
        betaPath: String
    ) {

        for (codeInfo in releaseCodes) {
            val file = getBetaFile(betaPath, codeInfo)
            if (file.exists()) file.delete()

            val dir = getBetaDir(betaPath)

            val files = dir.listFiles()
            if (files == null || files.isEmpty()) dir.delete()
        }

        for (codeInfo in betaCodes) {
            for (module in moduleMap) {
                val resDirPaths = module.value
                for (resDir in resDirPaths) {
                    val file = getAndroidFile(resDir, codeInfo)

                    if (file.exists()) file.delete()

                    val dir = getAndroidDir(resDir, codeInfo)
                    val files = dir.listFiles()
                    if (files == null || files.isEmpty()) dir.delete()
                }
            }
        }
    }

    fun getCodeInfosFromRes(resDirPaths: List<String>): List<CodeInfo> {
        val codesSet = mutableSetOf<CodeInfo>()

        for (path in resDirPaths) {
            try {
                val resDir = File(path)
                resDir.listFiles()?.forEach {
                    if (it.isDirectory && it.name.startsWith("values-")) {
                        val codeInfo = CodeInfo.fromResCode(it.name.substring(7))
                        if(codeInfo.isResCodeAvailable()) {
                            codesSet.add(codeInfo)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return codesSet.toList()
    }

    fun isAvaluableResDirName(name: String): Boolean {
        if(!name.startsWith("values-")) return false

        val codeInfo = CodeInfo.fromResCode(name.substring(7))
        return codeInfo.isResCodeAvailable()
    }

    //endregion

    //region Base Methods for translate


    /**
     * Accepts English strings.xml content and outputs translated strings.xml content
     */
    suspend fun translate(
        srcText: String,
        dstCode: CodeInfo,
        srcCode: CodeInfo,
        dstLastText: String = ""
    ): String {
        val items = parseXml(srcText)
        val lastItems = parseXml(dstLastText)

        val map = translateToMap(items, dstCode, srcCode, lastItems)

        return toXml(map, srcText, dstCode)
    }

    suspend fun translate(
        items: List<Item>,
        dstCode: CodeInfo
    ): String {

        val map = translateToMap(items, dstCode)

        val list = map.values.toList()

        return toXml(list)
    }

    private suspend fun translate(srcRawItems: List<Item>,
                                  codeInfo: CodeInfo,
                                  stringSource: StringSource): Map<String, Item> {

        val translatedItemsMap = mutableMapOf<String, Item>()
        val needTransItems = mutableListOf<Item>()

        srcRawItems.forEach {
            val newContent = stringSource.getString(codeInfo, it.name, it.content)
            if(newContent != null) {
                translatedItemsMap[it.name] = it.copy(newContent)
            } else {
                needTransItems.add(it)
            }
        }

        Log.i("      $codeInfo count: ${needTransItems.size} estimated: ${CostTime.getTimeText(codeInfo, needTransItems.size)}")

        val srcLocalizedItems = needTransItems.map {
            TranslationManager.LocalizedItem(
                it.name,
                it.content
            )
        }
        val dstLocalizedItems = mgr.translate(srcLocalizedItems, codeInfo, CodeInfo.default)

        var count = 0
        dstLocalizedItems.forEachIndexed { index, localizedItem ->

            val item = needTransItems[index]
            if(item.content != localizedItem.content) {
                translatedItemsMap[item.name] = item.copy(localizedItem.content)
                count++
            }
        }

        var percent = 100
        if(needTransItems.isNotEmpty()) {
            percent = count * 100 / needTransItems.size
        }

        Log.i("        translation rate: $percent% translated: $count")

        return translatedItemsMap
    }

    private suspend fun translateDefault(appModuleName: String,
                                         moduleResMap: Map<String, List<String>>,
                                         stringSource: MutipleStringSource) {

        val srcResItems = parseXmlFromRes(appModuleName, moduleResMap, CodeInfo.default)

        if(stringSource.size() > 1) {
            srcResItems.forEach {
                it.content = stringSource.getString(CodeInfo.default, it.name, it.content) ?: it.content
            }
        }

        val itemsMap = HashMap<String, Item>()

        val needTransItems = srcResItems.filter {
            !it.isEnglishOnly()
        }

        if(needTransItems.isEmpty()) return

        srcResItems.forEach {
            itemsMap[it.name] = it
        }

        val srcLocalizedItems = needTransItems.map {
            TranslationManager.LocalizedItem(
                it.name,
                it.content
            )
        }

        val dstLocalizedItems = mgr.translate(srcLocalizedItems, CodeInfo.default, CodeInfo.auto)

        var count = 0
        dstLocalizedItems.forEachIndexed { index, localizedItem ->

            val item = needTransItems[index]
            if(item.content != localizedItem.content) {
                itemsMap[item.name] = item.copy(localizedItem.content).restoreSentenceCasing()
                count++
            }
        }

        var percent = 100
        if(needTransItems.isNotEmpty()) {
            percent = count * 100 / needTransItems.size
        }
        Log.i("    ${CodeInfo.default}: $percent% translated: $count total: ${needTransItems.size}")

        writeToRes(itemsMap, CodeInfo.default, moduleResMap)
    }

    private suspend fun translateToMap(items: List<Item>,
                                       dstLang: CodeInfo,
                                       srcLang: CodeInfo = CodeInfo.default,
                                       lastItems: List<Item> = listOf()
    ): Map<String, Item> {
        val lastMap = HashMap<String, Item>()
        lastItems.forEach {
            lastMap[it.name] = it
        }

        val needTransItems = items.filter { !lastMap.contains(it.name) }
        val needTransLocalizedItems = needTransItems.map {
            TranslationManager.LocalizedItem(
                it.name,
                it.content
            )
        }

        val transItems = mgr.translate(needTransLocalizedItems, dstLang, srcLang)

        val map = HashMap<String, Item>()
        items.forEach {
            map[it.name] = Item(it.name, it.content, it.isCDATA)
        }

        needTransItems.forEachIndexed { index, item ->
            map[item.name]!!.content = transItems[index].content
        }

        lastItems.forEach {
            map[it.name]!!.content = it.content
        }

        return map
    }
    //endregion

    //region Base Methods for xml to parse, convert and save

    /**
     * Convert string to string model
     * 1. Filter the text when translatable is false.
     * 2. Filter the text when the length of a string exceeds MAX_LEN.
     */
    fun parseXml(text: String): List<Item> {
        if (text.isEmpty()) return listOf()

        val doc: Document = Jsoup.parse(text, "", Parser.xmlParser())
        //doc.outputSettings().prettyPrint(false)
        val elements: Elements = doc.select("string")

        val list = mutableListOf<Item>()

        for (element in elements) {
            val name = element.attr("name")
            val translatable = element.attr("translatable")
            var isCDATA = false

            var content = Parser.unescapeEntities(element.html(), false)

            for (childNode in element.childNodes()) {
                if (childNode.nodeName() == "#cdata") {
                    isCDATA = true
                    content = (childNode as CDataNode).text()
                    break
                }
            }

            if(!isCDATA && containsHtmlTagsWithQuotes(content)) {
                isCDATA = true
                content = replaceSingleQuotesWithDoubleInAttributes(content)
            }

            if (translatable == "false") {
                continue
            }

            list.add(Item(name, content, isCDATA))
        }

        return list
    }


    fun containsHtmlTagsWithQuotes(input: String): Boolean {
        // Checks for HTML/XML tags that contain at least one attribute surrounded by single quotes via regular expression
        val regex1 = "<([a-zA-Z0-9]+)\\b[^>]*\\s+.*?=.*?\".*?\".*?>.*?</\\1>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
        val regex2 = "<([a-zA-Z0-9]+)\\b[^>]*\\s+.*?=.*?'.*?'.*?>.*?</\\1>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
        return regex1.containsMatchIn(input) || regex2.containsMatchIn(input)
    }

    fun replaceSingleQuotesWithDoubleInAttributes(input: String): String {
        // Match attribute values with regular expressions, then replace double quotes with single quotes
        val regex = "(\\w+)='(.*?)'".toRegex()
        return regex.replace(input) { matchResult ->
            "${matchResult.groupValues[1]}=\"${matchResult.groupValues[2]}\""
        }
    }


    fun writeToRes(codeInfo: CodeInfo, text: String, resDirPath: String = "out") {

        //cover langCode to Android support Sting. mni-Mtei -> mni-rMTEI

        val dir = getAndroidDir(resDirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = getAndroidFile(resDirPath, codeInfo)
        file.printWriter().use { out ->
            out.println(text)
        }
    }

    private fun writeToRes(itemsMap: Map<String, Item>,
                           codeInfo: CodeInfo,
                           moduleMap: Map<String, List<String>>) {
        for(module in moduleMap) {
            val moduleResDirPaths = module.value

            for(resDir in moduleResDirPaths) {

                val srcDir = getAndroidDir(resDir)
                if(srcDir.exists()) {
                    val files = findStringResources(srcDir)
                    for(file in files) {
                        val srcText = file.readBytes().toString(Charsets.UTF_8)
                        val text = toXml(itemsMap, srcText, codeInfo)

                        if(codeInfo.isDefault()) {
                            val dstFile = getAndroidFile(resDir, CodeInfo.default, file.name)
                            dstFile.printWriter().use { out ->
                                out.println(text)
                            }
                        } else {
                            val androidDir = getAndroidDir(resDir, codeInfo)
                            if(!androidDir.exists()) androidDir.mkdirs()
                            val dstFile = getAndroidFile(resDir, codeInfo, file.name)
                            dstFile.printWriter().use { out ->
                                out.println(text)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun writeToDir(itemsMap: Map<String, Item>,
                           codeInfo: CodeInfo,
                           srcRawItems: List<Item>,
                           dirPath: String) {

        val set = HashSet<String>()
        val list = mutableListOf<Item>()
        for(rawItem in srcRawItems) {
            val name = rawItem.name
            if(itemsMap.contains(name)) {
                val item = itemsMap[name]
                if(!set.contains(name) && item != null) {
                    set.add(name)
                    list.add(item)
                } else {
                    Log.w("duplicate item: $name")
                }
            }
        }

        val dir = getBetaDir(dirPath)
        if(!dir.exists()) dir.mkdirs()
        val file = getBetaFile(dirPath, codeInfo)
        if(file.exists()) file.delete()
        val text = toXml(list)
        file.printWriter().use { out ->
            out.println(text)
        }
    }

    fun parseXmlFromRes(appModuleName: String,
                        moduleMap: Map<String, List<String>>,
                        codeInfo: CodeInfo
    ): List<Item> {

        val retSet = mutableSetOf<Item>()

        for(module in moduleMap) {
            val moduleName = module.key
            val resDirPaths = module.value
            for(resDir in resDirPaths) {

                val dir = getAndroidDir(resDir, codeInfo)
                if(!dir.exists()) continue

                val files = findStringResources(dir)

                for (file in files) {
                    val srcText = file.readBytes().toString(Charsets.UTF_8)
                    val list = parseXml(srcText)
                    list.forEach {
                        if(retSet.contains(it)) {
                            if(appModuleName == moduleName) {
                                retSet.add(it)
                            } else {
                                Log.w("duplicate item: ${it.name}")
                            }
                        } else {
                            retSet.add(it)
                        }
                    }
                }
            }
        }

        return retSet.toList()
    }

    fun parseXmlFromDir(dirPath: String, codeInfo: CodeInfo): List<Item> {

        val retList = mutableListOf<Item>()

        if(codeInfo.isDefault()) return retList

        val file = getBetaFile(dirPath, codeInfo)

        if(file.exists()) {
            val srcText = file.readBytes().toString(Charsets.UTF_8)
            val list = parseXml(srcText)
            retList.addAll(list)
        }

        return retList
    }

    fun parseXmlToMap(srcText: String): Map<String, CharSequence> {

        val list = parseXml(srcText)
        val map = HashMap<String, CharSequence>()
        list.forEach {
            map[it.name] = it.content
        }

        return map
    }

    private fun toXml(items: List<Item>): String {
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.newDocument()

        val rootElement = doc.createElement("resources")
        doc.appendChild(rootElement)

        for (item in items) {
            val stringElement = doc.createElement("string")
            stringElement.setAttribute("name", item.name)
            if (item.isCDATA) {
                val cdata = doc.createCDATASection(item.content)
                stringElement.appendChild(cdata)
            } else {
                stringElement.appendChild(doc.createTextNode(item.content))
            }
            rootElement.appendChild(stringElement)
        }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        }

        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))

        return writer.toString()
    }

    fun toXml(transMap: Map<String, Item>, text: String, codeInfo: CodeInfo): String {
        val filterSame = !codeInfo.isDefault()

        // 解析XML
        val doc: Document = Jsoup.parse(text, "", Parser.xmlParser())
        val stringElements = doc.select("string")

        val removeList = mutableListOf<org.jsoup.nodes.Element>()

        stringElements.forEach { element ->
            val name = element.attr("name")
            val itemModel = transMap[name]

            if (itemModel == null) {
                if(!codeInfo.isDefault()) {
                    removeList.add(element)
                }
            } else {
                val textNode = itemModel.content

                if (itemModel.isCDATA) {
                    element.empty()
                    element.html("<![CDATA[$textNode]]>")
                } else {
                    if (element.text().lowercase() == textNode.lowercase() && filterSame) {
                        removeList.add(element)
                    } else {
                        element.text(textNode)
                    }
                }
            }
        }

        if(stringElements.isNotEmpty()) {
            val rootNode = stringElements[0].parent()
            removeList.forEach {
                it.remove()
            }
        }

        var outText = doc.toString()
        outText = outText.replace("&lt;", "<").replace("&gt;", ">")

        return replaceResourcesContent(outText, text).trim()
    }

    fun replaceResourcesContent(a: String, b: String): String {
        // Get the content from A between <resources>...</resources> from A
        val patternA = "(?s)<resources[^>]*>(.*)</resources>".toRegex()
        val matchResultA = patternA.find(a)
        val contentA = matchResultA?.groups?.get(1)?.value ?: throw IllegalArgumentException("Unable to find <resources> content from A")

        // In B find <resources>...</resources> and replace the contents with the contents of A.
        val patternB = "(?s)(<resources[^>]*>).*?(</resources>)".toRegex()
        val resultB = patternB.replace(b) { matchResult ->
            matchResult.groups[1]?.value + contentA + matchResult.groups[2]?.value
        }

        return resultB
    }


    //endregion

    //region The path separator between windows and linux is not the same, need to standardize it. windows和linux下的路径分隔符不一致，需要统一一下
    fun getAndroidDir(dirPath: String, codeInfo: CodeInfo = CodeInfo.default): File {
        val path = if(codeInfo.isDefault()) "$dirPath/values"
        else "$dirPath/values-${codeInfo.resCode}"

        return File(path.replace('/', File.separatorChar))
    }
    fun getAndroidFile(dirPath: String, codeInfo: CodeInfo = CodeInfo.default, fileName: String = defaultStringsFileName): File {
        val path = if(codeInfo.isDefault()) "$dirPath/values/$fileName"
        else "$dirPath/values-${codeInfo.resCode}/$fileName"

        return File(path.replace('/', File.separatorChar))
    }


    private fun getBetaDir(dirPath: String): File {
        val path = "$dirPath/$betaFolderName"

        return File(path.replace('/', File.separatorChar))
    }
    private fun getBetaFile(dirPath: String, codeInfo: CodeInfo): File {
        val path = if(codeInfo.isDefault()) "$dirPath/$betaFolderName/$defaultStringsFileName"
        else "$dirPath/$betaFolderName/strings-${codeInfo.resCode}.xml"

        return File(path.replace('/', File.separatorChar))
    }

    private fun getBetaFile(dirPath: String, fileName: String): File {
        val path = dirPath + File.separator + betaFolderName + File.separator + fileName

        return File(path)
    }
    //endregion

    fun findStringResources(directory: File): List<File> {
        if(onlySupportedStringsXml) {
            val file = File(directory.absolutePath + File.separator + defaultStringsFileName)
            if(file.exists()) {
                return listOf(file)
            } else {
                return listOf()
            }
        }
        val result = mutableListOf<File>()
        directory.walk().filter { it.isFile && it.extension == "xml" }.forEach { file ->
            for(excludeXml in excludeXmls) {
                if(file.absolutePath.contains(excludeXml)) {
                    return@forEach
                }
            }
            file.bufferedReader().use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.contains("<string name=") && line.contains("</string>")) {
                        result.add(file)
                        break
                    }
                    line = reader.readLine()
                }
            }
        }
        return result
    }
}