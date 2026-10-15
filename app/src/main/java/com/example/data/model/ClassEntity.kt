package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val subject: String,
    val grade: String,
    val room: String,
    val schedule: String,
    val colorHex: String = "#2563EB",
    val createdAt: Long = System.currentTimeMillis()
)
