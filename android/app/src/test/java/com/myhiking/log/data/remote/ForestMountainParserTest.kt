package com.myhiking.log.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// saved real responses (test/resources/forest)
class ForestMountainParserTest {

    private fun resource(name: String) =
        javaClass.classLoader!!.getResource("forest/$name")!!.readText()

    @Test
    fun `search maps fields and cleans names`() {
        val page = parseForestSearch(resource("search_bukhan.xml"))
        assertEquals(2, page.totalCount)
        val baegundae = page.mountains.single { it.forestListNo == "113050202" }
        assertEquals("북한산 백운대", baegundae.name)
        assertEquals("북한산", baegundae.baseName)
        assertEquals("forest-113050202", baegundae.id)
        assertEquals("서울특별시 강북구 우이동", baegundae.address)
        assertEquals(listOf("서울"), baegundae.provinces)
        assertEquals(835.6, baegundae.heightMeters!!, 0.01)
        assertEquals("강북구청 · 02-901-6114", baegundae.manager)
        assertTrue(baegundae.selectionReason.startsWith("최고봉인 백운대"))
        assertTrue(baegundae.overview.isNotBlank())
        assertEquals(false, baegundae.isTop100)
        assertNull(baegundae.latitude)
        assertNull(baegundae.difficulty)
    }

    @Test
    fun `zero height means unknown`() {
        val page = parseForestSearch(resource("search_bukhan.xml"))
        assertNull(page.mountains.single { it.name == "북한산" }.heightMeters)
    }

    @Test
    fun `empty result`() {
        val page = parseForestSearch(resource("search_empty.xml"))
        assertEquals(0, page.totalCount)
        assertTrue(page.mountains.isEmpty())
    }

    @Test
    fun `images become ordered urls`() {
        assertEquals(
            listOf(1, 2, 3).map { "${FOREST_IMAGE_BASE}20000317_$it.jpg" },
            parseForestImages(resource("images_bukhan.xml"))
        )
    }

    @Test
    fun `placeholder text counts as empty`() {
        val xml = """<response><header><resultCode>00</resultCode></header><body><items><item>
            <mntiadd>강원특별자치도 정선군 북평면 숙암리</mntiadd><mntidetails>( - )</mntidetails><mntihigh>1273</mntihigh>
            <mntilistno>1</mntilistno><mntiname>갈미봉</mntiname><mntitop> </mntitop></item></items>
            <totalCount>1</totalCount></body></response>"""
        val mountain = parseForestSearch(xml).mountains.single()
        assertEquals("", mountain.overview)
        assertEquals("", mountain.selectionReason)
        assertNull(mountain.manager)
    }

    @Test(expected = ForestApiException::class)
    fun `gateway error throws`() {
        parseForestSearch(
            """<OpenAPI_ServiceResponse><cmmMsgHeader><errMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</errMsg>
            <returnAuthMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</returnAuthMsg><returnReasonCode>30</returnReasonCode>
            </cmmMsgHeader></OpenAPI_ServiceResponse>"""
        )
    }

    @Test(expected = ForestApiException::class)
    fun `non zero result code throws`() {
        parseForestSearch("<response><header><resultCode>22</resultCode><resultMsg>LIMITED</resultMsg></header></response>")
    }
}
