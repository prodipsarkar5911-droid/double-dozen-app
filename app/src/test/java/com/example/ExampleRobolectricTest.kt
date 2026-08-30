package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.DoubleDozenEngine
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Double Dozen", appName)
  }

  @Test
  fun `engine generates predictions correctly`() {
    val engine = DoubleDozenEngine()
    val spins = listOf(
      SpinRecord(1, 1, RouletteConstants.getNumberInfo(5), null, PredictionOutcome.NO_SIGNAL),
      SpinRecord(2, 2, RouletteConstants.getNumberInfo(14), null, PredictionOutcome.NO_SIGNAL),
      SpinRecord(3, 3, RouletteConstants.getNumberInfo(7), null, PredictionOutcome.NO_SIGNAL),
      SpinRecord(4, 4, RouletteConstants.getNumberInfo(2), null, PredictionOutcome.NO_SIGNAL)
    )

    val signal = engine.analyzeAndPredict(spins)
    assertNotNull(signal)
  }
}
