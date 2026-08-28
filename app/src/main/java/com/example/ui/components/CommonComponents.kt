package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PowerUpInventory
import com.example.ui.theme.LocalGameColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BrainTopBar(
    title: String,
    coins: Int,
    onBackClick: (() -> Unit)? = null,
    onShopClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val gameColors = LocalGameColors.current
    var prevCoins by remember { mutableStateOf(coins) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(coins) {
        if (coins != prevCoins) {
            scaleAnim.animateTo(1.3f, animationSpec = tween(120, easing = FastOutSlowInEasing))
            scaleAnim.animateTo(1.0f, animationSpec = tween(200, easing = FastOutSlowInEasing))
            prevCoins = coins
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .testTag("top_bar_back_button")
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(gameColors.surface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = gameColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = gameColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Coin Counter Badge
            Row(
                modifier = Modifier
                    .testTag("top_bar_coins_badge")
                    .scale(scaleAnim.value)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF332306), Color(0xFF5E4510))
                        )
                    )
                    .border(1.dp, gameColors.coinGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable { onShopClick?.invoke() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = "Reward Coins",
                    tint = gameColors.coinGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$coins",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = gameColors.coinGold
                )
            }

            if (onProfileClick != null) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .testTag("top_bar_profile_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(gameColors.surface)
                        .border(1.dp, gameColors.cardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Brain Profile",
                        tint = gameColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PowerUpActionBar(
    inventory: PowerUpInventory,
    onUseTimeFreeze: () -> Unit,
    onUseFiftyFifty: () -> Unit,
    onUseHint: () -> Unit,
    onUseMemoryEcho: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PowerUpChip(
            icon = Icons.Filled.HourglassTop,
            label = "Freeze +5s",
            count = inventory.timeFreezeCount,
            tint = gameColors.primary,
            tag = "powerup_time_freeze",
            onClick = onUseTimeFreeze
        )

        PowerUpChip(
            icon = Icons.Filled.AutoAwesome,
            label = "50:50 Oracle",
            count = inventory.fiftyFiftyCount,
            tint = gameColors.accent,
            tag = "powerup_fifty_fifty",
            onClick = onUseFiftyFifty
        )

        PowerUpChip(
            icon = Icons.Filled.HelpOutline,
            label = "Hint Spark",
            count = inventory.hintCount,
            tint = gameColors.coinGold,
            tag = "powerup_hint",
            onClick = onUseHint
        )

        if (onUseMemoryEcho != null) {
            PowerUpChip(
                icon = Icons.Filled.Replay,
                label = "Echo Replay",
                count = inventory.memoryEchoCount,
                tint = gameColors.secondary,
                tag = "powerup_memory_echo",
                onClick = onUseMemoryEcho
            )
        }
    }
}

@Composable
private fun PowerUpChip(
    icon: ImageVector,
    label: String,
    count: Int,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    val gameColors = LocalGameColors.current
    val isEnabled = count > 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isEnabled) gameColors.cardBg else gameColors.surface.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (isEnabled) tint.copy(alpha = 0.7f) else gameColors.cardBorder.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = if (isEnabled) tint else Color.Gray,
                    contentColor = Color.Black
                ) {
                    Text(
                        text = if (isEnabled) "$count" else "0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isEnabled) tint else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isEnabled) gameColors.textPrimary else gameColors.textMuted
        )
    }
}

// Particle & Confetti FX
data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var rotation: Float,
    var vRotation: Float
)

