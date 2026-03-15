package com.weeker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EventEntity::class, WeekNoteEntity::class, EventTemplateEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun weekNoteDao(): WeekNoteDao
    abstract fun eventTemplateDao(): EventTemplateDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `week_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `weekStartEpochDay` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_week_notes_weekStartEpochDay` ON `week_notes` (`weekStartEpochDay`)"
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `event_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
