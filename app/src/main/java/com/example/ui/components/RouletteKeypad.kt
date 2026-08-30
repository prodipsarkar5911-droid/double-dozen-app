package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Dozen
import com.example.model.RouletteColor
import com.example.model.RouletteConstants
import com.example.ui.theme.*

@Composable
fun RouletteKeypad(
  onNumberSelected: (Int) -> Unit,
  onUndo: () -> Unit,
  onClear: () -> Unit,
  onDemoSpin: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Full Keypad (0-36), 1 = Rapid Dozen (D1, D2, D3, 0)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, DarkCardBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
      .testTag("roulette_keypad_container"),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
      // Top Mode Selector & Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab Pills
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(2.dp)
        ) {
          TabPill(
            title = "All Numbers (0-36)",
            isSelected = selectedTab == 0,
            onClick = { selectedTab = 0 }
          )
          TabPill(
            title = "Quick Dozens",
            isSelected = selectedTab == 1,
            onClick = { selectedTab = 1 }
          )
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = onDemoSpin,
            modifier = Modifier
              .size(36.dp)
              .testTag("demo_spin_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Casino,
              contentDescription = "Simulate Spin",
              tint = CasinoGold,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = onUndo,
            modifier = Modifier
              .size(36.dp)
              .testTag("undo_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Undo,
              contentDescription = "Undo Spin",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
              .size(36.dp)
              .testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Tune,
              contentDescription = "Strategy & Sensitivity Settings",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Keypad Body
      if (selectedTab == 0) {
        FullBoardKeypad(onNumberSelected = onNumberSelected)
      } else {
        QuickDozenKeypad(onNumberSelected = onNumberSelected)
      }
    }
  }
}

@Composable
private fun TabPill(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) CasinoGold else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color(0xFF1E1500) else TextSecondary,
        fontSize = 11.sp
      )
    )
  }
}

@Composable
private fun FullBoardKeypad(
  onNumberSelected: (Int) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Zero row across top
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .clip(RoundedCornerShape(8.dp))
        .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .clickable { onNumberSelected(0) }
        .testTag("keypad_num_0"),
      color = RouletteGreen
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "0  (ZERO)",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Black,
              color = Color.White,
              letterSpacing = 1.sp
            )
          )
        }
      }
    }

    // 1 to 36 Grid formatted in 6 rows of 6 numbers
    val rows = listOf(
      listOf(1, 2, 3, 4, 5, 6),
      listOf(7, 8, 9, 10, 11, 12),
      listOf(13, 14, 15, 16, 17, 18),
      listOf(19, 20, 21, 22, 23, 24),
      listOf(25, 26, 27, 28, 29, 30),
      listOf(31, 32, 33, 34, 35, 36)
    )

    rows.forEach { rowNums ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        rowNums.forEach { num ->
          val numInfo = RouletteConstants.getNumberInfo(num)
          val bg = when (numInfo.color) {
            RouletteColor.RED -> RouletteRed
            RouletteColor.BLACK -> RouletteBlack
            RouletteColor.GREEN -> RouletteGreen
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(bg)
              .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
              .clickable { onNumberSelected(num) }
              .testTag("keypad_num_$num"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$num",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 15.sp
              )
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuickDozenKeypad(
  onNumberSelected: (Int) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "Instant 1-Tap Dozen Logging (Assigns median number from selected dozen)",
      style = MaterialTheme.typography.bodySmall.copy(
        fontSize = 11.sp,
        color = TextTertiary
      ),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickDozenButton(
        title = "1st DOZEN",
        range = "1 - 12",
        color = Dozen1Color,
        onClick = { onNumberSelected(6) },
        modifier = Modifier.weight(1f)
      )
      QuickDozenButton(
        title = "2nd DOZEN",
        range = "13 - 24",
        color = Dozen2Color,
        onClick = { onNumberSelected(18) },
        modifier = Modifier.weight(1f)
      )
      QuickDozenButton(
        title = "3rd DOZEN",
        range = "25 - 36",
        color = Dozen3Color,
        onClick = { onNumberSelected(30) },
        modifier = Modifier.weight(1f)
      )
    }

    // Zero button
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, ZeroColor, RoundedCornerShape(10.dp))
        .clickable { onNumberSelected(0) },
      color = ZeroColor.copy(alpha = 0.15f)
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "0  ZERO HIT",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Black,
            color = ZeroColor,
            letterSpacing = 1.sp
          )
        )
      }
    }
  }
}

@Composable
private fun QuickDozenButton(
  title: String,
  range: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .height(64.dp)
      .clip(RoundedCornerShape(12.dp))
      .border(1.5.dp, color, RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
    color = color.copy(alpha = 0.12f)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Black,
          color = color,
          fontSize = 12.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = range,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
      )
    }
  }
}
