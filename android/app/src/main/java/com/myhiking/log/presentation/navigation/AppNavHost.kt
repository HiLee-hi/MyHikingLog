package com.myhiking.log.presentation.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myhiking.log.R
import com.myhiking.log.presentation.adaptive.LocalAdaptiveLayoutInfo
import com.myhiking.log.presentation.adaptive.NavigationLayout
import com.myhiking.log.presentation.adaptive.ProvideAdaptiveLayoutInfo
import com.myhiking.log.presentation.home.HomeScreen
import com.myhiking.log.presentation.mountain.MountainsScreen
import com.myhiking.log.presentation.profile.ProfileScreen
import com.myhiking.log.presentation.stamp.StampsScreen
import com.myhiking.log.presentation.stats.StatsScreen
import com.myhiking.log.presentation.summit.SummitEditorScreen
import com.myhiking.log.presentation.summit.SummitEditorViewModel
import com.myhiking.log.util.AppLogger

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Mountains : Screen("mountains")
    object Stamps : Screen("stamps")
    object Stats : Screen("stats")
    object Profile : Screen("profile")

    // full screen, no nav bar. new: mountainId, edit: recordId
    object SummitEditor : Screen("summit/edit?mountainId={mountainId}&recordId={recordId}") {
        fun newRecord(mountainId: String) = "summit/edit?mountainId=${Uri.encode(mountainId)}"
        fun edit(recordId: String) = "summit/edit?recordId=${Uri.encode(recordId)}"
    }
}

data class BottomNavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, R.string.nav_home, Icons.Default.Home),
    BottomNavItem(Screen.Mountains, R.string.nav_mountains, Icons.Default.Terrain),
    BottomNavItem(Screen.Stamps, R.string.nav_stamps, Icons.Default.GridView),
    BottomNavItem(Screen.Stats, R.string.nav_stats, Icons.Default.BarChart),
    BottomNavItem(Screen.Profile, R.string.nav_profile, Icons.Default.Person)
)

// single top + save/restore state
private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(appViewModel: AppViewModel = hiltViewModel()) {
    // recomputed on fold / rotate / resize
    ProvideAdaptiveLayoutInfo {
        AppNavHostContent(appViewModel)
    }
}

@Composable
private fun AppNavHostContent(appViewModel: AppViewModel) {
    // NavController above the nav UI -> bar <-> rail switch keeps the back stack
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isLoggedIn by appViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val adaptiveInfo = LocalAdaptiveLayoutInfo.current

    val navigateToProfile: () -> Unit = {
        AppLogger.i("NAVIGATION", "LoginRequired → navigate to Profile")
        navController.navigateToTab(Screen.Profile.route)
    }

    val openRecord: (String) -> Unit = { id ->
        AppLogger.d("NAVIGATION", "Open summit record")
        navController.navigate(Screen.SummitEditor.edit(id)) { launchSingleTop = true }
    }

    // compact: bar, else rail. editor hides it
    val navigationSuiteType = when {
        currentDestination?.route == Screen.SummitEditor.route -> NavigationSuiteType.None
        adaptiveInfo.navigationLayout == NavigationLayout.Rail -> NavigationSuiteType.NavigationRail
        else -> NavigationSuiteType.NavigationBar
    }

    NavigationSuiteScaffold(
        layoutType = navigationSuiteType,
        navigationSuiteItems = {
            bottomNavItems.forEach { item ->
                item(
                    selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                    onClick = {
                        AppLogger.d("NAVIGATION", "BottomNav tab: ${item.screen.route}")
                        navController.navigateToTab(item.screen.route)
                    },
                    icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                    label = {
                        // label overflows on narrow cover screens -> one line, centered
                        Text(
                            text = stringResource(item.labelRes),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            // bar handles the bottom inset itself, rail doesn't
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
            )
        ) {
            composable(Screen.Home.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Home") }
                HomeScreen(
                    isLoggedIn = isLoggedIn,
                    onFindMountain = {
                        AppLogger.d("NAVIGATION", "Home → Mountains")
                        navController.navigateToTab(Screen.Mountains.route)
                    },
                    onOpenRecord = openRecord,
                    onNavigateToLogin = navigateToProfile
                )
            }
            composable(Screen.Mountains.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Mountains") }
                MountainsScreen(
                    isLoggedIn = isLoggedIn,
                    onRecordSummit = { id ->
                        AppLogger.d("NAVIGATION", "Mountains → SummitEditor: $id")
                        navController.navigate(Screen.SummitEditor.newRecord(id)) { launchSingleTop = true }
                    },
                    onOpenRecord = openRecord,
                    onNavigateToLogin = navigateToProfile
                )
            }
            composable(Screen.Stamps.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Stamps") }
                StampsScreen(isLoggedIn = isLoggedIn, onNavigateToLogin = navigateToProfile)
            }
            composable(Screen.Stats.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Stats") }
                StatsScreen(isLoggedIn = isLoggedIn, onNavigateToLogin = navigateToProfile)
            }
            composable(
                Screen.SummitEditor.route,
                arguments = listOf(
                    navArgument(SummitEditorViewModel.ARG_MOUNTAIN_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(SummitEditorViewModel.ARG_RECORD_ID) { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: SummitEditor") }
                // back tap + save finishing at once -> pop only once
                SummitEditorScreen(onClose = dropUnlessResumed { navController.popBackStack() })
            }
            composable(Screen.Profile.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Profile") }
                ProfileScreen(
                    onLoginSuccess = {
                        AppLogger.i("NAVIGATION", "Login success → navigate to Home")
                        navController.navigateToTab(Screen.Home.route)
                    }
                )
            }
        }
    }
}
