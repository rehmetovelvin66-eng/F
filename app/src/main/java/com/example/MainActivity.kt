package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.StyleStudioScreen
import com.example.ui.theme.SeffafKontrolTheme
import com.example.viewmodel.MainViewModel

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_STYLE = "style_studio"
const val ROUTE_DIAGNOSTICS = "diagnostics"

private data class NavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val mainTabs = listOf(
    NavTab(
        route = ROUTE_DASHBOARD,
        label = "Kontrol",
        selectedIcon = Icons.Rounded.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_tab_dashboard"
    ),
    NavTab(
        route = ROUTE_STYLE,
        label = "Cam Stili",
        selectedIcon = Icons.Rounded.BlurOn,
        unselectedIcon = Icons.Outlined.BlurOn,
        testTag = "nav_tab_style"
    ),
    NavTab(
        route = ROUTE_DIAGNOSTICS,
        label = "HyperOS",
        selectedIcon = Icons.Rounded.DeveloperBoard,
        unselectedIcon = Icons.Outlined.DeveloperBoard,
        testTag = "nav_tab_diagnostics"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeffafKontrolTheme {
                SeffafKontrolApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeffafKontrolApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSystemState()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        mainTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        mainTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DASHBOARD,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            uiState = uiState,
                            onLaunchOverlay = viewModel::launchGlassOverlay,
                            onNavigateToStyle = { navigateToTab(ROUTE_STYLE) },
                            onOpenAccessibility = viewModel::openAccessibilitySettings,
                            onOpenOverlayPermission = viewModel::openOverlaySettings,
                            onOpenWriteSettings = viewModel::openWriteSettings,
                            onWifiClick = viewModel::onWifiClick,
                            onMobileDataClick = viewModel::onMobileDataClick,
                            onBluetoothClick = viewModel::onBluetoothClick,
                            onFlashlightClick = viewModel::onFlashlightClick,
                            onBrightnessChange = viewModel::onBrightnessChange,
                            onVolumeChange = viewModel::onVolumeChange,
                            onAutoRotateClick = viewModel::onAutoRotateClick,
                            onRingerModeClick = viewModel::onRingerModeClick,
                            onCameraClick = viewModel::onCameraClick,
                            onSystemSettingsClick = viewModel::onSystemSettingsClick,
                            onMediaKey = viewModel::onMediaKey,
                            onGlobalAction = viewModel::onGlobalAction
                        )
                    }
                    composable(ROUTE_STYLE) {
                        StyleStudioScreen(
                            settings = uiState.settings,
                            onPanelAlphaChange = viewModel::updatePanelAlpha,
                            onTileAlphaChange = viewModel::updateTileAlpha,
                            onBlurRadiusChange = viewModel::updateBlurRadius,
                            onCornerRadiusChange = viewModel::updateCornerRadius,
                            onTintSelected = viewModel::updateTintPreset,
                            onEdgeTriggerEnabledChange = viewModel::updateEdgeTriggerEnabled,
                            onEdgePositionChange = viewModel::updateEdgePosition,
                            onEdgeWidthChange = viewModel::updateEdgeWidth,
                            onEdgeIndicatorVisibleChange = viewModel::updateEdgeIndicatorVisible,
                            onShowMediaCardChange = viewModel::updateShowMediaCard,
                            onShowTelemetryPillChange = viewModel::updateShowTelemetryPill,
                            onShowSystemActionsChange = viewModel::updateShowSystemActions,
                            onResetDefaults = viewModel::resetDefaults
                        )
                    }
                    composable(ROUTE_DIAGNOSTICS) {
                        DiagnosticsScreen(
                            diagnostics = uiState.diagnostics,
                            onCopiedCommand = viewModel::showMessage
                        )
                    }
                }
            }
        }
    }
}
