package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityRecord
import com.example.data.model.UserStats
import com.example.ui.components.BrainTopBar
import com.example.ui.components.SkillRadarChart
import com.example.ui.theme.LocalGameColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BrainProfileScreen(
    userStats: UserStats?,
    activityHistory: List<ActivityRecord>,
    onBackClick: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val stats = userStats ?: UserStats()

    val bqScore = remember(stats) {
        (stats.mathSkillScore + stats.memorySkillScore + stats.focusSkillScore + stats.logicSkillScore + stats.verbalSkillScore) / 5
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gameColors.background)
    ) {
        BrainTopBar(
            title = "Cognitive Profile",
            coins = stats.coins,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Identity Header Card
            item {
                ProfileHeroCard(stats = stats, bqScore = bqScore)
            }

            // 5-Point Radar Skill Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "Cognitive Skill Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = gameColors.textPrimary
                        )
                        Text(
                            text = "Based on infinite level accuracy and speed",
                            style = MaterialTheme.typography.labelSmall,
                            color = gameColors.textMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SkillRadarChart(
                            mathScore = stats.mathSkillScore,
                            memoryScore = stats.memorySkillScore,
                            focusScore = stats.focusSkillScore,
                            logicScore = stats.logicSkillScore,
                            verbalScore = stats.verbalSkillScore
                        )
                    }
                }
            }

            // Stats Summary Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        icon = Icons.Filled.FlashOn,
                        label = "Max Streak",
                        value = "${stats.highestStreak}x",
                        color = Color(0xFFFF9500),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Filled.Star,
                        label = "Total Solved",
                        value = "${stats.totalQuestionsSolved}",
                        color = gameColors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Filled.EmojiEvents,
                        label = "Mind XP",
                        value = "${stats.totalXp}",
                        color = gameColors.coinGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Activity History Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = gameColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Game Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = gameColors.textPrimary
                    )
                }
            }

            if (activityHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No game sessions recorded yet.\nPlay infinite levels to see your activity logs!",
                                color = gameColors.textMuted,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(activityHistory) { record ->
                    ActivityRecordItem(record = record)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    stats: UserStats,
    bqScore: Int
) {
    val gameColors = LocalGameColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(gameColors.primary, gameColors.secondary))
                    )
                    .border(2.dp, gameColors.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = "Avatar",
                    tint = Color.Black,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.currentTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = gameColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(gameColors.primary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$bqScore BQ",
                            color = gameColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Level ${1 + (stats.totalXp / 100)} Thinker",
                        fontSize = 12.sp,
                        color = gameColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = gameColors.textPrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = gameColors.textMuted
            )
        }
    }
}

@Composable
private fun ActivityRecordItem(record: ActivityRecord) {
    val gameColors = LocalGameColors.current
    val dateStr = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${record.gameType} (Lvl ${record.levelReached})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = gameColors.textPrimary
                )
                Text(
                    text = "$dateStr • Accuracy ${record.accuracyPercentage}%",
                    fontSize = 11.sp,
                    color = gameColors.textMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = gameColors.coinGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${record.coinsEarned}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = gameColors.coinGold
                )
            }
        }
    }
}
