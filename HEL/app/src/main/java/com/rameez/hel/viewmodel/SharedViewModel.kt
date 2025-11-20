// package com.rameez.hel.viewmodel

// import androidx.lifecycle.ViewModel
// import com.rameez.hel.data.model.WIPModel

// class SharedViewModel : ViewModel() {

//     var filteredWipsList = mutableListOf<WIPModel>()
//     var selectedHours: Int? = null
//     var selectedMins: Int? = null
//     var selectedSecs: Int?= null
//     var tagsList = mutableListOf<String>()
//     var categoryList = mutableListOf<String>()
//     var readCount: Float? = null
//     var viewedCount: Float? = null
//     var filteredWord: String? = null
//     var filteredMeaning: String? = null
//     var filteredSampleSen: String? = null
//     var isTimerRunning: Boolean = false
//     var readOperator: String? = null
//     var viewedOperator: String? = null
//     val leftSwipedItemList = arrayListOf<WIPModel>()
//     var notDeletedTags: ArrayList<String>? = null
//     var itemPos: Int? = null
//     var itemId: Int? = null
//     var itemPosFromHome: Int? = null
//     var itemIdFromHome: Int? = null
//     var isReadAloud: Boolean = false
//     var isWIPDeleted = false
//     var isWipAdded: Boolean = false


// }

package com.rameez.hel.viewmodel

import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    // Existing fields
    var categoryList: MutableList<String> = mutableListOf()
    var tagsList: MutableList<String> = mutableListOf()
    var readCount: Float? = null
    var viewedCount: Float? = null
    var readOperator: String? = null
    var viewedOperator: String? = null
    var filteredWord: String? = null
    var filteredMeaning: String? = null
    var filteredSampleSen: String? = null
    var filteredWipsList: MutableList<Any> = mutableListOf()
    var isReadAloud: Boolean = false
    var selectedHours: Int = 0
    var selectedMins: Int = 0
    var selectedSecs: Int = 0

    // NEW FIELDS FOR ENCOUNTERED COUNT FILTERING
    var encounteredCount: Int? = null
    var encounteredOperator: String? = null
    var encounteredLastUpdatedFrom: Long? = null
    var encounteredLastUpdatedTo: Long? = null
    var encounteredLastUpdatedOperator: String? = null

    // NEW FIELDS FOR VIEWED COUNT FILTERING (extended)
    var viewedCountValue: Int? = null
    var viewedCountOperator: String? = null
    var viewedLastUpdatedFrom: Long? = null
    var viewedLastUpdatedTo: Long? = null
    var viewedLastUpdatedOperator: String? = null

    // NEW FIELDS FOR FIRST TIME TIMESTAMPS
    var firstEncounteredFrom: Long? = null
    var firstEncounteredTo: Long? = null
    var firstEncounteredOperator: String? = null

    var firstViewedFrom: Long? = null
    var firstViewedTo: Long? = null
    var firstViewedOperator: String? = null

    fun clearAllFilters() {
        categoryList.clear()
        tagsList.clear()
        readCount = null
        viewedCount = null
        readOperator = null
        viewedOperator = null
        filteredWord = null
        filteredMeaning = null
        filteredSampleSen = null

        // Clear new filters
        encounteredCount = null
        encounteredOperator = null
        encounteredLastUpdatedFrom = null
        encounteredLastUpdatedTo = null
        encounteredLastUpdatedOperator = null

        viewedCountValue = null
        viewedCountOperator = null
        viewedLastUpdatedFrom = null
        viewedLastUpdatedTo = null
        viewedLastUpdatedOperator = null

        firstEncounteredFrom = null
        firstEncounteredTo = null
        firstEncounteredOperator = null

        firstViewedFrom = null
        firstViewedTo = null
        firstViewedOperator = null

        isReadAloud = false
        selectedHours = 0
        selectedMins = 0
        selectedSecs = 0
    }
}
