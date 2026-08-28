package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCategory
import com.example.engine.VisualType
import com.example.ui.components.ConfettiCanvas
import com.example.ui.components.PowerUpActionBar
import com.example.ui.screens.games.MultiChoiceGrid
import com.example.ui.screens.games.PatternMatrixView
import com.example.ui.screens.games.QuestionHeaderCard
import com.example.ui.screens.games.StroopFocusView
import com.example.ui.theme.LocalGameColors
import com.example.viewmodel.BrainViewModel
import com.example.viewmodel.GamePlayUiState

@Composable
fun GamePlayScreen(
    viewModel: BrainViewModel,
    uiState: GamePlayUiState,
    onExitGame: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val levelData = uiState.currentLevelData ?: return
    val currentQuestion = levelData.questions.getOrNull(uiState.currentQuestionIndex) ?: return
    val totalQuestions = levelData.questions.size

    val maxTime = currentQuestion.timeLimitSeconds.toFloat()
    val progressFloat = (uiState.timeRemainingSeconds / maxTime).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressFloat, label = "timerProgress")

    val timerColor = when {
        uiState.timeRemainingSeconds <= 3 -> gameColors.error
        uiState.timeRemainingSeconds <= 6 -> Color(0xFFFF9500)
        else -> gameColors.primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gameColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onExitGame,
                    modifier = Modifier
                        .testTag("gameplay_exit_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(gameColors.surface)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Exit Game",
                        tint = gameColors.textPrimary
                    )
                }

                // Level & Category Pill
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${levelData.gameCategoryTitle} • LVL ${levelData.levelNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = gameColors.textPrimary
                    )
                    Text(
                        text = "Question ${uiState.currentQuestionIndex + 1} of $totalQuestions",
                        style = MaterialTheme.typography.labelSmall,
                        color = gameColors.textMuted
                    )
                }

                // Combo Streak Pill
                Row(
                    modifier = Modifier
                        .testTag("gameplay_combo_pill")
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (uiState.comboStreak > 1) {
                                Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFFFF9500)))
                            } else {
                                Brush.horizontalGradient(listOf(gameColors.surface, gameColors.surface))
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = "Combo Streak",
                        tint = if (uiState.comboStreak > 1) Color.White else gameColors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.comboStreak}x",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (uiState.comboStreak > 1) Color.White else gameColors.textPrimary
                    )
                }
            }

            // Animated Timer Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Time",
                            tint = timerColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.timeRemainingSeconds}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = timerColor
                        )
                    }

                    Text(
                        text = "Score: ${uiState.score}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = gameColors.coinGold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = timerColor,
                    trackColor = gameColors.surface,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hint Banner if active
            AnimatedVisibility(visible = uiState.hintText != null) {
                uiState.hintText?.let { hint ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A200B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.coinGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = gameColors.coinGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFE082),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Question View based on visual type
            when (currentQuestion.visualType) {
                VisualType.STROOP_CARD -> {
                    StroopFocusView(
                        question = currentQuestion,
                        uiState = uiState,
                        onOptionSelected = { index -> viewModel.submitAnswer(index) }
                    )
                }
                VisualType.MATRIX_GRID -> {
                    QuestionHeaderCard(question = currentQuestion)
                    Spacer(modifier = Modifier.height(12.dp))
                    PatternMatrixView(
                        question = currentQuestion,
                        uiState = uiState,
                        onCellTapped = { cellIndex -> viewModel.onMatrixCellTapped(cellIndex) }
                    )
                }
                else -> {
                    QuestionHeaderCard(question = currentQuestion)
                    Spacer(modifier = Modifier.height(20.dp))
                    MultiChoiceGrid(
                        question = currentQuestion,
                        uiState = uiState,
                        onOptionSelected = { index -> viewModel.submitAnswer(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(16.dp))

            // Power-Up Actions Bar
            PowerUpActionBar(
                inventory = viewModel.getPowerUpInventory(),
                onUseTimeFreeze = { viewModel.useTimeFreeze() },
                onUseFiftyFifty = { viewModel.useFiftyFifty() },
                onUseHint = { viewModel.useHint() },
                onUseMemoryEcho = if (currentQuestion.visualType == VisualType.MATRIX_GRID) {
                    { viewModel.useMemoryEcho() }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Result Dialog on Level Finished / Defeat
        if (uiState.isLevelCompleted || uiState.isLevelFailed) {
            LevelResultDialog(
                isVictory = uiState.isLevelCompleted,
                levelNumber = uiState.levelNumber,
                score = uiState.score,
                correctCount = uiState.correctAnswersCount,
                totalQuestions = totalQuestions,
                coinsEarned = uiState.totalCoinsEarnedThisSession,
                maxStreak = uiState.maxComboStreak,
                onNextLevel = {
                    val cat = uiState.category ?: GameCategory.SPEED_MATH
                    viewModel.startLevel(cat, uiState.levelNumber + 1)
                },
                onRetry = {
                    val cat = uiState.category ?: GameCategory.SPEED_MATH
                    viewModel.startLevel(cat, uiState.levelNumber)
                },
                onReturnHome = onExitGame
            )
        }
    }
}

@Composable
fun LevelResultDialog(
    isVictory: Boolean,
    levelNumber: Int,
    score: Int,
    correctCount: Int,
    totalQuestions: Int,
    coinsEarned: Int,
    maxStreak: Int,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onReturnHome: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val accuracy = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
    val starCount = if (accuracy >= 90) 3 else if (accuracy >= 70) 2 else if (isVictory) 1 else 0

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(gameColors.cardBg)
                .border(2.dp, if (isVictory) gameColors.primary else gameColors.error, RoundedCornerShape(28.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isVictory) {
                ConfettiCanvas(isActive = true)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Badge
                Icon(
                    imageVector = if (isVictory) Icons.Filled.EmojiEvents else Icons.Filled.Replay,
                    contentDescription = null,
                    tint = if (isVictory) gameColors.coinGold else gameColors.error,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isVictory) "LEVEL $levelNumber CLEARED!" else "LEVEL $levelNumber FAILED",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isVictory) gameColors.primary else gameColors.error,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isVictory) "Infinite Mind Progression Unlocked!" else "Practice sharper focus to advance!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = gameColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isFilled = i <= starCount
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star $i",
                            tint = if (isFilled) gameColors.coinGold else gameColors.surface,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(gameColors.surface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResultStatColumn(label = "Score", value = "$score", color = gameColors.textPrimary)
                    ResultStatColumn(label = "Accuracy", value = "$accuracy%", color = gameColors.accent)
                    ResultStatColumn(label = "Max Streak", value = "${maxStreak}x", color = Color(0xFFFF9500))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coins Reward Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3B2A06), Color(0xFF6B4E0C))
                            )
                        )
                        .border(1.dp, gameColors.coinGold, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = "Coins",
                        tint = gameColors.coinGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+$coinsEarned Coins Earned!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = gameColors.coinGold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                if (isVictory) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .testTag("result_next_level_button")
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = gameColors.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Next Level (Lvl ${levelNumber + 1})",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                } else {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .testTag("result_retry_button")
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = gameColors.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Try Level $levelNumber Again",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Replay,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onReturnHome,
                    modifier = Modifier
                        .testTag("result_home_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        tint = gameColors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Main Hub & Shop",
                        color = gameColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultStatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
    }
}
