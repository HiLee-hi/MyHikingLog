package com.myhiking.log.domain.model

// first page only, totalCount can be bigger
data class MountainSearchPage(
    val mountains: List<Mountain>,
    val totalCount: Int
)
