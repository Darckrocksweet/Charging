package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppThemeSetting
import com.example.ui.EvTrackerApp
import com.example.ui.EvTrackerViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.widget.QuickChargeWidgetProvider

class MainActivity : ComponentActivity() {

  private val viewModel: EvTrackerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val quickPlace = intent?.getStringExtra(QuickChargeWidgetProvider.EXTRA_QUICK_PLACE)

    setContent {
      val preferences by viewModel.preferences.collectAsStateWithLifecycle()
      val isDark = when (preferences.themeSetting) {
        AppThemeSetting.LIGHT -> false
        AppThemeSetting.DARK -> true
        AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = isDark) {
        EvTrackerApp(
          viewModel = viewModel,
          initialQuickPlaceName = quickPlace
        )
      }
    }
  }
}
