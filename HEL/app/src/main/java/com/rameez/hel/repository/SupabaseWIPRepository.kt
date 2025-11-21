

package com.rameez.hel.repository

import com.rameez.hel.model.WIPModel
import com.rameez.hel.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WIPItemDto(
    val id: String? = null,
    val wip: String? = null,
    val meaning: String? = null,
    val sample_sentence: String? = null,
    val category: String? = null,
    val custom_tag: String? = null,
    val read_count: Float? = null,
    val display_count: Float? = null,
    val created_at: Long? = null,
    val updated_at: Long? = null,
    val uploaded_at: Long? = null,
    val encountered_count: Int? = 0,
    val viewed_count: Int? = 0,
    val encountered_last_updated_at: Long? = null,
    val viewed_last_updated_at: Long? = null,
    val first_encountered_at: Long? = null,
    val first_viewed_at: Long? = null
)

class SupabaseWIPRepository : WIPRepository {

    private val Bolt Database = SupabaseClient.client
    private val json = Json { ignoreUnknownKeys = true }

    private fun WIPItemDto.toWIPModel(): WIPModel {
        return WIPModel(
            id = this.id,
            wip = this.wip,
            meaning = this.meaning,
            sampleSentence = this.sample_sentence,
            category = this.category,
            customTag = this.custom_tag?.split(",")?.map { it.trim() },
            readCount = this.read_count,
            displayCount = this.display_count,
            createdAt = this.created_at,
            updatedAt = this.updated_at,
            uploadedAt = this.uploaded_at,
            encounteredCount = this.encountered_count,
            viewedCount = this.viewed_count,
            encounteredLastUpdatedAt = this.encountered_last_updated_at,
            viewedLastUpdatedAt = this.viewed_last_updated_at,
            firstEncounteredAt = this.first_encountered_at,
            firstViewedAt = this.first_viewed_at
        )
    }

    private fun WIPModel.toDto(): WIPItemDto {
        return WIPItemDto(
            id = this.id,
            wip = this.wip,
            meaning = this.meaning,
            sample_sentence = this.sampleSentence,
            category = this.category,
            custom_tag = this.customTag?.joinToString(","),
            read_count = this.readCount,
            display_count = this.displayCount,
            created_at = this.createdAt,
            updated_at = this.updatedAt,
            uploaded_at = this.uploadedAt,
            encountered_count = this.encounteredCount,
            viewed_count = this.viewedCount,
            encountered_last_updated_at = this.encounteredLastUpdatedAt,
            viewed_last_updated_at = this.viewedLastUpdatedAt,
            first_encountered_at = this.firstEncounteredAt,
            first_viewed_at = this.firstViewedAt
        )
    }

