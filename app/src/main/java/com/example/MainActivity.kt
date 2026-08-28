package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GameCategory
import com.example.ui.screens.BrainProfileScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HomeHubScreen
import com.example.ui.screens.SkillForgeShopScreen
import com.example.ui.theme.BrainAppTheme
import com.example.ui.theme.LocalGameColors
import com.example.viewmodel.BrainViewModel

enum class AppScreen {
    HOME,
    GAME_PLAY,
    SHOP,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private val brainViewModel: BrainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userStats by brainViewModel.userStats.collectAsStateWithLifecycle()
            val themeKey = userStats?.equippedTheme ?: "THEME_CYBER"

            BrainAppTheme(themeKey = themeKey) {
                MainAppContent(viewModel = brainViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: BrainViewModel) {
    val gameColors = LocalGameColors.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val allProgress by viewModel.allGameProgress.collectAsStateWithLifecycle()
    val unlockedPerks by viewModel.unlockedPerks.collectAsStateWithLifecycle()
    val dailyReward by viewModel.dailyReward.collectAsStateWithLifecycle()
    val recentActivity by viewModel.recentActivity.collectAsStateWithLifecycle()
    val gamePlayState by viewModel.gamePlayState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Switch to game screen automatically when level data is loaded
    LaunchedEffect(gamePlayState.currentLevelData) {
        if (gamePlayState.currentLevelData != null) {
            currentScreen = AppScreen.GAME_PLAY
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(gameColors.background),
        containerColor = gameColors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen != AppScreen.GAME_PLAY) {
                NavigationBar(
                    containerColor = gameColors.surface,
                    contentColor = gameColors.primary,
                    modifier = Modifier
                        .testTag("main_bottom_nav_bar")
                        .navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = {
                            viewModel.feedbackManager.playClick()
                            currentScreen = AppScreen.HOME
                        },
                        icon = { Icon(Icons.Filled.Gamepad, contentDescription = "Play") },
                        label = { Text("Games") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = gameColors.primary,
                            indicatorColor = gameColors.primary,
                            unselectedIconColor = gameColors.textMuted,
                            unselectedTextColor = gameColors.textMuted
                        ),
                        modifier = Modifier.testTag("nav_item_games")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SHOP,
                        onClick = {
                            viewModel.feedbackManager.playClick()
                            currentScreen = AppScreen.SHOP
                        },
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "Skill Forge") },
                        label = { Text("Forge") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = gameColors.primary,
                            indicatorColor = gameColors.primary,
                            unselectedIconColor = gameColors.textMuted,
                            unselectedTextColor = gameColors.textMuted
                        ),
                        modifier = Modifier.testTag("nav_item_shop")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROFILE,
                        onClick = {
                            viewModel.feedbackManager.playClick()
                            currentScreen = AppScreen.PROFILE
                        },
                        icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Brain Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = gameColors.primary,
                            indicatorColor = gameColors.primary,
                            unselectedIconColor = gameColors.textMuted,
                            unselectedTextColor = gameColors.textMuted
                        ),
                        modifier = Modifier.testTag("nav_item_profile")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.HOME -> {
                        HomeHubScreen(
                            viewModel = viewModel,
                            userStats = userStats,
                            gameProgressList = allProgress,
                            dailyReward = dailyReward,
                            onSelectGame = { category ->
                                viewModel.feedbackManager.playClick()
                                viewModel.startLevel(category)
                            },
                            onOpenShop = {
                                viewModel.feedbackManager.playClick()
                                currentScreen = AppScreen.SHOP
                            },
                            onOpenProfile = {
                                viewModel.feedbackManager.playClick()
                                currentScreen = AppScreen.PROFILE
                            }
                        )
                    }

                    AppScreen.GAME_PLAY -> {
                        GamePlayScreen(
                            viewModel = viewModel,
                            uiState = gamePlayState,
                            onExitGame = {
                                viewModel.feedbackManager.playClick()
                                viewModel.closeGameScreen()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.SHOP -> {
                        SkillForgeShopScreen(
                            viewModel = viewModel,
                            userStats = userStats,
                            unlockedPerks = unlockedPerks,
                            onBackClick = {
                                viewModel.feedbackManager.playClick()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.PROFILE -> {
                        BrainProfileScreen(
                            userStats = userStats,
                            activityHistory = recentActivity,
                            onBackClick = {
                                viewModel.feedbackManager.playClick()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
