package com.weeker.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventTemplateDao {
    @Query("SELECT * FROM event_templates ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<EventTemplateEntity>>

    @Query("SELECT * FROM event_templates ORDER BY sortOrder ASC, id ASC")
    suspend fun getAll(): List<EventTemplateEntity>

    @Query("SELECT * FROM event_templates WHERE id = :id")
    suspend fun getById(id: Long): EventTemplateEntity?

    @Query("SELECT COUNT(*) FROM event_templates")
    suspend fun count(): Int

    @Insert
    suspend fun insert(entity: EventTemplateEntity): Long

    @Insert
    suspend fun insertAll(entities: List<EventTemplateEntity>)

    @Update
    suspend fun update(entity: EventTemplateEntity)

    @Update
    suspend fun updateAll(entities: List<EventTemplateEntity>)

    @Delete
    suspend fun delete(entity: EventTemplateEntity)

    @Query("DELETE FROM event_templates")
    suspend fun deleteAll()
}
