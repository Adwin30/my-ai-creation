package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val rank: String = "A-Rank", // "S-Rank", "A-Rank", "B-Rank", "C-Rank"
    val category: String = "Mission", // "Mission", "Work", "Dev", "Intel"
    val priority: String = "High", // "High", "Medium", "Low"
    val recurrence: String = "None", // "None", "Daily", "Weekly", "Monthly"
    val reminderTime: String = "", // e.g. "Today 18:00", "09:00 AM", "Tomorrow 10:00"
    val hasReminder: Boolean = false,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val isEncrypted: Boolean = true,
    val syncStatus: String = "synced", // "synced", "pending", "merged"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
