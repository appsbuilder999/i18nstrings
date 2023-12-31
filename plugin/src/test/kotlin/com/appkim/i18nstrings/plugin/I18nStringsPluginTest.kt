/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.appkim.i18nstrings.plugin

import com.appkim.i18nstrings.CodeInfo
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'com.appkim.i18nstrings.plugin' plugin.
 */
class I18nStringsPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.appkim.i18nstrings")

        // Verify the result
        assertNotNull(project.tasks.findByName("i18nStrings"))
    }

    @Test
    fun testGetCodes() {
        val plugin = I18nStringsPlugin()
        val mockResDirPaths = listOf("/Users/petyrzhan/Desktop/Workspace/I18nStrings/app/src/main/res",
            "/Users/petyrzhan/Desktop/Workspace/I18nStrings/app/src/main/res_v2",
            "/Users/petyrzhan/Desktop/Workspace/I18nStrings/app/src/main/res_v3")
        val mockSuggestLanguages = listOf("zh-CN", "fr", "de", "id", "ja", "pt", "es").sorted()

        val mockDefaultLanguages = listOf("es", "fr", "zh-CN","zh-HK").sorted()

        val defaultCodes = plugin.getCodes(mockResDirPaths, "default").map { it.code }.sorted()
        assertEquals(arrayEquals(defaultCodes, mockDefaultLanguages), true)

        val suggestCodes = plugin.getCodes(mockResDirPaths, "suggest").map { it.code }.sorted()
        assertEquals(arrayEquals(suggestCodes, mockSuggestLanguages), true)


        val mockAllCodes = CodeInfo.codes.map {
            if(it == "iw") {
                "he"
            } else {
                it
            }
        }
        val allCodes = plugin.getCodes(mockResDirPaths, "all").map { it.code }
        assertEquals(arrayEquals(allCodes, mockAllCodes), true)

        val noneCodes = plugin.getCodes(mockResDirPaths, "none")
        assertEquals(0, noneCodes.size)

        val customCodes = plugin.getCodes(mockResDirPaths, "zh-rCN,fr,de,id,ja,pt,es").map { it.code }.sorted()
        assertEquals(arrayEquals(customCodes, listOf("zh-CN","fr","de","id","ja","pt","es").sorted()), true)
    }

    fun arrayEquals(a: List<String>, b: List<String>): Boolean {
        if (a.size != b.size) return false
        for (i in a.indices) {
            if (a[i] != b[i]) {
                println("i: $i, a[i]: ${a[i]}, b[i]: ${b[i]}")
                return false
            }
        }
        return true
    }
}
