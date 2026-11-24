package com.myhiking.log.data.remote

import com.myhiking.log.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton

// 산림청 산정보 서비스 (data.go.kr 15058662), XML only. key: local.properties
@Singleton
class ForestMountainApi @Inject constructor() {

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }

    suspend fun search(query: String, rows: Int = SEARCH_ROWS): String =
        call("mntInfoOpenAPI2", "searchWrd" to query, "numOfRows" to rows.toString())

    suspend fun images(listNo: String): String =
        call("mntInfoImgOpenAPI2", "mntiListNo" to listNo, "numOfRows" to "10")

    private suspend fun call(operation: String, vararg params: Pair<String, String>): String {
        val key = BuildConfig.FOREST_API_KEY
        if (key.isBlank()) throw ForestApiException("FOREST_API_KEY is not set in local.properties")
        return client.get("$BASE/$operation") {
            // use the Decoding key, Ktor encodes it
            parameter("ServiceKey", key)
            parameter("pageNo", "1")
            params.forEach { (name, value) -> parameter(name, value) }
        }.bodyAsText()
    }

    companion object {
        private const val BASE = "https://apis.data.go.kr/1400000/service/cultureInfoService2"
        const val SEARCH_ROWS = 100
    }
}
