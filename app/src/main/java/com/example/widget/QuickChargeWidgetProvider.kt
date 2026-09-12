package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class QuickChargeWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  companion object {
    const val EXTRA_QUICK_PLACE = "extra_quick_place"

    fun updateAppWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
    ) {
      val views = RemoteViews(context.packageName, R.layout.widget_quick_charge)

      // Main app launch
      val mainIntent = Intent(context, MainActivity::class.java)
      val mainPendingIntent = PendingIntent.getActivity(
        context,
        0,
        mainIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)

      // Buttons for Dacha, Butterfly, Work
      val places = listOf(
        R.id.widget_btn_home to "Дача",
        R.id.widget_btn_work to "Butterfly",
        R.id.widget_btn_public to "Работа",
        R.id.widget_btn_free to "Дом"
      )

      places.forEachIndexed { index, (viewId, placeName) ->
        val intent = Intent(context, MainActivity::class.java).apply {
          putExtra(EXTRA_QUICK_PLACE, placeName)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
          context,
          index + 10,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(viewId, pendingIntent)
      }

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
