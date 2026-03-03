package com.weeker.app.core.localization

import android.content.Context
import org.json.JSONObject

class LocalizationManager(context: Context) {
    private val defaultLanguage: String
    private val availableLanguages: List<String>
    private val dictionary: Map<String, Map<String, String>>

    init {
        val raw = context.assets.open("i18n.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        defaultLanguage = root.optString("defaultLanguage", "uk")

        val list = mutableListOf<String>()
        val languagesArray = root.optJSONArray("languages")
        if (languagesArray != null) {
            for (i in 0 until languagesArray.length()) {
                list += languagesArray.getString(i)
            }
        }
        if (list.isEmpty()) list += listOf(defaultLanguage, "en")
        availableLanguages = prioritizeLanguages(list.distinct())

        val strings = root.getJSONObject("strings")
        val map = mutableMapOf<String, Map<String, String>>()
        strings.keys().forEach { key ->
            val langObj = strings.getJSONObject(key)
            val byLang = mutableMapOf<String, String>()
            langObj.keys().forEach { lang ->
                byLang[lang] = langObj.getString(lang)
            }
            map[key] = byLang
        }
        dictionary = map
    }

    fun defaultLanguage(): String = defaultLanguage

    fun availableLanguages(): List<String> = availableLanguages

    fun text(key: String, language: String): String {
        val localized = dictionary[key]?.get(language)
        if (!localized.isNullOrBlank()) return localized
        val fallback = dictionary[key]?.get(defaultLanguage)
        if (!fallback.isNullOrBlank()) return fallback
        return key
    }

    private fun prioritizeLanguages(input: List<String>): List<String> {
        val preferred = listOf("en", "uk", "ru")
        val output = mutableListOf<String>()
        preferred.forEach { lang ->
            if (lang in input) output += lang
        }
        input.forEach { lang ->
            if (lang !in preferred) output += lang
        }
        return output
    }
}
