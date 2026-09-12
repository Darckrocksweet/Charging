package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChargingPlace
import com.example.data.model.DefaultEnergyUnit
import com.example.data.model.UserPreferences
import com.example.ui.components.PlaceEditDialog
import com.example.ui.components.VehicleProfileDialog
import com.example.ui.components.VehicleSettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesSettingsScreen(
  places: List<ChargingPlace>,
  preferences: UserPreferences,
  onSavePlace: (ChargingPlace) -> Unit,
  onDeletePlace: (ChargingPlace) -> Unit,
  onUpdatePreferences: (UserPreferences) -> Unit,
  onExportCsvClick: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = preferences.currencySymbol

  var placeToEdit by remember { mutableStateOf<ChargingPlace?>(null) }
  var isAddingPlace by remember { mutableStateOf(false) }
  var isVehicleSettingsOpen by remember { mutableStateOf(false) }
  var isVehicleProfileOpen by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Настройки",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("settings_back_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Назад"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    modifier = modifier
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Vehicle & App Settings Card
      item {
        Text(
          text = "Автомобиль и параметры",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      // Quick Vehicle Selector & Initial Odometer Setup
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isVehicleProfileOpen = true }
            .testTag("vehicle_profile_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
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
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = preferences.vehicleName.ifBlank { "Мой электромобиль" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                  ) {
                    Text(
                      text = "${preferences.batteryCapacityKwh.toInt()} кВт·ч",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val odoInfo = preferences.initialOdometer?.let {
                  "Базовый одометр: ${String.format(java.util.Locale.US, "%.0f", it)} ${preferences.distanceUnit}"
                } ?: "Начальный одометр не задан"
                Text(
                  text = "$odoInfo • Нажмите для смены авто",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // App & Theme Settings
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isVehicleSettingsOpen = true }
            .testTag("vehicle_settings_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "Валюта, единицы и тема оформления",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Валюта: $currency • Дистанция: ${preferences.distanceUnit} • Тема: ${preferences.themeSetting.titleRu}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 2. STATIONS CRUD SECTION
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Станции зарядки (${places.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          OutlinedButton(
            onClick = { isAddingPlace = true },
            modifier = Modifier.testTag("add_place_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Добавить")
          }
        }
      }

      items(places, key = { it.id }) { place ->
        val placeColor = Color(place.colorHex)
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { placeToEdit = place }
            .testTag("place_item_${place.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(placeColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
              ) {
                Text(text = place.emoji, fontSize = 22.sp)
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = place.name,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = place.getTariffSummary(currency),
                  style = MaterialTheme.typography.bodySmall,
                  color = placeColor
                )
              }
            }

            IconButton(onClick = { placeToEdit = place }) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Редактировать",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // 3. EXPORT CSV SECTION
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Данные и экспорт",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExportCsvClick)
            .testTag("export_csv_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.FileDownload,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "Экспорт истории в CSV",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Таблица со всеми сессиями, потерями и одометром",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }

  // Dialogs
  if (isVehicleProfileOpen) {
    VehicleProfileDialog(
      currentPreferences = preferences,
      onDismiss = { isVehicleProfileOpen = false },
      onSave = { name, model, capacity, initialOdo, initialBattery ->
        onUpdatePreferences(
          preferences.copy(
            vehicleName = name,
            vehicleModel = model,
            batteryCapacityKwh = capacity,
            initialOdometer = initialOdo,
            initialBatteryPercent = initialBattery
          )
        )
        isVehicleProfileOpen = false
      }
    )
  }

  if (isVehicleSettingsOpen) {
    VehicleSettingsDialog(
      currentPreferences = preferences,
      onDismiss = { isVehicleSettingsOpen = false },
      onSave = { updated ->
        onUpdatePreferences(updated)
        isVehicleSettingsOpen = false
      }
    )
  }

  if (isAddingPlace) {
    PlaceEditDialog(
      place = null,
      currency = currency,
      onDismiss = { isAddingPlace = false },
      onSave = { newPlace ->
        onSavePlace(newPlace)
        isAddingPlace = false
      }
    )
  }

  placeToEdit?.let { place ->
    PlaceEditDialog(
      place = place,
      currency = currency,
      onDismiss = { placeToEdit = null },
      onSave = { updated ->
        onSavePlace(updated)
        placeToEdit = null
      },
      onDelete = { toDelete ->
        onDeletePlace(toDelete)
        placeToEdit = null
      }
    )
  }
}