@Composable
fun ConfettiCanvas(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val particles = remember {
        val list = mutableListOf<ConfettiParticle>()
        val colors = listOf(
            Color(0xFF00F0FF), Color(0xFFFF007F), Color(0xFFFFD700),
            Color(0xFF00E676), Color(0xFFAF52DE), Color(0xFFFF9500)
        )
        for (i in 0 until 60) {
            list.add(
                ConfettiParticle(
                    x = Random.nextFloat(),
                    y = -0.1f - Random.nextFloat() * 0.4f,
                    vx = (Random.nextFloat() - 0.5f) * 0.008f,
                    vy = 0.007f + Random.nextFloat() * 0.012f,
                    color = colors.random(),
                    size = 12f + Random.nextFloat() * 16f,
                    rotation = Random.nextFloat() * 360f,
                    vRotation = (Random.nextFloat() - 0.5f) * 10f
                )
            )
        }
        mutableStateListOf(*list.toTypedArray())
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val frame by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            p.x += p.vx
            p.y += p.vy
            p.rotation += p.vRotation
            if (p.y > 1.2f) {
                p.y = -0.1f
                p.x = Random.nextFloat()
            }

            val px = p.x * size.width
            val py = p.y * size.height
            drawCircle(
                color = p.color,
                radius = p.size / 2,
                center = Offset(px, py)
            )
        }
    }
}

// 5-Point Cognitive Skill Radar Polygon
@Composable
fun SkillRadarChart(
    mathScore: Int,
    memoryScore: Int,
    focusScore: Int,
    logicScore: Int,
    verbalScore: Int,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    val scores = listOf(
        Pair("Math", mathScore.coerceIn(20, 100) / 100f),
        Pair("Memory", memoryScore.coerceIn(20, 100) / 100f),
        Pair("Focus", focusScore.coerceIn(20, 100) / 100f),
        Pair("Logic", logicScore.coerceIn(20, 100) / 100f),
        Pair("Verbal", verbalScore.coerceIn(20, 100) / 100f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * 0.38f
            val count = scores.size

            // Draw Web Grid Concentric Polygons
            for (level in 1..4) {
                val gridRadius = radius * (level / 4f)
                val gridPath = Path()
                for (i in 0 until count) {
                    val angle = (2 * PI / count) * i - (PI / 2)
                    val x = center.x + gridRadius * cos(angle).toFloat()
                    val y = center.y + gridRadius * sin(angle).toFloat()
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(gridPath, color = gameColors.cardBorder.copy(alpha = 0.5f), style = Stroke(width = 2f))
            }

            // Draw Spokes
            for (i in 0 until count) {
                val angle = (2 * PI / count) * i - (PI / 2)
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()
                drawLine(
                    color = gameColors.cardBorder.copy(alpha = 0.6f),
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = 2f
                )
            }

            // Draw Filled Data Polygon
            val dataPath = Path()
            val points = mutableListOf<Offset>()
            scores.forEachIndexed { i, pair ->
                val angle = (2 * PI / count) * i - (PI / 2)
                val r = radius * pair.second
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                points.add(Offset(x, y))
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // Fill
            drawPath(
                dataPath,
                brush = Brush.radialGradient(
                    colors = listOf(gameColors.primary.copy(alpha = 0.45f), gameColors.secondary.copy(alpha = 0.25f)),
                    center = center,
                    radius = radius
                ),
                style = Fill
            )
            // Stroke
            drawPath(
                dataPath,
                color = gameColors.primary,
                style = Stroke(width = 4f)
            )

            // Draw Vertex Glowing Dots
            points.forEach { pt ->
                drawCircle(color = gameColors.accent, radius = 6f, center = pt)
                drawCircle(color = Color.White, radius = 3f, center = pt)
            }
        }

        // Labels around the chart
        scores.forEachIndexed { i, pair ->
            val angle = (2 * PI / scores.size) * i - (PI / 2)
            val offsetDist = 95.dp
            val xOffset = (offsetDist.value * cos(angle)).dp
            val yOffset = (offsetDist.value * sin(angle)).dp

            Box(
                modifier = Modifier
                    .padding(
                        start = if (xOffset > 0.dp) xOffset else 0.dp,
                        end = if (xOffset < 0.dp) -xOffset else 0.dp,
                        top = if (yOffset > 0.dp) yOffset else 0.dp,
                        bottom = if (yOffset < 0.dp) -yOffset else 0.dp
                    )
            ) {
                Text(
                    text = "${pair.first} ${(pair.second * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = gameColors.textPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(gameColors.cardBg.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
