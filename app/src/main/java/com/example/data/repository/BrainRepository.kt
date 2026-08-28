package com.example.data.repository

import com.example.data.local.BrainDao
import com.example.data.model.ActivityRecord
import com.example.data.model.DailyReward
import com.example.data.model.GameProgress
import com.example.data.model.UnlockedPerk
import com.example.data.model.UserStats
import kotlinx.coroutines.flow.Flow

class BrainRepository(private val brainDao: BrainDao) {

    val userStats: Flow<UserStats?> = brainDao.getUserStatsFlow()
    val allGameProgress: Flow<List<GameProgress>> = brainDao.getAllGameProgressFlow()
    val unlockedPerks: Flow<List<UnlockedPerk>> = brainDao.getAllUnlockedPerksFlow()
    val dailyReward: Flow<DailyReward?> = brainDao.getDailyRewardFlow()
    val recentActivity: Flow<List<ActivityRecord>> = brainDao.getRecentActivityFlow()

    suspend fun getOrCreateUserStats(): UserStats {
        val existing = brainDao.getUserStats()
        if (existing != null) return existing
        val defaultStats = UserStats()
        brainDao.insertOrUpdateUserStats(defaultStats)
        return defaultStats
    }

    suspend fun saveUserStats(stats: UserStats) {
        brainDao.insertOrUpdateUserStats(stats)
    }

    suspend fun addCoins(amount: Int) {
        val stats = getOrCreateUserStats()
        val newCoins = (stats.coins + amount).coerceAtLeast(0)
        brainDao.insertOrUpdateUserStats(stats.copy(coins = newCoins))
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val stats = getOrCreateUserStats()
        if (stats.coins >= amount) {
            val newCoins = stats.coins - amount
            brainDao.insertOrUpdateUserStats(stats.copy(coins = newCoins))
            return true
        }
        return false
    }

    suspend fun getGameProgress(gameType: String): GameProgress {
        val existing = brainDao.getGameProgress(gameType)
        if (existing != null) return existing
        val defaultProgress = GameProgress(gameType = gameType, currentLevel = 1)
        brainDao.insertOrUpdateGameProgress(defaultProgress)
        return defaultProgress
    }

    suspend fun updateGameProgress(
        gameType: String,
        newLevel: Int,
        score: Int,
        streak: Int,
        starsEarned: Int
    ) {
        val current = getGameProgress(gameType)
        val updated = current.copy(
            currentLevel = maxOf(current.currentLevel, newLevel),
            highScore = maxOf(current.highScore, score),
            totalStars = current.totalStars + starsEarned,
            timesPlayed = current.timesPlayed + 1,
            bestStreak = maxOf(current.bestStreak, streak)
        )
        brainDao.insertOrUpdateGameProgress(updated)
    }

    suspend fun unlockOrUpgradePerk(perkId: String, cost: Int): Boolean {
        if (spendCoins(cost)) {
            val current = brainDao.getUnlockedPerk(perkId)
            val newLevel = (current?.level ?: 0) + 1
            brainDao.insertOrUpdatePerk(
                UnlockedPerk(
                    perkId = perkId,
                    level = newLevel,
                    isEquipped = true
                )
            )
            return true
        }
        return false
    }

    suspend fun recordGameSession(
        gameType: String,
        score: Int,
        coinsEarned: Int,
        accuracy: Int,
        levelReached: Int
    ) {
        brainDao.insertActivityRecord(
            ActivityRecord(
                gameType = gameType,
                score = score,
                coinsEarned = coinsEarned,
                accuracyPercentage = accuracy,
                levelReached = levelReached
            )
        )
    }

    suspend fun claimDailyReward(): Int {
        val daily = brainDao.getDailyReward() ?: DailyReward()
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        // Check if eligible
        if (now - daily.lastClaimDateMillis >= oneDayMillis || daily.lastClaimDateMillis == 0L) {
            val isConsecutive = (now - daily.lastClaimDateMillis) < (2 * oneDayMillis)
            val newStreak = if (isConsecutive && daily.lastClaimDateMillis != 0L) daily.streakDays + 1 else 1
            val rewardCoins = 100 + (newStreak * 25)

            brainDao.insertOrUpdateDailyReward(
                daily.copy(
                    lastClaimDateMillis = now,
                    streakDays = newStreak
                )
            )
            addCoins(rewardCoins)
            return rewardCoins
        }
        return 0
    }
}
