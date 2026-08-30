package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ResultBanner

@Composable
fun SignalCard(
  signal: PredictionSignal,
  lastResult: ResultBanner?,
  onDismissResult: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isActive = signal.status == SignalStatus.ACTIVE_SIGNAL && signal.recommendedPair != null

  // Infinite pulsing glow for active signal
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glowAlpha"
  )

  Column(modifier = modifier.fillMaxWidth()) {
    // Result banner if just completed a spin
    AnimatedVisibility(
      visible = lastResult != null,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      if (lastResult != null) {
        val bannerBg = if (lastResult.isWin) {
          Brush.horizontalGradient(listOf(Color(0xFF004D20), Color(0xFF007934)))
        } else {
          Brush.horizontalGradient(listOf(Color(0xFF5C1010), Color(0xFF8B1A1A)))
        }
        val bannerBorder = if (lastResult.isWin) NeonEmerald else RouletteRed

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, bannerBorder, RoundedCornerShape(12.dp)),
          color = Color.Transparent
        ) {
          Box(
            modifier = Modifier
              .background(bannerBg)
              .padding(horizontal = 16.dp, vertical = 10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (lastResult.isWin) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                  contentDescription = null,
                  tint = if (lastResult.isWin) NeonEmeraldGlow else Color(0xFFFF8A80),
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = lastResult.message,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
              IconButton(
                onClick = onDismissResult,
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Dismiss",
                  tint = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }

    // Main Signal Card
    val cardBackground = if (isActive) {
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF132822),
          Color(0xFF101B2B),
          Color(0xFF0D1522)
        )
      )
    } else {
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF182236),
          Color(0xFF121B2C),
          Color(0xFF0E1624)
        )
      )
    }

    val borderColor = if (isActive) {
      NeonEmerald.copy(alpha = glowAlpha)
    } else {
      DarkCardBorder
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = if (isActive) 12.dp else 4.dp,
          shape = RoundedCornerShape(20.dp),
          spotColor = if (isActive) NeonEmerald else Color.Black
        )
        .border(if (isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
        .testTag("signal_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
      Box(
        modifier = Modifier
          .background(cardBackground)
          .padding(18.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Status Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Live Status Pill
            Surface(
              shape = RoundedCornerShape(50),
              color = if (isActive) NeonEmerald.copy(alpha = 0.18f) else DarkSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isActive) NeonEmerald else DarkCardBorder
              )
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) NeonEmerald else CasinoGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isActive) "🎯 BET SIGNAL ACTIVE" else "⏳ OBSERVING & CALCULATING",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = if (isActive) NeonEmeraldGlow else CasinoGold
                  )
                )
              }
            }

            // Confidence Rating
            if (isActive) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = CasinoGold.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.6f))
              ) {
                Text(
                  text = "${signal.confidencePercentage}% ACCURACY",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = CasinoGoldGlow
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Prediction Display
          if (isActive && signal.recommendedPair != null) {
            val pair = signal.recommendedPair

            Text(
              text = "RECOMMENDED DOUBLE DOZEN BET",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Two Target Dozen Badges
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              DozenTargetBadge(
                dozen = pair.first,
                modifier = Modifier.weight(1f)
              )
              DozenTargetBadge(
                dozen = pair.second,
                modifier = Modifier.weight(1f)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coverage & Sizing Details
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = DarkSurface.copy(alpha = 0.8f),
              border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
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
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = pair.coverageText,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = TextPrimary
                    )
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = CasinoGold.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = "Size: ${signal.suggestedUnits}u each",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = CasinoGold
                    )
                  )
                }
              }
            }

          } else {
            // Waiting / Observing UI
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
            ) {
              Text(
                text = "Background Pattern Analysis Running",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = signal.reasoning,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = TextSecondary,
                  lineHeight = 18.sp
                )
              )
            }
          }

          // Reasoning & Factors Section
          if (signal.patternFactors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              signal.patternFactors.forEach { factor ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (isActive) NeonEmerald else CasinoGold,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = factor,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      color = if (isActive) TextPrimary else TextSecondary
                    )
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DozenTargetBadge(
  dozen: Dozen,
  modifier: Modifier = Modifier
) {
  val (color, name, range) = when (dozen) {
    Dozen.FIRST -> Triple(Dozen1Color, "1st DOZEN", "1 - 12")
    Dozen.SECOND -> Triple(Dozen2Color, "2nd DOZEN", "13 - 24")
    Dozen.THIRD -> Triple(Dozen3Color, "3rd DOZEN", "25 - 36")
    Dozen.ZERO -> Triple(ZeroColor, "ZERO", "0")
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = color.copy(alpha = 0.12f),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, color)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = name,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Black,
          color = color,
          letterSpacing = 0.5.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Numbers $range",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color.White
        )
      )
    }
  }
}
