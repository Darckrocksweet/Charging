package com.example.data.model

data class VehiclePreset(
  val name: String,
  val defaultBatteryKwh: Double,
  val iconEmoji: String = "⚡",
  val manufacturer: String = ""
)

object VehiclePresets {
  val popularVehicles = listOf(
    VehiclePreset("Zeekr 001", 100.0, "⚡", "Zeekr"),
    VehiclePreset("Zeekr X", 66.0, "⚡", "Zeekr"),
    VehiclePreset("Zeekr 007", 75.0, "⚡", "Zeekr"),
    VehiclePreset("Tesla Model Y Long Range", 75.0, "🏎️", "Tesla"),
    VehiclePreset("Tesla Model 3", 60.0, "🏎️", "Tesla"),
    VehiclePreset("Geely Geometry C", 70.0, "🚙", "Geely"),
    VehiclePreset("Geely Galaxy E8", 62.0, "🚙", "Geely"),
    VehiclePreset("BYD Yuan Plus (Atto 3)", 60.5, "🔋", "BYD"),
    VehiclePreset("BYD Song Plus EV", 87.0, "🔋", "BYD"),
    VehiclePreset("BYD Seal", 82.5, "🔋", "BYD"),
    VehiclePreset("Volkswagen ID.4", 77.0, "🚗", "Volkswagen"),
    VehiclePreset("Volkswagen ID.6", 84.8, "🚗", "Volkswagen"),
    VehiclePreset("Voyah Free", 39.0, "✨", "Voyah"),
    VehiclePreset("Nissan Leaf", 40.0, "🍃", "Nissan"),
    VehiclePreset("BMW iX3", 80.0, "⚡", "BMW"),
    VehiclePreset("Audi Q4 e-tron", 82.0, "⚡", "Audi"),
    VehiclePreset("Porsche Taycan", 93.4, "🏎️", "Porsche")
  )
}
