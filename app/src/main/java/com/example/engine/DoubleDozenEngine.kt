package com.example.engine

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class DoubleDozenEngine {

  fun analyzeAndPredict(
    history: List<SpinRecord>,
    strategy: StrategyMode = StrategyMode.AI_ENSEMBLE,
    sensitivity: SignalSensitivity = SignalSensitivity.BALANCED
  ): PredictionSignal {
    val totalSpins = history.size
    val currentSpinIndex = totalSpins + 1

    // Need at least 3 spins for initial calibration
    if (totalSpins < 3) {
      val needed = 3 - totalSpins
      return PredictionSignal(
        recommendedPair = null,
        confidencePercentage = 0,
        confidenceLevel = SignalConfidence.MODERATE,
        status = SignalStatus.WAITING_CALCULATING,
        reasoning = "Warming up pattern analyzer. Enter $needed more spin${if (needed > 1) "s" else ""} for signal generation.",
        patternFactors = listOf("Observation Mode", "Gathering baseline table physics & distribution"),
        suggestedUnits = 1,
        spinNumberGenerated = currentSpinIndex
      )
    }

    // Calculate sleep counts
    val sleepMap = calculateSleepCounts(history)
    val sleepD1 = sleepMap[Dozen.FIRST] ?: 0
    val sleepD2 = sleepMap[Dozen.SECOND] ?: 0
    val sleepD3 = sleepMap[Dozen.THIRD] ?: 0
    val sleepZero = sleepMap[Dozen.ZERO] ?: 0

    // Recent dozens
    val lastSpin = history.last()
    val lastDozen = lastSpin.numberInfo.dozen
    val recentDozenList = history.takeLast(12).map { it.numberInfo.dozen }
    val last5DozenList = history.takeLast(5).map { it.numberInfo.dozen }

    // Markov transition counts
    val markovProbabilities = calculateMarkovTransitions(history, lastDozen)

    // Score all 3 double dozen combinations
    val scores = mutableMapOf<DoubleDozenPair, PairScore>()

    for (pair in DoubleDozenPair.entries) {
      val score = calculatePairScore(
        pair = pair,
        history = history,
        lastDozen = lastDozen,
        sleepMap = sleepMap,
        recentDozens = recentDozenList,
        last5Dozens = last5DozenList,
        markovProbabilities = markovProbabilities,
        strategy = strategy
      )
      scores[pair] = score
    }

    // Find best scoring pair
    val bestEntry = scores.maxByOrNull { it.value.totalScore } ?: return PredictionSignal(
      recommendedPair = null,
      confidencePercentage = 0,
      confidenceLevel = SignalConfidence.MODERATE,
      status = SignalStatus.WAITING_CALCULATING,
      reasoning = "Analyzing table trends...",
      suggestedUnits = 1,
      spinNumberGenerated = currentSpinIndex
    )

    val bestPair = bestEntry.key
    val bestScoreObj = bestEntry.value
    val finalConfidence = min(96, max(52, bestScoreObj.totalScore.roundToInt()))

    val confidenceLevel = when {
      finalConfidence >= SignalConfidence.VERY_HIGH.minScore -> SignalConfidence.VERY_HIGH
      finalConfidence >= SignalConfidence.HIGH.minScore -> SignalConfidence.HIGH
      else -> SignalConfidence.MODERATE
    }

    // Determine if signal triggers based on sensitivity threshold
    val threshold = sensitivity.threshold

    // Check for high uncertainty / Zero warning
    val zeroWarning = last5DozenList.count { it == Dozen.ZERO } >= 2

    return if (finalConfidence >= threshold && !zeroWarning) {
      PredictionSignal(
        recommendedPair = bestPair,
        confidencePercentage = finalConfidence,
        confidenceLevel = confidenceLevel,
        status = SignalStatus.ACTIVE_SIGNAL,
        reasoning = bestScoreObj.primaryReason,
        patternFactors = bestScoreObj.factors,
        suggestedUnits = calculateSuggestedUnits(history),
        spinNumberGenerated = currentSpinIndex
      )
    } else {
      val waitingReason = if (zeroWarning) {
        "High Zero (0) frequency detected. Table in transition—waiting for stabilization."
      } else {
        "Pattern strength ($finalConfidence%) is below ${sensitivity.title} threshold ($threshold%). Monitoring background flow."
      }

      PredictionSignal(
        recommendedPair = bestPair,
        confidencePercentage = finalConfidence,
        confidenceLevel = confidenceLevel,
        status = SignalStatus.WAITING_CALCULATING,
        reasoning = waitingReason,
        patternFactors = listOf(
          "Leading Candidate: ${bestPair.shortLabel} ($finalConfidence%)",
          "D1 Sleep: $sleepD1 spins | D2 Sleep: $sleepD2 spins | D3 Sleep: $sleepD3 spins",
          "Watching for clear double dozen breakout"
        ),
        suggestedUnits = 1,
        spinNumberGenerated = currentSpinIndex
      )
    }
  }

  private fun calculateSleepCounts(history: List<SpinRecord>): Map<Dozen, Int> {
    val map = mutableMapOf(
      Dozen.FIRST to history.size,
      Dozen.SECOND to history.size,
      Dozen.THIRD to history.size,
      Dozen.ZERO to history.size
    )

    for (dozen in Dozen.entries) {
      val lastIndex = history.indexOfLast { it.numberInfo.dozen == dozen }
      if (lastIndex != -1) {
        map[dozen] = history.size - 1 - lastIndex
      } else {
        map[dozen] = history.size
      }
    }
    return map
  }

  private fun calculateMarkovTransitions(
    history: List<SpinRecord>,
    lastDozen: Dozen
  ): Map<Dozen, Float> {
    val transitions = mutableMapOf(
      Dozen.FIRST to 1f,
      Dozen.SECOND to 1f,
      Dozen.THIRD to 1f,
      Dozen.ZERO to 0.2f
    )

    if (history.size >= 2) {
      for (i in 0 until history.size - 1) {
        val from = history[i].numberInfo.dozen
        val to = history[i + 1].numberInfo.dozen
        if (from == lastDozen) {
          transitions[to] = (transitions[to] ?: 1f) + 2f
        }
      }
    }

    val total = transitions.values.sum()
    return transitions.mapValues { it.value / total }
  }

  private fun calculatePairScore(
    pair: DoubleDozenPair,
    history: List<SpinRecord>,
    lastDozen: Dozen,
    sleepMap: Map<Dozen, Int>,
    recentDozens: List<Dozen>,
    last5Dozens: List<Dozen>,
    markovProbabilities: Map<Dozen, Float>,
    strategy: StrategyMode
  ): PairScore {
    val excluded = pair.excludedDozen()
    val excludedSleep = sleepMap[excluded] ?: 0
    val firstSleep = sleepMap[pair.first] ?: 0
    val secondSleep = sleepMap[pair.second] ?: 0

    // 1. Base European Double Dozen probability: 24 / 37 ~ 64.86%
    var score = 65.0f
    val factors = mutableListOf<String>()

    // 2. Sleep / Exclusion Factor:
    // If the excluded dozen has been sleeping for 3+ spins, the 2 active dozens have high cluster dominance
    val sleepBonus = when {
      excludedSleep >= 6 -> 16.0f
      excludedSleep >= 4 -> 12.0f
      excludedSleep >= 3 -> 8.0f
      excludedSleep == 0 -> -6.0f // Excluded dozen just hit!
      else -> 2.0f
    }

    if (excludedSleep >= 3) {
      factors.add("${excluded.shortName} sleeping for $excludedSleep spins (Cold Exclusion)")
    }

    // 3. Markov Transition Probability:
    // Probability that next dozen is in pair
    val markovPairProb = (markovProbabilities[pair.first] ?: 0.33f) + (markovProbabilities[pair.second] ?: 0.33f)
    val markovBonus = (markovPairProb - 0.65f) * 28.0f
    if (markovPairProb > 0.70f) {
      factors.add("Markov transition matrix favors ${pair.shortLabel} (${(markovPairProb * 100).toInt()}%)")
    }

    // 4. Momentum & Frequency (Last 12 spins):
    val countInPair12 = recentDozens.count { pair.contains(it) }
    val momentumBonus = if (recentDozens.isNotEmpty()) {
      val ratio = countInPair12.toFloat() / recentDozens.size.toFloat()
      (ratio - 0.65f) * 20.0f
    } else 0f

    if (countInPair12 >= 8 && recentDozens.size >= 10) {
      factors.add("${pair.shortLabel} hot momentum ($countInPair12 / ${recentDozens.size} recent spins)")
    }

    // 5. Pattern Repeat & Alternation (Last 4 spins):
    var patternBonus = 0f
    if (last5Dozens.size >= 3) {
      val last3 = last5Dozens.takeLast(3)
      val allInPair = last3.all { pair.contains(it) }
      if (allInPair) {
        patternBonus += 6.0f
        factors.add("Sustained 3-spin cluster in ${pair.shortLabel}")
      }

      // Check chop / alternation (e.g. D1 -> D2 -> D1)
      if (last3.size == 3 && last3[0] != last3[1] && last3[1] != last3[2] &&
        pair.contains(last3[0]) && pair.contains(last3[1]) && pair.contains(last3[2])
      ) {
        patternBonus += 8.0f
        factors.add("Active 2-Dozen oscillation (${pair.shortLabel})")
      }
    }

    // 6. Strategy Specific Weighting:
    when (strategy) {
      StrategyMode.AI_ENSEMBLE -> {
        score += (sleepBonus * 0.32f) + (markovBonus * 0.28f) + (momentumBonus * 0.22f) + (patternBonus * 0.18f)
      }
      StrategyMode.SLEEP_HUNTER -> {
        score += (sleepBonus * 0.58f) + (patternBonus * 0.20f) + (markovBonus * 0.12f) + (momentumBonus * 0.10f)
      }
      StrategyMode.MARKOV_MATRIX -> {
        score += (markovBonus * 0.55f) + (patternBonus * 0.20f) + (sleepBonus * 0.15f) + (momentumBonus * 0.10f)
      }
      StrategyMode.MOMENTUM_TREND -> {
        score += (momentumBonus * 0.50f) + (patternBonus * 0.25f) + (sleepBonus * 0.15f) + (markovBonus * 0.10f)
      }
    }

    // Primary Reason Construction
    val primaryReason = when {
      excludedSleep >= 4 -> "Dozen ${excluded.id} has slept $excludedSleep consecutive spins. High probability of continued ${pair.shortLabel} dominance."
      markovPairProb >= 0.75f -> "Post-Dozen ${lastDozen.id} transition model strongly targets ${pair.shortLabel} with ${(markovPairProb * 100).toInt()}% empirical rate."
      countInPair12 >= 8 -> "Strong cluster trend: ${pair.shortLabel} captured $countInPair12 of the last ${recentDozens.size} outcomes."
      else -> "Multi-factor statistical convergence indicates ${pair.shortLabel} as the optimal double dozen entry."
    }

    if (factors.isEmpty()) {
      factors.add("Statistical base coverage 64.8%")
      factors.add("Balanced volatility entry point")
    }

    return PairScore(
      totalScore = score,
      primaryReason = primaryReason,
      factors = factors.take(3)
    )
  }

  private fun calculateSuggestedUnits(history: List<SpinRecord>): Int {
    // Dynamic conservative unit suggestion based on recent signals
    val recentSignalSpins = history.filter { it.signalBeforeSpin?.status == SignalStatus.ACTIVE_SIGNAL }.takeLast(4)
    val consecutiveLosses = recentSignalSpins.reversed().takeWhile { it.outcome == PredictionOutcome.LOST }.size

    return when (consecutiveLosses) {
      0 -> 1
      1 -> 2
      2 -> 4
      else -> 1 // Reset on high loss streak to protect bankroll
    }
  }

  fun evaluateStats(history: List<SpinRecord>): SessionStats {
    val totalSpins = history.size
    val signalSpins = history.filter { it.signalBeforeSpin?.status == SignalStatus.ACTIVE_SIGNAL }
    val totalSignals = signalSpins.size
    val wins = signalSpins.count { it.outcome == PredictionOutcome.WON }
    val losses = signalSpins.count { it.outcome == PredictionOutcome.LOST }
    val winRate = if (totalSignals > 0) (wins.toFloat() / totalSignals.toFloat()) * 100f else 0f

    // Calculate streaks
    var currentStreak = 0
    var bestStreak = 0
    var tempWinStreak = 0

    for (record in signalSpins) {
      if (record.outcome == PredictionOutcome.WON) {
        tempWinStreak++
        if (tempWinStreak > bestStreak) bestStreak = tempWinStreak
        if (currentStreak >= 0) currentStreak++ else currentStreak = 1
      } else if (record.outcome == PredictionOutcome.LOST) {
        tempWinStreak = 0
        if (currentStreak <= 0) currentStreak-- else currentStreak = -1
      }
    }

    // Profit calculation for Double Dozen:
    // Bet 1 unit on Dozen A, 1 unit on Dozen B (total 2 units bet)
    // If Win: payout is 3 units -> net +1 unit
    // If Loss: net -2 units
    var units = 0
    for (record in signalSpins) {
      val betUnits = record.signalBeforeSpin?.suggestedUnits ?: 1
      if (record.outcome == PredictionOutcome.WON) {
        units += betUnits * 1
      } else if (record.outcome == PredictionOutcome.LOST) {
        units -= betUnits * 2
      }
    }

    // Dozen counts
    val dozenCounts = mutableMapOf(
      Dozen.FIRST to 0,
      Dozen.SECOND to 0,
      Dozen.THIRD to 0,
      Dozen.ZERO to 0
    )
    history.forEach { spin ->
      val dozen = spin.numberInfo.dozen
      dozenCounts[dozen] = (dozenCounts[dozen] ?: 0) + 1
    }

    val sleepMap = calculateSleepCounts(history)

    return SessionStats(
      totalSpins = totalSpins,
      totalSignals = totalSignals,
      wins = wins,
      losses = losses,
      winRate = winRate,
      currentStreak = currentStreak,
      bestWinStreak = bestStreak,
      unitsProfit = units,
      dozenCounts = dozenCounts,
      dozenSleepCounts = sleepMap
    )
  }

  private data class PairScore(
    val totalScore: Float,
    val primaryReason: String,
    val factors: List<String>
  )
}
