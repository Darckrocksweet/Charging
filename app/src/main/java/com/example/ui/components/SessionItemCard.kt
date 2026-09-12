package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChargingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionItemCard(
  session: ChargingSession,
  currency: String,
  distanceUnit: String,
  onClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
  enableSwipe: Boolean = true
) {
  val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
  val dateText = dateFormat.format(Date(session.timestamp))
  val placeColor = Color(session.placeColorHex)

  if (!enableSwipe) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .clickable(onClick = onClick)
        .testTag("session_card_${session.id}"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      SessionContent(
        session = session,
        dateText = dateText,
        placeColor = placeColor,
        currency = currency,
        distanceUnit = distanceUnit
      )
    }
  } else {
    val dismissState = rememberSwipeToDismissBoxState(
      confirmValueChange = { value ->
        if (value == SwipeToDismissBoxValue.EndToStart) {
          onDeleteClick()
          true
        } else if (value == SwipeToDismissBoxValue.StartToEnd) {
          onEditClick()
          false // Don't dismiss for edit
        } else {
          false
        }
      }
    )

    SwipeToDismissBox(
      state = dismissState,
      backgroundContent = {
        val direction = dismissState.dismissDirection
        val color by animateColorAsState(
          when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444)
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF0284C7)
            SwipeToDismissBoxValue.Settled -> Color.Transparent
          },
          label = "swipe_bg"
        )
        val alignment = when (direction) {
          SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
          SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
          else -> Alignment.Center
        }
        val icon = when (direction) {
          SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
          else -> Icons.Default.Delete
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 20.dp),
          contentAlignment = alignment
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
          )
        }
      },
      content = {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("session_card_${session.id}"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          SessionContent(
            session = session,
            dateText = dateText,
            placeColor = placeColor,
            currency = currency,
            distanceUnit = distanceUnit
          )
        }
      }
    )
  }
}

@Composable
private fun SessionContent(
  session: ChargingSession,
  dateText: String,
  placeColor: Color,
  currency: String,
  distanceUnit: String
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(15.dp)
  ) {
    // Top Row: Station info, date and Total Cost in BYN
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(placeColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
      ) {
        Text(text = session.placeEmoji, fontSize = 22.sp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = session.placeName,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = dateText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Cost badge in BYN
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${String.format(Locale.US, "%.2f", session.cost)} $currency",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        if (session.chargerKwh > 0 && session.cost > 0) {
          Text(
            text = "${String.format(Locale.US, "%.2f", session.cost / session.chargerKwh)} $currency/кВт·ч",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(11.dp))

    // Two Streams & Losses Data Pill Row
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Stream 1: Charger
        Column {
          Text(
            text = "⚡ От зарядки",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${String.format(Locale.US, "%.1f", session.chargerKwh)} кВт·ч",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Stream 2: Car
        Column {
          Text(
            text = "🚗 В батарею",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${String.format(Locale.US, "%.1f", session.carKwh)} кВт·ч",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF10B981)
          )
        }

        // Losses: Color-coded
        val lossColor = when {
          session.lossPercent > 15 -> Color(0xFFEF4444)
          session.lossPercent > 8 -> Color(0xFFF59E0B)
          else -> Color(0xFF10B981)
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "📉 Потери",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${String.format(Locale.US, "%.0f", session.lossPercent)}% (-${String.format(Locale.US, "%.1f", session.lossesKwh)} кВт·ч)",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = lossColor
          )
        }
      }
    }

    // Optional odometer & notes line
    if (session.odometer != null || session.notes.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (session.odometer != null) {
          Text(
            text = "📍 ${String.format(Locale.US, "%,.0f", session.odometer)} $distanceUnit",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        if (session.notes.isNotBlank()) {
          Text(
            text = "📝 ${session.notes}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }
    }
  }
}
