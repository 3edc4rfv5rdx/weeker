package com.weeker.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekNoteDao {
    @Query("SELECT * FROM week_notes WHERE weekStartEpochDay = :weekStart ORDER BY sortOrder ASC, id ASC")
    fun observeByWeek(weekStart: Long): Flow<List<WeekNoteEntity>>

    @Query("SELECT * FROM week_notes WHERE weekStartEpochDay = :weekStart ORDER BY sortOrder ASC, id ASC")
    suspend fun getByWeek(weekStart: Long): List<WeekNoteEntity>

    @Query("SELECT * FROM week_notes WHERE id = :id")
    suspend fun getById(id: Long): WeekNoteEntity?

    @Query("SELECT * FROM week_notes ORDER BY weekStartEpochDay DESC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<WeekNoteEntity>>

    @Query("SELECT * FROM week_notes WHERE text LIKE '%' || :query || '%' ORDER BY weekStartEpochDay DESC, sortOrder ASC, id ASC")
    fun search(query: String): Flow<List<WeekNoteEntity>>

    @Query("SELECT * FROM week_notes ORDER BY weekStartEpochDay DESC, sortOrder ASC, id ASC")
    suspend fun getAll(): List<WeekNoteEntity>

    @Query("SELECT COUNT(*) FROM week_notes WHERE weekStartEpochDay = :weekStart")
    suspend fun countByWeek(weekStart: Long): Int

    @Insert
    suspend fun insert(entity: WeekNoteEntity): Long

    @Insert
    suspend fun insertAll(entities: List<WeekNoteEntity>)

    @Update
    suspend fun update(entity: WeekNoteEntity)

    @Delete
    suspend fun delete(entity: WeekNoteEntity)

    @Query("SELECT DISTINCT weekStartEpochDay FROM week_notes")
    fun observeWeeksWithNotes(): Flow<List<Long>>

    @Query("DELETE FROM week_notes")
    suspend fun deleteAll()
}
