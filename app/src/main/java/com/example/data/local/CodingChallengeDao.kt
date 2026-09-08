package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CodingChallengeDao {
    @Query("SELECT * FROM coding_challenges ORDER BY isCompleted ASC, difficulty DESC")
    fun getAllChallenges(): Flow<List<CodingChallengeEntity>>

    @Query("SELECT * FROM coding_challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): CodingChallengeEntity?

    @Query("SELECT * FROM coding_challenges WHERE track = :track ORDER BY isCompleted ASC")
    fun getChallengesByTrack(track: String): Flow<List<CodingChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: CodingChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChallenges(challenges: List<CodingChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: CodingChallengeEntity)

    @Query("UPDATE coding_challenges SET userCode = :userCode, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateUserCode(id: String, userCode: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE coding_challenges SET executionOutput = :output, isCompleted = :isCompleted, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateExecutionResult(id: String, output: String, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE coding_challenges SET aiReviewOutput = :aiReview, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateAiReview(id: String, aiReview: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteChallenge(challenge: CodingChallengeEntity)

    @Query("SELECT COUNT(*) FROM coding_challenges WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM coding_challenges")
    suspend fun getTotalCount(): Int
}
