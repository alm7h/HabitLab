package com.hudnah.habitlab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table: habit (TWP §4)
 * Speichert die vom Nutzer definierten Gewohnheiten.
 */
@Entity(tableName = "habit")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
