package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SignalSensitivity
import com.example.model.StrategyMode
import com.example.ui.theme.*

@Composable
fun StrategySettingsDialog(
  currentStrategy: StrategyMode,
  currentSensitivity: SignalSensitivity,
  vibrationEnabled: Boolean,
  onStrategySelected: (StrategyMode) -> Unit,
  onSensitivitySelected: (SignalSensitivity) -> Unit,
  onVibrationToggled: (Boolean) -> Unit,
  onResetSession: () -> Unit,
  onDismiss: () -> Unit
) {
  var showResetConfirm by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
        .testTag("settings_dialog"),
      color = DarkSurface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Tune,
              contentDescription = null,
              tint = CasinoGold,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Predictor Engine Settings",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        HorizontalDivider(color = DarkCardBorder)

        // 1. Prediction Strategy Selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "ALGORITHMIC STRATEGY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CasinoGold,
              letterSpacing = 0.5.sp
            )
          )

          StrategyMode.entries.forEach { mode ->
            val isSelected = mode == currentStrategy
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) CasinoGold else DarkCardBorder,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { onStrategySelected(mode) },
              color = if (isSelected) CasinoGold.copy(alpha = 0.12f) else DarkSurfaceVariant
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = { onStrategySelected(mode) },
                  colors = RadioButtonDefaults.colors(
                    selectedColor = CasinoGold,
                    unselectedColor = TextTertiary
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = mode.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) CasinoGoldGlow else TextPrimary
                    )
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = mode.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = TextSecondary
                    )
                  )
                }
              }
            }
          }
        }

        // 2. Sensitivity Threshold
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "SIGNAL SENSITIVITY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CasinoGold,
              letterSpacing = 0.5.sp
            )
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SignalSensitivity.entries.forEach { sens ->
              val isSelected = sens == currentSensitivity
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) NeonEmerald else DarkCardBorder,
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable { onSensitivitySelected(sens) },
                color = if (isSelected) NeonEmerald.copy(alpha = 0.12f) else DarkSurfaceVariant
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = sens.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) NeonEmeraldGlow else TextPrimary,
                      fontSize = 11.sp
                    )
                  )
                  Text(
                    text = "${sens.threshold}%+",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Black,
                      color = if (isSelected) NeonEmerald else TextTertiary,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }
        }

        // 3. Vibration / Haptics Toggle
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
          color = DarkSurfaceVariant
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Vibration,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Haptic Vibration Alerts",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                  )
                )
                Text(
                  text = "Vibrates on signal and win/loss feedback",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = TextTertiary
                  )
                )
              }
            }

            Switch(
              checked = vibrationEnabled,
              onCheckedChange = onVibrationToggled,
              colors = SwitchDefaults.colors(
                checkedThumbColor = NeonEmerald,
                checkedTrackColor = NeonEmerald.copy(alpha = 0.3f),
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = DarkSurface
              )
            )
          }
        }

        // 4. Reset Session Option
        if (showResetConfirm) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, RouletteRed, RoundedCornerShape(12.dp)),
            color = RouletteRed.copy(alpha = 0.1f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Clear all spin history and reset statistics?",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = TextPrimary
                )
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                TextButton(onClick = { showResetConfirm = false }) {
                  Text("Cancel", color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                  onClick = {
                    showResetConfirm = false
                    onResetSession()
                    onDismiss()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = RouletteRed)
                ) {
                  Text("Yes, Reset", color = Color.White)
                }
              }
            }
          }
        } else {
          OutlinedButton(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RouletteRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, RouletteRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteSweep,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Session Data", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
