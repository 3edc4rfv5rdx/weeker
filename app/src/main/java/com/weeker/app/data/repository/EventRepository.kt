package com.weeker.app.data.repository

import com.weeker.app.data.local.EventDao
import com.weeker.app.data.local.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EventRepository(private val dao: EventDao) {
    fun observeDay(epochDay: Long): Flow<List<EventEntity>> = dao.observeByDay(epochDay)

    fun observeWeek(weekStartEpochDay: Long): Flow<List<EventEntity>> =
        dao.observeByWeek(weekStartEpochDay, weekStartEpochDay + 6)

    suspend fun addEvent(title: String, note: String, epochDay: Long) {
        val sortOrder = dao.countByDay(epochDay) + 1
        dao.insert(
            EventEntity(
                title = title,
                note = note,
                dateEpochDay = epochDay,
                isDone = false,
                sortOrder = sortOrder
            )
        )
    }

    suspend fun toggleDone(entity: EventEntity, value: Boolean) {
        dao.update(entity.copy(isDone = value))
    }

    suspend fun moveUndoneToNextWeek(weekStartEpochDay: Long) {
        val undone = dao.getUndoneByWeek(weekStartEpochDay, weekStartEpochDay + 6)
        undone.forEach { item ->
            dao.insert(
                item.copy(
                    id = 0,
                    dateEpochDay = item.dateEpochDay + 7,
                    isDone = false
                )
            )
        }
    }

    companion object {
        fun mondayStart(date: LocalDate): Long {
            val dayShift = date.dayOfWeek.value - 1
            return date.minusDays(dayShift.toLong()).toEpochDay()
        }
    }
}
