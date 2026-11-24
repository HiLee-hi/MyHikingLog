package com.myhiking.log.data

// same cleanup for top 100 CSV text and 산림청 API text
// sentences stay as in the source, only layout / symbols are fixed

private val FullWidth = mapOf(
    '！' to "!", '？' to "?", '，' to ",", '（' to "(", '）' to ")", '［' to "[", '］' to "]",
    'ㆍ' to "·", '`' to "'"
)

// paragraphs split by a blank line, list items ("- ...") one per line
fun tidyText(raw: String): String {
    val lines = raw
        .replace("\r\n", "\n").replace('\r', '\n')
        .map { FullWidth[it] ?: it.toString() }.joinToString("")
        .split('\n')
        .map { tidyLine(it) }
        // "- A - B" -> two items
        .flatMap { if (it.startsWith("- ")) it.split(" - ").map { item -> "- " + item.removePrefix("- ").trim() } else listOf(it) }
        .filter { it.isNotEmpty() && it != "- " }
    return buildString {
        lines.forEachIndexed { i, line ->
            if (i > 0) append(if (line.startsWith("- ")) "\n" else "\n\n")
            append(line)
        }
    }
}

private fun tidyLine(raw: String): String = raw
    .replace(Regex("\\s+"), " ")
    .trim()
    .replace("->", "→")
    .replace(Regex("^[->]\\s*"), "- ")
    // "있다.산은" -> "있다. 산은"
    .replace(Regex("(?<=[가-힣]\\.)(?=[가-힣A-Za-z])"), " ")
    .replace(" ,", ",")
    .replace(Regex("\\(\\s+"), "(").replace(Regex("\\s+\\)"), ")")
    .let { if (it == "-") "" else it }

fun tidyAddress(raw: String): String =
    raw.map { FullWidth[it] ?: it.toString() }.joinToString("").replace(Regex("\\s+"), " ").trim()

// "[소양강 비경 담은 신비한 암봉]\n가리산은 ..." -> ("소양강 비경 담은 신비한 암봉", "가리산은 ...")
fun splitTagline(text: String): Pair<String?, String> {
    val m = Regex("^\\[([^\\]]+)]\\s*").find(text) ?: return null to text
    return m.groupValues[1].trim() to text.substring(m.range.last + 1)
}

// "기타코스1" -> "기타 코스 1", "코스2(하산전용)" -> "코스 2 (하산 전용)"
fun tidyCourseLabel(raw: String): String = raw
    .replace(Regex("(?<=\\S)코스"), " 코스")
    .replace(Regex("코스(?=\\d)"), "코스 ")
    .replace(Regex("\\s*\\(하산전용\\)"), " (하산 전용)")
    .trim()

// "(편도)약 1시간 50분" -> "편도 약 1시간 50분"
fun tidyDuration(raw: String): String =
    raw.replace(Regex("^\\((편도|왕복)\\)\\s*"), "$1 ").trim()

// "A ① - B ② - C" -> "A ① → B ② → C"
fun tidyRoute(raw: String): String = tidyLine(raw).replace(" - ", " → ")
