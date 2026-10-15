package com.example.data

import com.example.data.dao.ClassRollDao
import com.example.data.model.AttendanceEntryEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassEntity
import com.example.data.model.DateUtils
import com.example.data.model.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random

object SampleDataSeeder {

    suspend fun seedIfEmpty(dao: ClassRollDao) = withContext(Dispatchers.IO) {
        val existingCount = dao.getClassCount()
        if (existingCount > 0) return@withContext

        // 1. Create Classes
        val class1 = ClassEntity(
            name = "Biology 101 (Period 2)",
            subject = "Life Science",
            grade = "Grade 10",
            room = "Room 204",
            schedule = "Mon, Wed, Fri • 9:00 AM",
            colorHex = "#2563EB"
        )
        val class2 = ClassEntity(
            name = "AP Calculus AB (Period 4)",
            subject = "Mathematics",
            grade = "Grade 11",
            room = "Room 310",
            schedule = "Daily • 11:15 AM",
            colorHex = "#0D9488"
        )
        val class3 = ClassEntity(
            name = "Intro to Computer Science",
            subject = "Technology",
            grade = "Grade 9-12",
            room = "Lab 12",
            schedule = "Tue, Thu • 1:30 PM",
            colorHex = "#7C3AED"
        )

        val c1Id = dao.insertClass(class1)
        val c2Id = dao.insertClass(class2)
        val c3Id = dao.insertClass(class3)

        // 2. Create Students for Class 1 (Biology)
        val bioStudents = listOf(
            StudentEntity(classId = c1Id, studentId = "BIO-101", firstName = "Alexander", lastName = "Chen", email = "a.chen@school.edu", guardianName = "David Chen", guardianContact = "+1 (555) 234-5678", avatarColorHex = "#2563EB", notes = "Lab group leader"),
            StudentEntity(classId = c1Id, studentId = "BIO-102", firstName = "Sophia", lastName = "Rodriguez", email = "s.rodriguez@school.edu", guardianName = "Elena Rodriguez", guardianContact = "+1 (555) 345-6789", avatarColorHex = "#EC4899", notes = "Honor roll candidate"),
            StudentEntity(classId = c1Id, studentId = "BIO-103", firstName = "Marcus", lastName = "Johnson", email = "m.johnson@school.edu", guardianName = "Alicia Johnson", guardianContact = "+1 (555) 456-7890", avatarColorHex = "#10B981", notes = "Track team captain"),
            StudentEntity(classId = c1Id, studentId = "BIO-104", firstName = "Emma", lastName = "Watson", email = "e.watson@school.edu", guardianName = "Robert Watson", guardianContact = "+1 (555) 567-8901", avatarColorHex = "#8B5CF6", notes = "Science olympiad participant"),
            StudentEntity(classId = c1Id, studentId = "BIO-105", firstName = "Liam", lastName = "Davis", email = "l.davis@school.edu", guardianName = "Karen Davis", guardianContact = "+1 (555) 678-9012", avatarColorHex = "#F59E0B", notes = "Prefers front-row seating"),
            StudentEntity(classId = c1Id, studentId = "BIO-106", firstName = "Olivia", lastName = "Kim", email = "o.kim@school.edu", guardianName = "Joon Kim", guardianContact = "+1 (555) 789-0123", avatarColorHex = "#06B6D4", notes = "Band practice on Wednesdays"),
            StudentEntity(classId = c1Id, studentId = "BIO-107", firstName = "Noah", lastName = "Patel", email = "n.patel@school.edu", guardianName = "Sanjay Patel", guardianContact = "+1 (555) 890-1234", avatarColorHex = "#EF4444", notes = "Needs allergy awareness during labs"),
            StudentEntity(classId = c1Id, studentId = "BIO-108", firstName = "Ava", lastName = "Martinez", email = "a.martinez@school.edu", guardianName = "Maria Martinez", guardianContact = "+1 (555) 901-2345", avatarColorHex = "#3B82F6", notes = "Peer tutor volunteer"),
            StudentEntity(classId = c1Id, studentId = "BIO-109", firstName = "Lucas", lastName = "Taylor", email = "l.taylor@school.edu", guardianName = "James Taylor", guardianContact = "+1 (555) 012-3456", avatarColorHex = "#84CC16", notes = "School newspaper editor"),
            StudentEntity(classId = c1Id, studentId = "BIO-110", firstName = "Mia", lastName = "Anderson", email = "m.anderson@school.edu", guardianName = "Sarah Anderson", guardianContact = "+1 (555) 123-4567", avatarColorHex = "#F97316", notes = "Art club vice president")
        )

        // Class 2 (Calculus)
        val calcStudents = listOf(
            StudentEntity(classId = c2Id, studentId = "MTH-201", firstName = "Ethan", lastName = "Brown", email = "e.brown@school.edu", guardianName = "Paul Brown", guardianContact = "+1 (555) 222-3344", avatarColorHex = "#0D9488", notes = "Math club captain"),
            StudentEntity(classId = c2Id, studentId = "MTH-202", firstName = "Isabella", lastName = "Garcia", email = "i.garcia@school.edu", guardianName = "Carlos Garcia", guardianContact = "+1 (555) 333-4455", avatarColorHex = "#D946EF", notes = "Academic decathlon"),
            StudentEntity(classId = c2Id, studentId = "MTH-203", firstName = "James", lastName = "Miller", email = "j.miller@school.edu", guardianName = "Grace Miller", guardianContact = "+1 (555) 444-5566", avatarColorHex = "#6366F1", notes = "Robotics team"),
            StudentEntity(classId = c2Id, studentId = "MTH-204", firstName = "Charlotte", lastName = "Wilson", email = "c.wilson@school.edu", guardianName = "Thomas Wilson", guardianContact = "+1 (555) 555-6677", avatarColorHex = "#E11D48", notes = "Yearbook committee"),
            StudentEntity(classId = c2Id, studentId = "MTH-205", firstName = "Benjamin", lastName = "Moore", email = "b.moore@school.edu", guardianName = "Lisa Moore", guardianContact = "+1 (555) 666-7788", avatarColorHex = "#14B8A6", notes = "Varsity soccer")
        )

        // Class 3 (CS)
        val csStudents = listOf(
            StudentEntity(classId = c3Id, studentId = "CS-301", firstName = "Henry", lastName = "Taylor", email = "h.taylor@school.edu", guardianName = "Anne Taylor", guardianContact = "+1 (555) 777-8899", avatarColorHex = "#7C3AED", notes = "Game dev hobbyist"),
            StudentEntity(classId = c3Id, studentId = "CS-302", firstName = "Chloe", lastName = "Nguyen", email = "c.nguyen@school.edu", guardianName = "Minh Nguyen", guardianContact = "+1 (555) 888-9900", avatarColorHex = "#2563EB", notes = "Coding club lead"),
            StudentEntity(classId = c3Id, studentId = "CS-303", firstName = "Daniel", lastName = "Jackson", email = "d.jackson@school.edu", guardianName = "Ruth Jackson", guardianContact = "+1 (555) 999-0011", avatarColorHex = "#F59E0B", notes = "3D printing specialist"),
            StudentEntity(classId = c3Id, studentId = "CS-304", firstName = "Grace", lastName = "White", email = "g.white@school.edu", guardianName = "Frank White", guardianContact = "+1 (555) 111-2233", avatarColorHex = "#10B981", notes = "Cybersecurity enthusiast")
        )

        val insertedBioIds = dao.insertStudents(bioStudents)
        val insertedCalcIds = dao.insertStudents(calcStudents)
        val insertedCsIds = dao.insertStudents(csStudents)

        // 3. Generate 14 school days of historical sessions and entries up to today
        val todayIso = DateUtils.todayIso()
        val random = Random(42) // Stable seed for consistent test data

        // Pre-seed sessions for each class across the last 14 days
        val classStudentPairs = listOf(
            Triple(c1Id, bioStudents.indices.map { insertedBioIds[it] }, "Biology 101"),
            Triple(c2Id, calcStudents.indices.map { insertedCalcIds[it] }, "AP Calculus AB"),
            Triple(c3Id, csStudents.indices.map { insertedCsIds[it] }, "CS Lab")
        )

        for (daysAgo in 14 downTo 0) {
            val dateIso = DateUtils.offsetDate(todayIso, -daysAgo)

            for ((cId, studentIds, className) in classStudentPairs) {
                val sessionId = dao.insertSession(
                    AttendanceSessionEntity(
                        classId = cId,
                        dateString = dateIso,
                        notes = if (daysAgo == 0) "Today's standard roll" else "Session recorded on $dateIso"
                    )
                )

                val entries = studentIds.mapIndexed { index, studentId ->
                    // Most students are present (~85%), some late (~8%), few absent (~7%)
                    // Give a couple of students specific patterns to demonstrate at-risk and perfect attendance
                    val status = when {
                        index == 0 -> AttendanceStatus.PRESENT // Perfect attendance student
                        index == 1 && daysAgo % 3 == 0 -> AttendanceStatus.LATE
                        index == 4 && (daysAgo == 1 || daysAgo == 3 || daysAgo == 7) -> AttendanceStatus.ABSENT // At-risk
                        index == 6 && daysAgo % 4 == 0 -> AttendanceStatus.ABSENT
                        else -> {
                            val roll = random.nextInt(100)
                            when {
                                roll < 84 -> AttendanceStatus.PRESENT
                                roll < 93 -> AttendanceStatus.LATE
                                else -> AttendanceStatus.ABSENT
                            }
                        }
                    }

                    val remark = when (status) {
                        AttendanceStatus.LATE -> if (daysAgo % 2 == 0) "Traffic delay" else "Arrived 10 min late"
                        AttendanceStatus.ABSENT -> if (daysAgo % 2 == 0) "Excused illness" else "Unexcused"
                        AttendanceStatus.PRESENT -> ""
                    }

                    AttendanceEntryEntity(
                        sessionId = sessionId,
                        studentId = studentId,
                        status = status,
                        remark = remark
                    )
                }

                dao.insertEntries(entries)
            }
        }
    }
}
