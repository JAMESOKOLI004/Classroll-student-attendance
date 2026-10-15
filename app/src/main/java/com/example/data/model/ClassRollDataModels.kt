package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ClassWithStats(
    val classEntity: ClassEntity,
    val studentCount: Int,
    val todayRate: Float?, // null if not recorded today, e.g. 94.5f
    val todayPresent: Int = 0,
    val todayLate: Int = 0,
    val todayAbsent: Int = 0
)

data class StudentWithStats(
    val student: StudentEntity,
    val totalDays: Int,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int,
    val attendanceRate: Float // 0f..100f
)

data class StudentAttendanceRecord(
    val dateString: String,
    val status: AttendanceStatus,
    val remark: String = "",
    val timestamp: Long = 0L
)

data class TrendPoint(
    val label: String, // e.g. "W1", "Oct 01"
    val percentage: Float // 0..100
)

data class DayAttendanceSummary(
    val dateString: String,
    val totalStudents: Int,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int,
    val attendancePercentage: Float,
    val studentRecords: List<DayStudentStatus> = emptyList()
)

data class DayStudentStatus(
    val student: StudentEntity,
    val status: AttendanceStatus,
    val remark: String = ""
)

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    private val shortDisplayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun todayIso(): String = isoFormat.format(Date())

    fun formatIsoToDisplay(iso: String): String {
        return try {
            val date = isoFormat.parse(iso) ?: return iso
            displayFormat.format(date)
        } catch (_: Exception) {
            iso
        }
    }

    fun formatIsoToShort(iso: String): String {
        return try {
            val date = isoFormat.parse(iso) ?: return iso
            shortDisplayFormat.format(date)
        } catch (_: Exception) {
            iso
        }
    }

    fun formatMonthYear(year: Int, monthIndex: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFormat.format(cal.time)
    }

    fun offsetDate(baseIso: String, daysOffset: Int): String {
        return try {
            val date = isoFormat.parse(baseIso) ?: return baseIso
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, daysOffset)
            }
            isoFormat.format(cal.time)
        } catch (_: Exception) {
            baseIso
        }
    }
}
