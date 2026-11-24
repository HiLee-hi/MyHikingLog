package com.myhiking.log.data.remote

import com.myhiking.log.data.tidyAddress
import com.myhiking.log.data.tidyText
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainSearchPage
import com.myhiking.log.domain.model.provincesIn
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

// API error response (bad key, wrong operation, quota)
class ForestApiException(message: String) : Exception(message)

// base path for imgfilename
internal const val FOREST_IMAGE_BASE = "https://www.forest.go.kr/images/data/down/mountain/"

// mntInfoOpenAPI2 XML. javax.xml so it runs in JVM tests
// same mountain comes once per district -> drop exact dupes (name + address)
fun parseForestSearch(xml: String): MountainSearchPage {
    val root = parse(xml)
    val mountains = root.items().map { item ->
        val listNo = item.text("mntilistno")
        val address = tidyAddress(item.text("mntiadd"))
        val manager = listOf(item.text("mntiadmin"), item.text("mntiadminnum"))
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        Mountain(
            id = "forest-$listNo",
            name = item.text("mntiname").replace('_', ' ').replace(Regex("\\s+"), " ").trim(),
            address = address,
            provinces = provincesIn(address),
            heightMeters = item.text("mntihigh").toDoubleOrNull()?.takeIf { it > 0 },
            latitude = null,
            longitude = null,
            isTop100 = false,
            hikingTime = null,
            difficulty = null,
            difficultyEstimated = false,
            selectionReason = tidyText(item.prose("mntitop")),
            tagline = null,
            overview = tidyText(item.prose("mntidetails")),
            hikingPoints = "",
            courses = emptyList(),
            transport = "",
            forestListNo = listNo,
            manager = manager.ifBlank { null }
        )
    }.distinctBy { it.name to it.address }
    val total = root.firstText("totalCount").toIntOrNull() ?: mountains.size
    return MountainSearchPage(mountains, total)
}

// mntInfoImgOpenAPI2 XML -> image URLs, sorted by imgno
fun parseForestImages(xml: String): List<String> =
    parse(xml).items()
        .sortedBy { it.text("imgno").toIntOrNull() ?: Int.MAX_VALUE }
        .map { it.text("imgfilename") }
        .filter { it.isNotBlank() }
        .map { FOREST_IMAGE_BASE + it }

private fun parse(xml: String): Element {
    val factory = DocumentBuilderFactory.newInstance().apply {
        // block DOCTYPE when supported (JVM). Android throws on this feature, so ignore failure
        runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        isExpandEntityReferences = false
    }
    val root = factory.newDocumentBuilder().parse(InputSource(StringReader(xml))).documentElement
    if (root.tagName == "OpenAPI_ServiceResponse") {
        throw ForestApiException(root.firstText("returnAuthMsg").ifBlank { root.firstText("errMsg") })
    }
    val code = root.firstText("resultCode")
    if (code.isNotEmpty() && code != "00") throw ForestApiException("resultCode=$code ${root.firstText("resultMsg")}")
    return root
}

private fun Element.items(): List<Element> {
    val nodes = getElementsByTagName("item")
    return (0 until nodes.length).map { nodes.item(it) as Element }
}

private fun Element.firstText(tag: String): String =
    getElementsByTagName(tag).item(0)?.textContent?.trim().orEmpty()

private fun Element.text(tag: String): String = firstText(tag)

// empty fields come as "( - )" etc.
private fun Element.prose(tag: String): String =
    text(tag).takeUnless { it.all { c -> c in "()-. " } }.orEmpty()
