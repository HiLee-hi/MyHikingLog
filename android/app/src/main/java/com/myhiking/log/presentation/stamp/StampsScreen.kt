package com.myhiking.log.presentation.stamp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
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
fun StampsScreen(isLoggedIn: Boolean, onNavigateToLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_stamps)) })
        if (!isLoggedIn) {
            LoginRequiredContent(
                message = stringResource(R.string.stamps_login_required),
                onNavigateToLogin = onNavigateToLogin
            )
        } else {
            ComingSoonContent(
                icon = Icons.Outlined.GridView,
                title = stringResource(R.string.stamps_coming_soon_title),
                message = stringResource(R.string.stamps_coming_soon_message)
            )
        }
    }
}
