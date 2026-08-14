package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeaderTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

enum class AppTab(val title: String, val iconSelected: ImageVector, val iconUnselected: ImageVector, val testTag: String) {
    DASHBOARD("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_dashboard"),
    WORKOUTS("Workouts", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "nav_workouts"),
    NUTRITION("Nutrition", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_nutrition"),
    WEARABLES("Sensors", Icons.Filled.Watch, Icons.Outlined.Watch, "nav_wearables"),
    ANALYTICS("Analytics", Icons.Filled.Insights, Icons.Outlined.Insights, "nav_analytics"),
    SOCIAL("Community", Icons.Filled.People, Icons.Outlined.People, "nav_social"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
            var showPremiumScreen by remember { mutableStateOf(false) }

            val activeSession by viewModel.activeSession.collectAsState()
            val userProfile by viewModel.userProfile.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showPremiumScreen) {
                        PremiumScreen(
                            viewModel = viewModel,
                            onBack = { showPremiumScreen = false }
                        )
                    } else if (activeSession != null) {
                        ActiveWorkoutScreen(
                            viewModel = viewModel,
                            session = activeSession!!,
                            onFinish = {
                                currentTab = AppTab.DASHBOARD
                            }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                AppHeaderTopBar(
                                    title = when (currentTab) {
                                        AppTab.DASHBOARD -> "ApexFit"
                                        AppTab.WORKOUTS -> "Workout Hub"
                                        AppTab.NUTRITION -> "Nutrition & Macros"
                                        AppTab.WEARABLES -> "Wearable Telemetry"
                                        AppTab.ANALYTICS -> "Progress & PRs"
                                        AppTab.SOCIAL -> "Community Feed"
                                        AppTab.PROFILE -> "Settings & Goals"
                                    },
                                    streakDays = userProfile?.streakDays ?: 14,
                                    subscriptionTier = userProfile?.subscriptionTier ?: "PRO",
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                                    onOpenProfile = { currentTab = AppTab.PROFILE },
                                    onOpenPremium = { showPremiumScreen = true }
                                )
                            },
                            bottomBar = {
                                ApexFitBottomNavigation(
                                    currentTab = currentTab,
                                    onTabSelected = { currentTab = it }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    AppTab.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToWorkouts = { currentTab = AppTab.WORKOUTS },
                                        onStartRoutine = { routine ->
                                            viewModel.startWorkoutSession(routine)
                                        },
                                        onNavigateToNutrition = { currentTab = AppTab.NUTRITION },
                                        onNavigateToWearables = { currentTab = AppTab.WEARABLES },
                                        onNavigateToSocial = { currentTab = AppTab.SOCIAL },
                                        onNavigateToPremium = { showPremiumScreen = true }
                                    )

                                    AppTab.WORKOUTS -> WorkoutsScreen(
                                        viewModel = viewModel,
                                        onStartRoutine = { routine ->
                                            viewModel.startWorkoutSession(routine)
                                        },
                                        onOpenPremium = { showPremiumScreen = true }
                                    )

                                    AppTab.NUTRITION -> NutritionScreen(
                                        viewModel = viewModel
                                    )

                                    AppTab.WEARABLES -> WearablesScreen(
                                        viewModel = viewModel
                                    )

                                    AppTab.ANALYTICS -> AnalyticsScreen(
                                        viewModel = viewModel
                                    )

                                    AppTab.SOCIAL -> SocialScreen(
                                        viewModel = viewModel
                                    )

                                    AppTab.PROFILE -> ProfileSettingsScreen(
                                        viewModel = viewModel,
                                        isDarkTheme = isDarkTheme,
                                        onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                                        onOpenPremium = { showPremiumScreen = true }
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

@Composable
fun ApexFitBottomNavigation(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("apexfit_bottom_navigation"),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
    ) {
        NavigationBar(
            containerColor = BentoSurface,
            tonalElevation = 0.dp
        ) {
            AppTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) tab.iconSelected else tab.iconUnselected,
                            contentDescription = tab.title,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoPrimaryDark,
                        indicatorColor = BentoPrimaryHighlight,
                        selectedTextColor = BentoPrimary,
                        unselectedIconColor = BentoTextSecondary,
                        unselectedTextColor = BentoTextSecondary
                    ),
                    modifier = Modifier.testTag(tab.testTag)
                )
            }
        }
    }
}
