package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserPreferences
import com.example.ui.DataSourceFilter
import com.example.ui.MonthSummary
import com.example.ui.PeriodFilter
import com.example.ui.PlaceBreakdown
import com.example.ui.StatisticsData
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
  statistics: StatisticsData,
  preferences: UserPreferences,
  onPeriodSelect: (PeriodFilter) -> Unit,
  onSourceSelect: (DataSourceFilter) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = preferences.currencySymbol
  val distanceUnit = preferences.distanceUnit

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Статистика расходов",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("stats_back_btn")
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
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. PERIOD SELECTOR (month / 3 months / 6 months / year / all time)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Период",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            PeriodFilter.entries.forEach { period ->
              FilterChip(
                selected = statistics.period == period,
                onClick = { onPeriodSelect(period) },
                label = { Text(period.titleRu) },
                modifier = Modifier.testTag("period_chip_${period.name.lowercase()}")
              )
            }
          }
        }
      }

      // 2. SOURCE SELECTOR (from charger / from car / both)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Источник данных",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            DataSourceFilter.entries.forEach { source ->
              FilterChip(
                selected = statistics.source == source,
                onClick = { onSourceSelect(source) },
                label = { Text(source.titleRu) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("source_chip_${source.name.lowercase()}")
              )
            }
          }
        }
      }

      // 3. KEY INSIGHT: DUAL STREAM & LOSSES HERO CARD
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⚡ Баланс энергии и потери",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (statistics.totalLossesKwh > 0.05) Color(0xFFF59E0B).copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = "Эффективность ${String.format(Locale.US, "%.1f", statistics.efficiencyPercent)}%",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (statistics.totalLossesKwh > 0.05) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Three column stats
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Charger
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "От зарядки",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${String.format(Locale.US, "%.1f", statistics.totalChargerKwh)}",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "кВт·ч",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // Car
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "В батарею",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${String.format(Locale.US, "%.1f", statistics.totalCarKwh)}",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF10B981)
                )
                Text(
                  text = "кВт·ч",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // Losses
              Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                Text(
                  text = "Потери (разница)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "-${String.format(Locale.US, "%.1f", statistics.totalLossesKwh)}",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (statistics.totalLossesKwh > 0.05) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "кВт·ч (${String.format(Locale.US, "%.1f", statistics.lossPercent)}%)",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                  color = if (statistics.totalLossesKwh > 0.05) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // 4. METRICS 2x2 GRID (total kWh, total money, avg price, losses, km, consumption, price/km)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Row 1: Total Money & Avg Price
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatMetricCard(
              title = "Всего расходов",
              value = "${String.format(Locale.US, "%.2f", statistics.totalCost)} $currency",
              subtitle = "${statistics.totalSessions} сессий",
              modifier = Modifier.weight(1f)
            )

            StatMetricCard(
              title = "Средняя цена кВт·ч",
              value = "${String.format(Locale.US, "%.2f", statistics.avgPricePerKwh)} $currency",
              subtitle = "за 1 кВт·ч от зарядки",
              modifier = Modifier.weight(1f)
            )
          }

          // Row 2: Total Energy (depends on source selector) & Losses
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val energyVal = when (statistics.source) {
              DataSourceFilter.FROM_CHARGER -> statistics.totalChargerKwh
              DataSourceFilter.FROM_CAR -> statistics.totalCarKwh
              DataSourceFilter.BOTH -> statistics.totalChargerKwh
            }
            val energySubtitle = when (statistics.source) {
              DataSourceFilter.FROM_CHARGER -> "По счётчику станций"
              DataSourceFilter.FROM_CAR -> "По батарее авто"
              DataSourceFilter.BOTH -> "Зарядка: ${String.format(Locale.US, "%.1f", statistics.totalChargerKwh)} / Авто: ${String.format(Locale.US, "%.1f", statistics.totalCarKwh)}"
            }

            StatMetricCard(
              title = "Общая энергия",
              value = "${String.format(Locale.US, "%.1f", energyVal)} кВт·ч",
              subtitle = energySubtitle,
              valueColor = MaterialTheme.colorScheme.primary,
              modifier = Modifier.weight(1f)
            )

            StatMetricCard(
              title = "Потери энергии",
              value = "${String.format(Locale.US, "%.1f", statistics.totalLossesKwh)} кВт·ч",
              subtitle = "${String.format(Locale.US, "%.1f", statistics.lossPercent)}% от зарядки",
              valueColor = if (statistics.totalLossesKwh > 0.05) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
          }

          // Row 3: Total KM & Consumption (shown for charger & car)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatMetricCard(
              title = "Общий пробег",
              value = "${String.format(Locale.US, "%,.0f", statistics.totalKm)} $distanceUnit",
              subtitle = "По записям одометра",
              modifier = Modifier.weight(1f)
            )

            val displayConsumption = when (statistics.source) {
              DataSourceFilter.FROM_CHARGER -> statistics.avgConsumptionCharger
              DataSourceFilter.FROM_CAR -> statistics.avgConsumptionCar
              DataSourceFilter.BOTH -> statistics.avgConsumptionCharger
            }
            val consumptionSub = if (statistics.avgConsumptionCharger != null && statistics.avgConsumptionCar != null) {
              "Зарядка: ${String.format(Locale.US, "%.1f", statistics.avgConsumptionCharger)} • Авто: ${String.format(Locale.US, "%.1f", statistics.avgConsumptionCar)}"
            } else if (displayConsumption != null) {
              "на 100 $distanceUnit"
            } else {
              "Нужно 2+ записи одометра"
            }

            StatMetricCard(
              title = "Расход на 100 км",
              value = if (displayConsumption != null) "${String.format(Locale.US, "%.1f", displayConsumption)} кВт·ч" else "—",
              subtitle = consumptionSub,
              valueColor = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.weight(1f)
            )
          }

          // Row 4: Price per 1 km & Price per 100 km
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatMetricCard(
              title = "Цена за 1 км",
              value = if (statistics.pricePer1Km != null) "${String.format(Locale.US, "%.2f", statistics.pricePer1Km)} $currency" else "—",
              subtitle = "Стоимость 1 $distanceUnit",
              modifier = Modifier.weight(1f)
            )

            StatMetricCard(
              title = "Цена за 100 км",
              value = if (statistics.pricePer100Km != null) "${String.format(Locale.US, "%.2f", statistics.pricePer100Km)} $currency" else "—",
              subtitle = "Стоимость 100 $distanceUnit",
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // 5. SIMPLE CHARTS: MONTHLY TREND
      item {
        Text(
          text = "Динамика по месяцам",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (statistics.monthlySummaries.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Нет данных за выбранный период",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        item {
          MonthlyTrendChartCard(
            monthlySummaries = statistics.monthlySummaries,
            source = statistics.source,
            currency = currency
          )
        }
      }

      // 6. SIMPLE CHARTS: BREAKDOWN BY STATION
      item {
        Text(
          text = "Распределение по станциям",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (statistics.placeBreakdowns.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Нет сессий за данный период",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(statistics.placeBreakdowns, key = { it.placeName }) { breakdown ->
          PlaceBreakdownCard(
            breakdown = breakdown,
            currency = currency,
            source = statistics.source
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun StatMetricCard(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = valueColor,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun MonthlyTrendChartCard(
  monthlySummaries: List<MonthSummary>,
  source: DataSourceFilter,
  currency: String
) {
  val maxEnergy = monthlySummaries.maxOfOrNull {
    when (source) {
      DataSourceFilter.FROM_CHARGER -> it.chargerKwh
      DataSourceFilter.FROM_CAR -> it.carKwh
      DataSourceFilter.BOTH -> maxOf(it.chargerKwh, it.carKwh)
    }
  }?.coerceAtLeast(1.0) ?: 1.0

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (source != DataSourceFilter.FROM_CAR) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "От зарядки",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        if (source != DataSourceFilter.FROM_CHARGER) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "В батарею",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bar Chart Rows for each month
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        monthlySummaries.forEach { month ->
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = month.displayTitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${String.format(Locale.US, "%.0f", month.totalCost)} $currency",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bars
            if (source != DataSourceFilter.FROM_CAR) {
              val chargerFrac = (month.chargerKwh / maxEnergy).toFloat().coerceIn(0.04f, 1f)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Box(
                  modifier = Modifier
                    .weight(chargerFrac)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.primary)
                )
                if (chargerFrac < 0.95f) {
                  Spacer(modifier = Modifier.weight(1f - chargerFrac))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${String.format(Locale.US, "%.1f", month.chargerKwh)} кВт·ч",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (source != DataSourceFilter.FROM_CHARGER) {
              val carFrac = (month.carKwh / maxEnergy).toFloat().coerceIn(0.04f, 1f)
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Box(
                  modifier = Modifier
                    .weight(carFrac)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF10B981))
                )
                if (carFrac < 0.95f) {
                  Spacer(modifier = Modifier.weight(1f - carFrac))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${String.format(Locale.US, "%.1f", month.carKwh)} кВт·ч",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF10B981)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PlaceBreakdownCard(
  breakdown: PlaceBreakdown,
  currency: String,
  source: DataSourceFilter
) {
  val placeColor = Color(breakdown.colorHex)
  val displayedKwh = when (source) {
    DataSourceFilter.FROM_CHARGER -> breakdown.chargerKwh
    DataSourceFilter.FROM_CAR -> breakdown.carKwh
    DataSourceFilter.BOTH -> breakdown.chargerKwh
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(placeColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = breakdown.emoji, fontSize = 20.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = breakdown.placeName,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${breakdown.sessionCount} сессий • потери: ${String.format(Locale.US, "%.1f", breakdown.lossesKwh)} кВт·ч",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${String.format(Locale.US, "%.1f", displayedKwh)} кВт·ч",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "${String.format(Locale.US, "%.2f", breakdown.totalCost)} $currency",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { breakdown.percentageOfTotalKwh },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = placeColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
    }
  }
}
