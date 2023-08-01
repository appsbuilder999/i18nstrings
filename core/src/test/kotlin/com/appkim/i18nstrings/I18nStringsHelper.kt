package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.TranslationManager
import com.google.gson.Gson
import java.io.File

class I18nStringsHelper {

    private val parser = AndroidParser()

    //region Parse xml file to LocalizedItem
    fun getDefaultLocalizedItemsMap(resourceDir: String): Map<String, TranslationManager.LocalizedItem> {
        val srcText = parser.getAndroidFile(resourceDir).readBytes().toString(Charsets.UTF_8)
        val srcItems = getLocalizedItems(srcText)
        val map = HashMap<String, TranslationManager.LocalizedItem>()
        srcItems.forEach { map[it.name] = it }

        return map
    }

    fun getAllLocalizeItemsMapFromResource(resourceDir: String): Map<CodeInfo, List<TranslationManager.LocalizedItem>> {
        val map = HashMap<CodeInfo, List<TranslationManager.LocalizedItem>>()
        for (i in 0 until CodeInfo.codes.size) {

            val resCode = CodeInfo.fromCode(CodeInfo.codes[i])

            val path = parser.getAndroidFile(resourceDir, resCode).absolutePath
            val dstItems = convertToItemsByPath(path)
            map[resCode] = dstItems
        }

        return map
    }

    private fun convertToItemsByPath(path: String): List<TranslationManager.LocalizedItem> {
        val file = File(path)
        return if (file.exists()) {
            val srcText = file.readBytes().toString(Charsets.UTF_8)
            getLocalizedItems(srcText)
        } else {
            listOf()
        }
    }

    /**
     * Convert string to string model
     * 1. Filter the text when translatable is false.
     * 2. Filter the text when the length of a string exceeds MAX_LEN.
     */
    fun getLocalizedItems(text: String): List<TranslationManager.LocalizedItem> {
        return parser.parseXml(text).map { TranslationManager.LocalizedItem(it.name, it.content) }
    }


    fun getLocalizedItems(
        dirPath: String,
        codeInfo: CodeInfo
    ): List<TranslationManager.LocalizedItem> {

        val dstFile = parser.getAndroidFile(dirPath, codeInfo)
        if (!dstFile.exists()) {
            return listOf()
        }

        val text = dstFile.readBytes().toString(Charsets.UTF_8)

        return getLocalizedItems(text)
    }

    data class JsonObject(val items: List<TranslationManager.LocalizedItem>)

    fun readFromJsonFile(
        fileName: String,
        dirPath: String = "out"
    ): List<TranslationManager.LocalizedItem> {
        val file = File("$dirPath/$fileName")
        if (!file.exists()) {
            return listOf()
        }
        val text = file.readBytes().toString(Charsets.UTF_8)
        val jsonObject = Gson().fromJson(text, JsonObject::class.java)
        return jsonObject.items
    }

    fun writeToJsonFile(
        items: List<TranslationManager.LocalizedItem>,
        fileName: String,
        dirPath: String = "out"
    ) {
        val dirFile = File(dirPath)
        if (!dirFile.exists()) dirFile.mkdirs()

        val file = File("$dirPath/$fileName")
        if (file.exists()) file.delete()
        file.createNewFile()
        file.printWriter().use { out ->
            out.println(Gson().toJson(JsonObject(items)))
        }
    }

    //endregion
}