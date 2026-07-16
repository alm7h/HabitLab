package com.hudnah.habitlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table: streak (TWP §4)
 * Persistierte Aktivitätsserie je Habit (1:1). Wird nach jedem Eintrag aktualisiert.
 */
@Entity(
    tableName = "streak",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"], unique = true)
    ]
)
data class Streak(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    /** Letzter aktiver Tag (Epoch-Day). */
    val lastActiveDate: Long? = null,
    /** Letzte Aktualisierung (Epoch ms). */
    val updatedAt: Long = System.currentTimeMillis()
)
