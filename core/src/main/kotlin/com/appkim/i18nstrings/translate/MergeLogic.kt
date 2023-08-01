package com.appkim.i18nstrings.translate

abstract class MergeLogic {

    abstract fun getTagHead(): String
    abstract fun getTagEnd(): String
    abstract fun getTagItemStart(): String
    abstract fun getTagItemEnd(): String
    abstract fun getTextExtra(): String

    //The following four methods are executed in a fixed order.
    // 1. preprocess copy
    // 2. remove extra added information
    // 3. remove headers and footers
    // 4. cut copy
    abstract fun preprocessText(text: String): String
    abstract fun removeExtraText(text: String): String
    abstract fun removeHeadEnd(text: String): String
    abstract fun splitText(text: String, hopeSize: Int): List<String>

}