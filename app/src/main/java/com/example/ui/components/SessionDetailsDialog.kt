package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun SessionDetailsDialog(
  session: ChargingSession,
  currency: String,
  distanceUnit: String,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("ru"))
  val dateText = dateFormat.format(Date(session.timestamp))
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }

  val placeColor = Color(session.placeColorHex)

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(placeColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = session.placeEmoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = session.placeName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Stream 1: От зарядки (From the charger)
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⚡ От зарядки (оплачено)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "${String.format(Locale.US, "%.2f", session.cost)} $currency",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Энергия по счётчику:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.2f", session.chargerKwh)} кВт·ч",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
            }
            if (session.pricePerKwh > 0) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Тариф:",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${String.format(Locale.US, "%.2f", session.pricePerKwh)} $currency/кВт·ч",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
          }
        }

        // Stream 2: От машины (From the car)
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "🚗 От машины (в батарею)",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Получено батареей:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.2f", session.carKwh)} кВт·ч",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF10B981)
              )
            }

            if (session.batteryStartPercent != null && session.batteryEndPercent != null) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Уровень батареи:",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${session.batteryStartPercent}% → ${session.batteryEndPercent}% (+${session.batteryEndPercent - session.batteryStartPercent}%)",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }
        }

        // Losses Card (Key Insight!)
        val isSignificantLoss = session.lossesKwh > 0.05
        val lossColor = when {
          session.lossPercent > 15 -> Color(0xFFEF4444)
          session.lossPercent > 8 -> Color(0xFFF59E0B)
          else -> Color(0xFF10B981)
        }

        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSignificantLoss) lossColor.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
          ),
          border = BorderStroke(1.dp, lossColor.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "📉 Потери зарядки (разница)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSignificantLoss) lossColor else MaterialTheme.colorScheme.onSurface
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = lossColor.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "-${String.format(Locale.US, "%.1f", session.lossPercent)}%",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = lossColor,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Потери энергии:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.2f", session.lossesKwh)} кВт·ч",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = lossColor
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Эффективность зарядки:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", session.efficiencyPercent)}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
              )
            }
          }
        }

        // Additional information (Odometer, Notes)
        if (session.odometer != null || session.durationMinutes != null || session.notes.isNotBlank()) {
          Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (session.odometer != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "📍 Одометр (пробег):",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${String.format(Locale.US, "%,.0f", session.odometer)} $distanceUnit",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
            if (session.durationMinutes != null && session.durationMinutes > 0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "⏱️ Время зарядки:",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${session.durationMinutes} мин",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
            if (session.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "📝 Заметка:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = session.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Actions Row
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              onDismiss()
              onEdit()
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_edit_btn")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Изменить")
          }

          OutlinedButton(
            onClick = {
              onDismiss()
              onDelete()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_delete_btn")
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Удалить")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(14.dp)
      ) {
        Text("Закрыть")
      }
    }
  )
}
