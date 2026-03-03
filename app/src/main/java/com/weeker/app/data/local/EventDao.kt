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

    @Query("SELECT * FROM events WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC, sortOrder ASC, id ASC")
    fun observeByWeek(startEpochDay: Long, endEpochDay: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay AND isDone = 0 ORDER BY dateEpochDay ASC, sortOrder ASC")
    suspend fun getUndoneByWeek(startEpochDay: Long, endEpochDay: Long): List<EventEntity>

    @Query("SELECT COUNT(*) FROM events WHERE dateEpochDay = :epochDay")
    suspend fun countByDay(epochDay: Long): Int

    @Insert
    suspend fun insert(entity: EventEntity): Long

    @Update
    suspend fun update(entity: EventEntity)

    @Delete
    suspend fun delete(entity: EventEntity)
}
