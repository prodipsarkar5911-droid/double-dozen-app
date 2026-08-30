package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.DoubleDozenEngine
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RouletteUiState(
  val history: List<SpinRecord> = emptyList(),
  val currentSignal: PredictionSignal = PredictionSignal(
    recommendedPair = null,
    confidencePercentage = 0,
    confidenceLevel = SignalConfidence.MODERATE,
    status = SignalStatus.WAITING_CALCULATING,
    reasoning = "Welcome to Double Dozen Predictor. Enter live spins from your roulette table to start pattern tracking.",
    patternFactors = listOf("Live Spin Input Ready", "Tap any number (0-36) or quick dozen button"),
    suggestedUnits = 1,
    spinNumberGenerated = 1
  ),
  val stats: SessionStats = SessionStats(),
  val strategy: StrategyMode = StrategyMode.AI_ENSEMBLE,
  val sensitivity: SignalSensitivity = SignalSensitivity.BALANCED,
  val vibrationEnabled: Boolean = true,
  val soundEnabled: Boolean = true,
  val isSettingsOpen: Boolean = false,
  val lastResultNotification: ResultBanner? = null
)

data class ResultBanner(
  val isWin: Boolean,
  val number: Int,
  val dozen: Dozen,
  val message: String
)

class RouletteViewModel(application: Application) : AndroidViewModel(application) {

  private val engine = DoubleDozenEngine()
  private val _uiState = MutableStateFlow(RouletteUiState())
  val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

  fun addSpin(number: Int) {
    if (number !in 0..36) return

    val numberInfo = RouletteConstants.getNumberInfo(number)
    val currentState = _uiState.value
    val signalBefore = currentState.currentSignal

    // Determine outcome if there was an active bet signal
    val outcome = if (signalBefore.status == SignalStatus.ACTIVE_SIGNAL && signalBefore.recommendedPair != null) {
      if (signalBefore.recommendedPair.containsNumber(number)) {
        PredictionOutcome.WON
      } else {
        PredictionOutcome.LOST
      }
    } else {
      PredictionOutcome.NO_SIGNAL
    }

    val newSpinIndex = currentState.history.size + 1
    val newRecord = SpinRecord(
      id = System.currentTimeMillis(),
      spinIndex = newSpinIndex,
      numberInfo = numberInfo,
      signalBeforeSpin = signalBefore,
      outcome = outcome
    )

    val updatedHistory = currentState.history + newRecord
    val updatedStats = engine.evaluateStats(updatedHistory)
    val nextSignal = engine.analyzeAndPredict(
      history = updatedHistory,
      strategy = currentState.strategy,
      sensitivity = currentState.sensitivity
    )

    // Result notification
    val banner = when (outcome) {
      PredictionOutcome.WON -> ResultBanner(
        isWin = true,
        number = number,
        dozen = numberInfo.dozen,
        message = "🎯 TARGET HIT! #${number} (${numberInfo.dozen.shortName}) - WIN (+1u)"
      )
      PredictionOutcome.LOST -> ResultBanner(
        isWin = false,
        number = number,
        dozen = numberInfo.dozen,
        message = "⚠️ MISSED! #${number} (${numberInfo.dozen.shortName}) - LOSS (-2u)"
      )
      PredictionOutcome.NO_SIGNAL -> null
    }

    // Trigger haptics
    if (currentState.vibrationEnabled) {
      when {
        outcome == PredictionOutcome.WON -> triggerHaptic(HapticType.WIN)
        outcome == PredictionOutcome.LOST -> triggerHaptic(HapticType.LOSS)
        nextSignal.status == SignalStatus.ACTIVE_SIGNAL -> triggerHaptic(HapticType.SIGNAL_ALERT)
        else -> triggerHaptic(HapticType.LIGHT_CLICK)
      }
    }

    _uiState.update {
      it.copy(
        history = updatedHistory,
        currentSignal = nextSignal,
        stats = updatedStats,
        lastResultNotification = banner
      )
    }
  }

