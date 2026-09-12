package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_places")
data class ChargingPlace(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val emoji: String,
  val colorHex: Long, // 0xAARRGGBB
  val tariffType: TariffType = TariffType.FIXED_KWH,
  val pricePerKwh: Double = 0.0,
  val pricePerMinute: Double = 0.0,
  val dayPricePerKwh: Double = 0.0,
  val nightPricePerKwh: Double = 0.0,
  val dayStartHour: Int = 7,   // 7 AM
  val nightStartHour: Int = 23, // 11 PM
  val sortOrder: Int = 0,
  val isDefault: Boolean = false
) {
  fun getTariffSummary(currency: String): String {
    return when (tariffType) {
      TariffType.FREE -> "Бесплатно"
      TariffType.FIXED_KWH -> "${String.format(java.util.Locale.US, "%.2f", pricePerKwh)} $currency/кВт·ч"
      TariffType.PER_MINUTE -> "${String.format(java.util.Locale.US, "%.2f", pricePerMinute)} $currency/мин"
      TariffType.DAY_NIGHT -> "День ${String.format(java.util.Locale.US, "%.2f", dayPricePerKwh)} $currency • Ночь ${String.format(java.util.Locale.US, "%.2f", nightPricePerKwh)} $currency"
    }
  }

  fun calculateCost(kwh: Double, durationMinutes: Int = 0, hourOfDay: Int = 12): Double {
    return when (tariffType) {
      TariffType.FREE -> 0.0
      TariffType.FIXED_KWH -> (kwh * pricePerKwh).coerceAtLeast(0.0)
      TariffType.PER_MINUTE -> (durationMinutes * pricePerMinute).coerceAtLeast(0.0)
      TariffType.DAY_NIGHT -> {
        val isDay = if (dayStartHour < nightStartHour) {
          hourOfDay in dayStartHour until nightStartHour
        } else {
          hourOfDay >= dayStartHour || hourOfDay < nightStartHour
        }
        val rate = if (isDay) dayPricePerKwh else nightPricePerKwh
        (kwh * rate).coerceAtLeast(0.0)
      }
    }
  }
}
