package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChargingPlace
import com.example.data.model.ChargingSession
import com.example.data.model.DefaultEnergyUnit
import com.example.data.model.TariffType
import com.example.data.model.UserPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFormScreen(
  initialPlaceId: Long?,
  existingSession: ChargingSession?,
  lastKnownBatteryState: Pair<Int, Double>?,
  places: List<ChargingPlace>,
  preferences: UserPreferences,
  onSaveSession: (ChargingSession) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currency = preferences.currencySymbol
  val batteryCapacity = preferences.batteryCapacityKwh
  val distanceUnit = preferences.distanceUnit

  // Current wizard step: 1 = Choose Station, 2 = Charger Data, 3 = Car Data, 4 = Optional / Review
  var currentStep by remember {
    mutableIntStateOf(
      if (existingSession != null) 2
      else if (initialPlaceId != null) 2
      else 1
    )
  }

  // Selected Station
  var selectedPlace by remember {
    mutableStateOf(
      if (existingSession != null) {
        places.find { it.id == existingSession.placeId }
          ?: places.find { it.name.equals(existingSession.placeName, ignoreCase = true) }
          ?: places.firstOrNull()
      } else if (initialPlaceId != null) {
        places.find { it.id == initialPlaceId } ?: places.firstOrNull()
      } else {
        places.firstOrNull()
      }
    )
  }

  // Date and Time
  var sessionTimestamp by remember {
    mutableLongStateOf(existingSession?.timestamp ?: System.currentTimeMillis())
  }

  // Step 2: Charger data
  var chargerKwhInput by remember {
    mutableStateOf(
      if (existingSession != null) String.format(Locale.US, "%.1f", existingSession.chargerKwh)
      else ""
    )
  }

  var pricePerKwhInput by remember {
    mutableStateOf(
      if (existingSession != null && existingSession.pricePerKwh > 0) {
        String.format(Locale.US, "%.2f", existingSession.pricePerKwh)
      } else {
        val place = selectedPlace
        if (place != null && place.pricePerKwh > 0) String.format(Locale.US, "%.2f", place.pricePerKwh)
        else "0.45"
      }
    )
  }

  var costInput by remember {
    mutableStateOf(
      if (existingSession != null) String.format(Locale.US, "%.2f", existingSession.cost)
      else ""
    )
  }
  var isCostManuallyEdited by remember { mutableStateOf(existingSession != null) }

  // Step 3: Car data
  // Mode: true = Percent %, false = direct kWh
  var inputInPercent by remember {
    mutableStateOf(
      if (existingSession != null) {
        existingSession.batteryStartPercent != null && existingSession.batteryEndPercent != null
      } else {
        preferences.defaultUnit == DefaultEnergyUnit.PERCENT
      }
    )
  }

  // Battery start (Auto-fill "before" from last known battery state!)
  var batteryStartPercentInput by remember {
    mutableStateOf(
      if (existingSession?.batteryStartPercent != null) {
        existingSession.batteryStartPercent.toString()
      } else if (lastKnownBatteryState?.first != null) {
        lastKnownBatteryState.first.toString()
      } else {
        "20"
      }
    )
  }

  var batteryEndPercentInput by remember {
    mutableStateOf(
      existingSession?.batteryEndPercent?.toString() ?: "80"
    )
  }

  var batteryStartKwhInput by remember {
    mutableStateOf(
      if (existingSession?.batteryStartKwh != null) {
        String.format(Locale.US, "%.1f", existingSession.batteryStartKwh)
      } else if (lastKnownBatteryState?.second != null) {
        String.format(Locale.US, "%.1f", lastKnownBatteryState.second)
      } else {
        String.format(Locale.US, "%.1f", batteryCapacity * 0.20)
      }
    )
  }

  var batteryEndKwhInput by remember {
    mutableStateOf(
      if (existingSession?.batteryEndKwh != null) {
        String.format(Locale.US, "%.1f", existingSession.batteryEndKwh)
      } else {
        String.format(Locale.US, "%.1f", batteryCapacity * 0.80)
      }
    )
  }

  // Step 4: Optional
  var odometerInput by remember {
    mutableStateOf(existingSession?.odometer?.let { String.format(Locale.US, "%.0f", it) } ?: "")
  }
  var durationInput by remember {
    mutableStateOf(existingSession?.durationMinutes?.toString() ?: "")
  }
  var notesInput by remember {
    mutableStateOf(existingSession?.notes ?: "")
  }

  // Auto calculate cost from charger kWh and place tariff
  fun recalculateCost() {
    val place = selectedPlace ?: return
    val kwh = chargerKwhInput.toDoubleOrNull() ?: 0.0
    val duration = durationInput.toIntOrNull() ?: 0
    val cal = Calendar.getInstance().apply { timeInMillis = sessionTimestamp }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val calculated = place.calculateCost(kwh = kwh, durationMinutes = duration, hourOfDay = hour)
    costInput = String.format(Locale.US, "%.2f", calculated)
  }

  // Update price input when selected place changes
  LaunchedEffect(selectedPlace) {
    val place = selectedPlace
    if (place != null) {
      if (!isCostManuallyEdited || existingSession == null) {
        val tariffPrice = when (place.tariffType) {
          TariffType.FREE -> 0.0
          TariffType.DAY_NIGHT -> {
            val cal = Calendar.getInstance().apply { timeInMillis = sessionTimestamp }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if (hour in place.dayStartHour until place.nightStartHour) place.dayPricePerKwh else place.nightPricePerKwh
          }
          TariffType.FIXED_KWH -> place.pricePerKwh
          TariffType.PER_MINUTE -> place.pricePerMinute
        }
        pricePerKwhInput = String.format(Locale.US, "%.2f", tariffPrice)
        if (chargerKwhInput.isNotBlank()) {
          recalculateCost()
        }
      }
    }
  }

  // Sync Percent ↔ kWh
  fun onPercentChanged(startPct: Int, endPct: Int) {
    val startKwh = (startPct.coerceIn(0, 100) / 100.0) * batteryCapacity
    val endKwh = (endPct.coerceIn(0, 100) / 100.0) * batteryCapacity
    batteryStartKwhInput = String.format(Locale.US, "%.1f", startKwh)
    batteryEndKwhInput = String.format(Locale.US, "%.1f", endKwh)
  }

  fun onKwhChanged(startKwh: Double, endKwh: Double) {
    if (batteryCapacity > 0) {
      val startPct = ((startKwh / batteryCapacity) * 100.0).toInt().coerceIn(0, 100)
      val endPct = ((endKwh / batteryCapacity) * 100.0).toInt().coerceIn(0, 100)
      batteryStartPercentInput = startPct.toString()
      batteryEndPercentInput = endPct.toString()
    }
  }

  // Calculate Car kWh and Losses
  val chargerKwh = chargerKwhInput.toDoubleOrNull() ?: 0.0

  val carKwh = if (inputInPercent) {
    val start = batteryStartPercentInput.toIntOrNull() ?: 0
    val end = batteryEndPercentInput.toIntOrNull() ?: 0
    val deltaPercent = (end - start).coerceAtLeast(0)
    (deltaPercent / 100.0) * batteryCapacity
  } else {
    val startKwh = batteryStartKwhInput.toDoubleOrNull() ?: 0.0
    val endKwh = batteryEndKwhInput.toDoubleOrNull() ?: 0.0
    (endKwh - startKwh).coerceAtLeast(0.0)
  }

  val lossesKwh = (chargerKwh - carKwh).coerceAtLeast(0.0)
  val lossPercent = if (chargerKwh > 0.0) (lossesKwh / chargerKwh) * 100.0 else 0.0
  val efficiencyPercent = if (chargerKwh > 0.0) ((carKwh / chargerKwh) * 100.0).coerceIn(0.0, 100.0) else 100.0

  // Validation: received cannot exceed charger energy!
  val isReceivedExceedingCharger = chargerKwh > 0.0 && carKwh > (chargerKwh + 0.01)

  val canProceedFromStep1 = selectedPlace != null
  val canProceedFromStep2 = chargerKwh > 0.0
  val canProceedFromStep3 = carKwh > 0.0 && !isReceivedExceedingCharger

  // Date and Time picker dialogs
  val calendar = Calendar.getInstance().apply { timeInMillis = sessionTimestamp }
  val datePickerDialog = DatePickerDialog(
    context,
    { _, y, m, d ->
      calendar.set(Calendar.YEAR, y)
      calendar.set(Calendar.MONTH, m)
      calendar.set(Calendar.DAY_OF_MONTH, d)
      sessionTimestamp = calendar.timeInMillis
      if (!isCostManuallyEdited) recalculateCost()
    },
    calendar.get(Calendar.YEAR),
    calendar.get(Calendar.MONTH),
    calendar.get(Calendar.DAY_OF_MONTH)
  )

  val timePickerDialog = TimePickerDialog(
    context,
    { _, h, min ->
      calendar.set(Calendar.HOUR_OF_DAY, h)
      calendar.set(Calendar.MINUTE, min)
      sessionTimestamp = calendar.timeInMillis
      if (!isCostManuallyEdited) recalculateCost()
    },
    calendar.get(Calendar.HOUR_OF_DAY),
    calendar.get(Calendar.MINUTE),
    true
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (existingSession != null) "Редактировать зарядку" else "Новая зарядка",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(
            onClick = {
              if (currentStep > 1) {
                currentStep--
              } else {
                onNavigateBack()
              }
            },
            modifier = Modifier.testTag("session_form_back_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    modifier = modifier
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
    ) {
      // Step Progress Indicator
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val stepTitles = listOf("Станция", "От зарядки", "От машины", "Итог")
          stepTitles.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isActive = currentStep == stepNumber
            val isPassed = currentStep > stepNumber
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable {
                if (stepNumber < currentStep) currentStep = stepNumber
              }
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (isPassed) MaterialTheme.colorScheme.primary
                    else if (isActive) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isPassed) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                  )
                } else {
                  Text(
                    text = "$stepNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
          progress = { (currentStep / 4f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      }

      // Step Contents (Scrollable)
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        when (currentStep) {
          // ================= STEP 1: CHOOSE STATION =================
          1 -> {
            Text(
              text = "Шаг 1: Выберите станцию",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Каждая станция хранит предустановленный тариф за 1 кВт·ч.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Large Tiles for Stations
            places.forEach { place ->
              val isSelected = selectedPlace?.id == place.id
              val placeColor = Color(place.colorHex)

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(20.dp))
                  .border(
                    width = if (isSelected) 2.5.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                  )
                  .clickable {
                    selectedPlace = place
                    currentStep = 2 // Advance to Step 2
                  }
                  .testTag("station_tile_${place.name.lowercase()}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                  else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(54.dp)
                      .clip(CircleShape)
                      .background(placeColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(text = place.emoji, fontSize = 28.sp)
                  }

                  Spacer(modifier = Modifier.width(16.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = place.name,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                      text = place.getTariffSummary(currency),
                      style = MaterialTheme.typography.bodySmall,
                      color = placeColor
                    )
                  }

                  if (isSelected) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          // ================= STEP 2: CHARGER DATA =================
          2 -> {
            Text(
              text = "Шаг 2: Данные со станции",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Показания счётчика станции и сумма оплаты (включает потери в кабеле и зарядном устройстве).",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Station indicator pill
            selectedPlace?.let { place ->
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = place.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = place.name,
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                  }
                  Text(
                    text = "Изменить",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { currentStep = 1 }
                  )
                }
              }
            }

            // Charger kWh Input
            OutlinedTextField(
              value = chargerKwhInput,
              onValueChange = {
                chargerKwhInput = it
                if (!isCostManuallyEdited) recalculateCost()
              },
              label = { Text("Энергия от зарядки (кВт·ч)") },
              placeholder = { Text("например: 36.5") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("charger_kwh_input")
            )

            // Quick add chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf(10, 20, 30, 40, 50).forEach { addVal ->
                FilterChip(
                  selected = false,
                  onClick = {
                    val cur = chargerKwhInput.toDoubleOrNull() ?: 0.0
                    val updated = (cur + addVal).coerceAtLeast(0.0)
                    chargerKwhInput = String.format(Locale.US, "%.1f", updated)
                    if (!isCostManuallyEdited) recalculateCost()
                  },
                  label = { Text("+$addVal") }
                )
              }
            }

            // Price and Cost Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = pricePerKwhInput,
                onValueChange = {
                  pricePerKwhInput = it
                  val price = it.toDoubleOrNull() ?: 0.0
                  val kwh = chargerKwhInput.toDoubleOrNull() ?: 0.0
                  costInput = String.format(Locale.US, "%.2f", kwh * price)
                },
                label = { Text("Тариф ($currency/кВт·ч)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
              )

              OutlinedTextField(
                value = costInput,
                onValueChange = {
                  costInput = it
                  isCostManuallyEdited = true
                },
                label = { Text("Стоимость ($currency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("cost_input")
              )
            }

            // Date and Time picker
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.US)
              val timeFmt = SimpleDateFormat("HH:mm", Locale.US)

              OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(dateFmt.format(Date(sessionTimestamp)))
              }

              OutlinedButton(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(timeFmt.format(Date(sessionTimestamp)))
              }
            }
          }

          // ================= STEP 3: CAR DATA (THE KEY INSIGHT: LOSSES) =================
          3 -> {
            Text(
              text = "Шаг 3: Данные из автомобиля",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Уровень батареи до и после зарядки. Приложение рассчитает поступившую энергию и потери.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Unit toggle: % vs kWh
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = inputInPercent,
                onClick = { inputInPercent = true },
                label = { Text("В процентах (%)") }
              )
              FilterChip(
                selected = !inputInPercent,
                onClick = { inputInPercent = false },
                label = { Text("В энергии (кВт·ч)") }
              )
            }

            if (inputInPercent) {
              // Battery % inputs
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedTextField(
                  value = batteryStartPercentInput,
                  onValueChange = {
                    batteryStartPercentInput = it
                    val s = it.toIntOrNull() ?: 0
                    val e = batteryEndPercentInput.toIntOrNull() ?: 0
                    onPercentChanged(s, e)
                  },
                  label = { Text("Батарея До (%)") },
                  supportingText = { Text("Автозаполнено") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("battery_start_input")
                )

                OutlinedTextField(
                  value = batteryEndPercentInput,
                  onValueChange = {
                    batteryEndPercentInput = it
                    val s = batteryStartPercentInput.toIntOrNull() ?: 0
                    val e = it.toIntOrNull() ?: 0
                    onPercentChanged(s, e)
                  },
                  label = { Text("Батарея После (%)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("battery_end_input")
                )
              }

              // Quick target % chips
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Цель:",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.align(Alignment.CenterVertically)
                )
                listOf(70, 80, 90, 100).forEach { target ->
                  FilterChip(
                    selected = batteryEndPercentInput == target.toString(),
                    onClick = {
                      batteryEndPercentInput = target.toString()
                      val s = batteryStartPercentInput.toIntOrNull() ?: 0
                      onPercentChanged(s, target)
                    },
                    label = { Text("$target%") }
                  )
                }
              }
            } else {
              // Direct kWh inputs
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedTextField(
                  value = batteryStartKwhInput,
                  onValueChange = {
                    batteryStartKwhInput = it
                    val s = it.toDoubleOrNull() ?: 0.0
                    val e = batteryEndKwhInput.toDoubleOrNull() ?: 0.0
                    onKwhChanged(s, e)
                  },
                  label = { Text("Энергия До (кВт·ч)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                  value = batteryEndKwhInput,
                  onValueChange = {
                    batteryEndKwhInput = it
                    val s = batteryStartKwhInput.toDoubleOrNull() ?: 0.0
                    val e = it.toDoubleOrNull() ?: 0.0
                    onKwhChanged(s, e)
                  },
                  label = { Text("Энергия После (кВт·ч)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            // ================= LIVE CALCULATION & LOSSES DISPLAY =================
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isReceivedExceedingCharger) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "📊 Расчёт баланса энергии",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (isReceivedExceedingCharger) MaterialTheme.colorScheme.onErrorContainer
                  else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "⚡ От зарядки:")
                  Text(
                    text = "${String.format(Locale.US, "%.1f", chargerKwh)} кВт·ч",
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "🚗 В батарею машины:")
                  Text(
                    text = "+${String.format(Locale.US, "%.1f", carKwh)} кВт·ч",
                    fontWeight = FontWeight.Bold,
                    color = if (isReceivedExceedingCharger) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // EXPLICIT LOSSES!
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "📉 Потери зарядки:",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReceivedExceedingCharger) MaterialTheme.colorScheme.error else Color(0xFFD97706)
                  )
                  Text(
                    text = if (!isReceivedExceedingCharger) {
                      "-${String.format(Locale.US, "%.1f", lossesKwh)} кВт·ч (${String.format(Locale.US, "%.1f", lossPercent)}%)"
                    } else {
                      "—"
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isReceivedExceedingCharger) MaterialTheme.colorScheme.error else Color(0xFFD97706)
                  )
                }
              }
            }

            // VALIDATION WARNING
            if (isReceivedExceedingCharger) {
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "Энергия в батарею (${String.format(Locale.US, "%.1f", carKwh)} кВт·ч) не может превышать энергию от зарядной станции (${String.format(Locale.US, "%.1f", chargerKwh)} кВт·ч). Проверьте введённые данные.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                  )
                }
              }
            }
          }

          // ================= STEP 4: OPTIONAL & REVIEW =================
          4 -> {
            Text(
              text = "Шаг 4: Дополнительно и итог",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Одометр позволит рассчитать точный расход энергии на 100 км.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val currentOdoVal = odometerInput.toDoubleOrNull()
            val initOdo = preferences.initialOdometer
            val odoSupportingText = when {
              currentOdoVal != null && initOdo != null && currentOdoVal >= initOdo ->
                "Пробег со старта: +${String.format(Locale.US, "%.0f", currentOdoVal - initOdo)} $distanceUnit (расход рассчитается сразу)"
              initOdo != null ->
                "Базовый одометр автомобиля: ${String.format(Locale.US, "%.0f", initOdo)} $distanceUnit"
              else ->
                "Позволяет рассчитать точный расход энергии на 100 км"
            }

            // Odometer input
            OutlinedTextField(
              value = odometerInput,
              onValueChange = { odometerInput = it },
              label = { Text("Показания одометра ($distanceUnit)") },
              placeholder = { Text("например: 14520") },
              supportingText = { Text(odoSupportingText) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_input")
            )

            // Duration input
            OutlinedTextField(
              value = durationInput,
              onValueChange = { durationInput = it },
              label = { Text("Длительность зарядки (минут)") },
              placeholder = { Text("например: 45") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            // Notes input
            OutlinedTextField(
              value = notesInput,
              onValueChange = { notesInput = it },
              label = { Text("Заметки (необязательно)") },
              placeholder = { Text("например: CCS2, тёплая погода") },
              maxLines = 3,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Final Review Summary Card
            Card(
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedPlace?.emoji ?: "⚡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = selectedPlace?.name ?: "Станция",
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                  }
                  Text(
                    text = "${costInput.ifBlank { "0.00" }} $currency",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "⚡ От зарядки:", style = MaterialTheme.typography.bodyMedium)
                  Text(text = "$chargerKwhInput кВт·ч", fontWeight = FontWeight.SemiBold)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "🚗 В батарею:", style = MaterialTheme.typography.bodyMedium)
                  Text(
                    text = "${String.format(Locale.US, "%.1f", carKwh)} кВт·ч",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "📉 Потери зарядки:",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD97706)
                  )
                  Text(
                    text = "-${String.format(Locale.US, "%.1f", lossesKwh)} кВт·ч (${String.format(Locale.US, "%.0f", lossPercent)}%)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                  )
                }

                if (odometerInput.isNotBlank()) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = "📍 Одометр:", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "$odometerInput $distanceUnit", fontWeight = FontWeight.Medium)
                  }
                }
              }
            }
          }
        }
      }

      // Bottom Navigation Buttons Bar
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (currentStep > 1) {
            OutlinedButton(
              onClick = { currentStep-- },
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Назад")
            }
          }

          if (currentStep < 4) {
            val canProceed = when (currentStep) {
              1 -> canProceedFromStep1
              2 -> canProceedFromStep2
              3 -> canProceedFromStep3
              else -> true
            }

            Button(
              onClick = { currentStep++ },
              enabled = canProceed,
              modifier = Modifier
                .weight(if (currentStep > 1) 1.5f else 1f)
                .height(52.dp)
                .testTag("step_next_btn")
            ) {
              Text("Далее")
              Spacer(modifier = Modifier.width(6.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
          } else {
            // Save button on Step 4
            Button(
              onClick = {
                val place = selectedPlace ?: return@Button
                val costVal = costInput.toDoubleOrNull() ?: 0.0
                val priceVal = pricePerKwhInput.toDoubleOrNull() ?: 0.0
                val odoVal = odometerInput.toDoubleOrNull()
                val durVal = durationInput.toIntOrNull()
                val bStartPct = batteryStartPercentInput.toIntOrNull()
                val bEndPct = batteryEndPercentInput.toIntOrNull()
                val bStartKwh = batteryStartKwhInput.toDoubleOrNull()
                val bEndKwh = batteryEndKwhInput.toDoubleOrNull()

                val sessionToSave = (existingSession ?: ChargingSession(
                  placeId = place.id,
                  placeName = place.name,
                  placeEmoji = place.emoji,
                  placeColorHex = place.colorHex,
                  timestamp = sessionTimestamp,
                  chargerKwh = chargerKwh,
                  pricePerKwh = priceVal,
                  cost = costVal,
                  carKwh = carKwh,
                  batteryStartPercent = bStartPct,
                  batteryEndPercent = bEndPct,
                  batteryStartKwh = bStartKwh,
                  batteryEndKwh = bEndKwh,
                  odometer = odoVal,
                  durationMinutes = durVal,
                  notes = notesInput
                )).copy(
                  placeId = place.id,
                  placeName = place.name,
                  placeEmoji = place.emoji,
                  placeColorHex = place.colorHex,
                  timestamp = sessionTimestamp,
                  chargerKwh = chargerKwh,
                  pricePerKwh = priceVal,
                  cost = costVal,
                  carKwh = carKwh,
                  batteryStartPercent = bStartPct,
                  batteryEndPercent = bEndPct,
                  batteryStartKwh = bStartKwh,
                  batteryEndKwh = bEndKwh,
                  odometer = odoVal,
                  durationMinutes = durVal,
                  notes = notesInput
                )

                onSaveSession(sessionToSave)
                onNavigateBack()
              },
              enabled = chargerKwh > 0.0 && carKwh > 0.0 && !isReceivedExceedingCharger,
              modifier = Modifier
                .weight(if (currentStep > 1) 2f else 1f)
                .height(52.dp)
                .testTag("save_session_btn")
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Сохранить зарядку", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
