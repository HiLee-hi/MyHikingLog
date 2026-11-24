package com.myhiking.log.data

import com.myhiking.log.data.local.parseTop100
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SourceTextTest {

    @Test
    fun csvTransportBecomesParagraphsAndList() {
        // convert_top100.py output for 금산
        val raw = "기점은 남해다.\n남해 공용여객터미널에서 상주, 미조행 군내버스를 이용하면 금산에서 하차할 수 있다.\n" +
            "- 남해→상주, 미조 : 30~40분 간격 운행 - 남해→복곡 : 1일 4회 운행\n남해고속국도-진교 또는 하동IC-남해대교"
        assertEquals(
            "기점은 남해다.\n\n남해 공용여객터미널에서 상주, 미조행 군내버스를 이용하면 금산에서 하차할 수 있다.\n" +
                "- 남해→상주, 미조 : 30~40분 간격 운행\n- 남해→복곡 : 1일 4회 운행\n\n남해고속국도-진교 또는 하동IC-남해대교",
            tidyText(raw)
        )
    }

    @Test
    fun apiLineBreaksAndSymbols() {
        val raw = "！ 사량도가 아름답게 태어났다.\r\n\r\n\r\n감추어진 비경.\r\r\n `가야국'이 있었던 곳 ［露積］바위ㆍ선녀바위 "
        assertEquals("! 사량도가 아름답게 태어났다.\n\n감추어진 비경.\n\n'가야국'이 있었던 곳 [露積]바위·선녀바위", tidyText(raw))
    }

    @Test
    fun missingSpaceAfterSentence() {
        assertEquals("인접하고 있다. 산은 봉두산(753m)", tidyText("인접하고 있다.산은 봉두산(753m)"))
        // decimals and abbreviations stay
        assertEquals("해발 1376.1m, 'IC-6번 국도", tidyText("해발 1376.1m, 'IC-6번 국도"))
    }

    @Test
    fun overviewTagline() {
        val (tagline, body) = splitTagline(tidyText("[소양강 비경 담은 신비한 암봉]\n가리산은 해발 1051m의 산이다."))
        assertEquals("소양강 비경 담은 신비한 암봉", tagline)
        assertEquals("가리산은 해발 1051m의 산이다.", body)
        assertNull(splitTagline("구룡덕[봉]은 민둥이다.").first)
    }

    @Test
    fun courseFields() {
        assertEquals("추천 코스", tidyCourseLabel("추천코스"))
        assertEquals("기타 코스 1", tidyCourseLabel("기타코스1"))
        assertEquals("코스 2 (하산 전용)", tidyCourseLabel("코스2(하산전용)"))
        assertEquals("편도 약 1시간 50분", tidyDuration("(편도)약 1시간 50분"))
        assertEquals("매표소 ① → 강천사 ② → (도계 종주) → 강천사 ②", tidyRoute("매표소 ① - 강천사 ② - ( 도계 종주 ) - 강천사 ②"))
    }

    @Test
    fun addressSpacing() {
        assertEquals("서울특별시 강북구 우이동", tidyAddress("서울특별시  강북구 우이동 "))
        assertEquals("강원도 홍천군 두촌면·화촌면", tidyAddress("강원도 홍천군 두촌면ㆍ화촌면"))
    }

    @Test
    fun wholeTop100AssetIsTidy() {
        val all = parseTop100(File("src/main/assets/top100_mountains.json").readText())
        assertEquals(100, all.count { it.tagline != null })
        val texts = all.flatMap { listOf(it.address, it.selectionReason, it.overview, it.hikingPoints, it.transport) } +
            all.flatMap { m -> m.courses.flatMap { listOf(it.label, it.route, it.duration) } }
        val bad = Regex("[가-힣]\\.[가-힣]| {2}|[！？，（）［］ㆍ`]|\\( | \\)|\n{3}|^\\[")
        val hits = texts.filter { bad.containsMatchIn(it) }
        assertTrue(hits.take(3).joinToString("\n---\n"), hits.isEmpty())
    }
}
