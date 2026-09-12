package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.ChargingSession
import com.example.data.model.UserPreferences
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

  fun exportAndShare(
    context: Context,
    sessions: List<ChargingSession>,
    preferences: UserPreferences
  ): Intent {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

    val cacheDir = context.cacheDir
    val csvFile = File(cacheDir, "ev_charging_sessions_${System.currentTimeMillis()}.csv")

    FileWriter(csvFile).use { writer ->
      // Header row with both data streams and losses
      writer.append("ID,Date,Time,Station,Charger_kWh,Cost_${preferences.currencySymbol},Car_kWh,Losses_kWh,Losses_Percent,Battery_Start_Pct,Battery_End_Pct,Odometer_${preferences.distanceUnit},Notes\n")

      for (s in sessions) {
        val dateStr = dateFormat.format(Date(s.timestamp))
        val timeStr = timeFormat.format(Date(s.timestamp))
        val cleanPlace = escapeCsv(s.placeName)
        val chKwhStr = String.format(Locale.US, "%.2f", s.chargerKwh)
        val costStr = String.format(Locale.US, "%.2f", s.cost)
        val carKwhStr = String.format(Locale.US, "%.2f", s.carKwh)
        val lossesStr = String.format(Locale.US, "%.2f", s.lossesKwh)
        val lossPctStr = String.format(Locale.US, "%.1f", s.lossPercent)
        val bStartStr = s.batteryStartPercent?.toString() ?: ""
        val bEndStr = s.batteryEndPercent?.toString() ?: ""
        val odoStr = s.odometer?.let { String.format(Locale.US, "%.1f", it) } ?: ""
        val notesStr = escapeCsv(s.notes)

        writer.append("${s.id},\"$dateStr\",\"$timeStr\",$cleanPlace,$chKwhStr,$costStr,$carKwhStr,$lossesStr,$lossPctStr,$bStartStr,$bEndStr,$odoStr,$notesStr\n")
      }
    }

    val fileUri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      csvFile
    )

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/csv"
      putExtra(Intent.EXTRA_STREAM, fileUri)
      putExtra(Intent.EXTRA_SUBJECT, "EV Charging Sessions Export")
      putExtra(Intent.EXTRA_TEXT, "Here is your exported EV charging data (${sessions.size} sessions).")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return Intent.createChooser(sendIntent, "Export EV Sessions CSV")
  }

  private fun escapeCsv(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
  }
}
