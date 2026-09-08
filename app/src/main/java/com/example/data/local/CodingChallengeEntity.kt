package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coding_challenges")
data class CodingChallengeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val track: String, // "Advanced Algorithms", "Systems & Cryptography", "Concurrency & Parallelism", "Dynamic Programming"
    val difficulty: String, // "Hard", "S-Rank Master", "Forbidden / ANBU"
    val language: String, // "Kotlin", "Python", "Rust", "Go"
    val description: String,
    val timeComplexityTarget: String, // e.g. "O((V + E) log V)"
    val spaceComplexityTarget: String, // e.g. "O(V)"
    val initialCode: String,
    val userCode: String,
    val solutionTemplate: String,
    val testCasesJson: String, // JSON array of test cases
    val isCompleted: Boolean = false,
    val executionOutput: String = "",
    val aiReviewOutput: String = "",
    val xpReward: Int = 150,
    val tags: String = "",
    val isOnlineGenerated: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
