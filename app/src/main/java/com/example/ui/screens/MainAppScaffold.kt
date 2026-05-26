package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ConversationListViewModel
import kotlinx.coroutines.launch

object NavigationRoute {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val MODELS = "models"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(appContainer: AppContainer) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val factory = AppViewModelFactory(
        appContainer.chatRepository,
        appContainer.conversationRepository,
        appContainer.modelRepository,
        appContainer.settingsRepository
    )

    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val listViewModel: ConversationListViewModel = viewModel(factory = factory)
    
    // Abstract fluid blobs animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                drawerTonalElevation = 0.dp
            ) {
                ConversationDrawerContent(
                    navController = navController,
                    listViewModel = listViewModel,
                    chatViewModel = chatViewModel,
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Elegant Background Blobs
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top accent blob
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopStart)
                        .offset(x = (-80).dp, y = (-80).dp)
                        .rotate(rotation)
                        .size(350.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                center = Offset(150f, 150f),
                                radius = 400f
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .blur(60.dp)
                )
                // Bottom accent blob
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomEnd)
                        .offset(x = 120.dp, y = 120.dp)
                        .rotate(-rotation)
                        .size(450.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = Offset(200f, 200f),
                                radius = 500f
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .blur(80.dp)
                )
            }

            NavHost(
                navController = navController,
                startDestination = NavigationRoute.CHAT
            ) {
                composable(NavigationRoute.CHAT) {
                    ChatScreen(
                        viewModel = chatViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToSettings = { navController.navigate(NavigationRoute.SETTINGS) }
                    )
                }
                composable(NavigationRoute.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToModels = { navController.navigate(NavigationRoute.MODELS) },
                        viewModel = viewModel(factory = factory)
                    )
                }
                composable(NavigationRoute.MODELS) {
                    ModelManagementScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = viewModel(factory = factory)
                    )
                }
            }
        }
    }
}
