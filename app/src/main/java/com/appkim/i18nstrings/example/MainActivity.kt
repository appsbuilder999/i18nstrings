package com.appkim.i18nstrings.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.appkim.i18nstrings.I18nStringsApp


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun getDelegate(): AppCompatDelegate {
        return I18nStringsApp.getAppCompatDelegate(this, super.getDelegate())
    }
}