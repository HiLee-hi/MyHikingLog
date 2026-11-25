package com.myhiking.log.presentation.summit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.myhiking.log.R
import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.presentation.mountain.formatMeters
import com.myhiking.log.presentation.ui.rememberDateTimeFormatter
import com.myhiking.log.presentation.ui.theme.AcornColors
import com.myhiking.log.presentation.ui.theme.Jua

// home: with mountain name, detail pane: date first
@Composable
fun SummitRecordCard(record: SummitRecord, showMountain: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormatter = rememberDateTimeFormatter(R.string.summit_date_pattern)
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AcornColors.Surface)
            .border(1.dp, AcornColors.Outline, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (showMountain) record.mountainName else record.climbedOn.format(dateFormatter),
                fontFamily = Jua,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (showMountain && record.isTop100) {
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
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                record.rating?.let { RatingStars(rating = it, size = 16.dp) }
            }
        }
        val details = listOfNotNull(
            if (showMountain) record.climbedOn.format(dateFormatter) else null,
            record.mountainHeightMeters?.takeIf { showMountain && it > 0 }?.let(::formatMeters),
            record.durationMinutes?.let { formatDuration(it) },
            record.weather?.let { stringResource(it.labelRes) },
            record.companion?.let { stringResource(it.labelRes) }
        )
        if (details.isNotEmpty()) {
            Text(
                details.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = AcornColors.Forest,
                fontWeight = FontWeight.Medium
            )
        }
        // no course (too long), comment on one line -> same card height
        // TODO read-only record detail for the full comment, tap opens the editor for now
        record.comment?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// read-only when onChange is null. tap the current rating again -> clear
@Composable
fun RatingStars(rating: Int?, size: Dp, onChange: ((Int?) -> Unit)? = null) {
    val description = rating?.let { stringResource(R.string.summit_rating_star, it) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (onChange == null) 0.dp else 4.dp),
        modifier = if (onChange == null && description != null) Modifier.semantics(mergeDescendants = true) { contentDescription = description } else Modifier
    ) {
        for (value in 1..SummitRecord.MAX_RATING) {
            val filled = rating != null && value <= rating
            val label = stringResource(R.string.summit_rating_star, value)
            Icon(
                if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = if (onChange == null) null else label,
                tint = if (filled) AcornColors.Acorn else AcornColors.OutlineStrong,
                modifier = Modifier
                    .then(
                        if (onChange == null) Modifier else Modifier
                            .clip(CircleShape)
                            .selectable(selected = rating == value, role = Role.RadioButton) {
                                onChange(if (rating == value) null else value)
                            }
                            .padding(4.dp)
                    )
                    .size(size)
            )
        }
    }
}
