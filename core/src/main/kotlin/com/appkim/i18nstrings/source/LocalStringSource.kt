package com.appkim.i18nstrings.source

import com.appkim.i18nstrings.AndroidParser
import com.appkim.i18nstrings.CodeInfo

class LocalStringSource(resCodes: List<CodeInfo>,
                        betaCodes: List<CodeInfo>,
                        appModuleName: String,
                        moduleResMap: Map<String, List<String>>,
                        betaDirPath: String) : StringSource() {

    private val stringsMap: MutableMap<CodeInfo, Map<String, AndroidParser.Item>> = mutableMapOf()

    init {

        val parser = AndroidParser()

        val allCodes = mutableListOf<CodeInfo>()
        allCodes.addAll(resCodes)
        allCodes.addAll(betaCodes)

        allCodes.forEach {
            val betaItems = parser.parseXmlFromDir(betaDirPath, it)
            val resItems = parser.parseXmlFromRes(appModuleName, moduleResMap, it)

            val maps = mutableMapOf<String, AndroidParser.Item>()

            betaItems.forEach { item ->
                maps[item.name] = item
            }

            resItems.forEach { item ->
                maps[item.name] = item
            }

            stringsMap[it] = maps
        }
    }

    override fun contains(codeInfo: CodeInfo, key: String, rawText: String): Boolean {
        return stringsMap[codeInfo]?.contains(key) ?: false
    }

    override fun getString(codeInfo: CodeInfo, key: String, rawText: String): String? {
        return stringsMap[codeInfo]?.get(key)?.content
    }

    fun refreshDefault(appModuleName: String,
                       moduleResMap: Map<String, List<String>>) {
        val parser = AndroidParser()

        val existItems = parser.parseXmlFromRes(appModuleName, moduleResMap, CodeInfo.default)

        val maps = HashMap<String, AndroidParser.Item>()
        existItems.forEach { item ->
            maps[item.name] = item
        }

        stringsMap[CodeInfo.default] = maps
    }
}