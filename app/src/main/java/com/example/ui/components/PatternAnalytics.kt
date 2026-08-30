package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Dozen
import com.example.model.SessionStats
import com.example.ui.theme.*

@Composable
fun PatternAnalytics(
  stats: SessionStats,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Top Performance KPI Strip
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      KpiItem(
        label = "WIN RATE",
        value = if (stats.totalSignals > 0) "${stats.winRate.toInt()}%" else "—",
        subtext = "${stats.wins}W / ${stats.losses}L",
        accentColor = if (stats.winRate >= 65f || stats.totalSignals == 0) NeonEmerald else RouletteRed,
        modifier = Modifier.weight(1f)
      )
      KpiItem(
        label = "PROFIT",
        value = "${if (stats.unitsProfit >= 0) "+" else ""}${stats.unitsProfit}u",
        subtext = "${stats.totalSignals} signals",
        accentColor = if (stats.unitsProfit >= 0) CasinoGold else RouletteRed,
        modifier = Modifier.weight(1f)
      )
      KpiItem(
        label = "STREAK",
        value = when {
          stats.currentStreak > 0 -> "+${stats.currentStreak}W"
          stats.currentStreak < 0 -> "${stats.currentStreak}L"
          else -> "0"
        },
        subtext = "Best: ${stats.bestWinStreak}W",
        accentColor = if (stats.currentStreak >= 0) NeonEmerald else RouletteRed,
        modifier = Modifier.weight(1f)
      )
    }

    // Dozen Sleep & Missing Counters
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
        .testTag("sleep_counters_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Timer,
              contentDescription = null,
              tint = CasinoGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "DOZEN SLEEP TRACKER",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
              )
            )
          }
          Text(
            text = "Spins since last hit",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = TextTertiary
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SleepGaugeItem(
            name = "1st 12",
            sleepSpins = stats.dozenSleepCounts[Dozen.FIRST] ?: 0,
            color = Dozen1Color,
            modifier = Modifier.weight(1f)
          )
          SleepGaugeItem(
            name = "2nd 12",
            sleepSpins = stats.dozenSleepCounts[Dozen.SECOND] ?: 0,
            color = Dozen2Color,
            modifier = Modifier.weight(1f)
          )
          SleepGaugeItem(
            name = "3rd 12",
            sleepSpins = stats.dozenSleepCounts[Dozen.THIRD] ?: 0,
            color = Dozen3Color,
            modifier = Modifier.weight(1f)
          )
          SleepGaugeItem(
            name = "Zero",
            sleepSpins = stats.dozenSleepCounts[Dozen.ZERO] ?: 0,
            color = ZeroColor,
            modifier = Modifier.weight(0.9f)
          )
        }
      }
    }

    // Dozen Frequency & Distribution
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.BarChart,
              contentDescription = null,
              tint = NeonEmerald,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SESSION DISTRIBUTION",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
              )
            )
          }
          Text(
            text = "Total Spins: ${stats.totalSpins}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val total = if (stats.totalSpins > 0) stats.totalSpins.toFloat() else 1f
        val d1Count = stats.dozenCounts[Dozen.FIRST] ?: 0
        val d2Count = stats.dozenCounts[Dozen.SECOND] ?: 0
        val d3Count = stats.dozenCounts[Dozen.THIRD] ?: 0
        val zCount = stats.dozenCounts[Dozen.ZERO] ?: 0

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DistributionBar(
            label = "1st Dozen (1-12)",
            count = d1Count,
            total = stats.totalSpins,
            percentage = (d1Count / total) * 100f,
            color = Dozen1Color
          )
          DistributionBar(
            label = "2nd Dozen (13-24)",
            count = d2Count,
            total = stats.totalSpins,
            percentage = (d2Count / total) * 100f,
            color = Dozen2Color
          )
          DistributionBar(
            label = "3rd Dozen (25-36)",
            count = d3Count,
            total = stats.totalSpins,
            percentage = (d3Count / total) * 100f,
            color = Dozen3Color
          )
        }
      }
    }
  }
}

@Composable
private fun KpiItem(
  label: String,
  value: String,
  subtext: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = DarkSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Black,
          color = accentColor
        )
      )
      Text(
        text = subtext,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 10.sp,
          color = TextTertiary
        )
      )
    }
  }
}

@Composable
private fun SleepGaugeItem(
  name: String,
  sleepSpins: Int,
  color: Color,
  modifier: Modifier = Modifier
) {
  val isHighSleep = sleepSpins >= 4
  val isExtremeSleep = sleepSpins >= 6

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = if (isExtremeSleep) color.copy(alpha = 0.2f) else DarkSurfaceVariant,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isHighSleep) color else DarkCardBorder
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = name,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = color,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "$sleepSpins",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Black,
          color = if (isHighSleep) color else TextPrimary,
          fontSize = 20.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = when {
          isExtremeSleep -> "❄️ DEEP"
          isHighSleep -> "COLD"
          sleepSpins == 0 -> "🔥 HIT"
          else -> "active"
        },
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (isHighSleep) color else TextTertiary
        )
      )
    }
  }
}

@Composable
private fun DistributionBar(
  label: String,
  count: Int,
  total: Int,
  percentage: Float,
  color: Color
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextPrimary
        )
      )
      Text(
        text = "$count (${percentage.toInt()}%)",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = color
        )
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(DarkSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(if (total > 0) (percentage / 100f).coerceIn(0.01f, 1f) else 0.01f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(3.dp))
          .background(color)
      )
    }
  }
}
