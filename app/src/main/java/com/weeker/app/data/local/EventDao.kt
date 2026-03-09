package com.weeker.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE dateEpochDay = :epochDay ORDER BY sortOrder ASC, id ASC")
    fun observeByDay(epochDay: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE dateEpochDay = :epochDay ORDER BY sortOrder ASC, id ASC")
    suspend fun getByDay(epochDay: Long): List<EventEntity>

    @Query("SELECT * FROM events WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC, sortOrder ASC, id ASC")
    fun observeByWeek(startEpochDay: Long, endEpochDay: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC, sortOrder ASC, id ASC")
    suspend fun getAll(): List<EventEntity>

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM events WHERE dateEpochDay = :epochDay")
    suspend fun countByDay(epochDay: Long): Int

    @Insert
    suspend fun insert(entity: EventEntity): Long

    @Insert
    suspend fun insertAll(entities: List<EventEntity>)

    @Update
    suspend fun update(entity: EventEntity)

    @Update
    suspend fun updateAll(entities: List<EventEntity>)

    @Delete
    suspend fun delete(entity: EventEntity)
}