  fun undoLastSpin() {
    val currentHistory = _uiState.value.history
    if (currentHistory.isEmpty()) return

    val updatedHistory = currentHistory.dropLast(1)
    val updatedStats = engine.evaluateStats(updatedHistory)
    val restoredSignal = if (updatedHistory.isEmpty()) {
      PredictionSignal(
        recommendedPair = null,
        confidencePercentage = 0,
        confidenceLevel = SignalConfidence.MODERATE,
        status = SignalStatus.WAITING_CALCULATING,
        reasoning = "Session reset. Ready for live spin inputs.",
        suggestedUnits = 1,
        spinNumberGenerated = 1
      )
    } else {
      engine.analyzeAndPredict(
        history = updatedHistory,
        strategy = _uiState.value.strategy,
        sensitivity = _uiState.value.sensitivity
      )
    }

    _uiState.update {
      it.copy(
        history = updatedHistory,
        currentSignal = restoredSignal,
        stats = updatedStats,
        lastResultNotification = null
      )
    }
  }

  fun clearSession() {
    _uiState.update {
      it.copy(
        history = emptyList(),
        currentSignal = PredictionSignal(
          recommendedPair = null,
          confidencePercentage = 0,
          confidenceLevel = SignalConfidence.MODERATE,
          status = SignalStatus.WAITING_CALCULATING,
          reasoning = "New session started. Enter live spins to initiate pattern tracking.",
          patternFactors = listOf("Ready for table input", "Double dozen statistical engine initialized"),
          suggestedUnits = 1,
          spinNumberGenerated = 1
        ),
        stats = SessionStats(),
        lastResultNotification = null
      )
    }
  }

  fun addRandomDemoSpin() {
    // Generate realistic weighted roulette spin (0-36)
    val randomNum = Random.nextInt(0, 37)
    addSpin(randomNum)
  }

  fun setStrategy(mode: StrategyMode) {
    _uiState.update { currentState ->
      val updatedSignal = if (currentState.history.isNotEmpty()) {
        engine.analyzeAndPredict(
          history = currentState.history,
          strategy = mode,
          sensitivity = currentState.sensitivity
        )
      } else {
        currentState.currentSignal
      }
      currentState.copy(strategy = mode, currentSignal = updatedSignal)
    }
  }

  fun setSensitivity(sensitivity: SignalSensitivity) {
    _uiState.update { currentState ->
      val updatedSignal = if (currentState.history.isNotEmpty()) {
        engine.analyzeAndPredict(
          history = currentState.history,
          strategy = currentState.strategy,
          sensitivity = sensitivity
        )
      } else {
        currentState.currentSignal
      }
      currentState.copy(sensitivity = sensitivity, currentSignal = updatedSignal)
    }
  }

  fun toggleVibration(enabled: Boolean) {
    _uiState.update { it.copy(vibrationEnabled = enabled) }
  }

  fun toggleSettings(open: Boolean) {
    _uiState.update { it.copy(isSettingsOpen = open) }
  }

  fun dismissNotification() {
    _uiState.update { it.copy(lastResultNotification = null) }
  }

  private enum class HapticType {
    LIGHT_CLICK,
    SIGNAL_ALERT,
    WIN,
    LOSS
  }

  private fun triggerHaptic(type: HapticType) {
    try {
      val context = getApplication<Application>().applicationContext
      val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }

      if (vibrator == null || !vibrator.hasVibrator()) return

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = when (type) {
          HapticType.LIGHT_CLICK -> VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
          HapticType.SIGNAL_ALERT -> VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1)
          HapticType.WIN -> VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 50, 40, 100), -1)
          HapticType.LOSS -> VibrationEffect.createWaveform(longArrayOf(0, 150), -1)
        }
        vibrator.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(30)
      }
    } catch (_: Exception) {
      // Gracefully handle on devices without vibration support
    }
  }
}
