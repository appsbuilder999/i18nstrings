package com.appkim.i18nstrings

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.ViewPumpAppCompatDelegate
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.restring.LocaleProvider
import dev.b3nedikt.restring.PluralKeyword
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.restring.StringRepository
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import java.util.Locale

object RestringHelper {

    fun initRestring(context: Context, isFollowSystem: Boolean = false) {
        if(!isFollowSystem) {
            Restring.init(context)
        } else {
            Restring.stringRepository = EmptyStringRepository()
        }
        Restring.localeProvider = AppLocaleLocaleProvider
        ViewPump.init(RewordInterceptor)
    }

    fun getAppCompatDelegate(context: Context, delegate: AppCompatDelegate): AppCompatDelegate {
        return ViewPumpAppCompatDelegate(
                baseDelegate = delegate,
                baseContext = context,
                wrapContext = Restring::wrapContext
            )
    }

    fun wrapResources(applicationContext: Context, resources: Resources): Resources {
        return AppLocale.wrapResources(applicationContext, resources)
    }
}

object AppLocaleLocaleProvider : LocaleProvider {

    override val isInitial
        get() = AppLocale.isInitial

    override var currentLocale
        get() = AppLocale.desiredLocale
        set(value) {
            AppLocale.desiredLocale = value
        }
}

class EmptyStringRepository: StringRepository {
    override val quantityStrings: Map<Locale, Map<String, Map<PluralKeyword, CharSequence>>>
        get() = mapOf()
    override val stringArrays: Map<Locale, Map<String, Array<CharSequence>>>
        get() = mapOf()
    override val strings: Map<Locale, Map<String, CharSequence>>
        get() = mapOf()
    override val supportedLocales: Set<Locale>
        get() = setOf()

}