package com.example.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameQuestion
import com.example.ui.theme.LocalGameColors
import com.example.viewmodel.GamePlayUiState

@Composable
fun QuestionHeaderCard(
    question: GameQuestion,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (question.subPrompt.isNotEmpty()) {
                Text(
                    text = question.subPrompt,
                    style = MaterialTheme.typography.labelSmall,
                    color = gameColors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = question.prompt,
                style = MaterialTheme.typography.displayMedium,
                color = gameColors.textPrimary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 1. Standard Multiple Choice Grid (Used for Speed Math, Logic, Anagrams)
@Composable
fun MultiChoiceGrid(
    question: GameQuestion,
    uiState: GamePlayUiState,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        question.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowOptions.forEachIndexed { colIndex, optionText ->
                    val optionIndex = rowIndex * 2 + colIndex
                    val isEliminated = uiState.eliminatedOptionIndices.contains(optionIndex)
                    val isSelected = uiState.selectedOptionIndex == optionIndex
                    val isCorrectTarget = question.correctIndex == optionIndex

                    val cardBg = when {
                        isEliminated -> gameColors.surface.copy(alpha = 0.3f)
                        isSelected && isCorrectTarget -> gameColors.success
                        isSelected && !isCorrectTarget -> gameColors.error
                        uiState.isAnswerLocked && isCorrectTarget -> gameColors.success.copy(alpha = 0.8f)
                        else -> gameColors.cardBg
                    }

                    val borderColor = when {
                        isEliminated -> Color.Transparent
                        isSelected -> Color.White
                        else -> gameColors.cardBorder
                    }

                    val scale = remember { Animatable(1f) }
                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            scale.animateTo(1.05f, spring(stiffness = Spring.StiffnessHigh))
                            scale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp)
                            .scale(scale.value)
                            .testTag("option_button_$optionIndex")
                            .clickable(
                                enabled = !uiState.isAnswerLocked && !isEliminated,
                                onClick = { onOptionSelected(optionIndex) }
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isEliminated) {
                                Text(
                                    text = "—",
                                    color = gameColors.textMuted,
                                    fontSize = 24.sp
                                )
                            } else {
                                Text(
                                    text = optionText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else gameColors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Pattern Memory Matrix View
@Composable
fun PatternMatrixView(
    question: GameQuestion,
    uiState: GamePlayUiState,
    onCellTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    val gridSize = (question.metadata["gridSize"] as? Int) ?: 3
    @Suppress("UNCHECKED_CAST")
    val targetIndices = (question.metadata["targetIndices"] as? List<Int>) ?: emptyList()
    val totalCells = gridSize * gridSize

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (uiState.matrixRevealedState) "⚡ MEMORIZE GLOWING TILES!" else "👉 Tap the tiles you memorized!",
                style = MaterialTheme.typography.titleMedium,
                color = if (uiState.matrixRevealedState) gameColors.accent else gameColors.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(totalCells) { index ->
                    val isTarget = targetIndices.contains(index)
                    val isTapped = uiState.matrixTappedIndices.contains(index)

                    val cellColor = when {
                        uiState.matrixRevealedState && isTarget -> gameColors.primary
                        isTapped && isTarget -> gameColors.success
                        isTapped && !isTarget -> gameColors.error
                        else -> gameColors.surface
                    }

                    val cellBorder = when {
                        uiState.matrixRevealedState && isTarget -> Color.White
                        isTapped -> Color.White
                        else -> gameColors.cardBorder
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cellColor)
                            .border(1.5.dp, cellBorder, RoundedCornerShape(12.dp))
                            .testTag("matrix_cell_$index")
                            .clickable(
                                enabled = !uiState.matrixRevealedState && !uiState.isAnswerLocked,
                                onClick = { onCellTapped(index) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTapped) {
                            Icon(
                                imageVector = if (isTarget) Icons.Filled.CheckCircle else Icons.Filled.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. Stroop Focus Centerpiece Card
@Composable
fun StroopFocusView(
    question: GameQuestion,
    uiState: GamePlayUiState,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    val colorHex = (question.metadata["textColorHex"] as? Long) ?: 0xFFFF3B30
    val displayColor = Color(colorHex)
    val wordText = (question.metadata["wordText"] as? String) ?: question.prompt

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stroop Word Billboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, displayColor.copy(alpha = 0.6f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = question.subPrompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = gameColors.textMuted,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = wordText,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = displayColor,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options
        MultiChoiceGrid(
            question = question,
            uiState = uiState,
            onOptionSelected = onOptionSelected
        )
    }
}
