package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainSearchPage
import com.myhiking.log.domain.repository.MountainRepository
import javax.inject.Inject

// top 100 name match + API results
// API entry already in top 100 -> use the top 100 one (백운대 -> 북한산, no dupes)
class SearchMountainsUseCase @Inject constructor(
    private val repository: MountainRepository
) {
    suspend operator fun invoke(query: String, top100: List<Mountain>): MountainSearchPage {
        val page = repository.search(query.trim())
        return page.copy(mountains = mergeWithTop100(page.mountains, top100))
    }

    companion object {
        // same base name + province overlap = same mountain
        fun mergeWithTop100(found: List<Mountain>, top100: List<Mountain>): List<Mountain> =
            found.map { other ->
                top100.firstOrNull { it.name == other.baseName && it.provinces.any(other.provinces::contains) } ?: other
            }.distinctBy { it.id }

        // empty query -> top 100
        fun results(query: String, top100: List<Mountain>, found: List<Mountain>): List<Mountain> {
            val q = query.trim()
            if (q.isEmpty()) return top100
            return (top100.filter { it.name.contains(q, ignoreCase = true) } + found).distinctBy { it.id }
        }
    }
}
