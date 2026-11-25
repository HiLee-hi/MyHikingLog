package com.myhiking.log.presentation.mountain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myhiking.log.R
import com.myhiking.log.domain.model.Difficulty
import com.myhiking.log.domain.model.HeightRange
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainFilter
import com.myhiking.log.domain.model.MountainView
import com.myhiking.log.domain.model.RegionGroup
import com.myhiking.log.presentation.adaptive.AdaptiveDimens
import com.myhiking.log.presentation.adaptive.LocalAdaptiveLayoutInfo
import com.myhiking.log.presentation.adaptive.paneScaffoldDirective
import com.myhiking.log.presentation.ui.theme.AcornColors
import com.myhiking.log.presentation.ui.theme.Jua

// search + view tabs + list. narrow: detail replaces list, wide: side by side
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MountainsScreen(
    isLoggedIn: Boolean,
    onRecordSummit: (mountainId: String) -> Unit,
    onOpenRecord: (recordId: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: MountainsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    // filter changed -> scroll to top
    var shownFilter by remember { mutableStateOf(state.filter) }
    LaunchedEffect(state.filter) {
        if (state.filter != shownFilter) {
            shownFilter = state.filter
            listState.scrollToItem(0)
        }
    }

    val adaptiveInfo = LocalAdaptiveLayoutInfo.current
    val paneCount = adaptiveInfo.paneCount(
        listPaneWidth = AdaptiveDimens.ListPaneWidth,
        detailPaneMinWidth = AdaptiveDimens.DetailPaneMinWidth
    )
    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        scaffoldDirective = adaptiveInfo.paneScaffoldDirective(paneCount, AdaptiveDimens.ListPaneWidth)
    )
    val selectedId = navigator.currentDestination
        ?.takeIf { it.pane == ListDetailPaneScaffoldRole.Detail }
        ?.content

    BackHandler(enabled = navigator.canNavigateBack()) { navigator.navigateBack() }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                MountainListPane(
                    state = state,
                    listState = listState,
                    selectedId = if (paneCount > 1) selectedId else null,
                    onQueryChange = viewModel::onQueryChange,
                    onViewChange = viewModel::onViewChange,
                    onRegionChange = viewModel::onRegionChange,
                    onDifficultyChange = viewModel::onDifficultyChange,
                    onHeightChange = viewModel::onHeightChange,
                    onRetry = viewModel::load,
                    onRetrySearch = viewModel::retrySearch,
                    onMountainClick = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it.id) }
                )
            }
        },
        detailPane = {
            AnimatedPane(modifier = Modifier.testTag(MountainsTestTags.DetailPane)) {
                val mountain = selectedId?.let(viewModel::mountain)
                if (mountain != null) {
                    key(mountain.id) {
                        val photos by viewModel.photos.collectAsStateWithLifecycle()
                        val records by viewModel.records.collectAsStateWithLifecycle()
                        LaunchedEffect(Unit) { viewModel.loadPhotos(mountain) }
                        MountainDetailPane(
                            mountain = mountain,
                            photos = photos[mountain.id].orEmpty(),
                            records = if (isLoggedIn) records.forMountain(mountain.id) else emptyList(),
                            showBack = paneCount == 1,
                            onBack = { navigator.navigateBack() },
                            // signed out -> profile tab
                            onRecordSummit = { if (isLoggedIn) onRecordSummit(mountain.id) else onNavigateToLogin() },
                            onOpenRecord = onOpenRecord
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.mountains_detail_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MountainListPane(
    state: MountainsUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    selectedId: String?,
    onQueryChange: (String) -> Unit,
    onViewChange: (MountainView) -> Unit,
    onRegionChange: (RegionGroup?) -> Unit,
    onDifficultyChange: (Difficulty?) -> Unit,
    onHeightChange: (HeightRange?) -> Unit,
    onRetry: () -> Unit,
    onRetrySearch: () -> Unit,
    onMountainClick: (Mountain) -> Unit,
) {
    val filter = state.filter
    // keep text locally. reading it back from the flow can lag a frame and break Hangul composition
    var query by rememberSaveable { mutableStateOf(filter.query) }
    val changeQuery = { text: String ->
        query = text
        onQueryChange(text)
    }
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().testTag(MountainsTestTags.ListPane)) {
        Text(
            text = stringResource(R.string.nav_mountains),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = changeQuery,
            placeholder = { Text(stringResource(R.string.mountains_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { changeQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.mountains_search_clear))
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AcornColors.Surface,
                focusedContainerColor = AcornColors.Surface,
                unfocusedBorderColor = AcornColors.Outline
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )

        val views = MountainView.entries
        PrimaryTabRow(
            selectedTabIndex = views.indexOf(filter.view),
            containerColor = MaterialTheme.colorScheme.background,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(views.indexOf(filter.view), matchContentSize = false),
                    width = 64.dp,
                    color = AcornColors.Forest
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            views.forEach { view ->
                val selected = view == filter.view
                Tab(
                    selected = selected,
                    onClick = { onViewChange(view) },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Text(
                            stringResource(view.labelRes),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        OptionChips(
            filter = filter,
            onRegionChange = onRegionChange,
            onDifficultyChange = onDifficultyChange,
            onHeightChange = onHeightChange
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error -> Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.mountains_load_failed))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
            }
            else -> {
                ResultSummary(state = state, onRetrySearch = onRetrySearch)
                if (state.results.isEmpty() && !state.others.searching) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.mountains_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.results, key = { it.id }) { mountain ->
                            MountainCard(
                                mountain = mountain,
                                view = filter.view,
                                searching = filter.query.isNotBlank(),
                                selected = mountain.id == selectedId,
                                onClick = { onMountainClick(mountain) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionChips(
    filter: MountainFilter,
    onRegionChange: (RegionGroup?) -> Unit,
    onDifficultyChange: (Difficulty?) -> Unit,
    onHeightChange: (HeightRange?) -> Unit,
) {
    // (label, selected, onClick), "전체" first
    val options: List<Triple<Int, Boolean, () -> Unit>> = when (filter.view) {
        MountainView.REGION -> listOf(Triple(R.string.option_all, filter.region == null) { onRegionChange(null) }) +
            RegionGroup.entries.map { Triple(it.labelRes, filter.region == it) { onRegionChange(it) } }
        MountainView.DIFFICULTY -> listOf(Triple(R.string.option_all, filter.difficulty == null) { onDifficultyChange(null) }) +
            Difficulty.entries.map { Triple(it.labelRes, filter.difficulty == it) { onDifficultyChange(it) } }
        MountainView.HEIGHT -> listOf(Triple(R.string.option_all, filter.height == null) { onHeightChange(null) }) +
            HeightRange.entries.map { Triple(it.labelRes, filter.height == it) { onHeightChange(it) } }
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag(MountainsTestTags.Options)
    ) {
        items(options.size) { i ->
            val (label, selected, onClick) = options[i]
            FilterChip(
                selected = selected,
                onClick = onClick,
                label = { Text(stringResource(label)) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AcornColors.Surface,
                    selectedContainerColor = AcornColors.Moss,
                    selectedLabelColor = AcornColors.Ink
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = AcornColors.Outline,
                    selectedBorderColor = AcornColors.Moss
                )
            )
        }
    }
}

// count + API search status
@Composable
private fun ResultSummary(state: MountainsUiState, onRetrySearch: () -> Unit) {
    val others = state.others
    val searching = state.filter.query.isNotBlank()
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)) {
        Text(
            text = if (searching) {
                stringResource(R.string.mountains_result_count_search, state.results.size)
            } else {
                stringResource(R.string.mountains_result_count, state.results.size)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!searching) return@Column
        val note = when {
            others.searching -> stringResource(R.string.mountains_others_searching)
            others.failed -> stringResource(R.string.mountains_others_failed)
            others.totalCount > others.fetched && others.fetched > 0 ->
                stringResource(R.string.mountains_others_truncated, others.totalCount, others.fetched)
            else -> null
        }
        if (note != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (others.searching) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (others.failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (others.failed) {
                    TextButton(onClick = onRetrySearch) { Text(stringResource(R.string.common_retry)) }
                }
            }
        }
    }
}

@Composable
private fun MountainCard(
    mountain: Mountain,
    view: MountainView,
    searching: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) AcornColors.MossLight else AcornColors.Surface)
            .border(
                if (selected) BorderStroke(2.dp, AcornColors.Forest) else BorderStroke(1.dp, AcornColors.Outline),
                shape
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        MountainArt(
            seed = mountain.name.hashCode(),
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    mountain.name,
                    fontFamily = Jua,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                // mark top 100 in search results
                if (searching && mountain.isTop100) {
                    Text(
                        stringResource(R.string.mountain_badge_top100_short),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AcornColors.Moss)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = cardLocation(mountain),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // tag = current sort key
            val tag = when (view) {
                MountainView.REGION -> null
                MountainView.DIFFICULTY -> mountain.difficulty?.let { difficulty ->
                    stringResource(difficulty.labelRes) +
                        (mountain.recommendedCourse?.duration?.let { " · $it" } ?: "")
                }
                MountainView.HEIGHT -> null
            }
            if (tag != null) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = AcornColors.Forest,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (view == MountainView.HEIGHT) {
            Text(
                mountain.heightMeters?.let(::formatMeters) ?: "-",
                fontFamily = Jua,
                style = MaterialTheme.typography.titleMedium,
                color = AcornColors.Forest
            )
        }
    }
}

// top 100: provinces + height. API: + 시/군 (갈미봉 x11)
private fun cardLocation(mountain: Mountain): String {
    val place = if (mountain.isTop100) {
        mountain.provinces.joinToString(" · ")
    } else {
        // "경상북도 울릉군 서면 남양리" -> "경북 울릉군"
        val parts = mountain.address.split(' ')
        listOfNotNull(mountain.provinces.firstOrNull() ?: parts.firstOrNull(), parts.getOrNull(1)).joinToString(" ")
    }
    return listOfNotNull(place.ifBlank { null }, mountain.heightMeters?.let(::formatMeters)).joinToString(" · ")
}

internal object MountainsTestTags {
    const val ListPane = "mountains_list_pane"
    const val DetailPane = "mountains_detail_pane"
    const val Options = "mountains_options"
}
