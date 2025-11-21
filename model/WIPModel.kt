
package com.rameez.hel.model

import kotlinx.serialization.Serializable

@Serializable
data class WIPModel(
    val id: String? = null,
    val wip: String? = null,
    val meaning: String? = null,
    val sampleSentence: String? = null,
    val category: String? = null,
    val customTag: List<String>? = null,
    val readCount: Float? = null,
    val displayCount: Float? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val uploadedAt: Long? = null,
    
    // NEW FIELDS - Add these to your existing model
    val encounteredCount: Int? = 0,
    val viewedCount: Int? = 0,
    val encounteredLastUpdatedAt: Long? = null,
    val viewedLastUpdatedAt: Long? = null,
    val firstEncounteredAt: Long? = null,
    val firstViewedAt: Long? = null
)
