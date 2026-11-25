package com.myhiking.log.domain.repository

import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.model.SummitRecords
import kotlinx.coroutines.flow.StateFlow

// Supabase only for now, kept in memory for the session
interface SummitRecordRepository {
    val records: StateFlow<SummitRecords>

    // full reload. throws on failure
    suspend fun refresh()

    // insert or update by id, returns the saved row
    suspend fun save(record: SummitRecord): SummitRecord

    // soft delete
    suspend fun delete(id: String)

    // sign-out
    fun clear()
}
