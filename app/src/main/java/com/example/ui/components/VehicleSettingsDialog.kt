package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeSetting
import com.example.data.model.DefaultEnergyUnit
import com.example.data.model.UserPreferences

@Composable
fun VehicleSettingsDialog(
  currentPreferences: UserPreferences,
  onDismiss: () -> Unit,
  onSave: (UserPreferences) -> Unit
) {
  var vehicleName by remember { mutableStateOf(currentPreferences.vehicleName) }
  var batteryCapacityStr by remember {
    mutableStateOf(currentPreferences.batteryCapacityKwh.toString())
  }
  var initialOdometerStr by remember {
    mutableStateOf(currentPreferences.initialOdometer?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "")
  }
  var initialBatteryPercentStr by remember {
    mutableStateOf(currentPreferences.initialBatteryPercent?.toString() ?: "100")
  }
  var currency by remember { mutableStateOf(currentPreferences.currencySymbol) }
  var distanceUnit by remember { mutableStateOf(currentPreferences.distanceUnit) }
  var defaultUnit by remember { mutableStateOf(currentPreferences.defaultUnit) }
  var themeSetting by remember { mutableStateOf(currentPreferences.themeSetting) }

  val commonCurrencies = listOf("BYN", "бел. руб.", "р.", "₽", "$", "€", "₸", "₴")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Параметры автомобиля и приложения",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Vehicle Name
        OutlinedTextField(
          value = vehicleName,
          onValueChange = { vehicleName = it },
          label = { Text("Название / Имя автомобиля") },
          placeholder = { Text("например: Zeekr 001, Tesla Model Y") },
          supportingText = { Text("Отображается на главной странице") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Battery Capacity
        OutlinedTextField(
          value = batteryCapacityStr,
          onValueChange = { batteryCapacityStr = it },
          label = { Text("Ёмкость батареи (кВт·ч)") },
          placeholder = { Text("например, 60.0") },
          supportingText = { Text("Используется для пересчёта % ↔ кВт·ч") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Initial Odometer
        OutlinedTextField(
          value = initialOdometerStr,
          onValueChange = { initialOdometerStr = it },
          label = { Text("Начальный одометр ($distanceUnit)") },
          placeholder = { Text("например: 14500") },
          supportingText = { Text("Необходимо для расчёта расхода до 1-й заправки") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Initial Battery %
        OutlinedTextField(
          value = initialBatteryPercentStr,
          onValueChange = { initialBatteryPercentStr = it },
          label = { Text("Начальный заряд батареи (%)") },
          placeholder = { Text("100") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Default Unit for car input: % or kWh
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Единицы ввода по умолчанию",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DefaultEnergyUnit.entries.forEach { unit ->
              FilterChip(
                selected = defaultUnit == unit,
                onClick = { defaultUnit = unit },
                label = { Text(if (unit == DefaultEnergyUnit.PERCENT) "% (Проценты)" else "кВт·ч (Энергия)") }
              )
            }
          }
        }

        // Theme Setting: System / Light / Dark
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Тема оформления",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppThemeSetting.entries.forEach { theme ->
              FilterChip(
                selected = themeSetting == theme,
                onClick = { themeSetting = theme },
                label = { Text(theme.titleRu) }
              )
            }
          }
        }

        // Currency selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Валюта",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            commonCurrencies.take(4).forEach { cur ->
              FilterChip(
                selected = currency == cur,
                onClick = { currency = cur },
                label = { Text(cur) }
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            commonCurrencies.drop(4).forEach { cur ->
              FilterChip(
                selected = currency == cur,
                onClick = { currency = cur },
                label = { Text(cur) }
              )
            }
          }
        }

        // Distance unit: km or mi
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Единицы расстояния",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = distanceUnit == "км",
              onClick = { distanceUnit = "км" },
              label = { Text("Километры (км)") }
            )
            FilterChip(
              selected = distanceUnit == "ми",
              onClick = { distanceUnit = "ми" },
              label = { Text("Мили (ми)") }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val capacity = batteryCapacityStr.toDoubleOrNull() ?: 60.0
          val odo = initialOdometerStr.toDoubleOrNull()
          val bat = initialBatteryPercentStr.toIntOrNull()?.coerceIn(0, 100) ?: 100
          onSave(
            currentPreferences.copy(
              vehicleName = vehicleName.trim().ifBlank { "Мой электромобиль" },
              batteryCapacityKwh = capacity.coerceAtLeast(5.0),
              initialOdometer = odo,
              initialBatteryPercent = bat,
              currencySymbol = currency.ifBlank { "BYN" },
              distanceUnit = distanceUnit,
              defaultUnit = defaultUnit,
              themeSetting = themeSetting
            )
          )
          onDismiss()
        }
      ) {
        Text("Сохранить")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена")
      }
    }
  )
}
