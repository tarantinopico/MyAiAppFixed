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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    const val CUSTOM_PROVIDERS = "custom_providers"
    const val APPEARANCE = "appearance"
    const val PROMPT_LIBRARY = "prompt_library"
    const val SKILLS = "skills"
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
        appContainer.settingsRepository,
        appContainer.sessionRestoreManager,
        appContainer.webSearchManager,
        appContainer.customProviderRepository,
        appContainer.promptPreferences,
        appContainer.database
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
                drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
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
        val mainContentBlur by animateDpAsState(
            targetValue = if (drawerState.targetValue == DrawerValue.Open) 16.dp else 0.dp,
            label = "main_content_blur"
        )
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).blur(radius = mainContentBlur)) {
            // Blurred Glass Orbs - More vibrant and modern
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = (-150).dp, y = (-100).dp)
                    .blur(radius = 120.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                com.example.ui.theme.AccentPurple.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 150.dp, y = 350.dp)
                    .blur(radius = 150.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                com.example.ui.theme.AccentBlue.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 0.dp, y = 700.dp)
                    .blur(radius = 130.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                com.example.ui.theme.AccentTeal.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            NavHost(
                navController = navController,
                startDestination = NavigationRoute.CHAT
            ) {
                composable(NavigationRoute.CHAT) {
                    ChatScreen(
                        viewModel = chatViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToSettings = { navController.navigate(NavigationRoute.SETTINGS) },
                        onNavigateToPromptLibrary = { navController.navigate(NavigationRoute.PROMPT_LIBRARY) },
                        onNavigateToSkills = { navController.navigate(NavigationRoute.SKILLS) }
                    )
                }
                composable(NavigationRoute.PROMPT_LIBRARY) {
                    val vm: com.example.ui.viewmodel.PromptLibraryViewModel = viewModel(factory = factory)
                    val prompts by vm.prompts.collectAsStateWithLifecycle()
                    PromptLibraryScreen(
                        prompts = prompts,
                        onSave = { p -> vm.savePrompt(p) },
                        onDelete = { id -> vm.deletePrompt(id) },
                        onSelect = { prompt -> 
                            chatViewModel.onInputChanged(prompt.text)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavigationRoute.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToModels = { navController.navigate(NavigationRoute.MODELS) },
                        onNavigateToCustomProviders = { navController.navigate(NavigationRoute.CUSTOM_PROVIDERS) },
                        onNavigateToAppearance = { navController.navigate(NavigationRoute.APPEARANCE) },
                        viewModel = viewModel(factory = factory)
                    )
                }
                composable(NavigationRoute.MODELS) {
                    ModelManagementScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = viewModel(factory = factory)
                    )
                }
                composable(NavigationRoute.CUSTOM_PROVIDERS) {
                    val vm: com.example.ui.viewmodel.CustomProvidersViewModel = viewModel(factory = factory)
                    val providers by vm.providers.collectAsStateWithLifecycle()
                    CustomProviderEditorScreen(
                        providers = providers,
                        onSave = { vm.addProvider(it) },
                        onDelete = { vm.deleteProvider(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavigationRoute.APPEARANCE) {
                    val themeMode by appContainer.themePreferences.themeMode.collectAsStateWithLifecycle()
                    AppearanceCustomizationScreen(
                        onBack = { navController.popBackStack() },
                        blurIntensity = 100f,
                        onBlurIntensityChange = {},
                        animationSpeed = 1f,
                        onAnimationSpeedChange = {},
                        currentThemeMode = themeMode,
                        onThemeModeChange = { appContainer.themePreferences.setThemeMode(it) }
                    )
                }
                composable(NavigationRoute.SKILLS) {
                    val vm: com.example.ui.viewmodel.SkillsViewModel = viewModel(factory = factory)
                    val skills by vm.skills.collectAsStateWithLifecycle()
                    SkillsScreen(
                        skills = skills,
                        onSave = { s -> vm.saveSkill(s) },
                        onDelete = { id -> vm.deleteSkill(id) },
                        onSelect = { skill ->
                            chatViewModel.setSelectedSkill(skill.id)
                            chatViewModel.onInputChanged(skill.systemPrompt) // just to show it works temporarily, or we inject into system prompt
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
