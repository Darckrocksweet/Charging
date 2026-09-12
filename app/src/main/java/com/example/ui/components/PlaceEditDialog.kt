package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.ChargingPlace
import com.example.data.model.TariffType

val AVAILABLE_EMOJIS = listOf("🏡", "🦋", "🏢", "🏠", "⚡", "☀️", "🔌", "🚗", "🔋", "🏨", "🛒", "⛽")
val AVAILABLE_COLORS = listOf(
  0xFF10B981L, // Emerald
  0xFF8B5CF6L, // Purple
  0xFF3B82F6L, // Blue
  0xFF059669L, // Forest Green
  0xFFF59E0BL, // Amber
  0xFF0284C7L, // Sky
  0xFFEAB308L, // Yellow
  0xFFEC4899L, // Pink
  0xFFEF4444L, // Red
  0xFF64748BL  // Slate
)

@Composable
fun PlaceEditDialog(
  place: ChargingPlace?,
  currency: String,
  onDismiss: () -> Unit,
  onSave: (ChargingPlace) -> Unit,
  onDelete: ((ChargingPlace) -> Unit)? = null
) {
  var name by remember { mutableStateOf(place?.name ?: "") }
  var emoji by remember { mutableStateOf(place?.emoji ?: "🏡") }
  var selectedColorHex by remember { mutableLongStateOf(place?.colorHex ?: 0xFF10B981L) }
  var tariffType by remember { mutableStateOf(place?.tariffType ?: TariffType.FIXED_KWH) }

  var pricePerKwhStr by remember {
    mutableStateOf(if (place != null && place.pricePerKwh > 0) place.pricePerKwh.toString() else "0.45")
  }
  var pricePerMinStr by remember {
    mutableStateOf(if (place != null && place.pricePerMinute > 0) place.pricePerMinute.toString() else "0.20")
  }
  var dayPriceStr by remember {
    mutableStateOf(if (place != null && place.dayPricePerKwh > 0) place.dayPricePerKwh.toString() else "0.28")
  }
  var nightPriceStr by remember {
    mutableStateOf(if (place != null && place.nightPricePerKwh > 0) place.nightPricePerKwh.toString() else "0.16")
  }
  var dayStartHour by remember { mutableIntStateOf(place?.dayStartHour ?: 7) }
  var nightStartHour by remember { mutableIntStateOf(place?.nightStartHour ?: 23) }

  val isEditing = place != null && place.id != 0L

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isEditing) "Редактировать станцию" else "Новая станция зарядки",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Name field
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Название станции") },
          placeholder = { Text("например: Дача, Butterfly, Работа") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("place_name_input")
        )

        // Emoji selection
        Text(
          text = "Иконка / Эмодзи",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AVAILABLE_EMOJIS.forEach { item ->
            val isSelected = item == emoji
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                  width = if (isSelected) 2.dp else 0.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = CircleShape
                )
                .clickable { emoji = item },
              contentAlignment = Alignment.Center
            ) {
              Text(text = item, fontSize = 22.sp)
            }
          }
        }

        // Color selection
        Text(
          text = "Цвет",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AVAILABLE_COLORS.forEach { hex ->
            val color = Color(hex)
            val isSelected = hex == selectedColorHex
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                  width = if (isSelected) 3.dp else 0.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                  shape = CircleShape
                )
                .clickable { selectedColorHex = hex }
            )
          }
        }

        // Tariff Type chips
        Text(
          text = "Тип тарифа",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            TariffType.FIXED_KWH to "Цена/кВт·ч",
            TariffType.DAY_NIGHT to "День/Ночь",
            TariffType.PER_MINUTE to "Поминутно",
            TariffType.FREE to "Бесплатно"
          ).forEach { (type, label) ->
            FilterChip(
              selected = tariffType == type,
              onClick = { tariffType = type },
              label = { Text(label) }
            )
          }
        }

        // Tariff inputs
        when (tariffType) {
          TariffType.FREE -> {
            Text(
              text = "Зарядка на этой станции будет бесплатной (0.00 $currency).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          TariffType.FIXED_KWH -> {
            OutlinedTextField(
              value = pricePerKwhStr,
              onValueChange = { pricePerKwhStr = it },
              label = { Text("Цена за 1 кВт·ч ($currency)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
          TariffType.PER_MINUTE -> {
            OutlinedTextField(
              value = pricePerMinStr,
              onValueChange = { pricePerMinStr = it },
              label = { Text("Цена за 1 минуту ($currency)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
          TariffType.DAY_NIGHT -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = dayPriceStr,
                onValueChange = { dayPriceStr = it },
                label = { Text("День ($currency/кВт·ч)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = nightPriceStr,
                onValueChange = { nightPriceStr = it },
                label = { Text("Ночь ($currency/кВт·ч)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
              )
            }
            Text(
              text = "Дневное время: с $dayStartHour:00 до $nightStartHour:00 (Ночь: с $nightStartHour:00 до $dayStartHour:00)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (isEditing && onDelete != null) {
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedButton(
            onClick = {
              if (place != null) {
                onDelete(place)
                onDismiss()
              }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Удалить станцию")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cleanName = name.trim().ifBlank { "Станция" }
          val updatedPlace = (place ?: ChargingPlace(name = cleanName, emoji = emoji, colorHex = selectedColorHex)).copy(
            name = cleanName,
            emoji = emoji,
            colorHex = selectedColorHex,
            tariffType = tariffType,
            pricePerKwh = pricePerKwhStr.toDoubleOrNull() ?: 0.0,
            pricePerMinute = pricePerMinStr.toDoubleOrNull() ?: 0.0,
            dayPricePerKwh = dayPriceStr.toDoubleOrNull() ?: 0.0,
            nightPricePerKwh = nightPriceStr.toDoubleOrNull() ?: 0.0,
            dayStartHour = dayStartHour,
            nightStartHour = nightStartHour
          )
          onSave(updatedPlace)
          onDismiss()
        },
        modifier = Modifier.testTag("save_place_button")
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
