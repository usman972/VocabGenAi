package com.rameez.hel.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rameez.hel.data.model.WIPModel

@Dao
interface WIPDao {

    // Insert raw (used by repository wrapper to set timestamps)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaw(wipItem: WIPModel): Long

    // Wrapper is implemented in repository for timestamp logic (keeping DAO small).
    // Basic queries:
    @Query("SELECT * FROM WIP_LIST ORDER BY id DESC")
    fun getWIPs(): LiveData<List<WIPModel>>

    @Query("SELECT * FROM WIP_LIST ORDER BY id DESC")
    suspend fun getWIPs2(): List<WIPModel>

    @Query("DELETE FROM WIP_LIST")
    suspend fun dropTable()

    @Query("SELECT * FROM WIP_LIST WHERE id = :id")
    fun getWIPById(id: Int): LiveData<WIPModel>

    @Query("""
        UPDATE WIP_LIST 
        SET category = :category, 
            wip = :wip, 
            meaning = :meaning, 
            sampleSentence = :sampleSentence, 
            customTag = :customTag, 
            readCount = :readCount, 
            displayCount = :displayCount,
            updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateWIP(
        id: Int,
        category: String,
        wip: String,
        meaning: String,
        sampleSentence: String,
        customTag: List<String>,
        readCount: Float,
        displayCount: Float,
        updatedAt: Long
    )

    @Query("UPDATE WIP_LIST SET readCount = :readCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReadCount(id: Int, readCount: Float, updatedAt: Long)

    @Query("UPDATE WIP_LIST SET displayCount = :viewCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateViewedCount(id: Int, viewCount: Float, updatedAt: Long)

    @Query("SELECT * FROM WIP_LIST WHERE customTag LIKE '%' || :tag || '%'")
    fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>>

    @Query("DELETE FROM WIP_LIST WHERE id = :id")
    suspend fun deleteWIPbyId(id: Int)

    @Query("DELETE FROM WIP_LIST WHERE category IN (:categories)")
    suspend fun deleteWholeCategory(categories: List<String?>)

    @Query("UPDATE WIP_LIST SET readCount = 0.0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun resetEncountered(id: Int, updatedAt: Long)

    @Query("UPDATE WIP_LIST SET displayCount = 0.0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun resetViewedCount(id: Int, updatedAt: Long)

    @Query("UPDATE WIP_LIST SET readCount = 0.0, updatedAt = :updatedAt WHERE category IN (:categories)")
    suspend fun resetEncounteredForCategories(categories: List<String>, updatedAt: Long)

    @Query("UPDATE WIP_LIST SET displayCount = 0.0, updatedAt = :updatedAt WHERE category IN (:categories)")
    suspend fun resetViewedForCategories(categories: List<String>, updatedAt: Long)

    @Query("UPDATE WIP_LIST SET uploadedAt = :uploadedAt, updatedAt = :uploadedAt WHERE id = :id")
    suspend fun markUploaded(id: Int, uploadedAt: Long)
    
    // FILTERS by timestamp ranges (useful for filter UI)
    @Query("SELECT * FROM WIP_LIST WHERE createdAt BETWEEN :start AND :end ORDER BY id DESC")
    fun filterByCreatedRange(start: Long, end: Long): LiveData<List<WIPModel>>

    @Query("SELECT * FROM WIP_LIST WHERE updatedAt BETWEEN :start AND :end ORDER BY id DESC")
    fun filterByUpdatedRange(start: Long, end: Long): LiveData<List<WIPModel>>

    @Query("SELECT * FROM WIP_LIST WHERE uploadedAt BETWEEN :start AND :end ORDER BY id DESC")
    fun filterByUploadedRange(start: Long, end: Long): LiveData<List<WIPModel>>
}
