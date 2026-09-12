package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChargingPlace
import com.example.data.model.ChargingSession
import com.example.data.model.UserPreferences
import com.example.ui.components.SessionDetailsDialog
import com.example.ui.components.SessionItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  sessions: List<ChargingSession>,
  places: List<ChargingPlace>,
  preferences: UserPreferences,
  lastDeletedSession: ChargingSession?,
  onEditSession: (ChargingSession) -> Unit,
  onDeleteSession: (ChargingSession) -> Unit,
  onRestoreDeletedSession: () -> Unit,
  onNewSessionClick: () -> Unit,
  onExportCsvClick: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = preferences.currencySymbol
  val distanceUnit = preferences.distanceUnit
  val snackbarHostState = remember { SnackbarHostState() }

  var selectedFilterPlace by remember { mutableStateOf<String?>(null) }
  var selectedSessionForDetails by remember { mutableStateOf<ChargingSession?>(null) }

  // Observe deleted session to show undo snackbar in Russian
  LaunchedEffect(lastDeletedSession) {
    if (lastDeletedSession != null) {
      val result = snackbarHostState.showSnackbar(
        message = "Зарядка удалена",
        actionLabel = "Отменить",
        duration = SnackbarDuration.Short
      )
      if (result == SnackbarResult.ActionPerformed) {
        onRestoreDeletedSession()
      }
    }
  }

  val filteredSessions = if (selectedFilterPlace == null) {
    sessions
  } else {
    sessions.filter { it.placeName.equals(selectedFilterPlace, ignoreCase = true) }
  }

  // Tap for details dialog
  selectedSessionForDetails?.let { session ->
    SessionDetailsDialog(
      session = session,
      currency = currency,
      distanceUnit = distanceUnit,
      onDismiss = { selectedSessionForDetails = null },
      onEdit = {
        selectedSessionForDetails = null
        onEditSession(session)
      },
      onDelete = {
        selectedSessionForDetails = null
        onDeleteSession(session)
      }
    )
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "История зарядок",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("history_back_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Назад"
            )
          }
        },
        actions = {
          IconButton(
            onClick = onExportCsvClick,
            modifier = Modifier.testTag("history_export_csv_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Экспорт в CSV",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNewSessionClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("history_fab_add")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Добавить зарядку")
      }
    },
    modifier = modifier
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
    ) {
      // Filter Chips Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedFilterPlace == null,
          onClick = { selectedFilterPlace = null },
          label = { Text("Все (${sessions.size})") },
          modifier = Modifier.testTag("filter_chip_all")
        )

        places.forEach { place ->
          val count = sessions.count { it.placeName.equals(place.name, ignoreCase = true) }
          FilterChip(
            selected = selectedFilterPlace.equals(place.name, ignoreCase = true),
            onClick = {
              selectedFilterPlace = if (selectedFilterPlace.equals(place.name, ignoreCase = true)) null else place.name
            },
            label = { Text("${place.emoji} ${place.name} ($count)") },
            modifier = Modifier.testTag("filter_chip_${place.name.lowercase()}")
          )
        }
      }

      if (filteredSessions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "⚡",
                style = MaterialTheme.typography.displayMedium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (selectedFilterPlace == null) "Зарядок пока нет" else "Нет зарядок для «$selectedFilterPlace»",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Нажмите «+» чтобы внести первую запись",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredSessions, key = { it.id }) { session ->
            SessionItemCard(
              session = session,
              currency = currency,
              distanceUnit = distanceUnit,
              onClick = { selectedSessionForDetails = session },
              onEditClick = { onEditSession(session) },
              onDeleteClick = { onDeleteSession(session) }
            )
          }
        }
      }
    }
  }
}
