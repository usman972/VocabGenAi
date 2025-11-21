package com.rameez.hel.repository

import com.rameez.hel.model.WIPModel

interface WIPRepository {
    suspend fun getAllWIPs(): List<WIPModel>
    suspend fun getWIPById(id: String): WIPModel?
    suspend fun insertWIP(wipItem: WIPModel)
    suspend fun updateWIP(wipItem: WIPModel)
    suspend fun deleteWIP(id: String)
    suspend fun getFilteredWIPs(
        categories: List<String>? = null,
        tags: List<String>? = null,
        readCount: Float? = null,
        readOperator: String? = null,
        viewedCount: Float? = null,
        viewedOperator: String? = null,
        word: String? = null,
        meaning: String? = null,
        sampleSentence: String? = null,
        encounteredCount: Int? = null,
        encounteredOperator: String? = null,
        encounteredLastUpdatedFrom: Long? = null,
        encounteredLastUpdatedTo: Long? = null,
        encounteredLastUpdatedOperator: String? = null,
        viewedCountValue: Int? = null,
        viewedCountOperator: String? = null,
        viewedLastUpdatedFrom: Long? = null,
        viewedLastUpdatedTo: Long? = null,
        viewedLastUpdatedOperator: String? = null,
        firstEncounteredFrom: Long? = null,
        firstEncounteredTo: Long? = null,
        firstEncounteredOperator: String? = null,
        firstViewedFrom: Long? = null,
        firstViewedTo: Long? = null,
        firstViewedOperator: String? = null
    ): List<WIPModel>
    suspend fun updateWIPCounts(
        id: String,
        encounteredCount: Int? = null,
        viewedCount: Int? = null,
        encounteredLastUpdatedAt: Long? = null,
        viewedLastUpdatedAt: Long? = null,
        firstEncounteredAt: Long? = null,
        firstViewedAt: Long? = null
    )
    suspend fun importWIPs(items: List<WIPModel>)
    suspend fun exportWIPs(): List<WIPModel>
}
