package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChargingPlace

@Composable
fun QuickPlaceCard(
  place: ChargingPlace,
  currency: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val placeColor = Color(place.colorHex)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onClick)
      .testTag("quick_place_${place.name.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Large circular emoji container with soft glowing background
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(placeColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = place.emoji,
            fontSize = 26.sp
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = place.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = placeColor.copy(alpha = 0.12f)
          ) {
            Text(
              text = place.getTariffSummary(currency),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = placeColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      // Tap prompt pill
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = placeColor.copy(alpha = 0.14f),
        modifier = Modifier.size(38.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "⚡",
            fontSize = 17.sp
          )
        }
      }
    }
  }
}

