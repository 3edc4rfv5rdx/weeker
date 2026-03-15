package com.weeker.app

import android.content.Context
import androidx.room.Room
import com.weeker.app.core.localization.LocalizationManager
import com.weeker.app.core.theme.ThemeManager
import com.weeker.app.data.local.AppDatabase
import com.weeker.app.data.repository.EventRepository
import com.weeker.app.data.repository.EventTemplateRepository
import com.weeker.app.data.repository.WeekNoteRepository
import com.weeker.app.data.settings.SettingsRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val db: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "weeker.db"
    ).addMigrations(AppDatabase.MIGRATION_1_2).build()

    val localizationManager = LocalizationManager(appContext)
    val themeManager = ThemeManager(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val eventRepository = EventRepository(db.eventDao())
    val weekNoteRepository = WeekNoteRepository(db.weekNoteDao())
    val eventTemplateRepository = EventTemplateRepository(db.eventTemplateDao())
}
