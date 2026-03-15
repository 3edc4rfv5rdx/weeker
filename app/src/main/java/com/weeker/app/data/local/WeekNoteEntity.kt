package com.weeker.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "week_notes",
    indices = [
        Index(value = ["weekStartEpochDay"])
    ]
)
data class WeekNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartEpochDay: Long,
    val text: String,
    val sortOrder: Int
)
