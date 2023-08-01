package com.appkim.i18nstrings.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.appkim.i18nstrings.I18nStringsApp
import com.appkim.i18nstrings.I18nStringsAppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class I18nSettingActivity : Activity() {
    private val model: I18nStringsAppModel = I18nStringsApp.model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_setting)

        val languageListView: ListView = findViewById(R.id.language_list_view)
        val itemsName = model.items.map {
            it.name
        }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemsName)
        languageListView.adapter = arrayAdapter

        languageListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if(position == 0) return@OnItemClickListener
            val item = model.items[position]

            if(!model.getDisclaimerAgree(this)) {
                showDisclaimerDialog(item)
            } else {
                processLanguageSwitch(item)
            }
        }

        languageListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            if(position == 0) return@OnItemLongClickListener false
            val item = model.items[position]
            if(item.type == I18nStringsAppModel.TYPE_ONLINE_CACHE) {
                showRefreshTranslateDialog(item)
                true
            } else {
                false
            }
        }
    }

    private fun processLanguageSwitch(item: I18nStringsAppModel.LanguageItem) {
        when(item.type) {
            I18nStringsAppModel.TYPE_NONE -> showRebootDialog(item)
            I18nStringsAppModel.TYPE_RELEASE -> showRebootDialog(item)
            I18nStringsAppModel.TYPE_BETA -> showRebootDialog(item)
            I18nStringsAppModel.TYPE_ONLINE_CACHE -> showRebootDialog(item)
            I18nStringsAppModel.TYPE_LOCAL -> showRebootDialog(item)
            else -> showRealtimeComfirmDialog(item)
        }
    }

    private fun showRebootDialog(item: I18nStringsAppModel.LanguageItem) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(R.string.i18n_ui_reboot_title)
            setMessage(R.string.i18n_ui_reboot_content)
            setPositiveButton(android.R.string.ok) { _, _ ->
                if(item.type == I18nStringsAppModel.TYPE_ONLINE) {
                    item.type = I18nStringsAppModel.TYPE_ONLINE_CACHE
                }
                model.setSelectedLanguageItems(context, item)
                killApp()
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showRealtimeTranslationFailsDialog(errorStr: String) {
        val message = getString(R.string.i18n_ui_realtime_translate_fails_content) + "\n" + errorStr
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(R.string.i18n_ui_realtime_translate_fails_title)
            setMessage(message)
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDisclaimerDialog(item: I18nStringsAppModel.LanguageItem) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(R.string.i18n_ui_disclaimer_title)
            setMessage(R.string.i18n_ui_disclaimer_content)
            setPositiveButton(R.string.i18n_ui_disclaimer_agree) { _, _ ->
                processLanguageSwitch(item)
                model.setDisclaimerAgree(context,true)
            }
            setNegativeButton(R.string.i18n_ui_disclaimer_disagree) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showRefreshTranslateDialog(item: I18nStringsAppModel.LanguageItem) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(R.string.i18n_ui_refresh_title)
            setMessage(R.string.i18n_ui_refresh_content)
            setPositiveButton(android.R.string.ok) { _, _ ->
                showRealtimeTranslateDialog(item)
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showRealtimeComfirmDialog(item: I18nStringsAppModel.LanguageItem) {
        val pair = I18nStringsApp.getEstimatedTime(this, item.codeInfo)
        val size = pair.first
        val time = pair.second
        val min = time / 60
        val sec = time % 60
        val message = if(min > 0) {
            getString(R.string.i18n_ui_realtime_translate_comfirm_min, min, size)
        } else {
            getString(R.string.i18n_ui_realtime_translate_comfirm_sec, sec, size)
        }

        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(R.string.i18n_ui_realtime_translate_title)
            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ ->
                showRealtimeTranslateDialog(item)
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showRealtimeTranslateDialog(item: I18nStringsAppModel.LanguageItem) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(R.string.i18n_ui_realtime_translate_title)
            setMessage(R.string.i18n_ui_realtime_translate_text)
            setCancelable(true)
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()

        val job = GlobalScope.launch(Dispatchers.IO) {
            var errorStr = ""
            try {
                I18nStringsApp.loadOnlineLanguage(this@I18nSettingActivity, item.codeInfo)
            } catch (e: Exception) {
                e.printStackTrace()
                errorStr = e.toString()
            }

            withContext(Dispatchers.Main) {
                dialog.dismiss()
                if (errorStr.isEmpty()) {
                    showRebootDialog(item)
                } else {
                    showRealtimeTranslationFailsDialog(errorStr)
                }
            }
        }

        dialog.setOnCancelListener {
            job.cancel()
        }
    }

    private fun killApp() {
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }
}
