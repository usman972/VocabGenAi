package com.rameez.hel.data.repository

import androidx.lifecycle.LiveData
import com.rameez.hel.data.WIPDao
import com.rameez.hel.data.model.WIPModel

class WIPRepository(private val wipDao: WIPDao) {

    suspend fun insertWIP(wipItem: WIPModel) {
        val now = System.currentTimeMillis()
        if (wipItem.createdAt == null) {
            wipItem.createdAt = now
        }
        wipItem.updatedAt = now
        wipDao.insertRaw(wipItem)
    }

    fun getWIPs(): LiveData<List<WIPModel>> = wipDao.getWIPs()

    suspend fun getWIPs2(): List<WIPModel> = wipDao.getWIPs2()

    suspend fun dropTable() = wipDao.dropTable()

    fun getWIPById(id: Int): LiveData<WIPModel> = wipDao.getWIPById(id)

    suspend fun updateWIP(
        id: Int,
        category: String,
        wip: String,
        meaning: String,
        sampleSentence: String,
        customTag: List<String>,
        readCount: Float,
        displayCount: Float
    ) {
        val now = System.currentTimeMillis()
        wipDao.updateWIP(id, category, wip, meaning, sampleSentence, customTag, readCount, displayCount, now)
    }

    suspend fun updateReadCount(id: Int, readCount: Float) {
        val now = System.currentTimeMillis()
        wipDao.updateReadCount(id, readCount, now)
    }

    fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>> = wipDao.getWIPsWithCustomTag(tag)

    suspend fun updateViewedCount(id: Int, viewCount: Float) {
        val now = System.currentTimeMillis()
        wipDao.updateViewedCount(id, viewCount, now)
    }

    suspend fun deleteWIPById(id: Int) = wipDao.deleteWIPbyId(id)

    suspend fun deleteWholeCategory(categories: List<String?>) = wipDao.deleteWholeCategory(categories)

    suspend fun resetEncountered(id: Int) {
        val now = System.currentTimeMillis()
        wipDao.resetEncountered(id, now)
    }

    suspend fun resetViewed(id: Int) {
        val now = System.currentTimeMillis()
        wipDao.resetViewedCount(id, now)
    }

    suspend fun resetEncounteredForCategories(categories: List<String>) {
        val now = System.currentTimeMillis()
        wipDao.resetEncounteredForCategories(categories, now)
    }

    suspend fun resetViewedForCategories(categories: List<String>) {
        val now = System.currentTimeMillis()
        wipDao.resetViewedForCategories(categories, now)
    }

    suspend fun markUploaded(id: Int) {
        val now = System.currentTimeMillis()
        wipDao.markUploaded(id, now)
    }

    // Filters
    fun filterByCreatedRange(start: Long, end: Long) = wipDao.filterByCreatedRange(start, end)
    fun filterByUpdatedRange(start: Long, end: Long) = wipDao.filterByUpdatedRange(start, end)
    fun filterByUploadedRange(start: Long, end: Long) = wipDao.filterByUploadedRange(start, end)
}
