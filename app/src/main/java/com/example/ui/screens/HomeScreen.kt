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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChargingPlace
import com.example.data.model.ChargingSession
import com.example.data.model.UserPreferences
import com.example.ui.MainScreenConsumptionMetric
import com.example.ui.components.QuickPlaceCard
import com.example.ui.components.SessionDetailsDialog
import com.example.ui.components.VehicleProfileDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  places: List<ChargingPlace>,
  recentSessions: List<ChargingSession>,
  consumptionMetric: MainScreenConsumptionMetric,
  preferences: UserPreferences,
  onQuickPlaceClick: (ChargingPlace) -> Unit,
  onNewSessionClick: () -> Unit,
  onEditSessionClick: (ChargingSession) -> Unit,
  onDeleteSessionClick: (ChargingSession) -> Unit,
  onViewAllHistoryClick: () -> Unit,
  onViewStatisticsClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onUpdateVehicleProfile: ((name: String, model: String, capacity: Double, initialOdo: Double?, initialBattery: Int?) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val currency = preferences.currencySymbol
  val distanceUnit = preferences.distanceUnit

  var selectedSessionForDetails by remember { mutableStateOf<ChargingSession?>(null) }
  var isVehicleProfileDialogOpen by remember { mutableStateOf(false) }

  // Vehicle Profile Dialog
  if (isVehicleProfileDialogOpen) {
    VehicleProfileDialog(
      currentPreferences = preferences,
      onDismiss = { isVehicleProfileDialogOpen = false },
      onSave = { name, model, capacity, initialOdo, initialBattery ->
        onUpdateVehicleProfile?.invoke(name, model, capacity, initialOdo, initialBattery)
      }
    )
  }

  // Details dialog when session is tapped
  selectedSessionForDetails?.let { session ->
    SessionDetailsDialog(
      session = session,
      currency = currency,
      distanceUnit = distanceUnit,
      onDismiss = { selectedSessionForDetails = null },
      onEdit = {
        selectedSessionForDetails = null
        onEditSessionClick(session)
      },
      onDelete = {
        selectedSessionForDetails = null
        onDeleteSessionClick(session)
      }
    )
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        navigationIcon = {
          IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.testTag("home_settings_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Настройки",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        title = {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { isVehicleProfileDialogOpen = true }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = preferences.vehicleName.ifBlank { "Мой электромобиль" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Настроить авто",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Text(
                text = "${preferences.batteryCapacityKwh.toInt()} кВт·ч • $currency",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = onViewAllHistoryClick,
            modifier = Modifier.testTag("home_history_btn")
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "История",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
      contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 0. ACTIVE VEHICLE PROFILE CARD (Tap opens vehicle selection / initial odometer setup)
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isVehicleProfileDialogOpen = true }
            .testTag("active_vehicle_card")
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
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(26.dp)
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                Spacer(modifier = Modifier.height(3.dp))
                if (preferences.initialOdometer != null && preferences.initialOdometer > 0) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Speed,
                      contentDescription = null,
                      tint = Color(0xFF10B981),
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = "Старт одометра: ${String.format(Locale.US, "%.0f", preferences.initialOdometer)} $distanceUnit (${preferences.initialBatteryPercent ?: 100}%)",
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = Color(0xFF10B981)
                    )
                  }
                } else {
                  Text(
                    text = "⚠️ Нажмите, чтобы задать начальный пробег",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "Авто",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Выбор авто",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
      // 1. HERO EV COCKPIT CARD: AVERAGE CONSUMPTION (Tapping opens Statistics)
      item {
        val heroGradient = Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surface
          )
        )

        Card(
          shape = RoundedCornerShape(26.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(heroGradient)
            .clickable(onClick = onViewStatisticsClick)
            .testTag("home_consumption_hero")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Electric Cockpit Top Pill
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "БОРТОВОЙ КОМПЬЮТЕР • РАСХОД",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                  ),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Number Readout
            Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = consumptionMetric.displayText,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1.5).sp,
                lineHeight = 56.sp
              )
              if (consumptionMetric.value != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.padding(bottom = 10.dp)
                ) {
                  Text(
                    text = "кВт·ч / 100 $distanceUnit",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }

            // Dual streams indicator if available
            if (consumptionMetric.carValue != null && consumptionMetric.value != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "⚡ Станция: ${String.format(Locale.US, "%.1f", consumptionMetric.value)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "  •  ",
                    color = MaterialTheme.colorScheme.outline
                  )
                  Text(
                    text = "🔋 Батарея: ${String.format(Locale.US, "%.1f", consumptionMetric.carValue)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF10B981)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle / Prompt
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = consumptionMetric.subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
              )
            }
          }
        }
      }

      // 2. CENTER: HIGH-ENERGY "+ ADD CHARGING" ACTION BUTTON
      item {
        Button(
          onClick = onNewSessionClick,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .testTag("home_add_charging_button")
        ) {
          Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Добавить зарядку",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
          )
        }
      }

      // 3. BRIEF LAST SESSION SUMMARY (If exists)
      val lastSession = recentSessions.firstOrNull()
      if (lastSession != null) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Последняя зарядка",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Детали →",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { selectedSessionForDetails = lastSession }
              )
            }

            val placeColor = Color(lastSession.placeColorHex)
            Card(
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { selectedSessionForDetails = lastSession }
                .testTag("last_session_summary_card")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                val dateFormat = SimpleDateFormat("d MMMM, HH:mm", Locale("ru"))
                val dateStr = dateFormat.format(Date(lastSession.timestamp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(placeColor.copy(alpha = 0.16f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = lastSession.placeEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = lastSession.placeName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                      )
                      Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  // Total in BYN
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "${String.format(Locale.US, "%.2f", lastSession.cost)} $currency",
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.primary
                    )
                    if (lastSession.chargerKwh > 0 && lastSession.cost > 0) {
                      Text(
                        text = "${String.format(Locale.US, "%.2f", lastSession.cost / lastSession.chargerKwh)} $currency/кВт·ч",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Two streams & losses pill
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text(
                        text = "⚡ От зарядки",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = "${String.format(Locale.US, "%.1f", lastSession.chargerKwh)} кВт·ч",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                      )
                    }

                    Column {
                      Text(
                        text = "🚗 В батарею",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = "${String.format(Locale.US, "%.1f", lastSession.carKwh)} кВт·ч",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                      )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "📉 Потери",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      val lossColor = when {
                        lastSession.lossPercent > 15 -> Color(0xFFEF4444)
                        lastSession.lossPercent > 8 -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                      }
                      Text(
                        text = "${String.format(Locale.US, "%.0f", lastSession.lossPercent)}% (-${String.format(Locale.US, "%.1f", lastSession.lossesKwh)} кВт·ч)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = lossColor
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 4. QUICK STATIONS
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Быстрый старт по станциям",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Все станции →",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onSettingsClick)
          )
        }
      }

      items(places.take(3), key = { it.id }) { place ->
        QuickPlaceCard(
          place = place,
          currency = currency,
          onClick = { onQuickPlaceClick(place) }
        )
      }

      // 5. SECONDARY NAVIGATION: History & Statistics
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = onViewAllHistoryClick,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("История")
          }

          OutlinedButton(
            onClick = onViewStatisticsClick,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Статистика")
          }
        }
      }
    }
  }
}
