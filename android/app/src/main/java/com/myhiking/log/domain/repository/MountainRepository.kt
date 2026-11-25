package com.myhiking.log.domain.repository

import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainSearchPage

interface MountainRepository {
    // assets, offline
    suspend fun getTop100(): List<Mountain>

    // 산림청 API, first page only
    suspend fun search(query: String): MountainSearchPage

    // empty if none
    suspend fun photoUrls(mountain: Mountain): List<String>

    // top 100, or an API mountain seen in a search this session
    suspend fun find(id: String): Mountain?
}
