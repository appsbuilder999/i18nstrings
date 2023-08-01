package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.TranslationManager
import kotlinx.coroutines.test.runTest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AndroidParserUnitTest {

    private val parser = AndroidParser()

    /**
     * make sure the new parse can parse the xml file with html content
     */
    @Test
    fun xmlContainsHtml() = runTest(timeout = Duration.INFINITE) {

        val testFile = File("test_data/xml_contains_html/strings.xml".replace("/", File.separator))
        val text = testFile.readBytes().toString(Charsets.UTF_8)
        val items = parser.parseXml(text)

        var result = false

        items.forEach {
            if(it.name == "smb_instructions") {
                result = it.content.contains("<html>")
            }
        }

        println("result: $result")
        assert(result)
    }

    @Test
    fun testReserveXmlHeadFormat() = runTest(timeout = Duration.INFINITE) {
        val testFile = File("test_data/android_parser/reserve_xml_head_format.xml".replace("/", File.separator))
        val text = testFile.readBytes().toString(Charsets.UTF_8)
        val items = parser.parseXml(text)

        val map = mutableMapOf<String, AndroidParser.Item>()
        items.forEach {
            map[it.name] = it
        }
        val newXmlText = parser.toXml(map, text, CodeInfo.default)

        println(newXmlText)

        assert(newXmlText == text)
    }

    @Test
    fun testSingleQuote() {
        val text = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><resources>\n" +
                "    <string name=\"dialog_delete_title\">Permanently delete item?</string>\n" +
                "    <string name=\"deleting\">Delete</string>\n" +
                "    <string name=\"question_set_path_as_home\">Do you want to set the current path as the home page for this tab?</string>\n" +
                "    <string name=\"directorysort\">Directory sorting mode</string>\n" +
                "    <string name=\"sort_by\">Sorting Method</string>\n" +
                "    <string name=\"sort_only_this\">This folder only</string>\n" +
                "    <string name=\"theme\">Theme</string>\n" +
                "    <string name=\"random\">Random Skin</string>\n" +
                "    <string name=\"random_summary\">Set random primary color on startup</string>\n" +
                "    <string name=\"colorize\">Color the icon</string>\n" +
                "    <string name=\"colorize_summary\">Set static icon color. does not override directory icon color preferences.</string>\n" +
                "    <string name=\"smb_instructions\">\n" +
                "        <html>\n" +
                "            <body>\n" +
                "                <h1>how to access shared windows folder on android (smb)</h1>\\n\\n\n" +
                "                <ul>\n" +
                "                    <li>\n" +
                "                        <b>enable file sharing on windows</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\\'margin-left: 40px\\'>\n" +
                "                        open the control panel, click choose homegroup and sharing options under network\n" +
                "                        and internet, and click \"change advanced sharing settings,\" enable the \"file and\n" +
                "                        printer sharing\" feature.\\n\\n\n" +
                "                    </p>\n" +
                "\n" +
                "                    <li>\n" +
                "                        <b>additional file sharing settings</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\\'margin-left: 40px\\'>\n" +
                "                        you may also want to configure the other advanced sharing settings here. \\n\n" +
                "                        for example, you could enable access to your files without a password if you trust all\n" +
                "                        the devices on your local network. once file and printer sharing is enabled, you can\n" +
                "                        open file explorer or windows explorer, right-click a folder you want to share, and\n" +
                "                        select properties. \\n\n" +
                "                        click the share button and make the folder available on the network.\\n\\n\n" +
                "                        </p>\n" +
                "\n" +
                "                    <li>\n" +
                "                        <b>make sure both devices are on same wifi</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\\'margin-left: 40px\\'>\n" +
                "                        this feature makes files available on the local network, so your pc and mobile devices\n" +
                "                        have to be on the same local network. you can\\’t access a shared windows folder\n" +
                "                        over the internet or when your smartphone is connected to its mobile data — it\n" +
                "                        has to be connected to wi-fi.\\n\\n\n" +
                "                    </p>\n" +
                "\n" +
                "                    <li>\n" +
                "                        <b>find ip address</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\\'margin-left: 40px\\'>\n" +
                "                        open command prompt. type \\'ipconfig\\' and press enter. look for default gateway\n" +
                "                        under your network adapter for your router\\'s ip address. look for\n" +
                "                        \\\\\\\"ipv4 address\\\\\\\" under the same adapter section to find your computer\\'s ip\n" +
                "                        address.\\n\\n\n" +
                "                    </p>\n" +
                "\n" +
                "                    <b>enter details in smb dialog box</b>\\n\n" +
                "                </ul>\n" +
                "            </body>\n" +
                "        </html>\n" +
                "    </string>\n" +
                "</resources>\n" +
                "\n" +
                "\n" +
                "\n"

        var ret = true
        try {
            val doc: Document = Jsoup.parse(text, "", Parser.xmlParser())
            val stringElements = doc.select("string")
            println(stringElements.size)
        } catch (e: Exception) {
            ret = false
        }

        assert(ret)
    }

    @Test
    fun testReplaceResourcesContent() {
        val text1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><resources>\n" +
                "    \n" +
                "    <string name=\"paste\">粘贴</string>\n" +
                "    <string name=\"history\">历史记录</string>\n" +
                "    <string name=\"copy\">复制</string>\n" +
                "    <string name=\"root_mode\">根资源管理器</string>\n" +
                "    <string name=\"root_mode_summary\">仅可用的已取得 root 权限的设备。仅检查您是否确定。</string>\n" +
                "    <string name=\"root_failure\">未授予根访问权限</string>\n" +
                "    <string name=\"select_all\">全选</string>\n" +
                "    <string name=\"delete\">删除</string>\n" +
                "    <string name=\"set_as_home\">设为主页</string>\n" +
                "    <string name=\"operation_not_supported\">不支持操作</string>\n" +
                "</resources>"
        val text2 = "<!-- hello world --><?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<resources>\n" +
                "    <string name=\"home\" translatable=\"false\">Home Page</string>\n" +
                "    <string name=\"paste\">Paste</string>\n" +
                "    <string name=\"history\">History</string>\n" +
                "    <string name=\"copy\">Copy</string>\n" +
                "    <string name=\"root_mode\">Root Explorer</string>\n" +
                "    <string name=\"root_mode_summary\">Only available rooted device. only check if you are sure.</string>\n" +
                "    <string name=\"root_failure\">Root access not granted</string>\n" +
                "    <string name=\"select_all\">Select All</string>\n" +
                "    <string name=\"delete\">Delete</string>\n" +
                "    <string name=\"set_as_home\">Set as homepage</string>\n" +
                "    <string name=\"operation_not_supported\">Does not support operation</string>\n" +
                "</resources>\n" +
                "\n" +
                "\n"

        val text3 = parser.replaceResourcesContent(text1, text2)

        val hopeResult = "<!-- hello world --><?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<resources>\n" +
                "    \n" +
                "    <string name=\"paste\">粘贴</string>\n" +
                "    <string name=\"history\">历史记录</string>\n" +
                "    <string name=\"copy\">复制</string>\n" +
                "    <string name=\"root_mode\">根资源管理器</string>\n" +
                "    <string name=\"root_mode_summary\">仅可用的已取得 root 权限的设备。仅检查您是否确定。</string>\n" +
                "    <string name=\"root_failure\">未授予根访问权限</string>\n" +
                "    <string name=\"select_all\">全选</string>\n" +
                "    <string name=\"delete\">删除</string>\n" +
                "    <string name=\"set_as_home\">设为主页</string>\n" +
                "    <string name=\"operation_not_supported\">不支持操作</string>\n" +
                "</resources>\n" +
                "\n" +
                "\n"
        assert(text3 == hopeResult)
    }

    /**
     * make sure the new parse with jsoup is the same as the old parse with java xml
     */
    @Test
    fun testXmlReadAndWrite() = runTest(timeout = Duration.INFINITE) {
        val testFile = File("test_data/android_parser/strings.xml".replace("/", File.separator))
        val text = testFile.readBytes().toString(Charsets.UTF_8)
        val items = parser.parseXml(text)

        val map = mutableMapOf<String, AndroidParser.Item>()
        items.forEach {
            map[it.name] = it
        }
        val newXmlText = parser.toXml(map, text, CodeInfo.default)

        assert(newXmlText == text)
    }

    @Test
    fun testContainsHtmlOrXmlTags() {

        assert(parser.containsHtmlTagsWithQuotes("<div class=\"test\">Hello</div>"))  // true
        assert(parser.containsHtmlTagsWithQuotes("<u id='123'>Hello</u>"))  // false
        assert(!parser.containsHtmlTagsWithQuotes("<div>Hello</div>"))  // false
        assert(!parser.containsHtmlTagsWithQuotes("</div>"))  // false
        assert(parser.containsHtmlTagsWithQuotes("<div class=\"test\"></div>"))
        assert(parser.containsHtmlTagsWithQuotes("<p style=\"margin-left: 40px\"></p>"))

        val text = "\n" +
                "        <html>\n" +
                "            <body>\n" +
                "                <h1>How to access shared Windows folder on Android (SMB)</h1>\\n\\n\n" +
                "                <ul>\n" +
                "                    <li>\n" +
                "                        <b>Enable File Sharing on Windows</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\"margin-left: 40px\">\n" +
                "                        Open the Control Panel, click Choose HomeGroup and Sharing options under Network\n" +
                "                        and Internet, and click \"Change Advanced Sharing Settings,\" enable the \"File and\n" +
                "                        Printer Sharing\" feature.\\n\\n\n" +
                "                    </p>\n" +
                "\n" +
                "                    <li>\n" +
                "                        <b>Additional File Sharing settings</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\"margin-left: 40px\">\n" +
                "                        You may also want to configure the other advanced sharing settings here. \\n\n" +
                "                        For example, you could enable access to your files without a password if you trust all\n" +
                "                        the devices on your local network. Once file and printer sharing is enabled, you can\n" +
                "                        open File Explorer or Windows Explorer, right-click a folder you want to share, and\n" +
                "                        select Properties. \\n\n" +
                "                        Click the Share button and make the folder available on the network.\\n\\n\n" +
                "                        </p>\n" +
                "\n" +
                "                    <li>\n" +
                "                        <b>Make sure both devices are on same Wifi</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\"margin-left: 40px\">\n" +
                "                        This feature makes files available on the local network, so your PC and mobile devices\n" +
                "                        have to be on the same local network. You can\\’t access a shared Windows folder\n" +
                "                        over the Internet or when your smartphone is connected to its mobile data — it\n" +
                "                        has to be connected to Wi-Fi.\\n\\n\n" +
                "                    </p>\n" +
                "\n" +
                "                    <li>\n" +
                "                        <b>Find IP Address</b>\\n\n" +
                "                    </li>\n" +
                "                    <p style=\"margin-left: 40px\">\n" +
                "                        Open Command Prompt. Type \\'ipconfig\\' and press Enter. Look for Default Gateway\n" +
                "                        under your network adapter for your router\\'s IP address. Look for\n" +
                "                        \\\\\\\"IPv4 Address\\\\\\\" under the same adapter section to find your computer\\'s IP\n" +
                "                        address.\\n\\n\n" +
                "                    </p>\n" +
                "\n" +
                "                    <b>Enter details in SMB dialog box</b>\\n\n" +
                "                </ul>\n" +
                "            </body>\n" +
                "        </html>\n" +
                "    "

        assert(parser.containsHtmlTagsWithQuotes(text))
    }

    @Test
    fun testReplaceDoubleQuotesWithSingleInAttributes() {
        assert(parser.replaceSingleQuotesWithDoubleInAttributes("<div class='test'>Hello</div>") == "<div class=\"test\">Hello</div>")  // output：<div class='test'>Hello</div>
        assert(parser.replaceSingleQuotesWithDoubleInAttributes("<u id='123'>Hello</u>") == "<u id=\"123\">Hello</u>")  // output：<u id='123'>Hello</u>
        assert(parser.replaceSingleQuotesWithDoubleInAttributes("<div>Hello</div>") == "<div>Hello</div>")  // output：<div>Hello</div>
        assert(parser.replaceSingleQuotesWithDoubleInAttributes("</div>") == "</div>")  // output：</div>
        assert(parser.replaceSingleQuotesWithDoubleInAttributes("<div class=\"test\"></div>") == "<div class=\"test\"></div>")  // output：<div class='test'></div>
    }

    @Test
    fun testGetLocaleFromFolderName() {

        val result0 = parser.isAvaluableResDirName("values-auto")
        assertFalse(result0)

        // 测试有效的 BCP 47 标签
        val result1 = parser.isAvaluableResDirName("values-b+en+US")
        assertTrue(result1)

        // 测试有效的旧格式标签
        val result2 = parser.isAvaluableResDirName("values-zh-rCN")
        assertTrue(result2)

        // 测试无效的标签
        val result3 = parser.isAvaluableResDirName("values-xyz")
        assertFalse(result3)

        // 测试非 "values-" 开头的文件夹名称
        val result4 = parser.isAvaluableResDirName("drawable-en-rUS")
        assertFalse(result4)

        // 测试非语言相关的资源文件夹名称
        val result5 = parser.isAvaluableResDirName("values-sw600dp")
        assertFalse(result5)

        val result6 = parser.isAvaluableResDirName("values-v21")
        assertFalse(result6)

        val result7 = parser.isAvaluableResDirName("values-w820dp")
        assertFalse(result7)

        // Test 'values-mmi-rIN', expected result is a Locale object, representing Manipuri (Manipuri language) in India
        val result8 = parser.isAvaluableResDirName("values-mni-rIN")
        assertTrue(result8)

        val result9 = parser.isAvaluableResDirName("values-zh-rHK")
        assertTrue(result9)
    }

    @Test
    fun testPreprocessAngleBrackets() {

        val mgr = TranslationManager()

        val text = "Monster Mother<di v18=''><</di>"
        assert(mgr.preprocessAngleBrackets(text) == "Monster Mother")

        val text2 = "Agree ><div86>"
        assert(mgr.preprocessAngleBrackets(text2) == "Agree")
    }
}