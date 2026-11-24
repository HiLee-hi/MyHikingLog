package com.myhiking.log.presentation.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.myhiking.log.R
import com.myhiking.log.presentation.ui.ComingSoonContent
import com.myhiking.log.presentation.ui.LoginRequiredContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(isLoggedIn: Boolean, onNavigateToLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_stats)) })
        if (!isLoggedIn) {
            LoginRequiredContent(
                message = stringResource(R.string.stats_login_required),
                onNavigateToLogin = onNavigateToLogin
            )
        } else {
            ComingSoonContent(
                icon = Icons.Outlined.BarChart,
                title = stringResource(R.string.stats_coming_soon_title),
                message = stringResource(R.string.stats_coming_soon_message)
            )
        }
    }
}
