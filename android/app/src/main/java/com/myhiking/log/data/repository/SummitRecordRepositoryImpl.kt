package com.myhiking.log.data.repository

import com.myhiking.log.data.remote.supabase.SummitRecordDto
import com.myhiking.log.data.remote.supabase.toDomain
import com.myhiking.log.data.remote.supabase.toWriteDto
import com.myhiking.log.domain.model.LoadStatus
import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.model.SummitRecordOrder
import com.myhiking.log.domain.model.SummitRecords
import com.myhiking.log.domain.repository.SummitRecordRepository
import com.myhiking.log.util.AppLogger
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject

class SummitRecordRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : SummitRecordRepository {

    private val _records = MutableStateFlow(SummitRecords())
    override val records: StateFlow<SummitRecords> = _records.asStateFlow()

    private fun userId(): String =
        auth.currentUserOrNull()?.id ?: throw IllegalStateException("not signed in")

    override suspend fun refresh() {
        // keep showing the old list while reloading
        _records.update { it.copy(status = LoadStatus.LOADING) }
        try {
            val rows = postgrest[TABLE].select {
                filter { exact("deleted_at", null) }
                order("climbed_on", Order.DESCENDING)
                order("created_at", Order.DESCENDING)
            }.decodeList<SummitRecordDto>()
            _records.value = SummitRecords(LoadStatus.LOADED, rows.map { it.toDomain() })
            AppLogger.i("REPO_SUMMIT", "Loaded ${rows.size} summit records")
        } catch (e: CancellationException) {
            // account switch / sign-out, not a failure
            throw e
        } catch (e: Exception) {
            AppLogger.e("REPO_SUMMIT", "Loading summit records failed: ${e.message}", e)
            _records.update { it.copy(status = LoadStatus.FAILED) }
            throw e
        }
    }

    override suspend fun save(record: SummitRecord): SummitRecord {
        AppLogger.i("REPO_SUMMIT", "Save: id=${AppLogger.shortId(record.id)}, mountain=${record.mountainId}")
        try {
            val saved = postgrest[TABLE].upsert(record.toWriteDto(userId())) {
                onConflict = "id"
                select()
            }.decodeSingle<SummitRecordDto>().toDomain()
            _records.update { state ->
                state.copy(items = (state.items.filterNot { it.id == saved.id } + saved).sortedWith(SummitRecordOrder))
            }
            AppLogger.i("REPO_SUMMIT", "Save success: id=${AppLogger.shortId(saved.id)}")
            return saved
        } catch (e: Exception) {
            AppLogger.e("REPO_SUMMIT", "Save failed: id=${AppLogger.shortId(record.id)}, error=${e.message}", e)
            throw e
        }
    }

    override suspend fun delete(id: String) {
        AppLogger.i("REPO_SUMMIT", "Delete: id=${AppLogger.shortId(id)}")
        try {
            postgrest[TABLE].update({ set("deleted_at", Instant.now().toString()) }) {
                filter { eq("id", id) }
            }
            _records.update { state -> state.copy(items = state.items.filterNot { it.id == id }) }
            AppLogger.i("REPO_SUMMIT", "Delete success: id=${AppLogger.shortId(id)}")
        } catch (e: Exception) {
            AppLogger.e("REPO_SUMMIT", "Delete failed: id=${AppLogger.shortId(id)}, error=${e.message}", e)
            throw e
        }
    }

    override fun clear() {
        _records.value = SummitRecords()
    }

    private companion object {
        const val TABLE = "summit_records"
    }
}
