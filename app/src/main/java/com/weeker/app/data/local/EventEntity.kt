package com.weeker.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["dateEpochDay", "sortOrder"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String,
    val dateEpochDay: Long,
    val isDone: Boolean,
    val sortOrder: Int
)
