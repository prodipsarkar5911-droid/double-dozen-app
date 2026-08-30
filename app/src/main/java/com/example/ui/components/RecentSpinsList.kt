package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun RecentSpinsList(
  history: List<SpinRecord>,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
      .testTag("recent_spins_strip"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = CasinoGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE SPIN HISTORY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              letterSpacing = 0.5.sp
            )
          )
        }

        Text(
          text = if (history.isNotEmpty()) "Latest → Left" else "Waiting for spins",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            color = TextTertiary
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (history.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Casino,
              contentDescription = null,
              tint = TextTertiary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Tap numbers on the keypad below to log live table spins",
              style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
            )
          }
        }
      } else {
        val scrollState = rememberScrollState()
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Show most recent first (reversed)
          history.reversed().take(20).forEach { record ->
            SpinChip(record = record)
          }
        }
      }
    }
  }
}

@Composable
private fun SpinChip(record: SpinRecord) {
  val num = record.numberInfo
  val chipBg = when (num.color) {
    RouletteColor.RED -> RouletteRed
    RouletteColor.BLACK -> RouletteBlack
    RouletteColor.GREEN -> RouletteGreen
  }

  val (dozenColor, dozenLabel) = when (num.dozen) {
    Dozen.FIRST -> Pair(Dozen1Color, "D1")
    Dozen.SECOND -> Pair(Dozen2Color, "D2")
    Dozen.THIRD -> Pair(Dozen3Color, "D3")
    Dozen.ZERO -> Pair(ZeroColor, "0")
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(42.dp)
  ) {
    // Number Circle
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(chipBg)
        .border(
          width = 1.5.dp,
          color = when (record.outcome) {
            PredictionOutcome.WON -> NeonEmerald
            PredictionOutcome.LOST -> RouletteRed
            PredictionOutcome.NO_SIGNAL -> DarkCardBorder
          },
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "${num.number}",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Black,
          color = Color.White,
          fontSize = 15.sp
        )
      )
    }

    Spacer(modifier = Modifier.height(3.dp))

    // Dozen & Outcome Tag
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = dozenLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = dozenColor
        )
      )
      if (record.outcome != PredictionOutcome.NO_SIGNAL) {
        Text(
          text = if (record.outcome == PredictionOutcome.WON) "✓" else "✗",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = if (record.outcome == PredictionOutcome.WON) NeonEmerald else RouletteRed
          )
        )
      }
    }
  }
}