    override suspend fun getAllWIPs(): List<WIPModel> {
        return try {
            val result = supabase.from("wip_items")
                .select()
                .decodeList<WIPItemDto>()
            result.map { it.toWIPModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getWIPById(id: String): WIPModel? {
        return try {
            val result = supabase.from("wip_items")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<WIPItemDto>()
            result?.toWIPModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insertWIP(wipItem: WIPModel) {
        try {
            val currentTime = System.currentTimeMillis()
            val itemToInsert = wipItem.copy(
                id = wipItem.id ?: java.util.UUID.randomUUID().toString(),
                createdAt = currentTime,
                updatedAt = currentTime
            )
            supabase.from("wip_items").insert(itemToInsert.toDto())
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun updateWIP(wipItem: WIPModel) {
        try {
            val currentTime = System.currentTimeMillis()
            val itemToUpdate = wipItem.copy(updatedAt = currentTime)
            supabase.from("wip_items").update(itemToUpdate.toDto()) {
                filter {
                    eq("id", wipItem.id!!)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun deleteWIP(id: String) {
        try {
            supabase.from("wip_items").delete {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun getFilteredWIPs(
        categories: List<String>?,
        tags: List<String>?,
        readCount: Float?,
        readOperator: String?,
        viewedCount: Float?,
        viewedOperator: String?,
        word: String?,
        meaning: String?,
        sampleSentence: String?,
        encounteredCount: Int?,
        encounteredOperator: String?,
        encounteredLastUpdatedFrom: Long?,
        encounteredLastUpdatedTo: Long?,
        encounteredLastUpdatedOperator: String?,
        viewedCountValue: Int?,
        viewedCountOperator: String?,
        viewedLastUpdatedFrom: Long?,
        viewedLastUpdatedTo: Long?,
        viewedLastUpdatedOperator: String?,
        firstEncounteredFrom: Long?,
        firstEncounteredTo: Long?,
        firstEncounteredOperator: String?,
        firstViewedFrom: Long?,
        firstViewedTo: Long?,
        firstViewedOperator: String?
    ): List<WIPModel> {
        return try {
            val allItems = getAllWIPs()
            
            allItems.filter { item ->
                var matches = true

                // Category filter
                if (!categories.isNullOrEmpty()) {
                    matches = matches && categories.contains(item.category)
                }

                // Tags filter
                if (!tags.isNullOrEmpty()) {
                    matches = matches && item.customTag?.any { tags.contains(it) } == true
                }

                // Read count filter
                if (readCount != null && readOperator != null) {
                    val itemReadCount = item.readCount ?: 0f
                    matches = matches && when (readOperator) {
                        ">" -> itemReadCount > readCount
                        "<" -> itemReadCount < readCount
                        "=" -> itemReadCount == readCount
                        ">=" -> itemReadCount >= readCount
                        "<=" -> itemReadCount <= readCount
                        else -> true
                    }
                }

                // Viewed count filter (legacy)
                if (viewedCount != null && viewedOperator != null) {
                    val itemViewedCount = item.displayCount ?: 0f
                    matches = matches && when (viewedOperator) {
                        ">" -> itemViewedCount > viewedCount
                        "<" -> itemViewedCount < viewedCount
                        "=" -> itemViewedCount == viewedCount
                        ">=" -> itemViewedCount >= viewedCount
                        "<=" -> itemViewedCount <= viewedCount
                        else -> true
                    }
                }

                // Word filter
                if (!word.isNullOrEmpty()) {
                    matches = matches && item.wip?.contains(word, ignoreCase = true) == true
                }

                // Meaning filter
                if (!meaning.isNullOrEmpty()) {
                    matches = matches && item.meaning?.contains(meaning, ignoreCase = true) == true
                }

                // Sample sentence filter
                if (!sampleSentence.isNullOrEmpty()) {
                    matches = matches && item.sampleSentence?.contains(sampleSentence, ignoreCase = true) == true
                }

                // Encountered count filter
                if (encounteredCount != null && encounteredOperator != null) {
                    val itemEncounteredCount = item.encounteredCount ?: 0
                    matches = matches && when (encounteredOperator) {
                        ">" -> itemEncounteredCount > encounteredCount
                        "<" -> itemEncounteredCount < encounteredCount
                        "=" -> itemEncounteredCount == encounteredCount
                        ">=" -> itemEncounteredCount >= encounteredCount
                        "<=" -> itemEncounteredCount <= encounteredCount
                        else -> true
                    }
                }

                // Encountered last updated filter
                if (encounteredLastUpdatedFrom != null && encounteredLastUpdatedTo != null && encounteredLastUpdatedOperator != null) {
                    val itemTimestamp = item.encounteredLastUpdatedAt ?: 0L
                    matches = matches && when (encounteredLastUpdatedOperator) {
                        "between" -> itemTimestamp in encounteredLastUpdatedFrom..encounteredLastUpdatedTo
                        "before" -> itemTimestamp < encounteredLastUpdatedFrom
                        "after" -> itemTimestamp > encounteredLastUpdatedTo
                        else -> true
                    }
                }

                // Viewed count filter (new)
                if (viewedCountValue != null && viewedCountOperator != null) {
                    val itemViewedCountValue = item.viewedCount ?: 0
                    matches = matches && when (viewedCountOperator) {
                        ">" -> itemViewedCountValue > viewedCountValue
                        "<" -> itemViewedCountValue < viewedCountValue
                        "=" -> itemViewedCountValue == viewedCountValue
                        ">=" -> itemViewedCountValue >= viewedCountValue
                        "<=" -> itemViewedCountValue <= viewedCountValue
                        else -> true
                    }
                }

                // Viewed last updated filter
                if (viewedLastUpdatedFrom != null && viewedLastUpdatedTo != null && viewedLastUpdatedOperator != null) {
                    val itemTimestamp = item.viewedLastUpdatedAt ?: 0L
                    matches = matches && when (viewedLastUpdatedOperator) {
                        "between" -> itemTimestamp in viewedLastUpdatedFrom..viewedLastUpdatedTo
                        "before" -> itemTimestamp < viewedLastUpdatedFrom
                        "after" -> itemTimestamp > viewedLastUpdatedTo
                        else -> true
                    }
                }

                // First encountered filter
                if (firstEncounteredFrom != null && firstEncounteredTo != null && firstEncounteredOperator != null) {
                    val itemTimestamp = item.firstEncounteredAt ?: 0L
                    matches = matches && when (firstEncounteredOperator) {
                        "between" -> itemTimestamp in firstEncounteredFrom..firstEncounteredTo
                        "before" -> itemTimestamp < firstEncounteredFrom
                        "after" -> itemTimestamp > firstEncounteredTo
                        else -> true
                    }
                }

                // First viewed filter
                if (firstViewedFrom != null && firstViewedTo != null && firstViewedOperator != null) {
                    val itemTimestamp = item.firstViewedAt ?: 0L
                    matches = matches && when (firstViewedOperator) {
                        "between" -> itemTimestamp in firstViewedFrom..firstViewedTo
                        "before" -> itemTimestamp < firstViewedFrom
                        "after" -> itemTimestamp > firstViewedTo
                        else -> true
                    }
                }

                matches
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateWIPCounts(
        id: String,
        encounteredCount: Int?,
        viewedCount: Int?,
        encounteredLastUpdatedAt: Long?,
        viewedLastUpdatedAt: Long?,
        firstEncounteredAt: Long?,
        firstViewedAt: Long?
    ) {
        try {
            val currentItem = getWIPById(id) ?: return
            
            val updates = mutableMapOf<String, Any>()
            
            encounteredCount?.let { updates["encountered_count"] = it }
            viewedCount?.let { updates["viewed_count"] = it }
            encounteredLastUpdatedAt?.let { updates["encountered_last_updated_at"] = it }
            viewedLastUpdatedAt?.let { updates["viewed_last_updated_at"] = it }
            firstEncounteredAt?.let { updates["first_encountered_at"] = it }
            firstViewedAt?.let { updates["first_viewed_at"] = it }
            updates["updated_at"] = System.currentTimeMillis()

            if (updates.isNotEmpty()) {
                supabase.from("wip_items").update(updates) {
                    filter {
                        eq("id", id)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun importWIPs(items: List<WIPModel>) {
        try {
            items.forEach { item ->
                val itemWithTimestamps = item.copy(
                    id = item.id ?: java.util.UUID.randomUUID().toString(),
                    createdAt = item.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                supabase.from("wip_items").insert(itemWithTimestamps.toDto())
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun exportWIPs(): List<WIPModel> {
        return getAllWIPs()
    }
}
