package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index("studentId")
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val studentId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val guardianName: String = "",
    val guardianContact: String = "",
    val avatarColorHex: String = "#2563EB",
    val photoUri: String? = null,
    val notes: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName"

    val initials: String
        get() = "${firstName.take(1)}${lastName.take(1)}".uppercase()
}
