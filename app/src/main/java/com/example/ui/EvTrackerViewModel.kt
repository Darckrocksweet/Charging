package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChargingPlace
import com.example.data.model.ChargingSession
import com.example.data.model.PreferencesManager
import com.example.data.model.UserPreferences
import com.example.data.repository.EvTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodFilter(val titleRu: String, val days: Int) {
  MONTH("Месяц", 30),
  THREE_MONTHS("3 месяца", 90),
  SIX_MONTHS("6 месяцев", 180),
  YEAR("Год", 365),
  ALL_TIME("Всё время", -1)
}

enum class DataSourceFilter(val titleRu: String) {
  FROM_CHARGER("От зарядки"),
  FROM_CAR("От машины"),
  BOTH("Оба источника")
}

data class MonthSummary(
  val yearMonthKey: String, // e.g. "2026-09"
  val displayTitle: String, // e.g. "Сентябрь 2026"
  val totalCost: Double,
  val chargerKwh: Double,
  val carKwh: Double,
  val lossesKwh: Double,
  val lossPercent: Double,
  val sessionCount: Int,
  val avgPricePerKwh: Double
)

data class PlaceBreakdown(
  val placeName: String,
  val emoji: String,
  val colorHex: Long,
  val totalCost: Double,
  val chargerKwh: Double,
  val carKwh: Double,
  val lossesKwh: Double,
  val percentageOfTotalKwh: Float,
  val sessionCount: Int
)

data class StatisticsData(
  val period: PeriodFilter = PeriodFilter.ALL_TIME,
  val source: DataSourceFilter = DataSourceFilter.BOTH,
  val totalCost: Double = 0.0,
  val totalChargerKwh: Double = 0.0,
  val totalCarKwh: Double = 0.0,
  val totalLossesKwh: Double = 0.0,
  val lossPercent: Double = 0.0,
  val efficiencyPercent: Double = 100.0,
  val totalSessions: Int = 0,
  val avgPricePerKwh: Double = 0.0,
  val totalKm: Double = 0.0,
  val avgConsumptionCharger: Double? = null, // кВт·ч / 100 км по зарядке
  val avgConsumptionCar: Double? = null,     // кВт·ч / 100 км по батарее машины
  val pricePer1Km: Double? = null,
  val pricePer100Km: Double? = null,
  val monthlySummaries: List<MonthSummary> = emptyList(),
  val placeBreakdowns: List<PlaceBreakdown> = emptyList()
)

data class MainScreenConsumptionMetric(
  val value: Double?,
  val displayText: String,
  val subtitleText: String,
  val carValue: Double? = null
)

class EvTrackerViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val repository = EvTrackerRepository(
    database.chargingPlaceDao(),
    database.chargingSessionDao()
  )
  private val preferencesManager = PreferencesManager(application)

  val preferences: StateFlow<UserPreferences> = preferencesManager.preferencesFlow

  val allPlaces: StateFlow<List<ChargingPlace>> = repository.allPlaces
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val allSessions: StateFlow<List<ChargingSession>> = repository.allSessions
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val recentSessions: StateFlow<List<ChargingSession>> = repository.getRecentSessions(5)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Filter selections for Statistics Screen
  private val _selectedPeriod = MutableStateFlow(PeriodFilter.ALL_TIME)
  val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

  private val _selectedSource = MutableStateFlow(DataSourceFilter.BOTH)
  val selectedSource: StateFlow<DataSourceFilter> = _selectedSource.asStateFlow()

  fun setPeriodFilter(period: PeriodFilter) {
    _selectedPeriod.value = period
  }

  fun setSourceFilter(source: DataSourceFilter) {
    _selectedSource.value = source
  }

  // Combined statistics for Statistics Screen
  val statistics: StateFlow<StatisticsData> = combine(
    repository.allSessions,
    repository.allPlaces,
    preferences,
    _selectedPeriod,
    _selectedSource
  ) { sessions, places, prefs, period, source ->
    calculateStatistics(sessions, places, prefs, period, source)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = StatisticsData()
  )

  // Hero consumption metric for Main Screen ("data from last month")
  val mainScreenConsumption: StateFlow<MainScreenConsumptionMetric> = repository.allSessions
    .combine(preferences) { sessions, prefs ->
      calculateMainScreenConsumption(sessions, prefs)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = MainScreenConsumptionMetric(null, "—", "Нажмите для подробной статистики")
    )

  // Last deleted session for undo snackbar
  private val _lastDeletedSession = MutableStateFlow<ChargingSession?>(null)
  val lastDeletedSession: StateFlow<ChargingSession?> = _lastDeletedSession.asStateFlow()

  init {
    viewModelScope.launch {
      repository.ensureDefaultPlaces()
    }
  }

  fun saveSession(session: ChargingSession) {
    viewModelScope.launch {
      if (session.id == 0L) {
        repository.insertSession(session)
      } else {
        repository.updateSession(session)
      }
    }
  }

  fun deleteSession(session: ChargingSession) {
    viewModelScope.launch {
      _lastDeletedSession.value = session
      repository.deleteSession(session)
    }
  }

  fun restoreLastDeletedSession() {
    val sessionToRestore = _lastDeletedSession.value ?: return
    viewModelScope.launch {
      repository.insertSession(sessionToRestore.copy(id = 0L))
      _lastDeletedSession.value = null
    }
  }

  fun clearLastDeletedSession() {
    _lastDeletedSession.value = null
  }

  fun savePlace(place: ChargingPlace) {
    viewModelScope.launch {
      if (place.id == 0L) {
        repository.insertPlace(place)
      } else {
        repository.updatePlace(place)
      }
    }
  }

  fun deletePlace(place: ChargingPlace) {
    viewModelScope.launch {
      repository.deletePlace(place)
    }
  }

  fun updatePreferences(newPrefs: UserPreferences) {
    preferencesManager.updatePreferences(newPrefs)
  }

  fun updateVehicleProfile(
    name: String,
    model: String,
    capacity: Double,
    initialOdo: Double?,
    initialBattery: Int?
  ) {
    preferencesManager.updateVehicleProfile(name, model, capacity, initialOdo, initialBattery)
  }

  // Get last known battery state from the most recent session or initial car settings
  fun getLastKnownBatteryState(): Pair<Int, Double>? {
    val latest = allSessions.value.firstOrNull()
    if (latest != null) {
      val percent = latest.batteryEndPercent
      val kwh = latest.batteryEndKwh
      if (percent != null || kwh != null) {
        return Pair(percent ?: 20, kwh ?: 12.0)
      }
    }
    val initialPct = preferences.value.initialBatteryPercent ?: 100
    val cap = preferences.value.batteryCapacityKwh
    return Pair(initialPct, cap * (initialPct / 100.0))
  }

  private fun calculateMainScreenConsumption(
    sessions: List<ChargingSession>,
    prefs: UserPreferences
  ): MainScreenConsumptionMetric {
    val distanceUnit = prefs.distanceUnit
    val initialOdo = prefs.initialOdometer

    if (sessions.isEmpty()) {
      return if (initialOdo != null && initialOdo > 0) {
        MainScreenConsumptionMetric(
          value = null,
          displayText = "—",
          subtitleText = "Старт одометра: ${String.format(Locale.US, "%.0f", initialOdo)} $distanceUnit • Запишите первую зарядку"
        )
      } else {
        MainScreenConsumptionMetric(
          value = null,
          displayText = "—",
          subtitleText = "Нажмите на авто для ввода начального одометра"
        )
      }
    }

    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    // Previous calendar month bounds
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfCurrentMonth = calendar.timeInMillis

    calendar.add(Calendar.MONTH, -1)
    val startOfLastMonth = calendar.timeInMillis
    val endOfLastMonth = startOfCurrentMonth - 1

    val lastMonthSessions = sessions.filter { it.timestamp in startOfLastMonth..endOfLastMonth }

    val targetSessions = if (lastMonthSessions.isNotEmpty()) {
      lastMonthSessions
    } else {
      // Fallback to last 30 days or all sessions
      val thirtyDaysAgo = now - (30L * 24 * 3600 * 1000)
      val recent30 = sessions.filter { it.timestamp >= thirtyDaysAgo }
      if (recent30.isNotEmpty()) recent30 else sessions
    }

    val isPreviousMonth = lastMonthSessions.isNotEmpty()
    val periodLabel = if (isPreviousMonth) "за прошлый месяц" else "за период"

    // Calculate distance and energy starting from initial odometer if available
    val sortedAsc = targetSessions.sortedBy { it.timestamp }
    var totalDistance = 0.0
    var totalChargerKwh = 0.0
    var totalCarKwh = 0.0
    var lastOdo: Double? = initialOdo

    for (s in sortedAsc) {
      val odo = s.odometer
      if (odo != null && odo > 0) {
        if (lastOdo != null && odo > lastOdo) {
          totalDistance += (odo - lastOdo)
          totalChargerKwh += s.chargerKwh
          totalCarKwh += s.carKwh
        }
        lastOdo = odo
      }
    }

    // If we have distance in the subset
    if (totalDistance >= 5.0 && totalChargerKwh > 0.0) {
      val consumption = (totalChargerKwh / totalDistance) * 100.0
      val carConsumption = (totalCarKwh / totalDistance) * 100.0
      val infoLabel = if (sessions.size == 1 && initialOdo != null) {
        "Расход с 1-й заправки (пробег ${String.format(Locale.US, "%.0f", totalDistance)} $distanceUnit)"
      } else {
        "Средний расход $periodLabel (пробег ${String.format(Locale.US, "%.0f", totalDistance)} $distanceUnit)"
      }
      return MainScreenConsumptionMetric(
        value = consumption,
        displayText = String.format(Locale.US, "%.1f", consumption),
        subtitleText = infoLabel,
        carValue = carConsumption
      )
    }

    // Check all-time distance if target had too few odometer points
    val allSorted = sessions.sortedBy { it.timestamp }
    var allDist = 0.0
    var allChargerKwh = 0.0
    var allCarKwh = 0.0
    var odoPrev: Double? = initialOdo

    for (s in allSorted) {
      val odo = s.odometer
      if (odo != null && odo > 0) {
        if (odoPrev != null && odo > odoPrev) {
          allDist += (odo - odoPrev)
          allChargerKwh += s.chargerKwh
          allCarKwh += s.carKwh
        }
        odoPrev = odo
      }
    }

    if (allDist >= 5.0 && allChargerKwh > 0.0) {
      val consumption = (allChargerKwh / allDist) * 100.0
      val carConsumption = (allCarKwh / allDist) * 100.0
      return MainScreenConsumptionMetric(
        value = consumption,
        displayText = String.format(Locale.US, "%.1f", consumption),
        subtitleText = "Средний расход (всего пробег ${String.format(Locale.US, "%.0f", allDist)} $distanceUnit)",
        carValue = carConsumption
      )
    }

    // Sessions exist but no odometer readings entered yet
    val totalKwh = sessions.sumOf { it.chargerKwh }
    return MainScreenConsumptionMetric(
      value = null,
      displayText = String.format(Locale.US, "%.0f", totalKwh),
      subtitleText = "Всего заряжено ${String.format(Locale.US, "%.1f", totalKwh)} кВт·ч (укажите одометр для расчёта расхода)"
    )
  }

  private fun calculateStatistics(
    allSessions: List<ChargingSession>,
    places: List<ChargingPlace>,
    prefs: UserPreferences,
    period: PeriodFilter,
    source: DataSourceFilter
  ): StatisticsData {
    if (allSessions.isEmpty()) {
      return StatisticsData(period = period, source = source)
    }

    val now = System.currentTimeMillis()
    val filteredSessions = if (period.days > 0) {
      val cutoff = now - (period.days.toLong() * 24 * 3600 * 1000)
      allSessions.filter { it.timestamp >= cutoff }
    } else {
      allSessions
    }

    if (filteredSessions.isEmpty()) {
      return StatisticsData(period = period, source = source)
    }

    val totalCost = filteredSessions.sumOf { it.cost }
    val totalChargerKwh = filteredSessions.sumOf { it.chargerKwh }
    val totalCarKwh = filteredSessions.sumOf { it.carKwh }
    val totalLossesKwh = (totalChargerKwh - totalCarKwh).coerceAtLeast(0.0)
    val lossPercent = if (totalChargerKwh > 0.0) (totalLossesKwh / totalChargerKwh) * 100.0 else 0.0
    val efficiencyPercent = if (totalChargerKwh > 0.0) ((totalCarKwh / totalChargerKwh) * 100.0).coerceIn(0.0, 100.0) else 100.0
    val avgPricePerKwh = if (totalChargerKwh > 0.0) totalCost / totalChargerKwh else 0.0

    // Odometer distance using initial odometer if available
    val sortedAsc = filteredSessions.sortedBy { it.timestamp }
    var totalDistance = 0.0
    var distChargerKwh = 0.0
    var distCarKwh = 0.0
    var lastOdo: Double? = prefs.initialOdometer

    for (s in sortedAsc) {
      val odo = s.odometer
      if (odo != null && odo > 0) {
        if (lastOdo != null && odo > lastOdo) {
          totalDistance += (odo - lastOdo)
          distChargerKwh += s.chargerKwh
          distCarKwh += s.carKwh
        }
        lastOdo = odo
      }
    }

    val avgConsumptionCharger = if (totalDistance >= 5.0 && distChargerKwh > 0) {
      (distChargerKwh / totalDistance) * 100.0
    } else null

    val avgConsumptionCar = if (totalDistance >= 5.0 && distCarKwh > 0) {
      (distCarKwh / totalDistance) * 100.0
    } else null

    val pricePer1Km = if (totalDistance >= 5.0 && totalCost > 0) {
      totalCost / totalDistance
    } else null

    val pricePer100Km = pricePer1Km?.let { it * 100.0 }

    // Monthly breakdown (Russian month titles)
    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    val monthTitleFormat = SimpleDateFormat("LLLL yyyy", Locale("ru"))

    val groupedByMonth = filteredSessions.groupBy { monthFormat.format(Date(it.timestamp)) }
    val monthlySummaries = groupedByMonth.entries.map { (key, mSessions) ->
      val cost = mSessions.sumOf { it.cost }
      val chKwh = mSessions.sumOf { it.chargerKwh }
      val cKwh = mSessions.sumOf { it.carKwh }
      val lKwh = (chKwh - cKwh).coerceAtLeast(0.0)
      val lPct = if (chKwh > 0.0) (lKwh / chKwh) * 100.0 else 0.0
      val firstDate = Date(mSessions.first().timestamp)
      val formattedTitle = monthTitleFormat.format(firstDate)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }

      MonthSummary(
        yearMonthKey = key,
        displayTitle = formattedTitle,
        totalCost = cost,
        chargerKwh = chKwh,
        carKwh = cKwh,
        lossesKwh = lKwh,
        lossPercent = lPct,
        sessionCount = mSessions.size,
        avgPricePerKwh = if (chKwh > 0.0) cost / chKwh else 0.0
      )
    }.sortedByDescending { it.yearMonthKey }

    // Breakdown by station
    val groupedByPlace = filteredSessions.groupBy { it.placeName }
    val placeBreakdowns = groupedByPlace.map { (placeName, placeSessions) ->
      val cost = placeSessions.sumOf { it.cost }
      val chKwh = placeSessions.sumOf { it.chargerKwh }
      val cKwh = placeSessions.sumOf { it.carKwh }
      val lKwh = (chKwh - cKwh).coerceAtLeast(0.0)
      val first = placeSessions.first()
      val matchedPlace = places.find { it.name.equals(placeName, ignoreCase = true) }
      val emoji = matchedPlace?.emoji ?: first.placeEmoji
      val colorHex = matchedPlace?.colorHex ?: first.placeColorHex
      val pct = if (totalChargerKwh > 0.0) (chKwh / totalChargerKwh).toFloat() else 0f

      PlaceBreakdown(
        placeName = placeName,
        emoji = emoji,
        colorHex = colorHex,
        totalCost = cost,
        chargerKwh = chKwh,
        carKwh = cKwh,
        lossesKwh = lKwh,
        percentageOfTotalKwh = pct,
        sessionCount = placeSessions.size
      )
    }.sortedByDescending { it.chargerKwh }

    return StatisticsData(
      period = period,
      source = source,
      totalCost = totalCost,
      totalChargerKwh = totalChargerKwh,
      totalCarKwh = totalCarKwh,
      totalLossesKwh = totalLossesKwh,
      lossPercent = lossPercent,
      efficiencyPercent = efficiencyPercent,
      totalSessions = filteredSessions.size,
      avgPricePerKwh = avgPricePerKwh,
      totalKm = totalDistance,
      avgConsumptionCharger = avgConsumptionCharger,
      avgConsumptionCar = avgConsumptionCar,
      pricePer1Km = pricePer1Km,
      pricePer100Km = pricePer100Km,
      monthlySummaries = monthlySummaries,
      placeBreakdowns = placeBreakdowns
    )
  }
}
