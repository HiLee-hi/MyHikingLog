package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.provincesIn
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchMountainsUseCaseTest {

    private fun mountain(name: String, address: String, top100: Boolean) = Mountain(
        id = name + address, name = name, address = address, provinces = provincesIn(address),
        heightMeters = null, latitude = null, longitude = null, isTop100 = top100, hikingTime = null,
        difficulty = null, difficultyEstimated = false, selectionReason = "", overview = "",
        hikingPoints = "", courses = emptyList(), transport = ""
    )

    private val bukhan = mountain("북한산", "서울특별시 강북구, 경기도 고양시", top100 = true)
    private val gwanak = mountain("관악산", "서울특별시 관악구", top100 = true)

    @Test
    fun `api entries the bundled 100 cover become that mountain`() {
        val found = listOf(
            mountain("북한산", "서울특별시 서대문구 홍은동", top100 = false),
            mountain("북한산 백운대", "서울특별시 강북구 우이동", top100 = false),
            // same name, other province
            mountain("북한산", "전라남도 어딘가", top100 = false),
            mountain("북한산성", "경기도 고양시", top100 = false),
        )
        assertEquals(
            listOf("북한산/서울특별시 강북구, 경기도 고양시", "북한산/전라남도 어딘가", "북한산성/경기도 고양시"),
            SearchMountainsUseCase.mergeWithTop100(found, listOf(bukhan)).map { "${it.name}/${it.address}" }
        )
    }

    @Test
    fun `a peak query still finds its bundled mountain`() {
        val found = SearchMountainsUseCase.mergeWithTop100(
            listOf(mountain("북한산 백운대", "서울특별시 강북구 우이동", top100 = false)),
            listOf(bukhan, gwanak)
        )
        assertEquals(listOf(bukhan), SearchMountainsUseCase.results("백운대", listOf(bukhan, gwanak), found))
    }

    @Test
    fun `results list name matches once and everything when the query is empty`() {
        val top100 = listOf(bukhan, gwanak)
        assertEquals(listOf(bukhan), SearchMountainsUseCase.results(" 북한 ", top100, listOf(bukhan)))
        assertEquals(top100, SearchMountainsUseCase.results("", top100, emptyList()))
        // API failed
        assertEquals(listOf(gwanak), SearchMountainsUseCase.results("관악", top100, emptyList()))
    }

    @Test
    fun `provinces from old and new names`() {
        assertEquals(listOf("강원"), provincesIn("강원특별자치도 인제군"))
        assertEquals(listOf("강원"), provincesIn("강원도 홍천군"))
        assertEquals(listOf("경북", "경남"), provincesIn("경상북도 성주군, 경상남도 합천군"))
        assertEquals(emptyList<String>(), provincesIn("어딘가"))
    }
}
