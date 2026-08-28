package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.PerkCategory
import com.example.data.model.PerkDefinition
import com.example.data.model.UnlockedPerk
import com.example.data.model.UserStats
import com.example.ui.components.BrainTopBar
import com.example.ui.theme.LocalGameColors
import com.example.viewmodel.CATALOG_PERKS
import com.example.viewmodel.BrainViewModel

@Composable
fun SkillForgeShopScreen(
    viewModel: BrainViewModel,
    userStats: UserStats?,
    unlockedPerks: List<UnlockedPerk>,
    onBackClick: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val coins = userStats?.coins ?: 150
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Power-Ups", "Themes", "Avatars")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gameColors.background)
    ) {
        BrainTopBar(
            title = "Skill Forge & Shop",
            coins = coins,
            onBackClick = onBackClick
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = gameColors.surface,
            contentColor = gameColors.primary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = gameColors.primary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        val filteredPerks = remember(selectedTab) {
            when (selectedTab) {
                0 -> CATALOG_PERKS.filter { it.category == PerkCategory.IN_GAME_POWERUP }
                1 -> CATALOG_PERKS.filter { it.category == PerkCategory.THEME }
                else -> CATALOG_PERKS.filter { it.category == PerkCategory.AVATAR }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredPerks) { perk ->
                val unlocked = unlockedPerks.find { it.perkId == perk.id }
                val currentLvl = unlocked?.level ?: 0
                val isEquipped = when (perk.category) {
                    PerkCategory.THEME -> userStats?.equippedTheme == perk.id
                    PerkCategory.AVATAR -> userStats?.equippedAvatar == perk.id
                    else -> currentLvl > 0
                }

                PerkShopCard(
                    perk = perk,
                    currentLevel = currentLvl,
                    isEquipped = isEquipped,
                    userCoins = coins,
                    onBuyOrUpgrade = { viewModel.buyOrUpgradePerk(perk) },
                    onEquip = {
                        if (perk.category == PerkCategory.THEME) {
                            viewModel.equipTheme(perk.id)
                        } else if (perk.category == PerkCategory.AVATAR) {
                            viewModel.equipAvatar(perk.id, perk.name)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PerkShopCard(
    perk: PerkDefinition,
    currentLevel: Int,
    isEquipped: Boolean,
    userCoins: Int,
    onBuyOrUpgrade: () -> Unit,
    onEquip: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val isMaxLevel = currentLevel >= perk.maxLevel
    val isUnlocked = currentLevel > 0
    val nextCost = perk.costForLevel(currentLevel + 1)
    val canAfford = userCoins >= nextCost

    val iconVector: ImageVector = when (perk.iconName) {
        "hourglass_top" -> Icons.Filled.HourglassTop
        "auto_awesome" -> Icons.Filled.AutoAwesome
        "shield" -> Icons.Filled.Shield
        "monetization_on" -> Icons.Filled.MonetizationOn
        "lightbulb" -> Icons.Filled.Lightbulb
        "replay" -> Icons.Filled.Replay
        "palette" -> Icons.Filled.Palette
        "military_tech" -> Icons.Filled.MilitaryTech
        "psychology" -> Icons.Filled.Psychology
        else -> Icons.Filled.Face
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_item_${perk.id.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = gameColors.cardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isEquipped) gameColors.primary else gameColors.cardBorder
        )
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isUnlocked) gameColors.primary.copy(alpha = 0.2f) else gameColors.surface)
                            .border(
                                1.dp,
                                if (isUnlocked) gameColors.primary else gameColors.cardBorder,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = perk.name,
                            tint = if (isUnlocked) gameColors.primary else gameColors.textMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = perk.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = gameColors.textPrimary
                        )
                        Text(
                            text = perk.description,
                            fontSize = 12.sp,
                            color = gameColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Progress Pips (for upgrades) or Equip Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!perk.isCosmetic) {
                    // Level Indicator Pips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..perk.maxLevel) {
                            Box(
                                modifier = Modifier
                                    .size(width = 16.dp, height = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (i <= currentLevel) gameColors.accent else gameColors.surface
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMaxLevel) "MAX" else "Lvl $currentLevel/${perk.maxLevel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMaxLevel) gameColors.accent else gameColors.textMuted
                        )
                    }

                    // Upgrade Button
                    if (isMaxLevel) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(gameColors.accent.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Mastered", color = gameColors.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onBuyOrUpgrade,
                            enabled = canAfford,
                            modifier = Modifier
                                .testTag("buy_button_${perk.id.lowercase()}")
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = gameColors.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$nextCost Coins",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Cosmetic Items (Themes & Avatars)
                    if (isEquipped) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(gameColors.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = gameColors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Equipped", color = gameColors.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (isUnlocked) {
                        Button(
                            onClick = onEquip,
                            modifier = Modifier
                                .testTag("equip_button_${perk.id.lowercase()}")
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = gameColors.secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Equip", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onBuyOrUpgrade,
                            enabled = canAfford,
                            modifier = Modifier
                                .testTag("buy_button_${perk.id.lowercase()}")
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = gameColors.coinGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Unlock ($nextCost)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
