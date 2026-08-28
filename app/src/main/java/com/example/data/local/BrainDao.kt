package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityRecord
import com.example.data.model.DailyReward
import com.example.data.model.GameProgress
import com.example.data.model.UnlockedPerk
import com.example.data.model.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface BrainDao {
    // User Stats
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStats)

    // Game Progress
    @Query("SELECT * FROM game_progress")
    fun getAllGameProgressFlow(): Flow<List<GameProgress>>

    @Query("SELECT * FROM game_progress WHERE gameType = :gameType")
    suspend fun getGameProgress(gameType: String): GameProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameProgress(progress: GameProgress)

    // Perks & Upgrades
    @Query("SELECT * FROM unlocked_perks")
    fun getAllUnlockedPerksFlow(): Flow<List<UnlockedPerk>>

    @Query("SELECT * FROM unlocked_perks WHERE perkId = :perkId")
    suspend fun getUnlockedPerk(perkId: String): UnlockedPerk?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePerk(perk: UnlockedPerk)

    // Daily Reward
    @Query("SELECT * FROM daily_reward WHERE id = 1")
    fun getDailyRewardFlow(): Flow<DailyReward?>

    @Query("SELECT * FROM daily_reward WHERE id = 1")
    suspend fun getDailyReward(): DailyReward?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyReward(dailyReward: DailyReward)

    // Activity Log
    @Query("SELECT * FROM activity_records ORDER BY timestamp DESC LIMIT 20")
    fun getRecentActivityFlow(): Flow<List<ActivityRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityRecord(record: ActivityRecord)
}
