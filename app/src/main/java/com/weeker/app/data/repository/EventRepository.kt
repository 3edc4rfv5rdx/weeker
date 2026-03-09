package com.weeker.app.data.repository

import com.weeker.app.data.local.EventDao
import com.weeker.app.data.local.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EventRepository(private val dao: EventDao) {
    fun observeDay(epochDay: Long): Flow<List<EventEntity>> = dao.observeByDay(epochDay)

    fun observeWeek(weekStartEpochDay: Long): Flow<List<EventEntity>> =
        dao.observeByWeek(weekStartEpochDay, weekStartEpochDay + 6)

    private fun requireNotPast(epochDay: Long) {
        require(epochDay >= LocalDate.now().toEpochDay()) { "Past dates are not allowed" }
    }

    suspend fun addEvent(title: String, note: String, epochDay: Long) {
        requireNotPast(epochDay)
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
        val dayEvents = loadDayInTodayOrder(entity.dateEpochDay).toMutableList()
        val currentIndex = dayEvents.indexOfFirst { it.id == entity.id }
        if (currentIndex == -1) return

        val updated = dayEvents.removeAt(currentIndex).copy(isDone = value)
        val targetIndex = if (value) {
            dayEvents.size
        } else {
            dayEvents.indexOfFirst { it.isDone }.let { index -> if (index == -1) dayEvents.size else index }
        }
        dayEvents.add(targetIndex, updated)
        persistDayOrder(dayEvents)
    }

    suspend fun deleteEvent(entity: EventEntity) {
        dao.delete(entity)
        normalizeDayOrder(entity.dateEpochDay)
    }

    suspend fun moveEventToDate(entity: EventEntity, newEpochDay: Long) {
        requireNotPast(newEpochDay)
        val oldEpochDay = entity.dateEpochDay
        val sortOrder = dao.countByDay(newEpochDay) + 1
        dao.update(entity.copy(dateEpochDay = newEpochDay, sortOrder = sortOrder))
        normalizeDayOrder(oldEpochDay)
    }

    suspend fun copyEventToDate(entity: EventEntity, newEpochDay: Long) {
        requireNotPast(newEpochDay)
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

    suspend fun moveEventUpInToday(entity: EventEntity) {
        if (entity.isDone) return
        val dayEvents = loadDayInTodayOrder(entity.dateEpochDay).toMutableList()
        val currentIndex = dayEvents.indexOfFirst { it.id == entity.id }
        if (currentIndex <= 0) return
        if (dayEvents[currentIndex - 1].isDone) return
        val previous = dayEvents[currentIndex - 1]
        dayEvents[currentIndex - 1] = dayEvents[currentIndex]
        dayEvents[currentIndex] = previous
        persistDayOrder(dayEvents)
    }

    suspend fun moveEventDownInToday(entity: EventEntity) {
        if (entity.isDone) return
        val dayEvents = loadDayInTodayOrder(entity.dateEpochDay).toMutableList()
        val currentIndex = dayEvents.indexOfFirst { it.id == entity.id }
        if (currentIndex == -1 || currentIndex >= dayEvents.lastIndex) return
        if (dayEvents[currentIndex + 1].isDone) return
        val next = dayEvents[currentIndex + 1]
        dayEvents[currentIndex + 1] = dayEvents[currentIndex]
        dayEvents[currentIndex] = next
        persistDayOrder(dayEvents)
    }

    suspend fun exportEvents(): List<EventEntity> = dao.getAll()

    suspend fun replaceAllEvents(items: List<EventEntity>) {
        dao.deleteAll()
        val sanitized = items.map { it.copy(id = 0) }
        dao.insertAll(sanitized)
    }

    private suspend fun normalizeDayOrder(epochDay: Long) {
        persistDayOrder(loadDayInTodayOrder(epochDay))
    }

    private suspend fun loadDayInTodayOrder(epochDay: Long): List<EventEntity> =
        dao.getByDay(epochDay)
            .sortedWith(compareBy<EventEntity> { it.isDone }.thenBy { it.sortOrder }.thenBy { it.id })

    private suspend fun persistDayOrder(ordered: List<EventEntity>) {
        val normalized = ordered.mapIndexed { index, item ->
            item.copy(sortOrder = index + 1)
        }
        if (normalized.isNotEmpty()) {
            dao.updateAll(normalized)
        }
    }

    companion object {
        fun mondayStart(date: LocalDate): Long {
            val dayShift = date.dayOfWeek.value - 1
            return date.minusDays(dayShift.toLong()).toEpochDay()
        }
    }
}
