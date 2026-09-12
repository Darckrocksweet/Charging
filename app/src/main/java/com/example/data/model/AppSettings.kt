package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeSetting(val titleRu: String) {
  SYSTEM("Системная"),
  LIGHT("Светлая"),
  DARK("Тёмная")
}

enum class DefaultEnergyUnit(val labelRu: String) {
  KWH("кВт·ч"),
  PERCENT("%")
}

data class UserPreferences(
  val vehicleName: String = "Мой электромобиль",
  val vehicleModel: String = "",
  val currencySymbol: String = "BYN",
  val batteryCapacityKwh: Double = 60.0,
  val initialOdometer: Double? = null, // Начальный одометр перед началом учёта
  val initialBatteryPercent: Int? = 100, // Начальный заряд батареи перед первым использованием
  val distanceUnit: String = "км",
  val defaultUnit: DefaultEnergyUnit = DefaultEnergyUnit.PERCENT,
  val themeSetting: AppThemeSetting = AppThemeSetting.SYSTEM
)

class PreferencesManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("ev_tracker_prefs", Context.MODE_PRIVATE)

  private val _preferencesFlow = MutableStateFlow(loadPreferences())
  val preferencesFlow: StateFlow<UserPreferences> = _preferencesFlow.asStateFlow()

  private fun loadPreferences(): UserPreferences {
    val themeStr = prefs.getString(KEY_THEME, AppThemeSetting.SYSTEM.name) ?: AppThemeSetting.SYSTEM.name
    val unitStr = prefs.getString(KEY_DEFAULT_UNIT, DefaultEnergyUnit.PERCENT.name) ?: DefaultEnergyUnit.PERCENT.name

    val themeSetting = try {
      AppThemeSetting.valueOf(themeStr)
    } catch (_: Exception) {
      AppThemeSetting.SYSTEM
    }

    val defaultUnit = try {
      DefaultEnergyUnit.valueOf(unitStr)
    } catch (_: Exception) {
      DefaultEnergyUnit.PERCENT
    }

    val storedCurrency = prefs.getString(KEY_CURRENCY, "BYN") ?: "BYN"
    // Automatically migrate old default Russian ruble symbol to Belarusian rubles (BYN)
    val effectiveCurrency = if (storedCurrency == "₽") "BYN" else storedCurrency

    val vehicleName = prefs.getString(KEY_VEHICLE_NAME, "Мой электромобиль") ?: "Мой электромобиль"
    val vehicleModel = prefs.getString(KEY_VEHICLE_MODEL, "") ?: ""

    val storedOdo = prefs.getFloat(KEY_INITIAL_ODOMETER, -1.0f)
    val initialOdometer = if (storedOdo >= 0f) storedOdo.toDouble() else null

    val storedBatteryPct = prefs.getInt(KEY_INITIAL_BATTERY_PERCENT, 100)
    val initialBatteryPercent = if (storedBatteryPct in 0..100) storedBatteryPct else 100

    return UserPreferences(
      vehicleName = vehicleName,
      vehicleModel = vehicleModel,
      currencySymbol = effectiveCurrency,
      batteryCapacityKwh = prefs.getFloat(KEY_BATTERY_CAPACITY, 60.0f).toDouble(),
      initialOdometer = initialOdometer,
      initialBatteryPercent = initialBatteryPercent,
      distanceUnit = prefs.getString(KEY_DISTANCE_UNIT, "км") ?: "км",
      defaultUnit = defaultUnit,
      themeSetting = themeSetting
    )
  }

  fun updatePreferences(newPrefs: UserPreferences) {
    val editor = prefs.edit()
      .putString(KEY_VEHICLE_NAME, newPrefs.vehicleName)
      .putString(KEY_VEHICLE_MODEL, newPrefs.vehicleModel)
      .putString(KEY_CURRENCY, newPrefs.currencySymbol)
      .putFloat(KEY_BATTERY_CAPACITY, newPrefs.batteryCapacityKwh.toFloat())
      .putString(KEY_DISTANCE_UNIT, newPrefs.distanceUnit)
      .putString(KEY_DEFAULT_UNIT, newPrefs.defaultUnit.name)
      .putString(KEY_THEME, newPrefs.themeSetting.name)

    if (newPrefs.initialOdometer != null && newPrefs.initialOdometer >= 0.0) {
      editor.putFloat(KEY_INITIAL_ODOMETER, newPrefs.initialOdometer.toFloat())
    } else {
      editor.remove(KEY_INITIAL_ODOMETER)
    }

    if (newPrefs.initialBatteryPercent != null) {
      editor.putInt(KEY_INITIAL_BATTERY_PERCENT, newPrefs.initialBatteryPercent)
    } else {
      editor.remove(KEY_INITIAL_BATTERY_PERCENT)
    }

    editor.apply()
    _preferencesFlow.value = newPrefs
  }

  fun updateBatteryCapacity(capacity: Double) {
    updatePreferences(_preferencesFlow.value.copy(batteryCapacityKwh = capacity))
  }

  fun updateVehicleProfile(name: String, model: String, capacity: Double, initialOdo: Double?, initialBattery: Int?) {
    updatePreferences(
      _preferencesFlow.value.copy(
        vehicleName = name,
        vehicleModel = model,
        batteryCapacityKwh = capacity,
        initialOdometer = initialOdo,
        initialBatteryPercent = initialBattery
      )
    )
  }

  fun updateCurrency(currency: String) {
    updatePreferences(_preferencesFlow.value.copy(currencySymbol = currency))
  }

  fun updateTheme(theme: AppThemeSetting) {
    updatePreferences(_preferencesFlow.value.copy(themeSetting = theme))
  }

  fun updateDefaultUnit(unit: DefaultEnergyUnit) {
    updatePreferences(_preferencesFlow.value.copy(defaultUnit = unit))
  }

  companion object {
    private const val KEY_VEHICLE_NAME = "key_vehicle_name"
    private const val KEY_VEHICLE_MODEL = "key_vehicle_model"
    private const val KEY_INITIAL_ODOMETER = "key_initial_odometer"
    private const val KEY_INITIAL_BATTERY_PERCENT = "key_initial_battery_percent"
    private const val KEY_CURRENCY = "key_currency"
    private const val KEY_BATTERY_CAPACITY = "key_battery_capacity"
    private const val KEY_DISTANCE_UNIT = "key_distance_unit"
    private const val KEY_DEFAULT_UNIT = "key_default_unit"
    private const val KEY_THEME = "key_theme"
  }
}

