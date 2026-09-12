package com.example.data.repository

import com.example.data.local.ChargingPlaceDao
import com.example.data.local.ChargingSessionDao
import com.example.data.model.ChargingPlace
import com.example.data.model.ChargingSession
import com.example.data.model.TariffType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EvTrackerRepository(
  private val placeDao: ChargingPlaceDao,
  private val sessionDao: ChargingSessionDao
) {
  val allPlaces: Flow<List<ChargingPlace>> = placeDao.getAllPlaces()
  val allSessions: Flow<List<ChargingSession>> = sessionDao.getAllSessions()

  fun getRecentSessions(limit: Int = 4): Flow<List<ChargingSession>> {
    return sessionDao.getRecentSessions(limit)
  }

  suspend fun ensureDefaultPlaces() = withContext(Dispatchers.IO) {
    if (placeDao.getPlaceCount() == 0) {
      val defaultPlaces = listOf(
        ChargingPlace(
          name = "Дом (АС)",
          emoji = "🏠",
          colorHex = 0xFF059669, // Forest Green
          tariffType = TariffType.DAY_NIGHT,
          pricePerKwh = 0.25,
          dayPricePerKwh = 0.28,
          nightPricePerKwh = 0.16,
          dayStartHour = 7,
          nightStartHour = 23,
          sortOrder = 1,
          isDefault = true
        ),
        ChargingPlace(
          name = "Malanka DC",
          emoji = "⚡",
          colorHex = 0xFFF59E0B, // Amber Orange
          tariffType = TariffType.FIXED_KWH,
          pricePerKwh = 0.56,
          sortOrder = 2
        ),
        ChargingPlace(
          name = "Malanka AC",
          emoji = "🔌",
          colorHex = 0xFF0284C7, // Sky Blue
          tariffType = TariffType.FIXED_KWH,
          pricePerKwh = 0.43,
          sortOrder = 3
        ),
        ChargingPlace(
          name = "Дача",
          emoji = "🏡",
          colorHex = 0xFF10B981, // Emerald Green
          tariffType = TariffType.FIXED_KWH,
          pricePerKwh = 0.25,
          sortOrder = 4
        ),
        ChargingPlace(
          name = "Работа",
          emoji = "🏢",
          colorHex = 0xFF8B5CF6, // Purple
          tariffType = TariffType.FIXED_KWH,
          pricePerKwh = 0.22,
          sortOrder = 5
        ),
        ChargingPlace(
          name = "Бесплатная",
          emoji = "☀️",
          colorHex = 0xFFEAB308, // Sun Yellow
          tariffType = TariffType.FREE,
          pricePerKwh = 0.0,
          sortOrder = 6
        )
      )
      placeDao.insertPlaces(defaultPlaces)
    }
  }

  suspend fun getPlaceById(id: Long): ChargingPlace? = withContext(Dispatchers.IO) {
    placeDao.getPlaceById(id)
  }

  suspend fun insertPlace(place: ChargingPlace): Long = withContext(Dispatchers.IO) {
    placeDao.insertPlace(place)
  }

  suspend fun updatePlace(place: ChargingPlace) = withContext(Dispatchers.IO) {
    placeDao.updatePlace(place)
  }

  suspend fun deletePlace(place: ChargingPlace) = withContext(Dispatchers.IO) {
    placeDao.deletePlace(place)
  }

  suspend fun getSessionById(id: Long): ChargingSession? = withContext(Dispatchers.IO) {
    sessionDao.getSessionById(id)
  }

  suspend fun insertSession(session: ChargingSession): Long = withContext(Dispatchers.IO) {
    sessionDao.insertSession(session)
  }

  suspend fun updateSession(session: ChargingSession) = withContext(Dispatchers.IO) {
    sessionDao.updateSession(session)
  }

  suspend fun deleteSession(session: ChargingSession) = withContext(Dispatchers.IO) {
    sessionDao.deleteSession(session)
  }

  suspend fun deleteSessionById(id: Long) = withContext(Dispatchers.IO) {
    sessionDao.deleteSessionById(id)
  }

  suspend fun getAllSessionsSnapshot(): List<ChargingSession> = withContext(Dispatchers.IO) {
    sessionDao.getAllSessionsSnapshot()
  }
}
