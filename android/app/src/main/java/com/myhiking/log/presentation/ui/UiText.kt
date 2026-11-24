package com.myhiking.log.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import java.time.format.DateTimeFormatter

// string res or raw text from ViewModel
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class Resource(@StringRes val id: Int) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(id)
    }
}

// exception message or fallback
fun Throwable.toUiText(@StringRes fallback: Int): UiText =
    message?.let(UiText::Dynamic) ?: UiText.Resource(fallback)

@Composable
fun rememberDateTimeFormatter(@StringRes pattern: Int): DateTimeFormatter {
    val value = stringResource(pattern)
    return remember(value) { DateTimeFormatter.ofPattern(value) }
}
