package com.myhiking.log.domain.model

// finder tabs
enum class MountainView { REGION, DIFFICULTY, HEIGHT }

// shared by list and map (step 5). null = all
data class MountainFilter(
    val query: String = "",
    val view: MountainView = MountainView.REGION,
    val region: RegionGroup? = null,
    val difficulty: Difficulty? = null,
    val height: HeightRange? = null
)
