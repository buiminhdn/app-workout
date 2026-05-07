package com.marcus.workout.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    private const val DEFAULT_LANGUAGE = "en"

    /**
     * Force English on first launch if no locale has been explicitly set.
     * Call this once in Application or Activity.onCreate before setContent.
     */
    fun ensureDefaultLocale() {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            setLocale(DEFAULT_LANGUAGE)
        }
    }

    fun setLocale(languageTag: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getCurrentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) DEFAULT_LANGUAGE else locales[0]?.language ?: DEFAULT_LANGUAGE
    }
}
