package com.appkim.i18nstrings.source

import com.appkim.i18nstrings.CodeInfo

class MutipleStringSource(val sources: MutableList<StringSource> = mutableListOf<StringSource>()) : StringSource() {

    override fun contains(
        codeInfo: CodeInfo,
        key: String,
        rawText: String
    ): Boolean {
        for(source in sources) {
            if(source.contains(codeInfo, key, rawText)) {
                return true
            }
        }
        return false
    }

    override fun getString(
        codeInfo: CodeInfo,
        key: String,
        rawText: String
    ): String? {
        for(source in sources) {
            val str = source.getString(codeInfo, key, rawText)
            if(str != null) {
                return str
            }
        }
        return null
    }

    fun addSource(source : StringSource) {
        if(!sources.contains(source)) {
            sources.add(0, source)
        }
    }

    fun size(): Int {
        return sources.size
    }
}