package com.weeker.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val keyLanguage = stringPreferencesKey("language")
    private val keyTheme = stringPreferencesKey("theme")
    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyOnboardingDone = booleanPreferencesKey("onboarding_done")
    private val keyAllowEditPast = booleanPreferencesKey("allow_edit_past")
    private val keyRestoreUseDialog = booleanPreferencesKey("restore_use_dialog")

    val languageFlow: Flow<String?> = context.settingsDataStore.data.map { it[keyLanguage] }
    val themeFlow: Flow<String?> = context.settingsDataStore.data.map { it[keyTheme] }
    val themeModeFlow: Flow<String?> = context.settingsDataStore.data.map { it[keyThemeMode] }
    val onboardingDoneFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[keyOnboardingDone] ?: false }
    val allowEditPastFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[keyAllowEditPast] ?: false }
    val restoreUseDialogFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[keyRestoreUseDialog] ?: true }

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

    suspend fun setAllowEditPast(allow: Boolean) {
        context.settingsDataStore.edit { it[keyAllowEditPast] = allow }
    }

    suspend fun setRestoreUseDialog(use: Boolean) {
        context.settingsDataStore.edit { it[keyRestoreUseDialog] = use }
    }

    suspend fun exportSettings(): Map<String, String> {
        val prefs = context.settingsDataStore.data.first()
        val map = mutableMapOf<String, String>()
        prefs[keyLanguage]?.let { map["language"] = it }
        prefs[keyTheme]?.let { map["theme"] = it }
        prefs[keyThemeMode]?.let { map["theme_mode"] = it }
        prefs[keyOnboardingDone]?.let { map["onboarding_done"] = it.toString() }
        prefs[keyAllowEditPast]?.let { map["allow_edit_past"] = it.toString() }
        prefs[keyRestoreUseDialog]?.let { map["restore_use_dialog"] = it.toString() }
        return map
    }

    suspend fun restoreSettings(settings: Map<String, String>) {
        context.settingsDataStore.edit { prefs ->
            settings["language"]?.let { prefs[keyLanguage] = it }
            settings["theme"]?.let { prefs[keyTheme] = it }
            settings["theme_mode"]?.let { prefs[keyThemeMode] = it }
            settings["onboarding_done"]?.let { prefs[keyOnboardingDone] = it.toBoolean() }
            settings["allow_edit_past"]?.let { prefs[keyAllowEditPast] = it.toBoolean() }
            settings["restore_use_dialog"]?.let { prefs[keyRestoreUseDialog] = it.toBoolean() }
        }
    }
}
