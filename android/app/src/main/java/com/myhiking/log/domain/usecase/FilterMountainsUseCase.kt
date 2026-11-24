package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainFilter
import com.myhiking.log.domain.model.MountainView
import javax.inject.Inject

// region: top 100 first, then name / difficulty: shortest course first / height: tallest first
// missing value -> excluded when the option is picked, otherwise last
class FilterMountainsUseCase @Inject constructor() {

    operator fun invoke(mountains: List<Mountain>, filter: MountainFilter): List<Mountain> {
        val query = filter.query.trim()
        val matched = mountains.filter { mountain ->
            (query.isEmpty() || mountain.name.contains(query, ignoreCase = true)) &&
                when (filter.view) {
                    MountainView.REGION -> filter.region?.let(mountain::isIn) ?: true
                    MountainView.DIFFICULTY -> filter.difficulty?.let { it == mountain.difficulty } ?: true
                    MountainView.HEIGHT -> filter.height?.let { range ->
                        mountain.heightMeters?.let { it in range } ?: false
                    } ?: true
                }
        }
        return when (filter.view) {
            MountainView.REGION -> matched.sortedWith(
                compareByDescending<Mountain> { it.isTop100 }.thenBy { it.name }
            )
            MountainView.DIFFICULTY -> matched.sortedWith(
                compareBy<Mountain> { it.difficulty == null }
                    .thenBy { it.recommendedCourse?.minutes ?: Int.MAX_VALUE }
                    .thenBy { it.name }
            )
            MountainView.HEIGHT -> matched.sortedByDescending { it.heightMeters ?: -1.0 }
        }
    }
}
