package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileName: String,
    val fileType: String, // "py", "html", "css", "js", "xml", "pdf", "txt"
    val content: String,
    val filePath: String, // e.g., "D:/ItachiAI/vault/script.py" or local storage path
    val isEncrypted: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
