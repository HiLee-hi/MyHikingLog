package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.Difficulty
import com.myhiking.log.domain.model.HeightRange
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainCourse
import com.myhiking.log.domain.model.MountainFilter
import com.myhiking.log.domain.model.MountainView
import com.myhiking.log.domain.model.RegionGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterMountainsUseCaseTest {

    private val filter = FilterMountainsUseCase()

    private fun mountain(
        name: String,
        provinces: List<String>,
        height: Double,
        difficulty: Difficulty,
        minutes: Int?
    ) = Mountain(
        id = name, name = name, address = "", provinces = provinces, heightMeters = height,
        latitude = 0.0, longitude = 0.0, isTop100 = true, hikingTime = null,
        difficulty = difficulty, difficultyEstimated = false, selectionReason = "", overview = "",
        hikingPoints = "", transport = "",
        courses = listOf(MountainCourse("추천코스", "", "", minutes))
    )

    private val bukhan = mountain("북한산", listOf("서울", "경기"), 835.6, Difficulty.MODERATE, 240)
    private val seorak = mountain("설악산", listOf("강원"), 1708.0, Difficulty.HARD, 480)
    private val mani = mountain("마니산", listOf("인천"), 472.1, Difficulty.EASY, 120)
    private val halla = mountain("한라산", listOf("제주"), 1947.3, Difficulty.HARD, null)
    private val all = listOf(seorak, halla, bukhan, mani)

    private fun names(f: MountainFilter) = filter(all, f).map { it.name }

    // API-like: no height/difficulty
    private val unknown = mountain("가마봉", listOf("서울"), 0.0, Difficulty.EASY, null)
        .copy(heightMeters = null, difficulty = null, isTop100 = false, courses = emptyList())
    private val withUnknown = all + unknown

    @Test
    fun `no option keeps everything sorted by name`() {
        assertEquals(listOf("마니산", "북한산", "설악산", "한라산"), names(MountainFilter()))
    }

    @Test
    fun `region matches any of the group's provinces`() {
        assertEquals(listOf("마니산", "북한산"), names(MountainFilter(region = RegionGroup.CAPITAL)))
        assertEquals(listOf("한라산"), names(MountainFilter(region = RegionGroup.JEJU)))
    }

    @Test
    fun `query narrows by name`() {
        assertEquals(listOf("설악산"), names(MountainFilter(query = " 설악 ")))
        assertEquals(emptyList<String>(), names(MountainFilter(query = "지리")))
    }

    @Test
    fun `only the active view's option applies`() {
        val f = MountainFilter(view = MountainView.HEIGHT, region = RegionGroup.JEJU)
        assertEquals(4, filter(all, f).size)
    }

    @Test
    fun `difficulty view sorts by course minutes with unknown last`() {
        assertEquals(
            listOf("마니산", "북한산", "설악산", "한라산"),
            names(MountainFilter(view = MountainView.DIFFICULTY))
        )
        assertEquals(
            listOf("설악산", "한라산"),
            names(MountainFilter(view = MountainView.DIFFICULTY, difficulty = Difficulty.HARD))
        )
    }

    @Test
    fun `height view sorts tallest first and ranges are half open`() {
        assertEquals(
            listOf("한라산", "설악산", "북한산", "마니산"),
            names(MountainFilter(view = MountainView.HEIGHT))
        )
        assertEquals(
            listOf("북한산"),
            names(MountainFilter(view = MountainView.HEIGHT, height = HeightRange.FROM_500_TO_1000))
        )
        assertEquals(true, 1000.0 in HeightRange.OVER_1000)
        assertEquals(false, 1000.0 in HeightRange.FROM_500_TO_1000)
    }

    @Test
    fun `region view lists the bundled 100 before others`() {
        val result = filter(withUnknown, MountainFilter(region = RegionGroup.CAPITAL)).map { it.name }
        assertEquals(listOf("마니산", "북한산", "가마봉"), result)
    }

    @Test
    fun `missing values drop out once an option is picked and sort last otherwise`() {
        assertEquals(
            listOf("북한산"),
            filter(withUnknown, MountainFilter(view = MountainView.HEIGHT, height = HeightRange.FROM_500_TO_1000)).map { it.name }
        )
        assertEquals("가마봉", filter(withUnknown, MountainFilter(view = MountainView.HEIGHT)).last().name)
        assertEquals("가마봉", filter(withUnknown, MountainFilter(view = MountainView.DIFFICULTY)).last().name)
        assertEquals(
            listOf("마니산"),
            filter(withUnknown, MountainFilter(view = MountainView.DIFFICULTY, difficulty = Difficulty.EASY)).map { it.name }
        )
    }
}
