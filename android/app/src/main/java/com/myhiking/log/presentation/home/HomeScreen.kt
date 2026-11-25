package com.myhiking.log.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myhiking.log.R
import com.myhiking.log.domain.model.LoadStatus
import com.myhiking.log.presentation.adaptive.AdaptiveDimens
import com.myhiking.log.presentation.adaptive.constrainedWidth
import com.myhiking.log.presentation.summit.SummitRecordCard
import com.myhiking.log.presentation.ui.theme.AcornColors
import com.myhiking.log.presentation.ui.theme.acornButtonColors

@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onFindMountain: () -> Unit,
    onOpenRecord: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    // records only exist while signed in
    val items = if (isLoggedIn) records.items else emptyList()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        item {
            Column(modifier = Modifier.constrainedWidth(AdaptiveDimens.ReadableMaxWidth)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painter = painterResource(R.drawable.ic_acorn_mark), contentDescription = null)
                    Text(text = stringResource(R.string.home_greeting), style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(28.dp))
                Text(text = stringResource(R.string.home_recent), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        when {
            items.isNotEmpty() -> items(items, key = { it.id }) { record ->
                SummitRecordCard(
                    record = record,
                    showMountain = true,
                    onClick = { onOpenRecord(record.id) },
                    modifier = Modifier.constrainedWidth(AdaptiveDimens.ReadableMaxWidth)
                )
            }
            isLoggedIn && records.status in setOf(LoadStatus.IDLE, LoadStatus.LOADING) -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            isLoggedIn && records.status == LoadStatus.FAILED -> item {
                EmptyCard(
                    message = stringResource(R.string.home_records_load_failed),
                    action = stringResource(R.string.common_retry),
                    onAction = viewModel::retry
                )
            }
            isLoggedIn -> item {
                EmptyCard(
                    message = stringResource(R.string.home_recent_empty),
                    action = stringResource(R.string.home_find_mountain),
                    onAction = onFindMountain
                )
            }
            else -> item {
                EmptyCard(
                    message = stringResource(R.string.home_recent_signed_out),
                    action = stringResource(R.string.login_required_action),
                    onAction = onNavigateToLogin
                )
            }
        }

        // reload failed but old list still shown
        if (items.isNotEmpty() && records.status == LoadStatus.FAILED) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.constrainedWidth(AdaptiveDimens.ReadableMaxWidth)
                ) {
                    Text(
                        stringResource(R.string.home_records_load_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.common_retry)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .constrainedWidth(AdaptiveDimens.ReadableMaxWidth)
            .clip(MaterialTheme.shapes.large)
            .background(AcornColors.MossLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(colors = acornButtonColors(), onClick = onAction, modifier = Modifier.widthIn(min = 160.dp)) {
            Text(action)
        }
    }
}
