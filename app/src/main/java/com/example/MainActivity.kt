package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.DevCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.StudioViewModel

enum class StudioNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    PROJECTS("projects", "Projects", Icons.Default.Folder),
    WORKSPACE("workspace", "Workspace", Icons.Default.Code),
    TERMINAL("terminal", "Terminal", Icons.Default.Terminal),
    AGENTS("agents", "Agents", Icons.Default.SmartToy),
    HEALTH("health", "Health", Icons.Default.MonitorHeart),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val onboardingStep by viewModel.onboardingStep.collectAsState()
                var currentDestination by remember { mutableStateOf(StudioNavDestination.PROJECTS) }

                BackHandler(enabled = currentDestination != StudioNavDestination.PROJECTS) {
                    currentDestination = StudioNavDestination.PROJECTS
                }

                if (onboardingStep > 0) {
                    OnboardingScreen(viewModel = viewModel)
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate950),
                        bottomBar = {
                            NavigationBar(
                                containerColor = Slate900,
                                contentColor = DevCyan,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                StudioNavDestination.values().forEach { destination ->
                                    val isSelected = currentDestination == destination
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = destination },
                                        icon = {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = destination.title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = DevCyan,
                                            selectedTextColor = DevCyan,
                                            indicatorColor = Slate800,
                                            unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF64748B),
                                            unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF64748B)
                                        ),
                                        modifier = Modifier.testTag("nav_item_${destination.route}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDestination) {
                                StudioNavDestination.PROJECTS -> ProjectsScreen(
                                    viewModel = viewModel,
                                    onNavigateToWorkspace = {
                                        currentDestination = StudioNavDestination.WORKSPACE
                                    }
                                )
                                StudioNavDestination.WORKSPACE -> WorkspaceScreen(
                                    viewModel = viewModel,
                                    onBackToProjects = {
                                        currentDestination = StudioNavDestination.PROJECTS
                                    }
                                )
                                StudioNavDestination.TERMINAL -> StandaloneTerminalScreen(
                                    viewModel = viewModel
                                )
                                StudioNavDestination.AGENTS -> AgentsScreen(
                                    viewModel = viewModel
                                )
                                StudioNavDestination.HEALTH -> RuntimeHealthScreen(
                                    viewModel = viewModel
                                )
                                StudioNavDestination.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
