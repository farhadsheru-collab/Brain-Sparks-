package com.example.engine

data class GameQuestion(
    val id: String,
    val prompt: String,
    val subPrompt: String = "",
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = "",
    val timeLimitSeconds: Int = 12,
    val visualType: VisualType = VisualType.TEXT_OPTIONS,
    val metadata: Map<String, Any> = emptyMap()
)

enum class VisualType {
    TEXT_OPTIONS,
    MATH_OPERATORS,
    STROOP_CARD,
    MATRIX_GRID,
    WORD_ANAGRAM,
    LOGIC_ILLUSTRATION
}

data class StroopData(
    val displayText: String,
    val textColorHex: Long,
    val rule: StroopRule,
    val promptQuestion: String
)

enum class StroopRule {
    MATCH_INK_COLOR,
    MATCH_WORD_NAME,
    MATCH_BACKGROUND_CONFLICT
}

data class MatrixChallenge(
    val gridSize: Int,
    val targetIndices: List<Int>,
    val displayDurationMs: Long,
    val isSequential: Boolean
)

data class AnagramChallenge(
    val targetWord: String,
    val scrambledLetters: List<Char>,
    val hintCategory: String,
    val definitionHint: String
)

data class ActiveGameLevel(
    val levelNumber: Int,
    val gameCategoryTitle: String,
    val questions: List<GameQuestion>,
    val targetAccuracyForClear: Float = 0.6f,
    val baseCoinReward: Int = 30,
    val difficultyLabel: String = "Normal"
)

data class PowerUpInventory(
    val timeFreezeCount: Int = 0,
    val fiftyFiftyCount: Int = 0,
    val streakShieldCount: Int = 0,
    val hintCount: Int = 0,
    val memoryEchoCount: Int = 0,
    val coinMultiplier: Float = 1.0f
)
