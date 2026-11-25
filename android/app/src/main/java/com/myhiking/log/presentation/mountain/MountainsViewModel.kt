package com.myhiking.log.presentation.mountain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myhiking.log.domain.model.Difficulty
import com.myhiking.log.domain.model.HeightRange
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainFilter
import com.myhiking.log.domain.model.MountainView
import com.myhiking.log.domain.model.RegionGroup
import com.myhiking.log.domain.model.SummitRecords
import com.myhiking.log.domain.usecase.FilterMountainsUseCase
import com.myhiking.log.domain.usecase.GetMountainPhotosUseCase
import com.myhiking.log.domain.usecase.GetTop100MountainsUseCase
import com.myhiking.log.domain.usecase.ObserveSummitRecordsUseCase
import com.myhiking.log.domain.usecase.SearchMountainsUseCase
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// API search state
data class OtherMountainsState(
    val searching: Boolean = false,
    val failed: Boolean = false,
    // can be > fetched
    val totalCount: Int = 0,
    val fetched: Int = 0
)

data class MountainsUiState(
    val isLoading: Boolean = true,
    val filter: MountainFilter = MountainFilter(),
    // list now, map later
    val results: List<Mountain> = emptyList(),
    val error: Boolean = false,
    val others: OtherMountainsState = OtherMountainsState()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class MountainsViewModel @Inject constructor(
    private val getTop100Mountains: GetTop100MountainsUseCase,
    private val searchMountains: SearchMountainsUseCase,
    private val getMountainPhotos: GetMountainPhotosUseCase,
    private val filterMountains: FilterMountainsUseCase,
    observeSummitRecords: ObserveSummitRecordsUseCase
) : ViewModel() {

    // detail pane: my summits of the open mountain
    val records: StateFlow<SummitRecords> = observeSummitRecords()

    private val all = MutableStateFlow<List<Mountain>?>(null)
    private val error = MutableStateFlow(false)
    private val filter = MutableStateFlow(MountainFilter())
    private val found = MutableStateFlow<List<Mountain>>(emptyList())
    private val others = MutableStateFlow(OtherMountainsState())
    private val retrySearch = MutableStateFlow(0)

    // keeps the open detail alive after a new query
    private val seen = mutableMapOf<String, Mountain>()

    // in-flight photo requests. detail pane calls twice while animating
    private val photoRequests = mutableSetOf<String>()

    private val _photos = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val photos: StateFlow<Map<String, List<String>>> = _photos

    val uiState: StateFlow<MountainsUiState> =
        combine(all, filter, error, found, others) { mountains, f, failed, other, otherState ->
            // name match is done in results() (API can match a peak name), so query is cleared here
            val pool = SearchMountainsUseCase.results(f.query, mountains.orEmpty(), other)
            MountainsUiState(
                isLoading = mountains == null && !failed,
                filter = f,
                results = filterMountains(pool, f.copy(query = "")),
                error = failed,
                others = otherState
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MountainsUiState())

    init {
        load()
        viewModelScope.launch { searchAsYouType() }
    }

    fun load() {
        error.value = false
        viewModelScope.launch {
            runCatching { getTop100Mountains() }
                .onSuccess { all.value = it }
                .onFailure {
                    AppLogger.e("VM_MOUNTAINS", "Loading mountains failed: ${it.message}", it)
                    error.value = true
                }
        }
    }

    private suspend fun searchAsYouType() {
        val top100 = all.filterNotNull().first()
        combine(filter.map { it.query.trim() }.distinctUntilChanged(), retrySearch) { q, _ -> q }
            .debounce(SEARCH_DEBOUNCE_MS)
            .collectLatest { query ->
                found.value = emptyList()
                if (query.isEmpty()) {
                    others.value = OtherMountainsState()
                    return@collectLatest
                }
                others.value = OtherMountainsState(searching = true)
                runCatching { searchMountains(query, top100) }
                    .onSuccess { page ->
                        page.mountains.forEach { seen[it.id] = it }
                        found.value = page.mountains
                        others.value = OtherMountainsState(totalCount = page.totalCount, fetched = page.mountains.size)
                    }
                    .onFailure {
                        AppLogger.e("VM_MOUNTAINS", "Mountain search failed: ${it.message}", it)
                        others.value = OtherMountainsState(failed = true)
                    }
            }
    }

    fun retrySearch() = retrySearch.update { it + 1 }

    fun mountain(id: String): Mountain? = all.value?.firstOrNull { it.id == id } ?: seen[id]

    // once per mountain. no row while loading / on failure
    fun loadPhotos(mountain: Mountain) {
        if (mountain.id in _photos.value || !photoRequests.add(mountain.id)) return
        viewModelScope.launch {
            runCatching { getMountainPhotos(mountain) }
                .also { photoRequests.remove(mountain.id) }
                .onSuccess { urls -> _photos.update { it + (mountain.id to urls) } }
                // not cached -> retry on next open
                .onFailure { AppLogger.e("VM_MOUNTAINS", "Photos for ${mountain.name} failed: ${it.message}", it) }
        }
    }

    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }
    fun onViewChange(view: MountainView) = filter.update { it.copy(view = view) }
    fun onRegionChange(region: RegionGroup?) = filter.update { it.copy(region = region) }
    fun onDifficultyChange(difficulty: Difficulty?) = filter.update { it.copy(difficulty = difficulty) }
    fun onHeightChange(height: HeightRange?) = filter.update { it.copy(height = height) }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
