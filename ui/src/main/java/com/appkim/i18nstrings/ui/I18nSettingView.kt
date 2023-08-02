package com.appkim.i18nstrings.ui

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.appkim.i18nstrings.I18nStringsApp
import com.appkim.i18nstrings.I18nStringsAppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class I18nSettingView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val model: I18nStringsAppModel = I18nStringsApp.model
    private var languageText: TextView

    init {
        View.inflate(context, R.layout.language_setting_view, this)
        languageText = findViewById(R.id.language_text)
        setOnClickListener {
            if(model.isConfigFileExist) {
                showLanguageListDialog()
            } else {
                AlertDialog.Builder(context)
                    .setTitle(R.string.i18n_ui_setting)
                    .setMessage(R.string.i18n_not_supported)
                    .setPositiveButton(android.R.string.ok) {
                            dialog, _ -> dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateLanguage()
    }

    private fun updateLanguage() {
        if(model.selectedType == I18nStringsAppModel.TYPE_NONE) {
            languageText.text = model.items[0].name
        } else {
            languageText.text = model.items[0].codeInfo.name
        }
    }

    private fun showLanguageListDialog() {

        val itemsName = model.items.map { it.name }.toTypedArray()

        val builder = AlertDialog.Builder(context) //.setTitle("Choose a Language")
        builder.setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, itemsName)) { _, position ->
                if (position == 0) return@setAdapter
                val item = model.items[position]

                if (!model.getDisclaimerAgree(context)) {
                    showDisclaimerDialog(item)
                } else {
                    processLanguageSwitch(item)
                }
            }
            .show()

//        languageListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
//            if(position == 0) return@OnItemLongClickListener false
//            val item = model.items[position]
//            if(item.type == I18nStringsAppModel.TYPE_ONLINE_CACHE) {
//                showRefreshTranslateDialog(item)
//                true
//            } else {
//                false
//            }
//        }
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
        val builder = AlertDialog.Builder(context)
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
        val message = context.getString(R.string.i18n_ui_realtime_translate_fails_content) + "\n" + errorStr
        val builder = AlertDialog.Builder(context)
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
        val builder = AlertDialog.Builder(context)
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
        val builder = AlertDialog.Builder(context)
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
        val pair = I18nStringsApp.getEstimatedTime(context, item.codeInfo)
        val size = pair.first
        val time = pair.second
        val min = time / 60
        val sec = time % 60
        val message = if(min > 0) {
            context.getString(R.string.i18n_ui_realtime_translate_comfirm_min, min, size)
        } else {
            context.getString(R.string.i18n_ui_realtime_translate_comfirm_sec, sec, size)
        }

        val builder = AlertDialog.Builder(context)
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
        val builder = AlertDialog.Builder(context)
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
                I18nStringsApp.loadOnlineLanguage(context, item.codeInfo)
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
