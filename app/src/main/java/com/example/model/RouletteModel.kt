package com.example.model

enum class Dozen(val id: Int, val shortName: String, val rangeText: String) {
  ZERO(0, "0", "0"),
  FIRST(1, "1st 12", "1 - 12"),
  SECOND(2, "2nd 12", "13 - 24"),
  THIRD(3, "3rd 12", "25 - 36");

  companion object {
    fun fromNumber(number: Int): Dozen = when (number) {
      0 -> ZERO
      in 1..12 -> FIRST
      in 13..24 -> SECOND
      in 25..36 -> THIRD
      else -> ZERO
    }
  }
}

enum class RouletteColor {
  RED,
  BLACK,
  GREEN
}

data class RouletteNumber(
  val number: Int,
  val color: RouletteColor,
  val dozen: Dozen
)

object RouletteConstants {
  val RED_NUMBERS = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
  val BLACK_NUMBERS = setOf(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35)

  fun getNumberInfo(number: Int): RouletteNumber {
    val color = when {
      number == 0 -> RouletteColor.GREEN
      RED_NUMBERS.contains(number) -> RouletteColor.RED
      else -> RouletteColor.BLACK
    }
    val dozen = Dozen.fromNumber(number)
    return RouletteNumber(number, color, dozen)
  }
}

enum class DoubleDozenPair(
  val first: Dozen,
  val second: Dozen,
  val displayName: String,
  val shortLabel: String,
  val coverageText: String
) {
  D1_D2(Dozen.FIRST, Dozen.SECOND, "1st & 2nd Dozen", "D1 + D2", "Numbers 1-24 (64.8%)"),
  D2_D3(Dozen.SECOND, Dozen.THIRD, "2nd & 3rd Dozen", "D2 + D3", "Numbers 13-36 (64.8%)"),
  D1_D3(Dozen.FIRST, Dozen.THIRD, "1st & 3rd Dozen", "D1 + D3", "Numbers 1-12 & 25-36 (64.8%)");

  fun contains(dozen: Dozen): Boolean {
    return dozen == first || dozen == second
  }

  fun containsNumber(number: Int): Boolean {
    val dozen = Dozen.fromNumber(number)
    return contains(dozen)
  }

  fun excludedDozen(): Dozen {
    return when (this) {
      D1_D2 -> Dozen.THIRD
      D2_D3 -> Dozen.FIRST
      D1_D3 -> Dozen.SECOND
    }
  }
}

enum class SignalStatus {
  WAITING_CALCULATING,
  ACTIVE_SIGNAL
}

enum class SignalConfidence(val label: String, val minScore: Int) {
  MODERATE("Moderate", 65),
  HIGH("High Confidence", 75),
  VERY_HIGH("Ultra High Confidence", 85)
}

data class PredictionSignal(
  val id: String = System.currentTimeMillis().toString(),
  val recommendedPair: DoubleDozenPair?,
  val confidencePercentage: Int,
  val confidenceLevel: SignalConfidence,
  val status: SignalStatus,
  val reasoning: String,
  val patternFactors: List<String> = emptyList(),
  val suggestedUnits: Int = 1,
  val spinNumberGenerated: Int = 0
)

enum class PredictionOutcome {
  NO_SIGNAL,
  WON,
  LOST
}

data class SpinRecord(
  val id: Long = System.currentTimeMillis(),
  val spinIndex: Int,
  val numberInfo: RouletteNumber,
  val signalBeforeSpin: PredictionSignal?,
  val outcome: PredictionOutcome
)

enum class StrategyMode(
  val title: String,
  val subtitle: String
) {
  AI_ENSEMBLE(
    "AI Pattern Ensemble",
    "Analyzes sleep, Markov transitions, hot momentum & streak reversion together"
  ),
  SLEEP_HUNTER(
    "Sleep & Exclude Engine",
    "Detects cold sleeping dozens to bet on the two active high-probability dozens"
  ),
  MARKOV_MATRIX(
    "Markov Transition Matrix",
    "Predicts based on probability transitions directly after the latest hit dozen"
  ),
  MOMENTUM_TREND(
    "Trend Follower",
    "Follows dominant flow, repeating patterns and cluster momentum"
  )
}

enum class SignalSensitivity(
  val title: String,
  val threshold: Int,
  val description: String
) {
  HIGH_PRECISION("High Precision", 78, "Highest accuracy, waits for prime statistical alignments"),
  BALANCED("Balanced", 70, "Optimal balance of frequent signals and high win-rate"),
  AGGRESSIVE("Aggressive", 62, "More frequent bet opportunities")
}

data class SessionStats(
  val totalSpins: Int = 0,
  val totalSignals: Int = 0,
  val wins: Int = 0,
  val losses: Int = 0,
  val winRate: Float = 0f,
  val currentStreak: Int = 0,
  val bestWinStreak: Int = 0,
  val unitsProfit: Int = 0,
  val dozenCounts: Map<Dozen, Int> = mapOf(
    Dozen.FIRST to 0,
    Dozen.SECOND to 0,
    Dozen.THIRD to 0,
    Dozen.ZERO to 0
  ),
  val dozenSleepCounts: Map<Dozen, Int> = mapOf(
    Dozen.FIRST to 0,
    Dozen.SECOND to 0,
    Dozen.THIRD to 0,
    Dozen.ZERO to 0
  )
)
