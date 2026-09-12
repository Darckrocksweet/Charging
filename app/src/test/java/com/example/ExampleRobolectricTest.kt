package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChargingSession
import org.junit.Assert.assertEquals
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
    assertEquals("Зарядка EV", appName)
  }

  @Test
  fun `verify dual stream losses calculation`() {
    val session = ChargingSession(
      placeName = "Дача",
      chargerKwh = 40.0,
      cost = 260.0,
      carKwh = 36.0,
      batteryStartPercent = 20,
      batteryEndPercent = 80
    )

    // losses = charger energy − car energy
    assertEquals(4.0, session.lossesKwh, 0.001)
    assertEquals(10.0, session.lossPercent, 0.001)
    assertEquals(90.0, session.efficiencyPercent, 0.001)
  }
}
