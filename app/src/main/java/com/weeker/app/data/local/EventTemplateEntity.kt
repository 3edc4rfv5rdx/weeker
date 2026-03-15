package com.weeker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_templates")
data class EventTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sortOrder: Int
)
