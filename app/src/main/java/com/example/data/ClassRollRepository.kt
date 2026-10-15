package com.example.data

import android.content.Context
import com.example.data.dao.ClassRollDao
import com.example.data.model.AttendanceEntryEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassEntity
import com.example.data.model.ClassWithStats
import com.example.data.model.DateUtils
import com.example.data.model.DayAttendanceSummary
import com.example.data.model.DayStudentStatus
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentEntity
import com.example.data.model.StudentWithStats
import com.example.data.model.TrendPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClassRollRepository(
    private val dao: ClassRollDao,
    private val context: Context
) {

    suspend fun initialize() {
        SampleDataSeeder.seedIfEmpty(dao)
    }

    // Classes Flow with real-time stats
    fun getClassesWithStatsFlow(): Flow<List<ClassWithStats>> {
        val today = DateUtils.todayIso()
        return dao.getAllClassesFlow().map { classes ->
            withContext(Dispatchers.IO) {
                classes.map { clazz ->
                    val students = dao.getStudentsByClass(clazz.id)
                    val studentCount = students.size
                    val session = dao.getSessionByDate(clazz.id, today)

                    if (session != null) {
                        val entries = dao.getEntriesForSession(session.id)
                        val present = entries.count { it.status == AttendanceStatus.PRESENT }
                        val late = entries.count { it.status == AttendanceStatus.LATE }
                        val absent = entries.count { it.status == AttendanceStatus.ABSENT }
                        val rate = if (entries.isNotEmpty()) {
                            // Standard pedagogical attendance rate: Present + Late (counted as present or 0.8)
                            ((present + late).toFloat() / entries.size.toFloat()) * 100f
                        } else null

                        ClassWithStats(
                            classEntity = clazz,
                            studentCount = studentCount,
                            todayRate = rate,
                            todayPresent = present,
                            todayLate = late,
                            todayAbsent = absent
                        )
                    } else {
                        ClassWithStats(
                            classEntity = clazz,
                            studentCount = studentCount,
                            todayRate = null
                        )
                    }
                }
            }
        }
    }

    suspend fun getClassById(classId: Long): ClassEntity? = withContext(Dispatchers.IO) {
        dao.getClassById(classId)
    }

    suspend fun createClass(name: String, subject: String, grade: String, room: String, schedule: String, colorHex: String): Long = withContext(Dispatchers.IO) {
        dao.insertClass(
            ClassEntity(
                name = name,
                subject = subject,
                grade = grade,
                room = room,
                schedule = schedule,
                colorHex = colorHex
            )
        )
    }

    suspend fun deleteClass(classEntity: ClassEntity) = withContext(Dispatchers.IO) {
        dao.deleteClass(classEntity)
    }

    // Students with Attendance Stats
    fun getStudentsWithStatsForClassFlow(classId: Long): Flow<List<StudentWithStats>> {
        return dao.getStudentsByClassFlow(classId).map { students ->
            withContext(Dispatchers.IO) {
                students.map { student ->
                    val entries = dao.getEntriesForStudent(student.id)
                    val total = entries.size
                    val present = entries.count { it.status == AttendanceStatus.PRESENT }
                    val late = entries.count { it.status == AttendanceStatus.LATE }
                    val absent = entries.count { it.status == AttendanceStatus.ABSENT }
                    val rate = if (total > 0) {
                        ((present + late).toFloat() / total.toFloat()) * 100f
                    } else 100f

                    StudentWithStats(
                        student = student,
                        totalDays = total,
                        presentCount = present,
                        lateCount = late,
                        absentCount = absent,
                        attendanceRate = rate
                    )
                }
            }
        }
    }

    suspend fun getStudentById(studentId: Long): StudentEntity? = withContext(Dispatchers.IO) {
        dao.getStudentById(studentId)
    }

    suspend fun saveStudent(
        id: Long = 0,
        classId: Long,
        studentId: String,
        firstName: String,
        lastName: String,
        email: String,
        guardianName: String,
        guardianContact: String,
        avatarColorHex: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val student = StudentEntity(
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
        if (id == 0L) {
            dao.insertStudent(student)
        } else {
            dao.updateStudent(student)
            id
        }
    }

    suspend fun deleteStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        dao.deleteStudent(student)
    }

    // Take Attendance
    suspend fun getAttendanceForClassAndDate(
        classId: Long,
        dateIso: String
    ): Pair<AttendanceSessionEntity?, Map<Long, AttendanceStatus>> = withContext(Dispatchers.IO) {
        val session = dao.getSessionByDate(classId, dateIso)
        val map = mutableMapOf<Long, AttendanceStatus>()
        if (session != null) {
            val entries = dao.getEntriesForSession(session.id)
            for (entry in entries) {
                map[entry.studentId] = entry.status
            }
        }
        Pair(session, map)
    }

    suspend fun submitAttendance(
        classId: Long,
        dateIso: String,
        notes: String,
        marks: Map<Long, AttendanceStatus>
    ): Long = withContext(Dispatchers.IO) {
        val session = AttendanceSessionEntity(
            classId = classId,
            dateString = dateIso,
            notes = notes
        )
        val entries = marks.map { (studentId, status) ->
            AttendanceEntryEntity(
                sessionId = 0,
                studentId = studentId,
                status = status
            )
        }
        dao.saveSessionWithEntries(session, entries)
    }

    // Attendance History & Calendar
    suspend fun getHistorySummaryForDate(
        classId: Long?,
        dateIso: String
    ): DayAttendanceSummary? = withContext(Dispatchers.IO) {
        val sessions = if (classId != null && classId > 0) {
            listOfNotNull(dao.getSessionByDate(classId, dateIso))
        } else {
            dao.getSessionsForDate(dateIso)
        }

        if (sessions.isEmpty()) return@withContext null

        val allEntries = mutableListOf<AttendanceEntryEntity>()
        for (s in sessions) {
            allEntries.addAll(dao.getEntriesForSession(s.id))
        }

        if (allEntries.isEmpty()) return@withContext null

        val present = allEntries.count { it.status == AttendanceStatus.PRESENT }
        val late = allEntries.count { it.status == AttendanceStatus.LATE }
        val absent = allEntries.count { it.status == AttendanceStatus.ABSENT }
        val rate = ((present + late).toFloat() / allEntries.size.toFloat()) * 100f

        val studentDetails = mutableListOf<DayStudentStatus>()
        for (e in allEntries) {
            val st = dao.getStudentById(e.studentId)
            if (st != null) {
                studentDetails.add(DayStudentStatus(student = st, status = e.status, remark = e.remark))
            }
        }

        DayAttendanceSummary(
            dateString = dateIso,
            totalStudents = allEntries.size,
            presentCount = present,
            lateCount = late,
            absentCount = absent,
            attendancePercentage = rate,
            studentRecords = studentDetails.sortedBy { it.student.lastName }
        )
    }

    suspend fun getMonthAttendanceRates(
        classId: Long?,
        year: Int,
        monthIndex: Int // 0-based: 0=Jan, 9=Oct
    ): Map<String, Float> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val result = mutableMapOf<String, Float>()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sessions = if (classId != null && classId > 0) {
            dao.getSessionsForClass(classId)
        } else {
            dao.getAllSessions()
        }

        val sessionsByDate = sessions.groupBy { it.dateString }

        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = format.format(cal.time)
            val daySessions = sessionsByDate[dateStr]
            if (!daySessions.isNullOrEmpty()) {
                val entries = mutableListOf<AttendanceEntryEntity>()
                for (s in daySessions) {
                    entries.addAll(dao.getEntriesForSession(s.id))
                }
                if (entries.isNotEmpty()) {
                    val present = entries.count { it.status == AttendanceStatus.PRESENT }
                    val late = entries.count { it.status == AttendanceStatus.LATE }
                    val rate = ((present + late).toFloat() / entries.size.toFloat()) * 100f
                    result[dateStr] = rate
                }
            }
        }
        result
    }

    // Student Profile Data
    suspend fun getStudentProfileData(studentId: Long): Triple<StudentEntity?, StudentWithStats?, Pair<List<TrendPoint>, List<StudentAttendanceRecord>>> = withContext(Dispatchers.IO) {
        val student = dao.getStudentById(studentId) ?: return@withContext Triple(null, null, Pair(emptyList(), emptyList()))
        val entries = dao.getEntriesForStudent(studentId)
        val sessions = dao.getAllSessions().associateBy { it.id }

        val records = entries.mapNotNull { entry ->
            val session = sessions[entry.sessionId]
            if (session != null) {
                StudentAttendanceRecord(
                    dateString = session.dateString,
                    status = entry.status,
                    remark = entry.remark,
                    timestamp = session.timestamp
                )
            } else null
        }.sortedByDescending { it.dateString }

        val total = entries.size
        val present = entries.count { it.status == AttendanceStatus.PRESENT }
        val late = entries.count { it.status == AttendanceStatus.LATE }
        val absent = entries.count { it.status == AttendanceStatus.ABSENT }
        val rate = if (total > 0) ((present + late).toFloat() / total.toFloat()) * 100f else 100f

        val stats = StudentWithStats(
            student = student,
            totalDays = total,
            presentCount = present,
            lateCount = late,
            absentCount = absent,
            attendanceRate = rate
        )

        // Generate trend points by week / group of days
        val trendPoints = mutableListOf<TrendPoint>()
        if (records.isNotEmpty()) {
            val chronological = records.reversed()
            val chunkSize = (chronological.size / 5).coerceAtLeast(1)
            chronological.chunked(chunkSize).takeLast(6).forEachIndexed { index, chunk ->
                val chunkPresent = chunk.count { it.status != AttendanceStatus.ABSENT }
                val chunkRate = (chunkPresent.toFloat() / chunk.size.toFloat()) * 100f
                val label = try {
                    val firstDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(chunk.first().dateString)
                    SimpleDateFormat("MM/dd", Locale.getDefault()).format(firstDate ?: Date())
                } catch (_: Exception) {
                    "W${index + 1}"
                }
                trendPoints.add(TrendPoint(label = label, percentage = chunkRate))
            }
        }

        Triple(student, stats, Pair(trendPoints, records))
    }

    // Reports & CSV Export
    suspend fun generateCsvReport(classId: Long?, rangeFilter: String): File = withContext(Dispatchers.IO) {
        val classes = if (classId != null && classId > 0) {
            listOfNotNull(dao.getClassById(classId))
        } else {
            dao.getAllClasses()
        }

        val csvBuilder = StringBuilder()
        csvBuilder.append("ClassRoll Attendance Report\n")
        csvBuilder.append("Generated Date: ${DateUtils.todayIso()}\n")
        csvBuilder.append("Filter: $rangeFilter\n\n")

        for (clazz in classes) {
            csvBuilder.append("CLASS: ${clazz.name} (${clazz.room})\n")
            csvBuilder.append("Student ID,Student Name,Email,Total Days,Present,Late,Absent,Attendance Rate (%)\n")

            val students = dao.getStudentsByClass(clazz.id)
            for (st in students) {
                val entries = dao.getEntriesForStudent(st.id)
                val total = entries.size
                val present = entries.count { it.status == AttendanceStatus.PRESENT }
                val late = entries.count { it.status == AttendanceStatus.LATE }
                val absent = entries.count { it.status == AttendanceStatus.ABSENT }
                val rate = if (total > 0) {
                    String.format(Locale.US, "%.1f", ((present + late).toFloat() / total.toFloat()) * 100f)
                } else "100.0"

                csvBuilder.append("\"${st.studentId}\",\"${st.fullName}\",\"${st.email}\",$total,$present,$late,$absent,$rate%\n")
            }
            csvBuilder.append("\n")
        }

        val cacheFile = File(context.cacheDir, "ClassRoll_Report_${System.currentTimeMillis()}.csv")
        cacheFile.writeText(csvBuilder.toString())
        cacheFile
    }
}
