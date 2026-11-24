package com.myhiking.log.data.repository

import android.content.Context
import com.myhiking.log.data.local.parseTop100
import com.myhiking.log.data.remote.ForestMountainApi
import com.myhiking.log.data.remote.parseForestImages
import com.myhiking.log.data.remote.parseForestSearch
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainSearchPage
import com.myhiking.log.domain.repository.MountainRepository
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

// top 100 from assets + 산림청 API. API results cached in memory for the session
class MountainRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ForestMountainApi
) : MountainRepository {

    private val mutex = Mutex()
    private var cache: List<Mountain>? = null
    private val searches = LruCache<String, MountainSearchPage>(30)
    private val photos = LruCache<String, List<String>>(50)

    override suspend fun getTop100(): List<Mountain> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            val text = context.assets.open(ASSET).bufferedReader().use { it.readText() }
            parseTop100(text).also {
                AppLogger.i("REPO_MOUNTAIN", "Loaded ${it.size} top-100 mountains from assets")
            }
        }.also { cache = it }
    }

    override suspend fun search(query: String): MountainSearchPage {
        searches[query]?.let { return it }
        return withContext(Dispatchers.IO) { parseForestSearch(api.search(query)) }.also {
            AppLogger.i("REPO_MOUNTAIN", "Forest search '$query': ${it.mountains.size} of ${it.totalCount}")
            searches[query] = it
        }
    }

    override suspend fun photoUrls(mountain: Mountain): List<String> {
        photos[mountain.id]?.let { return it }
        val urls = withContext(Dispatchers.IO) {
            val listNos = mountain.forestListNo?.let(::listOf) ?: candidatesFor(mountain)
            // can match several district entries, take the first with photos
            var found = emptyList<String>()
            for (listNo in listNos) {
                found = parseForestImages(api.images(listNo))
                if (found.isNotEmpty()) break
            }
            found
        }
        AppLogger.i("REPO_MOUNTAIN", "Photos for ${mountain.name}: ${urls.size}")
        return urls.also { photos[mountain.id] = it }
    }

    // same base name + province overlap, closest height first
    private suspend fun candidatesFor(mountain: Mountain): List<String> =
        search(mountain.name).mountains
            .filter { it.baseName == mountain.name && it.provinces.any(mountain.provinces::contains) }
            .sortedBy { other -> abs((other.heightMeters ?: 0.0) - (mountain.heightMeters ?: 0.0)) }
            .mapNotNull { it.forestListNo }
            .take(3)

    private class LruCache<K, V>(private val max: Int) {
        private val map = object : LinkedHashMap<K, V>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > max
        }
        @Synchronized operator fun get(key: K): V? = map[key]
        @Synchronized operator fun set(key: K, value: V) { map[key] = value }
    }

    private companion object {
        const val ASSET = "top100_mountains.json"
    }
}
