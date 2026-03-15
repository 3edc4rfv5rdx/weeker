package com.weeker.app.data.repository

import com.weeker.app.data.local.EventTemplateDao
import com.weeker.app.data.local.EventTemplateEntity
import kotlinx.coroutines.flow.Flow

class EventTemplateRepository(private val dao: EventTemplateDao) {

    fun observeAll(): Flow<List<EventTemplateEntity>> = dao.observeAll()

    suspend fun getAll(): List<EventTemplateEntity> = dao.getAll()

    suspend fun addTemplate(title: String) {
        val sortOrder = dao.count() + 1
        dao.insert(
            EventTemplateEntity(
                title = title,
                sortOrder = sortOrder
            )
        )
    }

    suspend fun updateTemplate(entity: EventTemplateEntity, title: String) {
        dao.update(entity.copy(title = title))
    }

    suspend fun deleteTemplate(entity: EventTemplateEntity) {
        dao.delete(entity)
        normalizeOrder()
    }

    suspend fun moveUp(entity: EventTemplateEntity) {
        val all = dao.getAll().toMutableList()
        val index = all.indexOfFirst { it.id == entity.id }
        if (index <= 0) return
        val prev = all[index - 1]
        all[index - 1] = all[index]
        all[index] = prev
        persistOrder(all)
    }

    suspend fun moveDown(entity: EventTemplateEntity) {
        val all = dao.getAll().toMutableList()
        val index = all.indexOfFirst { it.id == entity.id }
        if (index == -1 || index >= all.lastIndex) return
        val next = all[index + 1]
        all[index + 1] = all[index]
        all[index] = next
        persistOrder(all)
    }

    suspend fun exportTemplates(): List<EventTemplateEntity> = dao.getAll()

    suspend fun replaceAllTemplates(items: List<EventTemplateEntity>) {
        dao.deleteAll()
        val sanitized = items.map { it.copy(id = 0) }
        dao.insertAll(sanitized)
    }

    private suspend fun normalizeOrder() {
        persistOrder(dao.getAll())
    }

    private suspend fun persistOrder(ordered: List<EventTemplateEntity>) {
        val normalized = ordered.mapIndexed { index, item ->
            item.copy(sortOrder = index + 1)
        }
        if (normalized.isNotEmpty()) {
            dao.updateAll(normalized)
        }
    }
}
