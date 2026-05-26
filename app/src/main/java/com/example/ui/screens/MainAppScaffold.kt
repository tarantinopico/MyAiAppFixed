package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.domain.model.ProviderModel
import com.example.domain.model.ProviderType
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
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
