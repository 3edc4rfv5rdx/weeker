package com.weeker.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val keyLanguage = stringPreferencesKey("language")
    private val keyTheme = stringPreferencesKey("theme")
    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyOnboardingDone = booleanPreferencesKey("onboarding_done")

    val languageFlow: Flow<String?> = context.settingsDataStore.data.map { it[keyLanguage] }
    val themeFlow: Flow<String?> = context.settingsDataStore.data.map { it[keyTheme] }
    val themeModeFlow: Flow<String?> = context.settingsDataStore.data.map { it[keyThemeMode] }
    val onboardingDoneFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[keyOnboardingDone] ?: false }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { it[keyLanguage] = language }
    }

    suspend fun setTheme(themeId: String) {
        context.settingsDataStore.edit { it[keyTheme] = themeId }
    }

    suspend fun setThemeMode(modeId: String) {
        context.settingsDataStore.edit { it[keyThemeMode] = modeId }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.settingsDataStore.edit { it[keyOnboardingDone] = done }
    }
}
