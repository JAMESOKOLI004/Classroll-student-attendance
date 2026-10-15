package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.ClassRollDao
import com.example.data.model.AttendanceEntryEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toAttendanceStatus(value: String?): AttendanceStatus? {
        return value?.let {
            try {
                AttendanceStatus.valueOf(it)
            } catch (_: Exception) {
                AttendanceStatus.PRESENT
            }
        }
    }
}

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceSessionEntity::class,
        AttendanceEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClassRollDatabase : RoomDatabase() {
    abstract fun classRollDao(): ClassRollDao

    companion object {
        @Volatile
        private var INSTANCE: ClassRollDatabase? = null

        fun getDatabase(context: Context): ClassRollDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClassRollDatabase::class.java,
                    "classroll_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
