package com.hudnah.habitlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table: entry (TWP §4)
 * Ein Datensatz pro abgeschlossenem Habit-Tag.
 * UNIQUE (habitId, date) => max. ein Eintrag pro Habit und Tag.
 */
@Entity(
    tableName = "entry",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["habitId"])
    ]
)
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    /** Kalendertag als Epoch-Day (LocalDate.toEpochDay()). */
    val date: Long,
    /** Zeitstempel des Abschlusses (Epoch ms). */
    val completedAt: Long = System.currentTimeMillis(),
    val status: String = EntryStatus.DONE
)

object EntryStatus {
    const val DONE = "DONE"
}
