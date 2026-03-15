package com.weeker.app.data.repository

import com.weeker.app.data.local.WeekNoteDao
import com.weeker.app.data.local.WeekNoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeekNoteRepository(private val dao: WeekNoteDao) {

    fun observeByWeek(weekStart: Long): Flow<List<WeekNoteEntity>> =
        dao.observeByWeek(weekStart)

    fun observeAll(): Flow<List<WeekNoteEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<WeekNoteEntity>> = dao.search(query)

    fun observeWeeksWithNotes(): Flow<Set<Long>> =
        dao.observeWeeksWithNotes().map { it.toSet() }

    suspend fun getNote(id: Long): WeekNoteEntity? = dao.getById(id)

    suspend fun addNote(weekStart: Long, text: String) {
        val sortOrder = dao.countByWeek(weekStart) + 1
        dao.insert(
            WeekNoteEntity(
                weekStartEpochDay = weekStart,
                text = text,
                sortOrder = sortOrder
            )
        )
    }

    suspend fun updateNote(entity: WeekNoteEntity, text: String) {
        dao.update(entity.copy(text = text))
    }

    suspend fun deleteNote(entity: WeekNoteEntity) {
        dao.delete(entity)
    }

    suspend fun exportNotes(): List<WeekNoteEntity> = dao.getAll()

    suspend fun replaceAllNotes(items: List<WeekNoteEntity>) {
        dao.deleteAll()
        val sanitized = items.map { it.copy(id = 0) }
        dao.insertAll(sanitized)
    }
}
