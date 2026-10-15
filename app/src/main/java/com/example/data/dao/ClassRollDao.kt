package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AttendanceEntryEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassRollDao {
    // Classes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>): List<Long>

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)

    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClassesFlow(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes ORDER BY name ASC")
    suspend fun getAllClasses(): List<ClassEntity>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Long): ClassEntity?

    @Query("SELECT COUNT(*) FROM classes")
    suspend fun getClassCount(): Int

    // Students
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>): List<Long>

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY lastName ASC, firstName ASC")
    fun getStudentsByClassFlow(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY lastName ASC, firstName ASC")
    suspend fun getStudentsByClass(classId: Long): List<StudentEntity>

    @Query("SELECT * FROM students ORDER BY lastName ASC, firstName ASC")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY lastName ASC, firstName ASC")
    suspend fun getAllStudents(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    suspend fun getStudentCountForClass(classId: Long): Int

    // Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<AttendanceSessionEntity>): List<Long>

    @Update
    suspend fun updateSession(session: AttendanceSessionEntity)

    @Delete
    suspend fun deleteSession(session: AttendanceSessionEntity)

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId AND dateString = :dateString LIMIT 1")
    suspend fun getSessionByDate(classId: Long, dateString: String): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY dateString DESC")
    suspend fun getSessionsForClass(classId: Long): List<AttendanceSessionEntity>

    @Query("SELECT * FROM attendance_sessions WHERE dateString = :dateString")
    suspend fun getSessionsForDate(dateString: String): List<AttendanceSessionEntity>

    @Query("SELECT * FROM attendance_sessions ORDER BY dateString DESC")
    suspend fun getAllSessions(): List<AttendanceSessionEntity>

    @Query("SELECT * FROM attendance_sessions ORDER BY dateString DESC")
    fun getAllSessionsFlow(): Flow<List<AttendanceSessionEntity>>

    // Entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<AttendanceEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AttendanceEntryEntity): Long

    @Query("SELECT * FROM attendance_entries WHERE sessionId = :sessionId")
    suspend fun getEntriesForSession(sessionId: Long): List<AttendanceEntryEntity>

    @Query("SELECT * FROM attendance_entries WHERE sessionId = :sessionId")
    fun getEntriesForSessionFlow(sessionId: Long): Flow<List<AttendanceEntryEntity>>

    @Query("SELECT * FROM attendance_entries WHERE studentId = :studentId")
    suspend fun getEntriesForStudent(studentId: Long): List<AttendanceEntryEntity>

    @Query("SELECT * FROM attendance_entries")
    suspend fun getAllEntries(): List<AttendanceEntryEntity>

    @Query("DELETE FROM attendance_entries WHERE sessionId = :sessionId")
    suspend fun deleteEntriesForSession(sessionId: Long)

    @Transaction
    suspend fun saveSessionWithEntries(
        session: AttendanceSessionEntity,
        entries: List<AttendanceEntryEntity>
    ): Long {
        val existing = getSessionByDate(session.classId, session.dateString)
        val targetSessionId = if (existing != null) {
            existing.id
        } else {
            insertSession(session)
        }
        deleteEntriesForSession(targetSessionId)
        val entriesWithSessionId = entries.map { it.copy(sessionId = targetSessionId) }
        insertEntries(entriesWithSessionId)
        return targetSessionId
    }
}
