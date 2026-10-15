package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusAbsentRedDark
import com.example.ui.theme.StatusAbsentRedSubtle
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateAmberDark
import com.example.ui.theme.StatusLateAmberSubtle
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusPresentGreenDark
import com.example.ui.theme.StatusPresentGreenSubtle

enum class AttendanceStatus(val displayName: String, val shortCode: String) {
    PRESENT("Present", "P"),
    LATE("Late", "L"),
    ABSENT("Absent", "A");

    val color: Color
        get() = when (this) {
            PRESENT -> StatusPresentGreen
            LATE -> StatusLateAmber
            ABSENT -> StatusAbsentRed
        }

    val darkColor: Color
        get() = when (this) {
            PRESENT -> StatusPresentGreenDark
            LATE -> StatusLateAmberDark
            ABSENT -> StatusAbsentRedDark
        }

    val containerColor: Color
        get() = when (this) {
            PRESENT -> StatusPresentGreenSubtle
            LATE -> StatusLateAmberSubtle
            ABSENT -> StatusAbsentRedSubtle
        }
}
