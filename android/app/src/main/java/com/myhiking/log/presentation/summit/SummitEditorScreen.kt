package com.myhiking.log.presentation.summit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myhiking.log.R
import com.myhiking.log.domain.model.CompanionType
import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.model.Weather
import com.myhiking.log.domain.usecase.SummitRecordError
import com.myhiking.log.presentation.adaptive.AdaptiveDimens
import com.myhiking.log.presentation.adaptive.constrainedWidth
import com.myhiking.log.presentation.ui.rememberDateTimeFormatter
import com.myhiking.log.presentation.ui.theme.AcornColors
import com.myhiking.log.presentation.ui.theme.Jua
import com.myhiking.log.presentation.ui.theme.acornButtonColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummitEditorScreen(onClose: () -> Unit, viewModel: SummitEditorViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.done) { if (state.done) onClose() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(stringResource(if (state.isNew) R.string.summit_edit_title_new else R.string.summit_edit_title_edit))
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            },
            actions = {
                if (!state.isNew && !state.loading && !state.notFound) {
                    IconButton(onClick = { confirmDelete = true }, enabled = !state.saving && !state.deleting) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.summit_delete))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.notFound -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.summit_error_not_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> SummitFormContent(state = state, viewModel = viewModel)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.summit_delete_confirm_title)) },
            text = { Text(stringResource(R.string.summit_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text(stringResource(R.string.summit_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SummitFormContent(state: SummitEditorUiState, viewModel: SummitEditorViewModel) {
    val form = state.form
    val busy = state.saving || state.deleting
    var pickDate by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = rememberDateTimeFormatter(R.string.summit_date_pattern)

    Column(
        verticalArrangement = Arrangement.spacedBy(22.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .constrainedWidth(AdaptiveDimens.ReadableMaxWidth)
    ) {
        Text(
            state.mountainName,
            fontFamily = Jua,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )

        Field(stringResource(R.string.summit_date)) {
            OutlinedButton(onClick = { pickDate = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(form.date.format(dateFormatter), modifier = Modifier.padding(start = 8.dp).weight(1f))
            }
            ErrorText(state.errors, SummitRecordError.FUTURE_DATE, stringResource(R.string.summit_error_future_date))
        }

        Field(stringResource(R.string.summit_course)) {
            if (state.courses.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.courses.forEach { course ->
                        SuggestionChip(
                            onClick = { viewModel.onCoursePick(course) },
                            enabled = !busy,
                            label = { Text(course.label) }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = form.course,
                onValueChange = viewModel::onCourseChange,
                placeholder = { Text(stringResource(R.string.summit_course_placeholder)) },
                enabled = !busy,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Field(stringResource(R.string.summit_duration)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(form.hours, viewModel::onHoursChange, stringResource(R.string.summit_hours), !busy)
                NumberField(form.minutes, viewModel::onMinutesChange, stringResource(R.string.summit_minutes), !busy)
            }
            ErrorText(state.errors, SummitRecordError.INVALID_DURATION, stringResource(R.string.summit_error_duration))
        }

        Field(stringResource(R.string.summit_weather)) {
            Chips(Weather.entries, form.weather, { stringResource(it.labelRes) }, viewModel::onWeatherChange, !busy)
        }

        Field(stringResource(R.string.summit_companion)) {
            Chips(CompanionType.entries, form.companion, { stringResource(it.labelRes) }, viewModel::onCompanionChange, !busy)
        }

        Field(stringResource(R.string.summit_rating)) {
            RatingStars(rating = form.rating, size = 32.dp, onChange = if (busy) null else viewModel::onRatingChange)
        }

        Field(stringResource(R.string.summit_comment)) {
            OutlinedTextField(
                value = form.comment,
                onValueChange = viewModel::onCommentChange,
                placeholder = { Text(stringResource(R.string.summit_comment_placeholder)) },
                supportingText = {
                    Text(
                        stringResource(R.string.summit_comment_count, form.comment.length, SummitRecord.MAX_COMMENT_LENGTH),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                enabled = !busy,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            ErrorText(
                state.errors,
                SummitRecordError.COMMENT_TOO_LONG,
                stringResource(R.string.summit_error_comment_too_long, SummitRecord.MAX_COMMENT_LENGTH)
            )
        }

        state.message?.let {
            Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = viewModel::save,
            enabled = !busy,
            colors = acornButtonColors(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            if (state.saving) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            } else {
                Text(stringResource(R.string.summit_save))
            }
        }
    }

    if (pickDate) {
        DateDialog(date = form.date, onPick = viewModel::onDateChange, onDismiss = { pickDate = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(date: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    // DatePicker works in UTC millis
    val today = remember { LocalDate.now() }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        yearRange = 1950..today.year,
        selectableDates = remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcDate(utcTimeMillis) <= today
                override fun isSelectableYear(year: Int) = year <= today.year
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onPick(utcDate(it)) }
                onDismiss()
            }) { Text(stringResource(R.string.common_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

private fun utcDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun Field(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

@Composable
private fun ErrorText(errors: Set<SummitRecordError>, error: SummitRecordError, text: String) {
    if (error in errors) {
        Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, suffix: String, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        suffix = { Text(suffix) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        colors = fieldColors(),
        modifier = Modifier.width(120.dp)
    )
}

// single choice, tap the selected one again -> clear
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Chips(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onChange: (T?) -> Unit,
    enabled: Boolean
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onChange(if (isSelected) null else option) },
                enabled = enabled,
                label = { Text(label(option)) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AcornColors.Surface,
                    selectedContainerColor = AcornColors.Moss,
                    selectedLabelColor = AcornColors.Ink
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = AcornColors.Outline,
                    selectedBorderColor = AcornColors.Moss
                )
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = AcornColors.Surface,
    focusedContainerColor = AcornColors.Surface,
    disabledContainerColor = AcornColors.Surface,
    unfocusedBorderColor = AcornColors.Outline
)
