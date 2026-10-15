package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClassRollDatabase
import com.example.data.ClassRollRepository
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassEntity
import com.example.data.model.ClassWithStats
import com.example.data.model.DateUtils
import com.example.data.model.DayAttendanceSummary
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentEntity
import com.example.data.model.StudentWithStats
import com.example.data.model.TrendPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    data class StudentList(val classId: Long) : Screen()
    data class AddStudent(val classId: Long, val editStudentId: Long? = null) : Screen()
    data class TakeAttendance(val classId: Long, val dateIso: String = DateUtils.todayIso()) : Screen()
    data class AttendanceHistory(val initialClassId: Long? = null) : Screen()
    data class StudentProfile(val studentId: Long, val fromClassId: Long? = null) : Screen()
    data class Reports(val initialClassId: Long? = null) : Screen()
}

class ClassRollViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClassRollRepository
    init {
        val db = ClassRollDatabase.getDatabase(application)
        repository = ClassRollRepository(db.classRollDao(), application)
        viewModelScope.launch {
            repository.initialize()
        }
    }

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Login))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    val currentScreen: Screen
        get() = _screenStack.value.lastOrNull() ?: Screen.Login

    fun navigateTo(screen: Screen) {
        _screenStack.value = _screenStack.value + screen
    }

    fun popBack(): Boolean {
        if (_screenStack.value.size > 1) {
            _screenStack.value = _screenStack.value.dropLast(1)
            return true
        }
        return false
    }

    // Auth State
    val isLoggedIn = MutableStateFlow(false)
    val teacherEmail = MutableStateFlow("dr.jenkins@faculty.school.edu")
    val teacherName = MutableStateFlow("Dr. Sarah Jenkins")

    fun signIn(email: String, pass: String): Boolean {
        if (email.isNotBlank() && pass.length >= 4) {
            isLoggedIn.value = true
            teacherEmail.value = email
            teacherName.value = if (email.contains("jenkins", ignoreCase = true)) {
                "Dr. Sarah Jenkins"
            } else {
                "Teacher (${email.substringBefore('@').replace('.', ' ').capitalize()})"
            }
            navigateTo(Screen.Home)
            return true
        }
        return false
    }

    fun signInDemo() {
        isLoggedIn.value = true
        teacherEmail.value = "dr.jenkins@faculty.school.edu"
        teacherName.value = "Dr. Sarah Jenkins"
        navigateTo(Screen.Home)
    }

    fun signOut() {
        isLoggedIn.value = false
        _screenStack.value = listOf(Screen.Login)
    }

    // Classes
    val classesWithStats: StateFlow<List<ClassWithStats>> = repository.getClassesWithStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Take Attendance Screen State
    private val _takeAttendanceClass = MutableStateFlow<ClassEntity?>(null)
    val takeAttendanceClass = _takeAttendanceClass.asStateFlow()

    private val _attendanceDateIso = MutableStateFlow(DateUtils.todayIso())
    val attendanceDateIso = _attendanceDateIso.asStateFlow()

    private val _attendanceMarks = MutableStateFlow<Map<Long, AttendanceStatus>>(emptyMap())
    val attendanceMarks = _attendanceMarks.asStateFlow()

    private val _classStudentsForAttendance = MutableStateFlow<List<StudentEntity>>(emptyList())
    val classStudentsForAttendance = _classStudentsForAttendance.asStateFlow()

    private val _takeAttendanceSuccessMessage = MutableStateFlow<String?>(null)
    val takeAttendanceSuccessMessage = _takeAttendanceSuccessMessage.asStateFlow()

    fun loadTakeAttendance(classId: Long, dateIso: String) {
        viewModelScope.launch {
            _attendanceDateIso.value = dateIso
            _takeAttendanceSuccessMessage.value = null
            val clazz = repository.getClassById(classId)
            _takeAttendanceClass.value = clazz

            val db = ClassRollDatabase.getDatabase(getApplication())
            val students = db.classRollDao().getStudentsByClass(classId)
            _classStudentsForAttendance.value = students

            val (existingSession, existingMarks) = repository.getAttendanceForClassAndDate(classId, dateIso)
            if (existingSession != null && existingMarks.isNotEmpty()) {
                _attendanceMarks.value = existingMarks
            } else {
                // Default to Present for all students for speedy taking
                _attendanceMarks.value = students.associate { it.id to AttendanceStatus.PRESENT }
            }
        }
    }

    fun setStudentStatus(studentId: Long, status: AttendanceStatus) {
        val current = _attendanceMarks.value.toMutableMap()
        current[studentId] = status
        _attendanceMarks.value = current
    }

    fun markAllPresent() {
        val current = _attendanceMarks.value.toMutableMap()
        for (st in _classStudentsForAttendance.value) {
            current[st.id] = AttendanceStatus.PRESENT
        }
        _attendanceMarks.value = current
    }

    fun submitAttendance(notes: String = "", onComplete: () -> Unit) {
        viewModelScope.launch {
            val clazz = _takeAttendanceClass.value ?: return@launch
            repository.submitAttendance(
                classId = clazz.id,
                dateIso = _attendanceDateIso.value,
                notes = notes,
                marks = _attendanceMarks.value
            )
            _takeAttendanceSuccessMessage.value = "Attendance recorded successfully!"
            onComplete()
        }
    }

    // Add Class
    fun createClass(
        name: String,
        subject: String,
        grade: String,
        room: String,
        schedule: String,
        colorHex: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.createClass(
                name = name,
                subject = subject,
                grade = grade,
                room = room,
                schedule = schedule,
                colorHex = colorHex
            )
            onSuccess()
        }
    }

    // Students List for a Class
    private val _activeClassId = MutableStateFlow<Long?>(null)
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun getStudentsWithStatsFlow(classId: Long) = repository.getStudentsWithStatsForClassFlow(classId)

    // Add / Edit Student
    fun saveStudent(
        id: Long = 0,
        classId: Long,
        studentId: String,
        firstName: String,
        lastName: String,
        email: String,
        guardianName: String,
        guardianContact: String,
        avatarColorHex: String,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveStudent(
                id = id,
                classId = classId,
                studentId = studentId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                guardianName = guardianName,
                guardianContact = guardianContact,
                avatarColorHex = avatarColorHex,
                notes = notes
            )
            onComplete()
        }
    }

    // Attendance History State
    val historyClassId = MutableStateFlow<Long?>(null) // null = all classes
    val historyYear = MutableStateFlow(2026)
    val historyMonth = MutableStateFlow(9) // 0-based: 9 = October
    val historySelectedDate = MutableStateFlow(DateUtils.todayIso())
    val historyMonthRates = MutableStateFlow<Map<String, Float>>(emptyMap())
    val historyDaySummary = MutableStateFlow<DayAttendanceSummary?>(null)

    fun loadHistory(initialClassId: Long? = null) {
        if (initialClassId != null) {
            historyClassId.value = initialClassId
        }
        val cal = Calendar.getInstance()
        historyYear.value = cal.get(Calendar.YEAR)
        historyMonth.value = cal.get(Calendar.MONTH)
        historySelectedDate.value = DateUtils.todayIso()
        refreshHistoryMonth()
        selectHistoryDate(historySelectedDate.value)
    }

    fun setHistoryClass(classId: Long?) {
        historyClassId.value = classId
        refreshHistoryMonth()
        selectHistoryDate(historySelectedDate.value)
    }

    fun changeHistoryMonth(delta: Int) {
        var m = historyMonth.value + delta
        var y = historyYear.value
        if (m < 0) {
            m = 11
            y -= 1
        } else if (m > 11) {
            m = 0
            y += 1
        }
        historyMonth.value = m
        historyYear.value = y
        refreshHistoryMonth()
    }

    private fun refreshHistoryMonth() {
        viewModelScope.launch {
            val rates = repository.getMonthAttendanceRates(
                classId = historyClassId.value,
                year = historyYear.value,
                monthIndex = historyMonth.value
            )
            historyMonthRates.value = rates
        }
    }

    fun selectHistoryDate(dateIso: String) {
        historySelectedDate.value = dateIso
        viewModelScope.launch {
            val summary = repository.getHistorySummaryForDate(historyClassId.value, dateIso)
            historyDaySummary.value = summary
        }
    }

    // Student Profile State
    val profileStudent = MutableStateFlow<StudentEntity?>(null)
    val profileStats = MutableStateFlow<StudentWithStats?>(null)
    val profileTrend = MutableStateFlow<List<TrendPoint>>(emptyList())
    val profileRecords = MutableStateFlow<List<StudentAttendanceRecord>>(emptyList())

    fun loadStudentProfile(studentId: Long) {
        viewModelScope.launch {
            val (st, stats, pair) = repository.getStudentProfileData(studentId)
            profileStudent.value = st
            profileStats.value = stats
            profileTrend.value = pair.first
            profileRecords.value = pair.second
        }
    }

    // Reports Screen State
    val reportClassId = MutableStateFlow<Long?>(null)
    val reportRangeFilter = MutableStateFlow("Entire Term")
    val reportStudentRows = MutableStateFlow<List<StudentWithStats>>(emptyList())
    val reportAverageRate = MutableStateFlow(92.4f)
    val reportExportToast = MutableStateFlow<String?>(null)

    fun loadReports(classId: Long? = null) {
        if (classId != null) {
            reportClassId.value = classId
        }
        refreshReportData()
    }

    fun setReportClass(classId: Long?) {
        reportClassId.value = classId
        refreshReportData()
    }

    fun setReportRange(range: String) {
        reportRangeFilter.value = range
        refreshReportData()
    }

    private fun refreshReportData() {
        viewModelScope.launch {
            val db = ClassRollDatabase.getDatabase(getApplication())
            val targetClassId = reportClassId.value
            val students = if (targetClassId != null && targetClassId > 0) {
                db.classRollDao().getStudentsByClass(targetClassId)
            } else {
                db.classRollDao().getAllStudents()
            }

            val rows = students.map { st ->
                val entries = db.classRollDao().getEntriesForStudent(st.id)
                val total = entries.size
                val present = entries.count { it.status == AttendanceStatus.PRESENT }
                val late = entries.count { it.status == AttendanceStatus.LATE }
                val absent = entries.count { it.status == AttendanceStatus.ABSENT }
                val rate = if (total > 0) ((present + late).toFloat() / total.toFloat()) * 100f else 100f
                StudentWithStats(st, total, present, late, absent, rate)
            }.sortedBy { it.attendanceRate }

            reportStudentRows.value = rows
            if (rows.isNotEmpty()) {
                reportAverageRate.value = rows.map { it.attendanceRate }.average().toFloat()
            }
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            val file = repository.generateCsvReport(reportClassId.value, reportRangeFilter.value)
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "ClassRoll Attendance Report - ${DateUtils.todayIso()}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Attendance CSV"))
            } catch (_: Exception) {
                // Fallback copy content to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ClassRoll CSV", file.readText())
                clipboard.setPrimaryClip(clip)
                reportExportToast.value = "CSV copied to clipboard!"
            }
        }
    }

    fun clearToast() {
        reportExportToast.value = null
    }
}
