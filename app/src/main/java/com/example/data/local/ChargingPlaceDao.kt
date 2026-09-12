package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChargingPlace
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingPlaceDao {
  @Query("SELECT * FROM charging_places ORDER BY sortOrder ASC, id ASC")
  fun getAllPlaces(): Flow<List<ChargingPlace>>

  @Query("SELECT * FROM charging_places WHERE id = :id LIMIT 1")
  suspend fun getPlaceById(id: Long): ChargingPlace?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlace(place: ChargingPlace): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlaces(places: List<ChargingPlace>)

  @Update
  suspend fun updatePlace(place: ChargingPlace)

  @Delete
  suspend fun deletePlace(place: ChargingPlace)

  @Query("SELECT COUNT(*) FROM charging_places")
  suspend fun getPlaceCount(): Int
}
