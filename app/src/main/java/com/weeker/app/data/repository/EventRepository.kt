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

    suspend fun deleteEvent(entity: EventEntity) {
        dao.delete(entity)
    }

    suspend fun moveEventToDate(entity: EventEntity, newEpochDay: Long) {
        val sortOrder = dao.countByDay(newEpochDay) + 1
        dao.update(entity.copy(dateEpochDay = newEpochDay, sortOrder = sortOrder))
    }

    suspend fun copyEventToDate(entity: EventEntity, newEpochDay: Long) {
        val sortOrder = dao.countByDay(newEpochDay) + 1
        dao.insert(
            entity.copy(
                id = 0,
                dateEpochDay = newEpochDay,
                isDone = false,
                sortOrder = sortOrder
            )
        )
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

    suspend fun exportEvents(): List<EventEntity> = dao.getAll()

    suspend fun replaceAllEvents(items: List<EventEntity>) {
        dao.deleteAll()
        val sanitized = items.map { it.copy(id = 0) }
        dao.insertAll(sanitized)
    }

    companion object {
        fun mondayStart(date: LocalDate): Long {
            val dayShift = date.dayOfWeek.value - 1
            return date.minusDays(dayShift.toLong()).toEpochDay()
        }
    }
}
