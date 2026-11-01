package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.api.RetrofitClient
import com.example.data.repository.WellnessRepository
import com.example.ui.screens.CalmToolsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DoctorDashboardScreen
import com.example.ui.screens.FamilyDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MoodScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.SerenaTheme
import com.example.ui.viewmodel.WellnessViewModel
import com.example.ui.viewmodel.WellnessViewModelFactory
import com.example.util.SessionStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide device navigation buttons / system navigation bars while the app is in use
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())

        setContent {
            SerenaTheme {
                val repository = remember {
                    WellnessRepository(
                        geminiService = RetrofitClient.geminiService,
                        proKidsServiceProvider = { RetrofitClient.proKidsService },
                        sessionStore = SessionStore(applicationContext)
                    )
                }
                val factory = remember { WellnessViewModelFactory(repository) }
                val wellnessViewModel: WellnessViewModel = viewModel(factory = factory)

                SerenaApp(viewModel = wellnessViewModel)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    override fun onResume() {
        super.onResume()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

enum class NavigationDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    CHAT("chat", "Serena", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    JOURNAL("journal", "Libreta", Icons.Filled.Book, Icons.Outlined.Book),
    REWARDS("rewards", "Premios", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    ROUTINES("routines", "Rutinas", Icons.Filled.Spa, Icons.Outlined.Spa),
    MOOD("mood", "Ánimo", Icons.Filled.Mood, Icons.Outlined.Mood),
    CALM("calm", "Calma", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
    SETTINGS("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun SerenaApp(viewModel: WellnessViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val currentUser by viewModel.currentUser.collectAsState()

    // 1. Initial Heartbeat "Calma" Animation Splash Screen
    AnimatedContent(
        targetState = showSplash,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "splash_transition"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(onAnimationFinished = { showSplash = false })
        } else {
            // 2. If not logged in, show Login Screen
            if (currentUser == null) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {}
                )
            } else {
                // 3. Role-specific experiences
                when (currentUser!!.role) {
                    "MEDICO" -> {
                        var showSettings by remember { mutableStateOf(false) }
                        if (showSettings) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onLogout = { viewModel.logout() },
                                onNavigateBack = { showSettings = false }
                            )
                        } else {
                            DoctorDashboardScreen(
                                viewModel = viewModel,
                                onLogout = { viewModel.logout() },
                                onOpenSettings = { showSettings = true }
                            )
                        }
                    }
                    "FAMILIAR" -> {
                        var showSettings by remember { mutableStateOf(false) }
                        if (showSettings) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onLogout = { viewModel.logout() },
                                onNavigateBack = { showSettings = false }
                            )
                        } else {
                            FamilyDashboardScreen(
                                viewModel = viewModel,
                                onLogout = { viewModel.logout() },
                                onOpenSettings = { showSettings = true }
                            )
                        }
                    }
                    else -> {
                        // "PACIENTE" Mode
                        var currentDestination by remember { mutableStateOf(NavigationDestination.HOME) }

                        BackHandler(enabled = currentDestination != NavigationDestination.HOME) {
                            currentDestination = NavigationDestination.HOME
                        }

                        val bottomBarDestinations = listOf(
                            NavigationDestination.HOME,
                            NavigationDestination.CHAT,
                            NavigationDestination.JOURNAL,
                            NavigationDestination.REWARDS,
                            NavigationDestination.ROUTINES
                        )

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets.safeDrawing,
                            bottomBar = {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = ClayShadowColor),
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        bottomBarDestinations.forEach { destination ->
                                            val isSelected = currentDestination == destination

                                            Column(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { currentDestination = destination }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val isJournal = destination == NavigationDestination.JOURNAL
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .shadow(
                                                            elevation = if (isSelected) 6.dp else if (isJournal) 2.dp else 0.dp,
                                                            shape = CircleShape,
                                                            ambientColor = ClayShadowColor
                                                        )
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isSelected -> ClayDeepPurple
                                                                isJournal -> Color(0xFFECE4F8)
                                                                else -> Color.Transparent
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                                        contentDescription = destination.title,
                                                        tint = when {
                                                            isSelected -> Color.White
                                                            isJournal -> ClayDeepPurple
                                                            else -> Color(0xFF8B7E9F)
                                                        },
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = destination.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSelected) ClayDeepPurple else Color(0xFF8B7E9F)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            when (currentDestination) {
                                NavigationDestination.HOME -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToChat = { currentDestination = NavigationDestination.CHAT },
                                        onNavigateToMood = { currentDestination = NavigationDestination.MOOD },
                                        onNavigateToRoutines = { currentDestination = NavigationDestination.ROUTINES },
                                        onNavigateToCalm = { currentDestination = NavigationDestination.CALM },
                                        onNavigateToJournal = { currentDestination = NavigationDestination.JOURNAL },
                                        onNavigateToRewards = { currentDestination = NavigationDestination.REWARDS },
                                        onSwitchProfile = { currentDestination = NavigationDestination.SETTINGS },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.CHAT -> {
                                    ChatScreen(
                                        viewModel = viewModel,
                                        onNavigateToCalm = { currentDestination = NavigationDestination.CALM },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.JOURNAL -> {
                                    JournalScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.REWARDS -> {
                                    RewardsScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.ROUTINES -> {
                                    RoutinesScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.MOOD -> {
                                    MoodScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.CALM -> {
                                    CalmToolsScreen(
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                NavigationDestination.SETTINGS -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() },
                                        onNavigateBack = { currentDestination = NavigationDestination.HOME },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
