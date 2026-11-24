package com.myhiking.log.presentation.navigation

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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myhiking.log.R
import com.myhiking.log.presentation.adaptive.LocalAdaptiveLayoutInfo
import com.myhiking.log.presentation.adaptive.NavigationLayout
import com.myhiking.log.presentation.adaptive.ProvideAdaptiveLayoutInfo
import com.myhiking.log.presentation.home.HomeScreen
import com.myhiking.log.presentation.mountain.MountainsScreen
import com.myhiking.log.presentation.profile.ProfileScreen
import com.myhiking.log.presentation.stamp.StampsScreen
import com.myhiking.log.presentation.stats.StatsScreen
import com.myhiking.log.util.AppLogger

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Mountains : Screen("mountains")
    object Stamps : Screen("stamps")
    object Stats : Screen("stats")
    object Profile : Screen("profile")
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

    // compact: bar, else rail
    val navigationSuiteType = when (adaptiveInfo.navigationLayout) {
        NavigationLayout.Rail -> NavigationSuiteType.NavigationRail
        NavigationLayout.BottomBar -> NavigationSuiteType.NavigationBar
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
                    onFindMountain = {
                        AppLogger.d("NAVIGATION", "Home → Mountains")
                        navController.navigateToTab(Screen.Mountains.route)
                    }
                )
            }
            composable(Screen.Mountains.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Mountains") }
                MountainsScreen()
            }
            composable(Screen.Stamps.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Stamps") }
                StampsScreen(isLoggedIn = isLoggedIn, onNavigateToLogin = navigateToProfile)
            }
            composable(Screen.Stats.route) {
                LaunchedEffect(Unit) { AppLogger.d("NAVIGATION", "Screen: Stats") }
                StatsScreen(isLoggedIn = isLoggedIn, onNavigateToLogin = navigateToProfile)
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
