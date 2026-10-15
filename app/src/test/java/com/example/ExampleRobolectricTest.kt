package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AttendanceStatus
import com.example.data.model.DateUtils
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
    assertEquals("ClassRoll", appName)
  }

  @Test
  fun `attendance status values check`() {
    assertEquals("Present", AttendanceStatus.PRESENT.displayName)
    assertEquals("Late", AttendanceStatus.LATE.displayName)
    assertEquals("Absent", AttendanceStatus.ABSENT.displayName)
  }

  @Test
  fun `date utils today string is not empty`() {
    val today = DateUtils.todayIso()
    assertNotNull(today)
    assertEquals(10, today.length) // YYYY-MM-DD
  }
}
