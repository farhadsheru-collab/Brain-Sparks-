package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameFeedbackManager
import com.example.data.local.BrainDatabase
import com.example.data.model.ActivityRecord
import com.example.data.model.DailyReward
import com.example.data.model.GameCategory
import com.example.data.model.GameProgress
import com.example.data.model.PerkCategory
import com.example.data.model.PerkDefinition
import com.example.data.model.UnlockedPerk
import com.example.data.model.UserStats
import com.example.data.repository.BrainRepository
import com.example.engine.ActiveGameLevel
import com.example.engine.GameQuestion
import com.example.engine.InfiniteLevelGenerator
import com.example.engine.PowerUpInventory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GamePlayUiState(
    val category: GameCategory? = null,
    val levelNumber: Int = 1,
    val currentLevelData: ActiveGameLevel? = null,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val maxComboStreak: Int = 0,
    val correctAnswersCount: Int = 0,
    val totalCoinsEarnedThisSession: Int = 0,
    val timeRemainingSeconds: Int = 10,
    val isTimerPaused: Boolean = false,
    val isAnswerLocked: Boolean = false,
    val selectedOptionIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val eliminatedOptionIndices: Set<Int> = emptySet(),
    val hintText: String? = null,
    val isLevelCompleted: Boolean = false,
    val isLevelFailed: Boolean = false,
    val matrixRevealedState: Boolean = false,
    val matrixSelectedIndices: List<Int> = emptyList(),
    val matrixTappedIndices: Set<Int> = emptySet()
)

val CATALOG_PERKS = listOf(
    // Powerups
    PerkDefinition(
        id = "TIME_FREEZE",
        name = "Chronos Freeze",
        description = "Gives +5 bonus seconds when timer drops low in games.",
        category = PerkCategory.IN_GAME_POWERUP,
        iconName = "hourglass_top",
        baseCost = 60,
        maxLevel = 5
    ),
    PerkDefinition(
        id = "FIFTY_FIFTY",
        name = "Oracle 50:50",
        description = "Instantly eliminates 2 incorrect options in multiple choice rounds.",
        category = PerkCategory.IN_GAME_POWERUP,
        iconName = "auto_awesome",
        baseCost = 75,
        maxLevel = 5
    ),
    PerkDefinition(
        id = "STREAK_SHIELD",
        name = "Aegis Shield",
        description = "Saves your combo streak on a single mistake per run.",
        category = PerkCategory.IN_GAME_POWERUP,
        iconName = "shield",
        baseCost = 100,
        maxLevel = 5
    ),
    PerkDefinition(
        id = "COIN_MAGNET",
        name = "Coin Magnet",
        description = "Increases all coin rewards earned from infinite levels by +50% per level.",
        category = PerkCategory.IN_GAME_POWERUP,
        iconName = "monetization_on",
        baseCost = 120,
        maxLevel = 5
    ),
    PerkDefinition(
        id = "SYNAPSE_HINT",
        name = "Synapse Clue",
        description = "Reveals direct hints and logic breakdowns on tough questions.",
        category = PerkCategory.IN_GAME_POWERUP,
        iconName = "lightbulb",
        baseCost = 50,
        maxLevel = 5
    ),
    PerkDefinition(
        id = "MEMORY_ECHO",
        name = "Memory Echo",
        description = "Allows replaying the pattern flash animation in Memory Matrix.",
        category = PerkCategory.IN_GAME_POWERUP,
        iconName = "replay",
        baseCost = 80,
        maxLevel = 5
    ),
    // Themes
    PerkDefinition(
        id = "THEME_CYBER",
        name = "Cyberpunk Glow",
        description = "Electric neon cyan & ultraviolet laser palette.",
        category = PerkCategory.THEME,
        iconName = "palette",
        baseCost = 0,
        isCosmetic = true
    ),
    PerkDefinition(
        id = "THEME_NEBULA",
        name = "Cosmic Nebula",
        description = "Starlight indigo, teal & deep galactic magenta.",
        category = PerkCategory.THEME,
        iconName = "palette",
        baseCost = 150,
        isCosmetic = true
    ),
    PerkDefinition(
        id = "THEME_GOLD",
        name = "Golden Mastermind",
        description = "Luxurious royal amber gold and obsidian black.",
        category = PerkCategory.THEME,
        iconName = "palette",
        baseCost = 250,
        isCosmetic = true
    ),
    PerkDefinition(
        id = "THEME_ZEN",
        name = "Emerald Zen",
        description = "Calming jade green and soothing nature tones.",
        category = PerkCategory.THEME,
        iconName = "palette",
        baseCost = 200,
        isCosmetic = true
    ),
    PerkDefinition(
        id = "THEME_ARCADE",
        name = "80s Synthwave",
        description = "Retro arcade hot pink, sunset orange and deep violet.",
        category = PerkCategory.THEME,
        iconName = "palette",
        baseCost = 300,
        isCosmetic = true
    ),
    // Avatars
    PerkDefinition(
        id = "AVATAR_SPARK",
        name = "Neuron Spark",
        description = "Initial brain ignition avatar.",
        category = PerkCategory.AVATAR,
        iconName = "face",
        baseCost = 0,
        isCosmetic = true
    ),
    PerkDefinition(
        id = "AVATAR_CORTEX",
        name = "Cortex Knight",
        description = "Shielded brain warrior badge.",
        category = PerkCategory.AVATAR,
        iconName = "military_tech",
        baseCost = 120,
        isCosmetic = true
    ),
    PerkDefinition(
        id = "AVATAR_QUANTUM",
        name = "Quantum Sage",
        description = "Transcendent multi-dimensional mind avatar.",
        category = PerkCategory.AVATAR,
        iconName = "psychology",
        baseCost = 350,
        isCosmetic = true
    )
)

class BrainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrainRepository
    val feedbackManager: GameFeedbackManager

    val userStats: StateFlow<UserStats?>
    val allGameProgress: StateFlow<List<GameProgress>>
    val unlockedPerks: StateFlow<List<UnlockedPerk>>
    val dailyReward: StateFlow<DailyReward?>
    val recentActivity: StateFlow<List<ActivityRecord>>

    private val _gamePlayState = MutableStateFlow(GamePlayUiState())
    val gamePlayState: StateFlow<GamePlayUiState> = _gamePlayState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var timerJob: Job? = null

    init {
        val db = BrainDatabase.getDatabase(application)
        repository = BrainRepository(db.brainDao())
        feedbackManager = GameFeedbackManager(application)

        userStats = repository.userStats
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allGameProgress = repository.allGameProgress
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        unlockedPerks = repository.unlockedPerks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        dailyReward = repository.dailyReward
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        recentActivity = repository.recentActivity
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initial setup
        viewModelScope.launch {
            repository.getOrCreateUserStats()
            // Auto unlock default free theme & avatar
            if (unlockedPerks.value.none { it.perkId == "THEME_CYBER" }) {
                repository.unlockOrUpgradePerk("THEME_CYBER", 0)
            }
            if (unlockedPerks.value.none { it.perkId == "AVATAR_SPARK" }) {
                repository.unlockOrUpgradePerk("AVATAR_SPARK", 0)
            }
        }
    }

    fun getPowerUpInventory(): PowerUpInventory {
        val perks = unlockedPerks.value
        val freezeLvl = perks.find { it.perkId == "TIME_FREEZE" }?.level ?: 1
        val fiftyLvl = perks.find { it.perkId == "FIFTY_FIFTY" }?.level ?: 1
        val shieldLvl = perks.find { it.perkId == "STREAK_SHIELD" }?.level ?: 1
        val hintLvl = perks.find { it.perkId == "SYNAPSE_HINT" }?.level ?: 1
        val echoLvl = perks.find { it.perkId == "MEMORY_ECHO" }?.level ?: 1
        val magnetLvl = perks.find { it.perkId == "COIN_MAGNET" }?.level ?: 0

        return PowerUpInventory(
            timeFreezeCount = freezeLvl * 2,
            fiftyFiftyCount = fiftyLvl * 2,
            streakShieldCount = shieldLvl,
            hintCount = hintLvl * 2,
            memoryEchoCount = echoLvl * 2,
            coinMultiplier = 1.0f + (magnetLvl * 0.5f)
        )
    }

    fun startLevel(category: GameCategory, requestedLevel: Int? = null) {
        viewModelScope.launch {
            val progress = repository.getGameProgress(category.name)
            val levelToPlay = requestedLevel ?: progress.currentLevel
            val levelData = InfiniteLevelGenerator.generateLevel(category, levelToPlay)

            _gamePlayState.value = GamePlayUiState(
                category = category,
                levelNumber = levelToPlay,
                currentLevelData = levelData,
                currentQuestionIndex = 0,
                score = 0,
                comboStreak = 0,
                maxComboStreak = 0,
                correctAnswersCount = 0,
                totalCoinsEarnedThisSession = 0,
                timeRemainingSeconds = levelData.questions.firstOrNull()?.timeLimitSeconds ?: 12,
                isTimerPaused = false,
                isAnswerLocked = false
            )

            prepareCurrentQuestion()
        }
    }

    private fun prepareCurrentQuestion() {
        val state = _gamePlayState.value
        val levelData = state.currentLevelData ?: return
        if (state.currentQuestionIndex >= levelData.questions.size) {
            finishGameSession(isVictory = true)
            return
        }

        val question = levelData.questions[state.currentQuestionIndex]
        _gamePlayState.value = state.copy(
            timeRemainingSeconds = question.timeLimitSeconds,
            isTimerPaused = false,
            isAnswerLocked = false,
            selectedOptionIndex = null,
            isCorrect = null,
            eliminatedOptionIndices = emptySet(),
            hintText = null,
            matrixRevealedState = question.visualType == com.example.engine.VisualType.MATRIX_GRID,
            matrixTappedIndices = emptySet()
        )

        // If matrix grid, show pattern flash then hide
        if (question.visualType == com.example.engine.VisualType.MATRIX_GRID) {
            val displayDuration = (question.metadata["displayDurationMs"] as? Long) ?: 2000L
            viewModelScope.launch {
                delay(displayDuration)
                _gamePlayState.value = _gamePlayState.value.copy(matrixRevealedState = false)
                startTimer()
            }
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gamePlayState.value.timeRemainingSeconds > 0 && !_gamePlayState.value.isAnswerLocked) {
                delay(1000)
                if (!_gamePlayState.value.isTimerPaused && !_gamePlayState.value.isAnswerLocked) {
                    val newTime = _gamePlayState.value.timeRemainingSeconds - 1
                    _gamePlayState.value = _gamePlayState.value.copy(timeRemainingSeconds = newTime)
                    if (newTime <= 0) {
                        onTimeExpired()
                    }
                }
            }
        }
    }

    private fun onTimeExpired() {
        feedbackManager.playWrong()
        submitAnswer(-1) // Timeout counts as wrong
    }

    fun submitAnswer(optionIndex: Int) {
        if (_gamePlayState.value.isAnswerLocked) return
        timerJob?.cancel()

        val state = _gamePlayState.value
        val levelData = state.currentLevelData ?: return
        val question = levelData.questions.getOrNull(state.currentQuestionIndex) ?: return

        val isRight = (optionIndex == question.correctIndex)
        val newCombo = if (isRight) state.comboStreak + 1 else 0
        val comboBonus = if (isRight) newCombo * 15 else 0
        val timeBonus = if (isRight) state.timeRemainingSeconds * 5 else 0
        val pointsEarned = if (isRight) (100 + comboBonus + timeBonus) else 0

        // Coin reward
        val inventory = getPowerUpInventory()
        val coinsForAnswer = if (isRight) {
            ((5 + newCombo * 2) * inventory.coinMultiplier).toInt()
        } else 0

        if (isRight) {
            if (newCombo > 2) feedbackManager.playCombo(newCombo) else feedbackManager.playCorrect()
        } else {
            feedbackManager.playWrong()
        }

        _gamePlayState.value = state.copy(
            isAnswerLocked = true,
            selectedOptionIndex = optionIndex,
            isCorrect = isRight,
            score = state.score + pointsEarned,
            comboStreak = newCombo,
            maxComboStreak = maxOf(state.maxComboStreak, newCombo),
            correctAnswersCount = if (isRight) state.correctAnswersCount + 1 else state.correctAnswersCount,
            totalCoinsEarnedThisSession = state.totalCoinsEarnedThisSession + coinsForAnswer
        )

        viewModelScope.launch {
            delay(1300)
            val nextIndex = _gamePlayState.value.currentQuestionIndex + 1
            if (nextIndex < levelData.questions.size) {
                _gamePlayState.value = _gamePlayState.value.copy(currentQuestionIndex = nextIndex)
                prepareCurrentQuestion()
            } else {
                val totalQ = levelData.questions.size
                val correctQ = _gamePlayState.value.correctAnswersCount
                val accuracy = (correctQ.toFloat() / totalQ.toFloat())
                val isVictory = accuracy >= levelData.targetAccuracyForClear
                finishGameSession(isVictory = isVictory)
            }
        }
    }

    fun onMatrixCellTapped(cellIndex: Int) {
        val state = _gamePlayState.value
        if (state.isAnswerLocked || state.matrixRevealedState) return

        val question = state.currentLevelData?.questions?.getOrNull(state.currentQuestionIndex) ?: return
        @Suppress("UNCHECKED_CAST")
        val targetIndices = (question.metadata["targetIndices"] as? List<Int>) ?: emptyList()

        val currentTapped = state.matrixTappedIndices.toMutableSet()
        if (currentTapped.contains(cellIndex)) return
        currentTapped.add(cellIndex)

        feedbackManager.playClick()
        _gamePlayState.value = state.copy(matrixTappedIndices = currentTapped)

        // If player found all targets
        if (currentTapped.containsAll(targetIndices)) {
            submitAnswer(0) // Correct
        } else if (currentTapped.size >= targetIndices.size || !targetIndices.contains(cellIndex)) {
            // Tapped a wrong cell or ran out of attempts
            submitAnswer(-1)
        }
    }

    private fun finishGameSession(isVictory: Boolean) {
        val state = _gamePlayState.value
        val category = state.category ?: return
        val levelData = state.currentLevelData ?: return
        val totalQ = levelData.questions.size
        val correctQ = state.correctAnswersCount
        val accuracyPct = if (totalQ > 0) ((correctQ * 100) / totalQ) else 0

        val baseClearBonus = if (isVictory) (levelData.baseCoinReward * getPowerUpInventory().coinMultiplier).toInt() else 10
        val finalCoins = state.totalCoinsEarnedThisSession + baseClearBonus
        val stars = if (accuracyPct >= 90) 3 else if (accuracyPct >= 70) 2 else if (isVictory) 1 else 0

        if (isVictory) {
            feedbackManager.playLevelSuccess()
        }

        viewModelScope.launch {
            repository.addCoins(finalCoins)

            val nextLvl = if (isVictory) state.levelNumber + 1 else state.levelNumber
            repository.updateGameProgress(
                gameType = category.name,
                newLevel = nextLvl,
                score = state.score,
                streak = state.maxComboStreak,
                starsEarned = stars
            )

            // Update user skill scores
            val stats = repository.getOrCreateUserStats()
            val skillDelta = if (isVictory) 3 else 1
            val updatedStats = when (category) {
                GameCategory.SPEED_MATH -> stats.copy(mathSkillScore = minOf(100, stats.mathSkillScore + skillDelta))
                GameCategory.PATTERN_MATRIX -> stats.copy(memorySkillScore = minOf(100, stats.memorySkillScore + skillDelta))
                GameCategory.STROOP_FOCUS -> stats.copy(focusSkillScore = minOf(100, stats.focusSkillScore + skillDelta))
                GameCategory.LOGIC_RIDDLES -> stats.copy(logicSkillScore = minOf(100, stats.logicSkillScore + skillDelta))
                GameCategory.WORD_SPARKS -> stats.copy(verbalSkillScore = minOf(100, stats.verbalSkillScore + skillDelta))
            }
            repository.saveUserStats(
                updatedStats.copy(
                    totalQuestionsSolved = updatedStats.totalQuestionsSolved + correctQ,
                    highestStreak = maxOf(updatedStats.highestStreak, state.maxComboStreak),
                    totalXp = updatedStats.totalXp + (state.score / 10)
                )
            )

            repository.recordGameSession(
                gameType = category.title,
                score = state.score,
                coinsEarned = finalCoins,
                accuracy = accuracyPct,
                levelReached = state.levelNumber
            )

            _gamePlayState.value = state.copy(
                isLevelCompleted = isVictory,
                isLevelFailed = !isVictory,
                totalCoinsEarnedThisSession = finalCoins
            )
        }
    }

    // PowerUp Actions
    fun useTimeFreeze() {
        val state = _gamePlayState.value
        if (state.isAnswerLocked) return
        feedbackManager.playPowerUpUsed()
        _gamePlayState.value = state.copy(timeRemainingSeconds = state.timeRemainingSeconds + 5)
        _toastMessage.value = "⏳ Chronos Freeze activated! +5s added!"
    }

    fun useFiftyFifty() {
        val state = _gamePlayState.value
        val question = state.currentLevelData?.questions?.getOrNull(state.currentQuestionIndex) ?: return
        if (state.isAnswerLocked || state.eliminatedOptionIndices.isNotEmpty() || question.options.size <= 2) return

        feedbackManager.playPowerUpUsed()
        val wrongIndices = question.options.indices.filter { it != question.correctIndex }.shuffled().take(2)
        _gamePlayState.value = state.copy(eliminatedOptionIndices = wrongIndices.toSet())
        _toastMessage.value = "💡 50:50 Oracle eliminated 2 wrong options!"
    }

    fun useHint() {
        val state = _gamePlayState.value
        val question = state.currentLevelData?.questions?.getOrNull(state.currentQuestionIndex) ?: return
        feedbackManager.playPowerUpUsed()
        val clue = question.explanation.ifEmpty { "Focus on eliminating unlikely answers and observing patterns!" }
        _gamePlayState.value = state.copy(hintText = clue)
        _toastMessage.value = "🧠 Clue revealed: $clue"
    }

    fun useMemoryEcho() {
        val state = _gamePlayState.value
        val question = state.currentLevelData?.questions?.getOrNull(state.currentQuestionIndex) ?: return
        if (question.visualType != com.example.engine.VisualType.MATRIX_GRID) return

        feedbackManager.playPowerUpUsed()
        viewModelScope.launch {
            _gamePlayState.value = state.copy(matrixRevealedState = true)
            delay(1800)
            _gamePlayState.value = _gamePlayState.value.copy(matrixRevealedState = false)
        }
    }

    fun buyOrUpgradePerk(perk: PerkDefinition) {
        val currentPerk = unlockedPerks.value.find { it.perkId == perk.id }
        val currentLevel = currentPerk?.level ?: 0
        if (currentLevel >= perk.maxLevel) {
            _toastMessage.value = "${perk.name} is already at Max Level!"
            return
        }

        val cost = perk.costForLevel(currentLevel + 1)
        viewModelScope.launch {
            val success = repository.unlockOrUpgradePerk(perk.id, cost)
            if (success) {
                feedbackManager.playLevelSuccess()
                _toastMessage.value = "🎉 Successfully unlocked ${perk.name}!"
            } else {
                feedbackManager.playWrong()
                _toastMessage.value = "⚠️ Not enough coins! Play infinite levels to earn more."
            }
        }
    }

    fun equipTheme(themeId: String) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            repository.saveUserStats(stats.copy(equippedTheme = themeId))
            feedbackManager.playClick()
            _toastMessage.value = "🎨 Theme applied!"
        }
    }

    fun equipAvatar(avatarId: String, title: String) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            repository.saveUserStats(stats.copy(equippedAvatar = avatarId, currentTitle = title))
            feedbackManager.playClick()
            _toastMessage.value = "👑 Avatar & Title equipped!"
        }
    }

    fun claimDailyGift() {
        viewModelScope.launch {
            val rewardCoins = repository.claimDailyReward()
            if (rewardCoins > 0) {
                feedbackManager.playLevelSuccess()
                _toastMessage.value = "🎁 Claimed Daily Gift: +$rewardCoins Coins!"
            } else {
                _toastMessage.value = "⏳ Daily gift already claimed today! Check back tomorrow."
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun closeGameScreen() {
        timerJob?.cancel()
        _gamePlayState.value = GamePlayUiState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        feedbackManager.release()
    }
}
