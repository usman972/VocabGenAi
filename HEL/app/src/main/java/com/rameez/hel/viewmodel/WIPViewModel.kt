// package com.rameez.hel.viewmodel

// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.rameez.hel.data.WIPDatabase
// import com.rameez.hel.data.model.WIPModel
// import com.rameez.hel.data.repository.WIPRepository
// import kotlinx.coroutines.launch

// class WIPViewModel : ViewModel() {

//     private val wipDao = WIPDatabase.getDatabase()?.wipDao()
//     private val wipRepository = wipDao?.let { WIPRepository(it) }

//     val _getWipsObserver: MutableLiveData<List<WIPModel>> = MutableLiveData()
//     val getWipsObserver: LiveData<List<WIPModel>> = _getWipsObserver
//     var list = arrayListOf<WIPModel>()

//     fun insertWIP(wipItem: WIPModel) = viewModelScope.launch {
//         wipRepository?.insertWIP(wipItem)
//     }

//     fun getWIPs(): LiveData<List<WIPModel>>? = wipRepository?.getWIPs()

//     suspend fun getWIPs2(): List<WIPModel>? = wipRepository?.getWIPs2()

//     fun dropTable() = viewModelScope.launch {
//         wipRepository?.dropTable()
//     }

//     fun getWIPById(id: Int): LiveData<WIPModel>? = wipRepository?.getWIPById(id)

//     fun updateWIP(
//         id: Int,
//         category: String,
//         wip: String,
//         meaning: String,
//         sampleSentence: String,
//         customTag: List<String>,
//         readCount: Float,
//         viewedCount: Float
//     ) = viewModelScope.launch {
//         wipRepository?.updateWIP(id, category, wip, meaning, sampleSentence, customTag, readCount, viewedCount)
//     }

//     fun updateReadCount(id: Int, readCount: Float) = viewModelScope.launch {
//         wipRepository?.updateReadCount(id, readCount)
//     }

//     fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>>? = wipRepository?.getWIPsWithCustomTag(tag)

//     fun updateViewedCount(id: Int, viewCount: Float) = viewModelScope.launch {
//         wipRepository?.updateViewedCount(id, viewCount)
//     }

//     fun deleteWIPById(id: Int) = viewModelScope.launch {
//         wipRepository?.deleteWIPById(id)
//     }

//     fun deleteWholeCategory(categories: List<String?>) = viewModelScope.launch {
//         wipRepository?.deleteWholeCategory(categories)
//     }

//     fun resetEncountered(id: Int) = viewModelScope.launch {
//         wipRepository?.resetEncountered(id)
//     }

//     fun resetViewed(id: Int) = viewModelScope.launch {
//         wipRepository?.resetViewed(id)
//     }

//     fun resetEncounteredForCategories(categories: List<String>) = viewModelScope.launch {
//         wipRepository?.resetEncounteredForCategories(categories)
//     }

//     fun resetViewedForCategories(categories: List<String>) = viewModelScope.launch {
//         wipRepository?.resetViewedForCategories(categories)
//     }

//     fun markUploaded(id: Int) = viewModelScope.launch {
//         wipRepository?.markUploaded(id)
//     }

//     // Filters (returns LiveData)
//     fun filterByCreatedRange(start: Long, end: Long) = wipRepository?.filterByCreatedRange(start, end)
//     fun filterByUpdatedRange(start: Long, end: Long) = wipRepository?.filterByUpdatedRange(start, end)
//     fun filterByUploadedRange(start: Long, end: Long) = wipRepository?.filterByUploadedRange(start, end)
// }


package com.rameez.hel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rameez.hel.model.WIPItem
import com.rameez.hel.repository.WIPRepository
import kotlinx.coroutines.launch

class WIPViewModel(private val repository: WIPRepository) : ViewModel() {

    private val _wips = MutableLiveData<List<WIPItem>>()
    val wips: LiveData<List<WIPItem>> get() = _wips

    fun getWIPs(): LiveData<List<WIPItem>>? {
        viewModelScope.launch {
            val data = repository.getAllWIPs()
            _wips.postValue(data)
        }
        return _wips
    }

    fun getWIPById(id: String): WIPItem? {
        return _wips.value?.find { it.id == id }
    }

    // NEW METHOD: Increment viewed count (called automatically when flashcard is displayed)
    fun incrementViewedCount(wipId: String) {
        viewModelScope.launch {
            val currentWip = getWIPById(wipId)
            val newCount = (currentWip?.viewedCount ?: 0) + 1
            val currentTime = System.currentTimeMillis()

            // Set firstViewedAt only if it's null (first time)
            val firstViewedAt = currentWip?.firstViewedAt ?: currentTime

            repository.updateWIPCounts(
                id = wipId,
                viewedCount = newCount,
                viewedLastUpdatedAt = currentTime,
                firstViewedAt = firstViewedAt
            )

            // Refresh the list
            getWIPs()
        }
    }

    // NEW METHOD: Increment encountered count (called manually via + button)
    fun incrementEncounteredCount(wipId: String) {
        viewModelScope.launch {
            val currentWip = getWIPById(wipId)
            val newCount = (currentWip?.encounteredCount ?: 0) + 1
            val currentTime = System.currentTimeMillis()

            // Set firstEncounteredAt only if it's null (first time)
            val firstEncounteredAt = currentWip?.firstEncounteredAt ?: currentTime

            repository.updateWIPCounts(
                id = wipId,
                encounteredCount = newCount,
                encounteredLastUpdatedAt = currentTime,
                firstEncounteredAt = firstEncounteredAt
            )

            // Refresh the list
            getWIPs()
        }
    }

    // NEW METHOD: Decrement encountered count (called manually via - button)
    fun decrementEncounteredCount(wipId: String) {
        viewModelScope.launch {
            val currentWip = getWIPById(wipId)
            val newCount = ((currentWip?.encounteredCount ?: 0) - 1).coerceAtLeast(0)
            val currentTime = System.currentTimeMillis()

            repository.updateWIPCounts(
                id = wipId,
                encounteredCount = newCount,
                encounteredLastUpdatedAt = currentTime,
                firstEncounteredAt = currentWip?.firstEncounteredAt // Don't change first time
            )

            // Refresh the list
            getWIPs()
        }
    }

    // NEW METHOD: Decrement viewed count (if needed manually)
    fun decrementViewedCount(wipId: String) {
        viewModelScope.launch {
            val currentWip = getWIPById(wipId)
            val newCount = ((currentWip?.viewedCount ?: 0) - 1).coerceAtLeast(0)
            val currentTime = System.currentTimeMillis()

            repository.updateWIPCounts(
                id = wipId,
                viewedCount = newCount,
                viewedLastUpdatedAt = currentTime,
                firstViewedAt = currentWip?.firstViewedAt // Don't change first time
            )

            // Refresh the list
            getWIPs()
        }
    }

    fun insertWIP(wipItem: WIPItem) {
        viewModelScope.launch {
            repository.insertWIP(wipItem)
            getWIPs()
        }
    }

    fun updateWIP(wipItem: WIPItem) {
        viewModelScope.launch {
            repository.updateWIP(wipItem)
            getWIPs()
        }
    }

    fun deleteWIP(wipId: String) {
        viewModelScope.launch {
            repository.deleteWIP(wipId)
            getWIPs()
        }
    }
}
