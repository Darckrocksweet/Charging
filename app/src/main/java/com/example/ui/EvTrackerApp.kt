package com.example.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ChargingSession
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlacesSettingsScreen
import com.example.ui.screens.SessionFormScreen
import com.example.ui.screens.StatisticsScreen
import com.example.util.CsvExporter

object Routes {
  const val HOME = "home"
  const val HISTORY = "history"
  const val STATISTICS = "statistics"
  const val SETTINGS = "settings"
  const val SESSION_FORM = "session_form"
}

@Composable
fun EvTrackerApp(
  viewModel: EvTrackerViewModel,
  initialQuickPlaceName: String? = null,
  navController: NavHostController = rememberNavController()
) {
  val context = LocalContext.current
  val places by viewModel.allPlaces.collectAsStateWithLifecycle()
  val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
  val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
  val statistics by viewModel.statistics.collectAsStateWithLifecycle()
  val consumptionMetric by viewModel.mainScreenConsumption.collectAsStateWithLifecycle()
  val preferences by viewModel.preferences.collectAsStateWithLifecycle()
  val lastDeletedSession by viewModel.lastDeletedSession.collectAsStateWithLifecycle()

  var selectedFormPlaceId by remember { mutableStateOf<Long?>(null) }
  var sessionToEdit by remember { mutableStateOf<ChargingSession?>(null) }

  // Handle widget launch if an initial place name was passed
  LaunchedEffect(initialQuickPlaceName, places) {
    if (!initialQuickPlaceName.isNullOrBlank() && places.isNotEmpty()) {
      val matchedPlace = places.find { it.name.equals(initialQuickPlaceName, ignoreCase = true) }
        ?: places.firstOrNull()
      if (matchedPlace != null) {
        selectedFormPlaceId = matchedPlace.id
        sessionToEdit = null
        navController.navigate(Routes.SESSION_FORM)
      }
    }
  }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val showBottomBar = currentRoute in listOf(
    Routes.HOME,
    Routes.HISTORY,
    Routes.STATISTICS,
    Routes.SETTINGS
  )

  fun shareCsv() {
    val intent = CsvExporter.exportAndShare(context, allSessions, preferences)
    context.startActivity(intent)
  }

  // Last known battery state from the most recent session
  val lastKnownBattery = recentSessions.firstOrNull()?.let {
    val pct = it.batteryEndPercent ?: 20
    val kwh = it.batteryEndKwh ?: (preferences.batteryCapacityKwh * (pct / 100.0))
    Pair(pct, kwh)
  }

  Scaffold(
    bottomBar = {
      if (showBottomBar) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
              if (currentRoute != Routes.HOME) {
                navController.navigate(Routes.HOME) {
                  popUpTo(Routes.HOME) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = {
              Icon(
                if (currentRoute == Routes.HOME) Icons.Filled.EvStation else Icons.Outlined.EvStation,
                contentDescription = "Зарядка"
              )
            },
            label = { Text("Зарядка") },
            modifier = Modifier.testTag("nav_home")
          )

          NavigationBarItem(
            selected = currentRoute == Routes.HISTORY,
            onClick = {
              if (currentRoute != Routes.HISTORY) {
                navController.navigate(Routes.HISTORY) {
                  popUpTo(Routes.HOME) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = {
              Icon(
                if (currentRoute == Routes.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                contentDescription = "История"
              )
            },
            label = { Text("История") },
            modifier = Modifier.testTag("nav_history")
          )

          NavigationBarItem(
            selected = currentRoute == Routes.STATISTICS,
            onClick = {
              if (currentRoute != Routes.STATISTICS) {
                navController.navigate(Routes.STATISTICS) {
                  popUpTo(Routes.HOME) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = {
              Icon(
                if (currentRoute == Routes.STATISTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                contentDescription = "Статистика"
              )
            },
            label = { Text("Статистика") },
            modifier = Modifier.testTag("nav_stats")
          )

          NavigationBarItem(
            selected = currentRoute == Routes.SETTINGS,
            onClick = {
              if (currentRoute != Routes.SETTINGS) {
                navController.navigate(Routes.SETTINGS) {
                  popUpTo(Routes.HOME) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = {
              Icon(
                if (currentRoute == Routes.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                contentDescription = "Настройки"
              )
            },
            label = { Text("Настройки") },
            modifier = Modifier.testTag("nav_settings")
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Routes.HOME,
      modifier = Modifier.padding(innerPadding),
      enterTransition = { fadeIn(animationSpec = tween(180)) },
      exitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
      // 1. Home Screen
      composable(Routes.HOME) {
        HomeScreen(
          places = places,
          recentSessions = recentSessions,
          consumptionMetric = consumptionMetric,
          preferences = preferences,
          onQuickPlaceClick = { place ->
            selectedFormPlaceId = place.id
            sessionToEdit = null
            navController.navigate(Routes.SESSION_FORM)
          },
          onNewSessionClick = {
            selectedFormPlaceId = places.firstOrNull()?.id
            sessionToEdit = null
            navController.navigate(Routes.SESSION_FORM)
          },
          onEditSessionClick = { session ->
            sessionToEdit = session
            selectedFormPlaceId = session.placeId
            navController.navigate(Routes.SESSION_FORM)
          },
          onDeleteSessionClick = { session ->
            viewModel.deleteSession(session)
          },
          onViewAllHistoryClick = {
            navController.navigate(Routes.HISTORY)
          },
          onViewStatisticsClick = {
            navController.navigate(Routes.STATISTICS)
          },
          onSettingsClick = {
            navController.navigate(Routes.SETTINGS)
          },
          onUpdateVehicleProfile = { name, model, capacity, initialOdo, initialBattery ->
            viewModel.updateVehicleProfile(name, model, capacity, initialOdo, initialBattery)
          }
        )
      }

      // 2. Session Form (4-step Wizard)
      composable(Routes.SESSION_FORM) {
        SessionFormScreen(
          initialPlaceId = selectedFormPlaceId,
          existingSession = sessionToEdit,
          lastKnownBatteryState = lastKnownBattery,
          places = places,
          preferences = preferences,
          onSaveSession = { session ->
            viewModel.saveSession(session)
          },
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      // 3. History Screen
      composable(Routes.HISTORY) {
        HistoryScreen(
          sessions = allSessions,
          places = places,
          preferences = preferences,
          lastDeletedSession = lastDeletedSession,
          onEditSession = { session ->
            sessionToEdit = session
            selectedFormPlaceId = session.placeId
            navController.navigate(Routes.SESSION_FORM)
          },
          onDeleteSession = { session ->
            viewModel.deleteSession(session)
          },
          onRestoreDeletedSession = {
            viewModel.restoreLastDeletedSession()
          },
          onNewSessionClick = {
            selectedFormPlaceId = places.firstOrNull()?.id
            sessionToEdit = null
            navController.navigate(Routes.SESSION_FORM)
          },
          onExportCsvClick = {
            shareCsv()
          },
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      // 4. Statistics Screen
      composable(Routes.STATISTICS) {
        StatisticsScreen(
          statistics = statistics,
          preferences = preferences,
          onPeriodSelect = { viewModel.setPeriodFilter(it) },
          onSourceSelect = { viewModel.setSourceFilter(it) },
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      // 5. Places & Settings Screen
      composable(Routes.SETTINGS) {
        PlacesSettingsScreen(
          places = places,
          preferences = preferences,
          onSavePlace = { place ->
            viewModel.savePlace(place)
          },
          onDeletePlace = { place ->
            viewModel.deletePlace(place)
          },
          onUpdatePreferences = { newPrefs ->
            viewModel.updatePreferences(newPrefs)
          },
          onExportCsvClick = {
            shareCsv()
          },
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }
    }
  }
}
