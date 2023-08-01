package com.appkim.i18nstrings.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.appkim.i18nstrings.I18nStringsApp
import com.appkim.i18nstrings.I18nStringsAppModel
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.restring.Restring

class I18nSettingView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val model: I18nStringsAppModel = I18nStringsApp.model
    private lateinit var languageText: TextView

    init {
        View.inflate(context, R.layout.language_setting_view, this)
        languageText = findViewById(R.id.language_text)
        setOnClickListener {
            if(model.isConfigFileExist) {
                val intent = Intent(context, I18nSettingActivity::class.java)
                context.startActivity(intent)
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

    fun updateLanguage() {
        if(model.selectedType == I18nStringsAppModel.TYPE_NONE) {
            languageText.text = model.items[0].name
        } else {
            languageText.text = model.items[0].codeInfo.name
        }
    }
}
