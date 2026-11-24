package com.myhiking.log.presentation.mountain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.myhiking.log.R
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainCourse
import com.myhiking.log.presentation.adaptive.AdaptiveDimens
import com.myhiking.log.presentation.adaptive.constrainedWidth
import com.myhiking.log.presentation.ui.theme.AcornColors
import com.myhiking.log.presentation.ui.theme.Jua

// top 100 data or API data. photos from API for both
// TODO step 3: save summit
@Composable
fun MountainDetailPane(mountain: Mountain, photos: List<String>, showBack: Boolean, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box {
            MountainArt(seed = mountain.name.hashCode(), modifier = Modifier.fillMaxWidth().height(200.dp))
            if (showBack) {
                FilledIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AcornColors.Cream.copy(alpha = 0.92f)),
                    modifier = Modifier.statusBarsPadding().padding(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = AcornColors.Ink
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .constrainedWidth(AdaptiveDimens.ReadableMaxWidth)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        mountain.name,
                        fontFamily = Jua,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f, fill = false).semantics { heading() }
                    )
                    if (mountain.isTop100) {
                        Text(
                            stringResource(R.string.mountain_badge_top100),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AcornColors.Moss)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                mountain.tagline?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = AcornColors.Forest, fontWeight = FontWeight.Medium)
                }
                Text(mountain.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            val tiles = buildList {
                mountain.heightMeters?.let { add(stringResource(R.string.mountain_height) to formatMeters(it)) }
                mountain.difficulty?.let {
                    val label = if (mountain.difficultyEstimated) R.string.mountain_difficulty_estimated else R.string.mountain_difficulty
                    add(stringResource(label) to stringResource(it.labelRes))
                }
                mountain.recommendedCourse?.let {
                    add(stringResource(R.string.mountain_recommended_time) to it.duration.removePrefix("약 "))
                }
                if (!mountain.isTop100) mountain.manager?.let { add(stringResource(R.string.mountain_manager) to it) }
            }
            if (tiles.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    tiles.forEach { (label, value) -> InfoTile(label, value, Modifier.weight(1f)) }
                }
            }

            if (mountain.difficultyEstimated) {
                Text(
                    stringResource(R.string.mountain_difficulty_estimated_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (photos.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(stringResource(R.string.mountain_photos))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(photos) { index, url ->
                            // API photos are 250x178, too small for the header
                            AsyncImage(
                                model = url,
                                contentDescription = stringResource(R.string.mountain_photo_description, mountain.name, index + 1),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 168.dp, height = 120.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AcornColors.MossLight)
                            )
                        }
                    }
                }
            }

            Section(
                stringResource(if (mountain.isTop100) R.string.mountain_selection_reason else R.string.mountain_highlights),
                mountain.selectionReason
            )
            Section(stringResource(R.string.mountain_overview), mountain.overview)
            Section(stringResource(R.string.mountain_hiking_points), mountain.hikingPoints)

            if (mountain.courses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(stringResource(R.string.mountain_courses))
                    mountain.courses.forEach { CourseCard(it) }
                }
            }

            Section(stringResource(R.string.mountain_transport), mountain.transport)

            Text(
                listOfNotNull(
                    stringResource(if (mountain.isTop100) R.string.mountain_source else R.string.mountain_source_forest),
                    // top 100 photos are from the API -> separate credit
                    if (mountain.isTop100 && photos.isNotEmpty()) stringResource(R.string.mountain_top100_photo_source) else null
                ).joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    // status bar scrim
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
    )
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AcornColors.Surface)
            .border(1.dp, AcornColors.Outline, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun Section(title: String, body: String) {
    if (body.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(title)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CourseCard(course: MountainCourse) {
    val recommended = "추천" in course.label
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (recommended) AcornColors.MossLight else AcornColors.Surface)
            .border(1.dp, AcornColors.Outline, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(course.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(course.duration, style = MaterialTheme.typography.labelLarge, color = AcornColors.Forest, fontWeight = FontWeight.Bold)
        }
        Text(course.route, style = MaterialTheme.typography.bodySmall)
    }
}
