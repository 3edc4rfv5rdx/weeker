package com.weeker.app

import android.content.Context
import androidx.room.Room
import com.weeker.app.core.localization.LocalizationManager
import com.weeker.app.core.theme.ThemeManager
import com.weeker.app.data.local.AppDatabase
import com.weeker.app.data.repository.EventRepository
import com.weeker.app.data.settings.SettingsRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val db: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "weeker.db"
    ).build()

    val localizationManager = LocalizationManager(appContext)
    val themeManager = ThemeManager(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val eventRepository = EventRepository(db.eventDao())
}
