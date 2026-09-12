package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChargingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSessionDao {
  @Query("SELECT * FROM charging_sessions ORDER BY timestamp DESC")
  fun getAllSessions(): Flow<List<ChargingSession>>

  @Query("SELECT * FROM charging_sessions WHERE id = :id LIMIT 1")
  suspend fun getSessionById(id: Long): ChargingSession?

  @Query("SELECT * FROM charging_sessions ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentSessions(limit: Int): Flow<List<ChargingSession>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: ChargingSession): Long

  @Update
  suspend fun updateSession(session: ChargingSession)

  @Delete
  suspend fun deleteSession(session: ChargingSession)

  @Query("DELETE FROM charging_sessions WHERE id = :id")
  suspend fun deleteSessionById(id: Long)

  @Query("SELECT * FROM charging_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
  fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<ChargingSession>>

  @Query("SELECT * FROM charging_sessions ORDER BY timestamp ASC")
  suspend fun getAllSessionsSnapshot(): List<ChargingSession>
}
