package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RouletteViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        RoulettePredictorApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoulettePredictorApp(
  viewModel: RouletteViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground),
    containerColor = DarkBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DarkCardBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        color = DarkSurface
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // App Title & Icon
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CasinoGold.copy(alpha = 0.15f))
                .border(1.dp, CasinoGold, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = null,
                tint = CasinoGold,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "DOUBLE DOZEN",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  color = CasinoGoldGlow,
                  letterSpacing = 0.5.sp
                )
              )
              Text(
                text = "Live Predictor & Pattern Engine",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  color = TextSecondary
                )
              )
            }
          }

          // Strategy Pill Badge (Tap to customize)
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { viewModel.toggleSettings(true) }
              .testTag("strategy_pill")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = uiState.strategy.title.split(" ").take(2).joinToString(" "),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CasinoGold,
                  fontSize = 11.sp
                )
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = CasinoGold,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Main Scrollable Analytics & Prediction Area
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Hero Active Signal Card
        SignalCard(
          signal = uiState.currentSignal,
          lastResult = uiState.lastResultNotification,
          onDismissResult = { viewModel.dismissNotification() }
        )

        // 2. Recent Live Spins Strip
        RecentSpinsList(
          history = uiState.history
        )

        // 3. Pattern Analytics (Sleep counters, distribution, win-rate, streaks)
        PatternAnalytics(
          stats = uiState.stats
        )

        Spacer(modifier = Modifier.height(6.dp))
      }

      // Bottom Fixed Roulette Keypad
      RouletteKeypad(
        onNumberSelected = { number -> viewModel.addSpin(number) },
        onUndo = { viewModel.undoLastSpin() },
        onClear = { viewModel.clearSession() },
        onDemoSpin = { viewModel.addRandomDemoSpin() },
        onOpenSettings = { viewModel.toggleSettings(true) },
        modifier = Modifier.navigationBarsPadding()
      )
    }
  }

  // Strategy & Sensitivity Settings Dialog
  if (uiState.isSettingsOpen) {
    StrategySettingsDialog(
      currentStrategy = uiState.strategy,
      currentSensitivity = uiState.sensitivity,
      vibrationEnabled = uiState.vibrationEnabled,
      onStrategySelected = { mode -> viewModel.setStrategy(mode) },
      onSensitivitySelected = { sens -> viewModel.setSensitivity(sens) },
      onVibrationToggled = { enabled -> viewModel.toggleVibration(enabled) },
      onResetSession = { viewModel.clearSession() },
      onDismiss = { viewModel.toggleSettings(false) }
    )
  }
}
