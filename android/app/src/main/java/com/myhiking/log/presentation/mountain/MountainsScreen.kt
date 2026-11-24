package com.myhiking.log.presentation.mountain

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.myhiking.log.R
import com.myhiking.log.presentation.ui.ComingSoonContent

// browsing needs no login, only saving a summit
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MountainsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_mountains)) })
        ComingSoonContent(
            icon = Icons.Outlined.Terrain,
            title = stringResource(R.string.mountains_coming_soon_title),
            message = stringResource(R.string.mountains_coming_soon_message)
        )
    }
}
