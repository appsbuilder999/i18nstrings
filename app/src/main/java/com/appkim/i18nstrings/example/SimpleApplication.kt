package com.appkim.i18nstrings.example

import android.app.Application
import android.content.res.Resources
import com.appkim.i18nstrings.I18nStringsApp

class SimpleApplication:Application() {

    override fun onCreate() {
        super.onCreate()

        I18nStringsApp.init(applicationContext)
    }

    override fun getResources(): Resources {
        return I18nStringsApp.wrapResources(applicationContext, super.getResources())
    }
}

