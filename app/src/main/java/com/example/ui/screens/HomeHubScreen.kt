package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DailyReward
import com.example.data.model.GameCategory
import com.example.data.model.GameProgress
import com.example.data.model.UserStats
import com.example.ui.components.BrainTopBar
import com.example.ui.theme.LocalGameColors
import com.example.viewmodel.BrainViewModel

@Composable
fun HomeHubScreen(
    viewModel: BrainViewModel,
    userStats: UserStats?,
    gameProgressList: List<GameProgress>,
    dailyReward: DailyReward?,
    onSelectGame: (GameCategory) -> Unit,
    onOpenShop: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val coins = userStats?.coins ?: 150

    val bqScore = remember(userStats) {
        val math = userStats?.mathSkillScore ?: 50
        val mem = userStats?.memorySkillScore ?: 50
        val foc = userStats?.focusSkillScore ?: 50
        val log = userStats?.logicSkillScore ?: 50
        val verb = userStats?.verbalSkillScore ?: 50
        (math + mem + foc + log + verb) / 5
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gameColors.background)
    ) {
        // Top Navigation
        BrainTopBar(
            title = "Brain Sparks",
            coins = coins,
            onShopClick = onOpenShop,
            onProfileClick = onOpenProfile
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Graphic Banner
            item {
                HeroBannerCard(
                    onExploreShop = onOpenShop
                )
            }

            // Daily Reward Gift Banner
            item {
                DailyGiftCard(
                    dailyReward = dailyReward,
                    onClaim = { viewModel.claimDailyGift() }
                )
            }

            // Brain Quotient (BQ) & Infinite Mastery Overview
            item {
                BrainQuotientCard(
                    bqScore = bqScore,
                    title = userStats?.currentTitle ?: "Neuron Initiate",
                    onViewStats = onOpenProfile
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Infinite Mind Challenges",
                        style = MaterialTheme.typography.titleLarge,
                        color = gameColors.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Level ∞",
                        style = MaterialTheme.typography.labelSmall,
                        color = gameColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5 Infinite Game Mode Cards
            items(GameCategory.values()) { category ->
                val progress = gameProgressList.find { it.gameType == category.name }
                val currentLvl = progress?.currentLevel ?: 1
                val stars = progress?.totalStars ?: 0
                val highScore = progress?.highScore ?: 0

                GameModeCard(
                    category = category,
                    currentLevel = currentLvl,
                    stars = stars,
                    highScore = highScore,
                    onPlay = { onSelectGame(category) }
                )
            }
        }
    }
}

@Composable
private fun HeroBannerCard(
    onExploreShop: () -> Unit
) {
    val gameColors = LocalGameColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.brain_hero_banner),
                contentDescription = "Brain Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                gameColors.cardBg.copy(alpha = 0.85f),
                                gameColors.cardBg
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Infinite Cognitive Forge",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Solve puzzles • Earn coins • Unlock perks",
                            fontSize = 12.sp,
                            color = gameColors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onExploreShop,
                        modifier = Modifier
                            .testTag("hero_shop_button")
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = gameColors.primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Skill Forge",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyGiftCard(
    dailyReward: DailyReward?,
    onClaim: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val streak = dailyReward?.streakDays ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF261D0A)
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, gameColors.coinGold.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gameColors.coinGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = "Gift",
                        tint = gameColors.coinGold,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Daily Coin Bonus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = gameColors.coinGold
                    )
                    Text(
                        text = "Streak: $streak Days • Earn up to +200 Coins",
                        fontSize = 12.sp,
                        color = Color(0xFFFFE082)
                    )
                }
            }

            Button(
                onClick = onClaim,
                modifier = Modifier
                    .testTag("claim_daily_gift_button")
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = gameColors.coinGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Claim",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun BrainQuotientCard(
    bqScore: Int,
    title: String,
    onViewStats: () -> Unit
) {
    val gameColors = LocalGameColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onViewStats() },
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(gameColors.primary, gameColors.secondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$bqScore",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Brain Quotient: $bqScore BQ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = gameColors.textPrimary
                    )
                    Text(
                        text = "Rank: $title",
                        fontSize = 12.sp,
                        color = gameColors.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "View Profile",
                tint = gameColors.textMuted
            )
        }
    }
}

@Composable
private fun GameModeCard(
    category: GameCategory,
    currentLevel: Int,
    stars: Int,
    highScore: Int,
    onPlay: () -> Unit
) {
    val gameColors = LocalGameColors.current

    val iconVector: ImageVector = when (category) {
        GameCategory.SPEED_MATH -> Icons.Filled.Calculate
        GameCategory.PATTERN_MATRIX -> Icons.Filled.GridOn
        GameCategory.STROOP_FOCUS -> Icons.Filled.Psychology
        GameCategory.LOGIC_RIDDLES -> Icons.Filled.Lightbulb
        GameCategory.WORD_SPARKS -> Icons.Filled.Spellcheck
    }

    val accentColor = when (category) {
        GameCategory.SPEED_MATH -> gameColors.primary
        GameCategory.PATTERN_MATRIX -> gameColors.secondary
        GameCategory.STROOP_FOCUS -> gameColors.accent
        GameCategory.LOGIC_RIDDLES -> gameColors.coinGold
        GameCategory.WORD_SPARKS -> Color(0xFF00E676)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("game_mode_card_${category.name.lowercase()}")
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = category.title,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = category.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = gameColors.textPrimary
                        )
                        Text(
                            text = category.description,
                            fontSize = 12.sp,
                            color = gameColors.textSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Play Button
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .testTag("play_button_${category.name.lowercase()}")
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level & High Score Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(gameColors.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: Level $currentLevel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = gameColors.coinGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$stars Stars",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gameColors.textPrimary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Best: $highScore",
                        fontSize = 12.sp,
                        color = gameColors.textSecondary
                    )
                }
            }
        }
    }
}
