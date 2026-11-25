package com.myhiking.log.presentation.summit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myhiking.log.R
import com.myhiking.log.domain.model.CompanionType
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainCourse
import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.model.Weather
import com.myhiking.log.domain.usecase.DeleteSummitRecordUseCase
import com.myhiking.log.domain.usecase.FindMountainUseCase
import com.myhiking.log.domain.usecase.InvalidSummitRecordException
import com.myhiking.log.domain.usecase.ObserveSummitRecordsUseCase
import com.myhiking.log.domain.usecase.SaveSummitRecordUseCase
import com.myhiking.log.domain.usecase.SummitRecordError
import com.myhiking.log.presentation.ui.UiText
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SummitForm(
    val date: LocalDate = LocalDate.now(),
    val course: String = "",
    val hours: String = "",
    val minutes: String = "",
    val weather: Weather? = null,
    val companion: CompanionType? = null,
    val rating: Int? = null,
    val comment: String = ""
)

data class SummitEditorUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val isNew: Boolean = true,
    val mountainName: String = "",
    // top 100 only
    val courses: List<MountainCourse> = emptyList(),
    val form: SummitForm = SummitForm(),
    val errors: Set<SummitRecordError> = emptySet(),
    val saving: Boolean = false,
    val deleting: Boolean = false,
    val message: UiText? = null,
    // saved / deleted -> close
    val done: Boolean = false
)

// nav args: mountainId (new) or recordId (edit)
@HiltViewModel
class SummitEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val findMountain: FindMountainUseCase,
    private val observeRecords: ObserveSummitRecordsUseCase,
    private val saveRecord: SaveSummitRecordUseCase,
    private val deleteRecord: DeleteSummitRecordUseCase
) : ViewModel() {

    private val mountainId: String? = savedStateHandle[ARG_MOUNTAIN_ID]
    private val recordId: String? = savedStateHandle[ARG_RECORD_ID]

    private val _uiState = MutableStateFlow(SummitEditorUiState())
    val uiState: StateFlow<SummitEditorUiState> = _uiState.asStateFlow()

    // mountain snapshot + id, form fields go on top
    private var base: SummitRecord? = null
    // errors shown live only after the first save tap
    private var submitted = false

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val existing = recordId?.let { id -> observeRecords().value.items.firstOrNull { it.id == id } }
        val mountain = (existing?.mountainId ?: mountainId)?.let { id ->
            runCatching { findMountain(id) }.getOrNull()
        }
        val record = existing ?: mountain?.let(::newRecord)
        if (record == null) {
            // API mountain after process death, or record gone
            AppLogger.w("VM_SUMMIT", "Nothing to edit: mountainId=$mountainId, recordId=${recordId?.let(AppLogger::shortId)}")
            _uiState.value = SummitEditorUiState(loading = false, notFound = true)
            return
        }
        base = record
        val (hours, minutes) = durationFields(record.durationMinutes)
        _uiState.value = SummitEditorUiState(
            loading = false,
            isNew = existing == null,
            mountainName = record.mountainName,
            courses = mountain?.courses.orEmpty(),
            form = SummitForm(
                date = record.climbedOn,
                course = record.course.orEmpty(),
                hours = hours,
                minutes = minutes,
                weather = record.weather,
                companion = record.companion,
                rating = record.rating,
                comment = record.comment.orEmpty()
            )
        )
    }

    private fun newRecord(mountain: Mountain) = SummitRecord(
        id = "",
        mountainId = mountain.id,
        mountainName = mountain.name,
        mountainHeightMeters = mountain.heightMeters,
        isTop100 = mountain.isTop100,
        provinces = mountain.provinces,
        climbedOn = LocalDate.now(),
        course = null,
        durationMinutes = null,
        weather = null,
        companion = null,
        rating = null,
        comment = null
    )

    private fun edit(change: (SummitForm) -> SummitForm) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(
            form = form,
            errors = if (submitted) base?.let { SaveSummitRecordUseCase.validate(toRecord(it, form), LocalDate.now()) }.orEmpty() else state.errors,
            message = null
        )
    }

    fun onDateChange(date: LocalDate) = edit { it.copy(date = date) }
    fun onCourseChange(course: String) = edit { it.copy(course = course) }
    fun onHoursChange(hours: String) = edit { it.copy(hours = hours.filter(Char::isDigit).take(2)) }
    fun onMinutesChange(minutes: String) = edit { it.copy(minutes = minutes.filter(Char::isDigit).take(2)) }
    fun onWeatherChange(weather: Weather?) = edit { it.copy(weather = weather) }
    fun onCompanionChange(companion: CompanionType?) = edit { it.copy(companion = companion) }
    fun onRatingChange(rating: Int?) = edit { it.copy(rating = rating) }
    fun onCommentChange(comment: String) = edit { it.copy(comment = comment.take(SummitRecord.MAX_COMMENT_LENGTH)) }

    // course chip -> route text, time too if still empty
    fun onCoursePick(course: MountainCourse) = edit { form ->
        val (hours, minutes) = durationFields(course.minutes)
        val emptyTime = form.hours.isBlank() && form.minutes.isBlank()
        form.copy(
            course = course.route.ifBlank { course.label },
            hours = if (emptyTime) hours else form.hours,
            minutes = if (emptyTime) minutes else form.minutes
        )
    }

    fun save() {
        val record = base ?: return
        val state = _uiState.value
        if (state.saving || state.deleting) return
        submitted = true
        val draft = toRecord(record, state.form)
        val errors = SaveSummitRecordUseCase.validate(draft, LocalDate.now())
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        _uiState.update { it.copy(saving = true, errors = emptySet(), message = null) }
        viewModelScope.launch {
            runCatching { saveRecord(draft) }
                .onSuccess { _uiState.update { it.copy(saving = false, done = true) } }
                .onFailure { e ->
                    AppLogger.e("VM_SUMMIT", "Save failed: ${e.message}", e)
                    _uiState.update {
                        if (e is InvalidSummitRecordException) it.copy(saving = false, errors = e.errors)
                        else it.copy(saving = false, message = UiText.Resource(R.string.summit_error_save_failed))
                    }
                }
        }
    }

    fun delete() {
        val id = base?.id?.takeIf { it.isNotBlank() } ?: return
        if (_uiState.value.saving || _uiState.value.deleting) return
        _uiState.update { it.copy(deleting = true, message = null) }
        viewModelScope.launch {
            runCatching { deleteRecord(id) }
                .onSuccess { _uiState.update { it.copy(deleting = false, done = true) } }
                .onFailure { e ->
                    AppLogger.e("VM_SUMMIT", "Delete failed: ${e.message}", e)
                    _uiState.update { it.copy(deleting = false, message = UiText.Resource(R.string.summit_error_delete_failed)) }
                }
        }
    }

    private fun toRecord(base: SummitRecord, form: SummitForm) = base.copy(
        climbedOn = form.date,
        course = form.course,
        durationMinutes = parseDuration(form.hours, form.minutes),
        weather = form.weather,
        companion = form.companion,
        rating = form.rating,
        comment = form.comment
    )

    companion object {
        const val ARG_MOUNTAIN_ID = "mountainId"
        const val ARG_RECORD_ID = "recordId"
    }
}
