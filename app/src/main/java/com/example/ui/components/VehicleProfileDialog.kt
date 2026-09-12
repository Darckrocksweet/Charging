package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserPreferences
import com.example.data.model.VehiclePresets
import java.util.Locale

@Composable
fun VehicleProfileDialog(
  currentPreferences: UserPreferences,
  onDismiss: () -> Unit,
  onSave: (name: String, model: String, capacity: Double, initialOdo: Double?, initialBattery: Int?) -> Unit
) {
  var vehicleName by remember { mutableStateOf(currentPreferences.vehicleName) }
  var batteryCapacityStr by remember {
    mutableStateOf(String.format(Locale.US, "%.1f", currentPreferences.batteryCapacityKwh))
  }
  var initialOdometerStr by remember {
    mutableStateOf(currentPreferences.initialOdometer?.let { String.format(Locale.US, "%.0f", it) } ?: "")
  }
  var initialBatteryPercentStr by remember {
    mutableStateOf(currentPreferences.initialBatteryPercent?.toString() ?: "100")
  }

  val popularList = VehiclePresets.popularVehicles

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
        }
        Column {
          Text(
            text = "Профиль автомобиля",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Имя на главном экране и начальный пробег",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Quick vehicle presets selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Популярные электромобили (быстрый выбор):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            popularList.forEach { preset ->
              val isSelected = vehicleName.equals(preset.name, ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = {
                  vehicleName = preset.name
                  batteryCapacityStr = String.format(Locale.US, "%.1f", preset.defaultBatteryKwh)
                },
                leadingIcon = {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  } else {
                    Text(preset.iconEmoji, fontSize = 14.sp)
                  }
                },
                label = {
                  Text("${preset.name} (${preset.defaultBatteryKwh.toInt()} кВт·ч)")
                }
              )
            }
          }
        }

        // Vehicle Name Input
        OutlinedTextField(
          value = vehicleName,
          onValueChange = { vehicleName = it },
          label = { Text("Имя / Название автомобиля") },
          placeholder = { Text("например: Zeekr 001, Tesla Model Y") },
          supportingText = { Text("Будет отображаться в заголовке главной страницы") },
          leadingIcon = {
            Icon(Icons.Default.DirectionsCar, contentDescription = null)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("vehicle_name_input")
        )

        // Battery Capacity
        OutlinedTextField(
          value = batteryCapacityStr,
          onValueChange = { batteryCapacityStr = it },
          label = { Text("Ёмкость батареи (кВт·ч)") },
          placeholder = { Text("например, 100.0 или 75.0") },
          supportingText = { Text("Используется для точного пересчёта % и кВт·ч") },
          leadingIcon = {
            Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("battery_capacity_input")
        )

        // ================= INITIAL VALUES (KEY FEATURE FOR FIRST CHARGING) =================
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "Начальные значения до первой заправки",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Text(
              text = "Укажите пробег автомобиля перед началом использования приложения. Это позволит автоматически рассчитать расход энергии (${currentPreferences.distanceUnit}) уже при первой же вашей зарядке!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Initial Odometer
            OutlinedTextField(
              value = initialOdometerStr,
              onValueChange = { initialOdometerStr = it },
              label = { Text("Начальный одометр (${currentPreferences.distanceUnit})") },
              placeholder = { Text("например: 15400") },
              supportingText = { Text("Пробег на момент старта учёта") },
              leadingIcon = {
                Icon(Icons.Default.Speed, contentDescription = null)
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("initial_odometer_input")
            )

            // Initial Battery Level (%)
            OutlinedTextField(
              value = initialBatteryPercentStr,
              onValueChange = { initialBatteryPercentStr = it },
              label = { Text("Начальный заряд батареи (%)") },
              placeholder = { Text("100") },
              supportingText = { Text("Заряд в батарее на момент старта (0–100%)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("initial_battery_percent_input")
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val name = vehicleName.trim().ifBlank { "Мой электромобиль" }
          val cap = batteryCapacityStr.toDoubleOrNull() ?: 60.0
          val odo = initialOdometerStr.toDoubleOrNull()
          val bat = initialBatteryPercentStr.toIntOrNull()?.coerceIn(0, 100) ?: 100

          onSave(name, "", cap.coerceAtLeast(5.0), odo, bat)
          onDismiss()
        },
        modifier = Modifier.testTag("save_vehicle_button")
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
