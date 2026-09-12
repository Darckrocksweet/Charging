package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_sessions")
data class ChargingSession(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val placeId: Long = 0L,
  val placeName: String,
  val placeEmoji: String = "⚡",
  val placeColorHex: Long = 0xFF10B981L,
  val timestamp: Long = System.currentTimeMillis(),

  // Stream 1: "From the charger" — energy paid for and money spent
  val chargerKwh: Double,
  val pricePerKwh: Double = 0.0,
  val cost: Double = 0.0,

  // Stream 2: "From the car" — energy delivered to battery & km driven
  val carKwh: Double,
  val batteryStartPercent: Int? = null,
  val batteryEndPercent: Int? = null,
  val batteryStartKwh: Double? = null,
  val batteryEndKwh: Double? = null,
  val odometer: Double? = null,
  val durationMinutes: Int? = null,
  val notes: String = ""
) {
  // Key insight: losses = charger energy − car energy
  val lossesKwh: Double
    get() = (chargerKwh - carKwh).coerceAtLeast(0.0)

  val lossPercent: Double
    get() = if (chargerKwh > 0.0) ((lossesKwh / chargerKwh) * 100.0).coerceIn(0.0, 100.0) else 0.0

  val efficiencyPercent: Double
    get() = if (chargerKwh > 0.0) ((carKwh / chargerKwh) * 100.0).coerceIn(0.0, 100.0) else 100.0

  // Helper backward-compatible alias
  val kwh: Double
    get() = chargerKwh
}
