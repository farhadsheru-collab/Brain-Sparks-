package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 150, // Starter bonus coins
    val totalXp: Int = 0,
    val currentTitle: String = "Neuron Initiate",
    val equippedTheme: String = "THEME_CYBER",
    val equippedAvatar: String = "AVATAR_SPARK",
    val totalQuestionsSolved: Int = 0,
    val highestStreak: Int = 0,
    val mathSkillScore: Int = 50,
    val memorySkillScore: Int = 50,
    val focusSkillScore: Int = 50,
    val logicSkillScore: Int = 50,
    val verbalSkillScore: Int = 50
)

@Entity(tableName = "game_progress")
data class GameProgress(
    @PrimaryKey val gameType: String,
    val currentLevel: Int = 1,
    val highScore: Int = 0,
    val totalStars: Int = 0,
    val timesPlayed: Int = 0,
    val bestStreak: Int = 0
)

@Entity(tableName = "unlocked_perks")
data class UnlockedPerk(
    @PrimaryKey val perkId: String,
    val level: Int = 1,
    val isEquipped: Boolean = true,
    val unlockedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_reward")
data class DailyReward(
    @PrimaryKey val id: Int = 1,
    val lastClaimDateMillis: Long = 0L,
    val streakDays: Int = 0
)

@Entity(tableName = "activity_records")
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val gameType: String,
    val score: Int,
    val coinsEarned: Int,
    val accuracyPercentage: Int,
    val levelReached: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class GameCategory(val title: String, val description: String, val icon: String) {
    SPEED_MATH("Speed Math", "Rapid calculations & arithmetic reflexes", "calculate"),
    PATTERN_MATRIX("Memory Matrix", "Spatial sequence & visual recall", "grid_view"),
    STROOP_FOCUS("Stroop Focus", "Cognitive clash & attention speed", "psychology"),
    LOGIC_RIDDLES("Logic & Riddles", "Lateral reasoning & deductive puzzles", "lightbulb"),
    WORD_SPARKS("Word Sparks", "Anagram decoders & verbal agility", "spellcheck")
}

data class PerkDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: PerkCategory,
    val iconName: String,
    val baseCost: Int,
    val costMultiplier: Float = 1.5f,
    val maxLevel: Int = 5,
    val isCosmetic: Boolean = false
) {
    fun costForLevel(currentLevel: Int): Int {
        if (isCosmetic) return baseCost
        return (baseCost * Math.pow(costMultiplier.toDouble(), (currentLevel - 1).toDouble())).toInt()
    }
}

enum class PerkCategory {
    IN_GAME_POWERUP,
    THEME,
    AVATAR
}
