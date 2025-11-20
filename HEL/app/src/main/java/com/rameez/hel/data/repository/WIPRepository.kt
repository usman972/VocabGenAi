// package com.rameez.hel.data.repository

// import androidx.lifecycle.LiveData
// import com.rameez.hel.data.WIPDao
// import com.rameez.hel.data.model.WIPModel

// class WIPRepository(private val wipDao: WIPDao) {

//     suspend fun insertWIP(wipItem: WIPModel) {
//         val now = System.currentTimeMillis()
//         if (wipItem.createdAt == null) {
//             wipItem.createdAt = now
//         }
//         wipItem.updatedAt = now
//         wipDao.insertRaw(wipItem)
//     }

//     fun getWIPs(): LiveData<List<WIPModel>> = wipDao.getWIPs()

//     suspend fun getWIPs2(): List<WIPModel> = wipDao.getWIPs2()

//     suspend fun dropTable() = wipDao.dropTable()

//     fun getWIPById(id: Int): LiveData<WIPModel> = wipDao.getWIPById(id)

//     suspend fun updateWIP(
//         id: Int,
//         category: String,
//         wip: String,
//         meaning: String,
//         sampleSentence: String,
//         customTag: List<String>,
//         readCount: Float,
//         displayCount: Float
//     ) {
//         val now = System.currentTimeMillis()
//         wipDao.updateWIP(id, category, wip, meaning, sampleSentence, customTag, readCount, displayCount, now)
//     }

//     suspend fun updateReadCount(id: Int, readCount: Float) {
//         val now = System.currentTimeMillis()
//         wipDao.updateReadCount(id, readCount, now)
//     }

//     fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>> = wipDao.getWIPsWithCustomTag(tag)

//     suspend fun updateViewedCount(id: Int, viewCount: Float) {
//         val now = System.currentTimeMillis()
//         wipDao.updateViewedCount(id, viewCount, now)
//     }

//     suspend fun deleteWIPById(id: Int) = wipDao.deleteWIPbyId(id)

//     suspend fun deleteWholeCategory(categories: List<String?>) = wipDao.deleteWholeCategory(categories)

//     suspend fun resetEncountered(id: Int) {
//         val now = System.currentTimeMillis()
//         wipDao.resetEncountered(id, now)
//     }

//     suspend fun resetViewed(id: Int) {
//         val now = System.currentTimeMillis()
//         wipDao.resetViewedCount(id, now)
//     }

//     suspend fun resetEncounteredForCategories(categories: List<String>) {
//         val now = System.currentTimeMillis()
//         wipDao.resetEncounteredForCategories(categories, now)
//     }

//     suspend fun resetViewedForCategories(categories: List<String>) {
//         val now = System.currentTimeMillis()
//         wipDao.resetViewedForCategories(categories, now)
//     }

//     suspend fun markUploaded(id: Int) {
//         val now = System.currentTimeMillis()
//         wipDao.markUploaded(id, now)
//     }

//     // Filters
//     fun filterByCreatedRange(start: Long, end: Long) = wipDao.filterByCreatedRange(start, end)
//     fun filterByUpdatedRange(start: Long, end: Long) = wipDao.filterByUpdatedRange(start, end)
//     fun filterByUploadedRange(start: Long, end: Long) = wipDao.filterByUploadedRange(start, end)
// }


package com.rameez.hel.repository

import com.rameez.hel.model.WIPItem

interface WIPRepository {
    suspend fun getAllWIPs(): List<WIPItem>
    suspend fun getWIPById(id: String): WIPItem?
    suspend fun insertWIP(wipItem: WIPItem)
    suspend fun updateWIP(wipItem: WIPItem)
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
    ): List<WIPItem>
    suspend fun updateWIPCounts(
        id: String,
        encounteredCount: Int? = null,
        viewedCount: Int? = null,
        encounteredLastUpdatedAt: Long? = null,
        viewedLastUpdatedAt: Long? = null,
        firstEncounteredAt: Long? = null,
        firstViewedAt: Long? = null
    )
    suspend fun importWIPs(items: List<WIPItem>)
    suspend fun exportWIPs(): List<WIPItem>
}
2. SupabaseWIPRepository.kt

package com.rameez.hel.repository

import com.rameez.hel.model.WIPItem
import com.rameez.hel.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
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

    private fun WIPItemDto.toWIPItem(): WIPItem {
        return WIPItem(
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

    private fun WIPItem.toDto(): WIPItemDto {
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

    override suspend fun getAllWIPs(): List<WIPItem> {
        return try {
            val result = supabase.from("wip_items")
                .select()
                .decodeList<WIPItemDto>()
            result.map { it.toWIPItem() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getWIPById(id: String): WIPItem? {
        return try {
            val result = supabase.from("wip_items")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<WIPItemDto>()
            result?.toWIPItem()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insertWIP(wipItem: WIPItem) {
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

    override suspend fun updateWIP(wipItem: WIPItem) {
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
    ): List<WIPItem> {
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

    override suspend fun importWIPs(items: List<WIPItem>) {
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

    override suspend fun exportWIPs(): List<WIPItem> {
        return getAllWIPs()
    }
}
3. WIPFilterFragment_UPDATED.kt

package com.rameez.hel.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rameez.hel.R
import com.rameez.hel.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class WIPFilterFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    
    // Existing views
    private lateinit var categorySpinner: Spinner
    private lateinit var tagsInput: EditText
    private lateinit var readCountInput: EditText
    private lateinit var readOperatorSpinner: Spinner
    private lateinit var viewedCountInput: EditText
    private lateinit var viewedOperatorSpinner: Spinner
    private lateinit var wordInput: EditText
    private lateinit var meaningInput: EditText
    private lateinit var sampleSentenceInput: EditText
    
    // NEW: Encountered count views
    private lateinit var encounteredCountInput: EditText
    private lateinit var encounteredOperatorSpinner: Spinner
    private lateinit var encounteredLastUpdatedFromBtn: Button
    private lateinit var encounteredLastUpdatedToBtn: Button
    private lateinit var encounteredLastUpdatedOperatorSpinner: Spinner
    
    // NEW: Viewed count views (extended)
    private lateinit var viewedCountValueInput: EditText
    private lateinit var viewedCountOperatorSpinner: Spinner
    private lateinit var viewedLastUpdatedFromBtn: Button
    private lateinit var viewedLastUpdatedToBtn: Button
    private lateinit var viewedLastUpdatedOperatorSpinner: Spinner
    
    // NEW: First time timestamp views
    private lateinit var firstEncounteredFromBtn: Button
    private lateinit var firstEncounteredToBtn: Button
    private lateinit var firstEncounteredOperatorSpinner: Spinner
    private lateinit var firstViewedFromBtn: Button
    private lateinit var firstViewedToBtn: Button
    private lateinit var firstViewedOperatorSpinner: Spinner
    
    private lateinit var applyFiltersBtn: Button
    private lateinit var clearFiltersBtn: Button

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    private var encounteredLastUpdatedFrom: Long? = null
    private var encounteredLastUpdatedTo: Long? = null
    private var viewedLastUpdatedFrom: Long? = null
    private var viewedLastUpdatedTo: Long? = null
    private var firstEncounteredFrom: Long? = null
    private var firstEncounteredTo: Long? = null
    private var firstViewedFrom: Long? = null
    private var firstViewedTo: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wip_filter, container, false)
        
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        
        initializeViews(view)
        setupOperatorSpinners()
        setupDatePickers()
        setupButtons()
        
        return view
    }

    private fun initializeViews(view: View) {
        // Existing views
        categorySpinner = view.findViewById(R.id.categorySpinner)
        tagsInput = view.findViewById(R.id.tagsInput)
        readCountInput = view.findViewById(R.id.readCountInput)
        readOperatorSpinner = view.findViewById(R.id.readOperatorSpinner)
        viewedCountInput = view.findViewById(R.id.viewedCountInput)
        viewedOperatorSpinner = view.findViewById(R.id.viewedOperatorSpinner)
        wordInput = view.findViewById(R.id.wordInput)
        meaningInput = view.findViewById(R.id.meaningInput)
        sampleSentenceInput = view.findViewById(R.id.sampleSentenceInput)
        
        // NEW: Encountered count views
        encounteredCountInput = view.findViewById(R.id.encounteredCountInput)
        encounteredOperatorSpinner = view.findViewById(R.id.encounteredOperatorSpinner)
        encounteredLastUpdatedFromBtn = view.findViewById(R.id.encounteredLastUpdatedFromBtn)
        encounteredLastUpdatedToBtn = view.findViewById(R.id.encounteredLastUpdatedToBtn)
        encounteredLastUpdatedOperatorSpinner = view.findViewById(R.id.encounteredLastUpdatedOperatorSpinner)
        
        // NEW: Viewed count views (extended)
        viewedCountValueInput = view.findViewById(R.id.viewedCountValueInput)
        viewedCountOperatorSpinner = view.findViewById(R.id.viewedCountOperatorSpinner)
        viewedLastUpdatedFromBtn = view.findViewById(R.id.viewedLastUpdatedFromBtn)
        viewedLastUpdatedToBtn = view.findViewById(R.id.viewedLastUpdatedToBtn)
        viewedLastUpdatedOperatorSpinner = view.findViewById(R.id.viewedLastUpdatedOperatorSpinner)
        
        // NEW: First time timestamp views
        firstEncounteredFromBtn = view.findViewById(R.id.firstEncounteredFromBtn)
        firstEncounteredToBtn = view.findViewById(R.id.firstEncounteredToBtn)
        firstEncounteredOperatorSpinner = view.findViewById(R.id.firstEncounteredOperatorSpinner)
        firstViewedFromBtn = view.findViewById(R.id.firstViewedFromBtn)
        firstViewedToBtn = view.findViewById(R.id.firstViewedToBtn)
        firstViewedOperatorSpinner = view.findViewById(R.id.firstViewedOperatorSpinner)
        
        applyFiltersBtn = view.findViewById(R.id.applyFiltersBtn)
        clearFiltersBtn = view.findViewById(R.id.clearFiltersBtn)
    }

    private fun setupOperatorSpinners() {
        val operators = arrayOf(">", "<", "=", ">=", "<=")
        val dateOperators = arrayOf("between", "before", "after")
        
        val operatorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, operators)
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        val dateOperatorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateOperators)
        dateOperatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        readOperatorSpinner.adapter = operatorAdapter
        viewedOperatorSpinner.adapter = operatorAdapter
        encounteredOperatorSpinner.adapter = operatorAdapter
        viewedCountOperatorSpinner.adapter = operatorAdapter
        
        encounteredLastUpdatedOperatorSpinner.adapter = dateOperatorAdapter
        viewedLastUpdatedOperatorSpinner.adapter = dateOperatorAdapter
        firstEncounteredOperatorSpinner.adapter = dateOperatorAdapter
        firstViewedOperatorSpinner.adapter = dateOperatorAdapter
    }

    private fun setupDatePickers() {
        encounteredLastUpdatedFromBtn.setOnClickListener { showDatePicker { date ->
            encounteredLastUpdatedFrom = date
            encounteredLastUpdatedFromBtn.text = dateFormat.format(Date(date))
        }}
        
        encounteredLastUpdatedToBtn.setOnClickListener { showDatePicker { date ->
            encounteredLastUpdatedTo = date
            encounteredLastUpdatedToBtn.text = dateFormat.format(Date(date))
        }}
        
        viewedLastUpdatedFromBtn.setOnClickListener { showDatePicker { date ->
            viewedLastUpdatedFrom = date
            viewedLastUpdatedFromBtn.text = dateFormat.format(Date(date))
        }}
        
        viewedLastUpdatedToBtn.setOnClickListener { showDatePicker { date ->
            viewedLastUpdatedTo = date
            viewedLastUpdatedToBtn.text = dateFormat.format(Date(date))
        }}
        
        firstEncounteredFromBtn.setOnClickListener { showDatePicker { date ->
            firstEncounteredFrom = date
            firstEncounteredFromBtn.text = dateFormat.format(Date(date))
        }}
        
        firstEncounteredToBtn.setOnClickListener { showDatePicker { date ->
            firstEncounteredTo = date
            firstEncounteredToBtn.text = dateFormat.format(Date(date))
        }}
        
        firstViewedFromBtn.setOnClickListener { showDatePicker { date ->
            firstViewedFrom = date
            firstViewedFromBtn.text = dateFormat.format(Date(date))
        }}
        
        firstViewedToBtn.setOnClickListener { showDatePicker { date ->
            firstViewedTo = date
            firstViewedToBtn.text = dateFormat.format(Date(date))
        }}
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupButtons() {
        applyFiltersBtn.setOnClickListener {
            applyFilters()
        }
        
        clearFiltersBtn.setOnClickListener {
            clearAllFilters()
        }
    }

    private fun applyFilters() {
        // Existing filters
        val categories = categorySpinner.selectedItem?.toString()?.let { listOf(it) } ?: emptyList()
        val tags = tagsInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val readCount = readCountInput.text.toString().toFloatOrNull()
        val readOperator = readOperatorSpinner.selectedItem?.toString()
        val viewedCount = viewedCountInput.text.toString().toFloatOrNull()
        val viewedOperator = viewedOperatorSpinner.selectedItem?.toString()
        val word = wordInput.text.toString()
        val meaning = meaningInput.text.toString()
        val sampleSentence = sampleSentenceInput.text.toString()
        
        // NEW: Encountered filters
        val encounteredCount = encounteredCountInput.text.toString().toIntOrNull()
        val encounteredOperator = encounteredOperatorSpinner.selectedItem?.toString()
        val encounteredLastUpdatedOperator = encounteredLastUpdatedOperatorSpinner.selectedItem?.toString()
        
        // NEW: Viewed filters (extended)
        val viewedCountValue = viewedCountValueInput.text.toString().toIntOrNull()
        val viewedCountOperator = viewedCountOperatorSpinner.selectedItem?.toString()
        val viewedLastUpdatedOperator = viewedLastUpdatedOperatorSpinner.selectedItem?.toString()
        
        // NEW: First time filters
        val firstEncounteredOperator = firstEncounteredOperatorSpinner.selectedItem?.toString()
        val firstViewedOperator = firstViewedOperatorSpinner.selectedItem?.toString()
        
        // Update SharedViewModel
        sharedViewModel.categoryList = categories.toMutableList()
        sharedViewModel.tagsList = tags.toMutableList()
        sharedViewModel.readCount = readCount
        sharedViewModel.readOperator = readOperator
        sharedViewModel.viewedCount = viewedCount
        sharedViewModel.viewedOperator = viewedOperator
        sharedViewModel.filteredWord = word.ifEmpty { null }
        sharedViewModel.filteredMeaning = meaning.ifEmpty { null }
        sharedViewModel.filteredSampleSen = sampleSentence.ifEmpty { null }
        
        sharedViewModel.encounteredCount = encounteredCount
        sharedViewModel.encounteredOperator = encounteredOperator
        sharedViewModel.encounteredLastUpdatedFrom = encounteredLastUpdatedFrom
        sharedViewModel.encounteredLastUpdatedTo = encounteredLastUpdatedTo
        sharedViewModel.encounteredLastUpdatedOperator = encounteredLastUpdatedOperator
        
        sharedViewModel.viewedCountValue = viewedCountValue
        sharedViewModel.viewedCountOperator = viewedCountOperator
        sharedViewModel.viewedLastUpdatedFrom = viewedLastUpdatedFrom
        sharedViewModel.viewedLastUpdatedTo = viewedLastUpdatedTo
        sharedViewModel.viewedLastUpdatedOperator = viewedLastUpdatedOperator
        
        sharedViewModel.firstEncounteredFrom = firstEncounteredFrom
        sharedViewModel.firstEncounteredTo = firstEncounteredTo
        sharedViewModel.firstEncounteredOperator = firstEncounteredOperator
        sharedViewModel.firstViewedFrom = firstViewedFrom
        sharedViewModel.firstViewedTo = firstViewedTo
        sharedViewModel.firstViewedOperator = firstViewedOperator
        
        Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllFilters() {
        // Clear UI
        categorySpinner.setSelection(0)
        tagsInput.text.clear()
        readCountInput.text.clear()
        viewedCountInput.text.clear()
        wordInput.text.clear()
        meaningInput.text.clear()
        sampleSentenceInput.text.clear()
        
        encounteredCountInput.text.clear()
        viewedCountValueInput.text.clear()
        
        encounteredLastUpdatedFromBtn.text = "Select From Date"
        encounteredLastUpdatedToBtn.text = "Select To Date"
        viewedLastUpdatedFromBtn.text = "Select From Date"
        viewedLastUpdatedToBtn.text = "Select To Date"
        firstEncounteredFromBtn.text = "Select From Date"
        firstEncounteredToBtn.text = "Select To Date"
        firstViewedFromBtn.text = "Select From Date"
        firstViewedToBtn.text = "Select To Date"
        
        encounteredLastUpdatedFrom = null
        encounteredLastUpdatedTo = null
        viewedLastUpdatedFrom = null
        viewedLastUpdatedTo = null
        firstEncounteredFrom = null
        firstEncounteredTo = null
        firstViewedFrom = null
        firstViewedTo = null
        
        // Clear ViewModel
        sharedViewModel.clearAllFilters()
        
        Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show()
    }
}
4. CarouselFragment_UPDATED.kt

package com.rameez.hel.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.rameez.hel.R
import com.rameez.hel.model.WIPItem
import com.rameez.hel.repository.SupabaseWIPRepository
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import com.rameez.hel.viewmodel.WIPViewModelFactory

class CarouselFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var wipViewModel: WIPViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var btnIncrementEncountered: Button
    private lateinit var btnDecrementEncountered: Button
    private lateinit var tvEncounteredCount: TextView
    private lateinit var tvViewedCount: TextView
    
    private var currentWIPItems: List<WIPItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_carousel, container, false)
        
        val repository = SupabaseWIPRepository()
        val factory = WIPViewModelFactory(repository)
        wipViewModel = ViewModelProvider(this, factory).get(WIPViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        
        initializeViews(view)
        setupViewPager()
        loadWIPs()
        
        return view
    }

    private fun initializeViews(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        btnIncrementEncountered = view.findViewById(R.id.btnIncrementEncountered)
        btnDecrementEncountered = view.findViewById(R.id.btnDecrementEncountered)
        tvEncounteredCount = view.findViewById(R.id.tvEncounteredCount)
        tvViewedCount = view.findViewById(R.id.tvViewedCount)
    }

    private fun setupViewPager() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (currentWIPItems.isNotEmpty() && position < currentWIPItems.size) {
                    val currentItem = currentWIPItems[position]
                    currentItem.id?.let { id ->
                        // Automatically increment viewed count when flashcard is displayed
                        wipViewModel.incrementViewedCount(id)
                        updateCountDisplays(currentItem)
                    }
                }
            }
        })
        
        btnIncrementEncountered.setOnClickListener {
            getCurrentWIPItem()?.id?.let { id ->
                wipViewModel.incrementEncounteredCount(id)
                Toast.makeText(requireContext(), "Encountered +1", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnDecrementEncountered.setOnClickListener {
            getCurrentWIPItem()?.id?.let { id ->
                wipViewModel.decrementEncounteredCount(id)
                Toast.makeText(requireContext(), "Encountered -1", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadWIPs() {
        wipViewModel.wips.observe(viewLifecycleOwner) { items ->
            if (items != null) {
                // Apply filters from SharedViewModel
                val filteredItems = applyFilters(items)
                currentWIPItems = filteredItems
                
                if (filteredItems.isEmpty()) {
                    Toast.makeText(requireContext(), "No items found", Toast.LENGTH_SHORT).show()
                } else {
                    // Setup adapter with filtered items
                    val adapter = WIPCarouselAdapter(filteredItems)
                    viewPager.adapter = adapter
                    
                    // Update counts for first item
                    if (filteredItems.isNotEmpty()) {
                        updateCountDisplays(filteredItems[0])
                        // Increment viewed count for first item
                        filteredItems[0].id?.let { wipViewModel.incrementViewedCount(it) }
                    }
                }
            }
        }
        
        wipViewModel.getWIPs()
    }

    private fun applyFilters(items: List<WIPItem>): List<WIPItem> {
        return items.filter { item ->
            var matches = true
            
            // Apply all filters from SharedViewModel
            if (sharedViewModel.categoryList.isNotEmpty()) {
                matches = matches && sharedViewModel.categoryList.contains(item.category)
            }
            
            if (sharedViewModel.tagsList.isNotEmpty()) {
                matches = matches && item.customTag?.any { sharedViewModel.tagsList.contains(it) } == true
            }
            
            sharedViewModel.readCount?.let { count ->
                sharedViewModel.readOperator?.let { operator ->
                    val itemReadCount = item.readCount ?: 0f
                    matches = matches && when (operator) {
                        ">" -> itemReadCount > count
                        "<" -> itemReadCount < count
                        "=" -> itemReadCount == count
                        ">=" -> itemReadCount >= count
                        "<=" -> itemReadCount <= count
                        else -> true
                    }
                }
            }
            
            sharedViewModel.filteredWord?.let { word ->
                matches = matches && item.wip?.contains(word, ignoreCase = true) == true
            }
            
            sharedViewModel.filteredMeaning?.let { meaning ->
                matches = matches && item.meaning?.contains(meaning, ignoreCase = true) == true
            }
            
            sharedViewModel.filteredSampleSen?.let { sentence ->
                matches = matches && item.sampleSentence?.contains(sentence, ignoreCase = true) == true
            }
            
            // NEW: Apply encountered count filter
            sharedViewModel.encounteredCount?.let { count ->
                sharedViewModel.encounteredOperator?.let { operator ->
                    val itemEncounteredCount = item.encounteredCount ?: 0
                    matches = matches && when (operator) {
                        ">" -> itemEncounteredCount > count
                        "<" -> itemEncounteredCount < count
                        "=" -> itemEncounteredCount == count
                        ">=" -> itemEncounteredCount >= count
                        "<=" -> itemEncounteredCount <= count
                        else -> true
                    }
                }
            }
            
            // NEW: Apply viewed count filter
            sharedViewModel.viewedCountValue?.let { count ->
                sharedViewModel.viewedCountOperator?.let { operator ->
                    val itemViewedCount = item.viewedCount ?: 0
                    matches = matches && when (operator) {
                        ">" -> itemViewedCount > count
                        "<" -> itemViewedCount < count
                        "=" -> itemViewedCount == count
                        ">=" -> itemViewedCount >= count
                        "<=" -> itemViewedCount <= count
                        else -> true
                    }
                }
            }
            
            matches
        }
    }

    private fun getCurrentWIPItem(): WIPItem? {
        val position = viewPager.currentItem
        return if (position < currentWIPItems.size) currentWIPItems[position] else null
    }

    private fun updateCountDisplays(item: WIPItem) {
        tvEncounteredCount.text = "Encountered: ${item.encounteredCount ?: 0}"
        tvViewedCount.text = "Viewed: ${item.viewedCount ?: 0}"
    }
}

// Adapter for ViewPager2
class WIPCarouselAdapter(private val items: List<WIPItem>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<WIPCarouselAdapter.WIPViewHolder>() {

    class WIPViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvWip: TextView = view.findViewById(R.id.tvWip)
        val tvMeaning: TextView = view.findViewById(R.id.tvMeaning)
        val tvSampleSentence: TextView = view.findViewById(R.id.tvSampleSentence)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WIPViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wip_card, parent, false)
        return WIPViewHolder(view)
    }

    override fun onBindViewHolder(holder: WIPViewHolder, position: Int) {
        val item = items[position]
        holder.tvWip.text = item.wip ?: ""
        holder.tvMeaning.text = item.meaning ?: ""
        holder.tvSampleSentence.text = item.sampleSentence ?: ""
        holder.tvCategory.text = item.category ?: ""
    }

    override fun getItemCount() = items.size
}
5. ImportExportUtil.kt

package com.rameez.hel.util

import android.content.Context
import android.net.Uri
import com.rameez.hel.model.WIPItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object ImportExportUtil {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Export to JSON
    suspend fun exportToJSON(context: Context, items: List<WIPItem>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    val jsonString = json.encodeToString(items)
                    writer.write(jsonString)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Import from JSON
    suspend fun importFromJSON(context: Context, uri: Uri): List<WIPItem>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val jsonString = reader.readText()
                    json.decodeFromString<List<WIPItem>>(jsonString)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Export to CSV
    suspend fun exportToCSV(context: Context, items: List<WIPItem>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Header
                    writer.write("id,wip,meaning,sampleSentence,category,customTag,readCount,displayCount,createdAt,updatedAt,uploadedAt,encounteredCount,viewedCount,encounteredLastUpdatedAt,viewedLastUpdatedAt,firstEncounteredAt,firstViewedAt\n")
                    
                    // Data rows
                    items.forEach { item ->
                        val row = listOf(
                            item.id ?: "",
                            escapeCSV(item.wip ?: ""),
                            escapeCSV(item.meaning ?: ""),
                            escapeCSV(item.sampleSentence ?: ""),
                            escapeCSV(item.category ?: ""),
                            escapeCSV(item.customTag?.joinToString(";") ?: ""),
                            item.readCount?.toString() ?: "",
                            item.displayCount?.toString() ?: "",
                            item.createdAt?.toString() ?: "",
                            item.updatedAt?.toString() ?: "",
                            item.uploadedAt?.toString() ?: "",
                            item.encounteredCount?.toString() ?: "0",
                            item.viewedCount?.toString() ?: "0",
                            item.encounteredLastUpdatedAt?.toString() ?: "",
                            item.viewedLastUpdatedAt?.toString() ?: "",
                            item.firstEncounteredAt?.toString() ?: "",
                            item.firstViewedAt?.toString() ?: ""
                        ).joinToString(",")
                        writer.write("$row\n")
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Import from CSV
    suspend fun importFromCSV(context: Context, uri: Uri): List<WIPItem>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val items = mutableListOf<WIPItem>()
                    
                    // Skip header
                    reader.readLine()
                    
                    // Read data rows
                    reader.forEachLine { line ->
                        val values = parseCSVLine(line)
                        if (values.size >= 17) {
                            val item = WIPItem(
                                id = values[0].ifEmpty { null },
                                wip = values[1].ifEmpty { null },
                                meaning = values[2].ifEmpty { null },
                                sampleSentence = values[3].ifEmpty { null },
                                category = values[4].ifEmpty { null },
                                customTag = values[5].split(";").filter { it.isNotEmpty() },
                                readCount = values[6].toFloatOrNull(),
                                displayCount = values[7].toFloatOrNull(),
                                createdAt = values[8].toLongOrNull(),
                                updatedAt = values[9].toLongOrNull(),
                                uploadedAt = values[10].toLongOrNull(),
                                encounteredCount = values[11].toIntOrNull() ?: 0,
                                viewedCount = values[12].toIntOrNull() ?: 0,
                                encounteredLastUpdatedAt = values[13].toLongOrNull(),
                                viewedLastUpdatedAt = values[14].toLongOrNull(),
                                firstEncounteredAt = values[15].toLongOrNull(),
                                firstViewedAt = values[16].toLongOrNull()
                            )
                            items.add(item)
                        }
                    }
                    
                    items
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        var i = 0
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        
        result.add(current.toString())
        return result
    }
}
6. SupabaseClient.kt

package com.rameez.hel.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    private const val SUPABASE_KEY = "YOUR_SUPABASE_ANON_KEY"
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}
7. fragment_wip_filter_additions.xml

<?xml version="1.0" encoding="utf-8"?>
<!-- Add these sections to your existing fragment_wip_filter.xml -->

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <!-- ENCOUNTERED COUNT SECTION -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Encountered Count Filter"
        android:textStyle="bold"
        android:textSize="16sp"
        android:layout_marginTop="16dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Spinner
            android:id="@+id/encounteredOperatorSpinner"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"/>

        <EditText
            android:id="@+id/encounteredCountInput"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="2"
            android:hint="Encountered count"
            android:inputType="number"/>
    </LinearLayout>

    <!-- ENCOUNTERED LAST UPDATED SECTION -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Encountered Last Updated"
        android:textStyle="bold"
        android:textSize="16sp"
        android:layout_marginTop="16dp"/>

    <Spinner
        android:id="@+id/encounteredLastUpdatedOperatorSpinner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Button
            android:id="@+id/encounteredLastUpdatedFromBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select From Date"/>

        <Button
            android:id="@+id/encounteredLastUpdatedToBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select To Date"/>
    </LinearLayout>

    <!-- VIEWED COUNT SECTION (NEW) -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Viewed Count Filter"
        android:textStyle="bold"
        android:textSize="16sp"
        android:layout_marginTop="16dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Spinner
            android:id="@+id/viewedCountOperatorSpinner"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"/>

        <EditText
            android:id="@+id/viewedCountValueInput"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="2"
            android:hint="Viewed count"
            android:inputType="number"/>
    </LinearLayout>

    <!-- VIEWED LAST UPDATED SECTION -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Viewed Last Updated"
        android:textStyle="bold"
        android:textSize="16sp"
        android:layout_marginTop="16dp"/>

    <Spinner
        android:id="@+id/viewedLastUpdatedOperatorSpinner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Button
            android:id="@+id/viewedLastUpdatedFromBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select From Date"/>

        <Button
            android:id="@+id/viewedLastUpdatedToBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select To Date"/>
    </LinearLayout>

    <!-- FIRST ENCOUNTERED SECTION -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="First Encountered Date"
        android:textStyle="bold"
        android:textSize="16sp"
        android:layout_marginTop="16dp"/>

    <Spinner
        android:id="@+id/firstEncounteredOperatorSpinner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Button
            android:id="@+id/firstEncounteredFromBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select From Date"/>

        <Button
            android:id="@+id/firstEncounteredToBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select To Date"/>
    </LinearLayout>

    <!-- FIRST VIEWED SECTION -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="First Viewed Date"
        android:textStyle="bold"
        android:textSize="16sp"
        android:layout_marginTop="16dp"/>

    <Spinner
        android:id="@+id/firstViewedOperatorSpinner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Button
            android:id="@+id/firstViewedFromBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select From Date"/>

        <Button
            android:id="@+id/firstViewedToBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Select To Date"/>
    </LinearLayout>

    <!-- BUTTONS -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="24dp">

        <Button
            android:id="@+id/applyFiltersBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Apply Filters"/>

        <Button
            android:id="@+id/clearFiltersBtn"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Clear Filters"/>
    </LinearLayout>

</LinearLayout>
8. fragment_carousel_additions.xml

<?xml version="1.0" encoding="utf-8"?>
<!-- Add these sections to your existing fragment_carousel.xml -->

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <!-- ViewPager for flashcards -->
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

    <!-- Count displays -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginTop="16dp">

        <TextView
            android:id="@+id/tvEncounteredCount"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Encountered: 0"
            android:textSize="16sp"
            android:gravity="center"/>

        <TextView
            android:id="@+id/tvViewedCount"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Viewed: 0"
            android:textSize="16sp"
            android:gravity="center"/>
    </LinearLayout>

    <!-- Encountered count buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginTop="16dp">

        <Button
            android:id="@+id/btnDecrementEncountered"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="-"
            android:textSize="24sp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Encountered"
            android:textSize="16sp"
            android:layout_marginHorizontal="16dp"/>

        <Button
            android:id="@+id/btnIncrementEncountered"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="+"
            android:textSize="24sp"/>
    </LinearLayout>

</LinearLayout>

<!-- item_wip_card.xml - Card layout for individual flashcards -->
<!-- Create this as a separate file -->
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_margin="16dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="24dp"
        android:gravity="center">

        <TextView
            android:id="@+id/tvWip"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="32sp"
            android:textStyle="bold"
            android:gravity="center"/>

        <TextView
            android:id="@+id/tvMeaning"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="20sp"
            android:layout_marginTop="16dp"
            android:gravity="center"/>

        <TextView
            android:id="@+id/tvSampleSentence"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textStyle="italic"
            android:layout_marginTop="16dp"
            android:gravity="center"/>

        <TextView
            android:id="@+id/tvCategory"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:layout_marginTop="16dp"
            android:gravity="center"
            android:textColor="#666666"/>

    </LinearLayout>

</androidx.cardview.widget.CardView>
9. supabase_migration.sql

/*
  # WIP Items Table with Encountered and Viewed Tracking

  1. New Tables
    - `wip_items`
      - `id` (uuid, primary key)
      - `wip` (text) - The word/phrase
      - `meaning` (text) - Definition
      - `sample_sentence` (text) - Example usage
      - `category` (text) - Category classification
      - `custom_tag` (text) - Comma-separated tags
      - `read_count` (float) - Legacy read counter
      - `display_count` (float) - Legacy display counter
      - `created_at` (bigint) - Creation timestamp
      - `updated_at` (bigint) - Last update timestamp
      - `uploaded_at` (bigint) - Upload timestamp
      - `encountered_count` (integer) - Manual encounter tracking
      - `viewed_count` (integer) - Automatic view tracking
      - `encountered_last_updated_at` (bigint) - Last encountered timestamp
      - `viewed_last_updated_at` (bigint) - Last viewed timestamp
      - `first_encountered_at` (bigint) - First encounter timestamp
      - `first_viewed_at` (bigint) - First view timestamp

  2. Security
    - Enable RLS on `wip_items` table
    - Add policies for authenticated users to manage their own data
*/

CREATE TABLE IF NOT EXISTS wip_items (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  wip text,
  meaning text,
  sample_sentence text,
  category text,
  custom_tag text,
  read_count float DEFAULT 0,
  display_count float DEFAULT 0,
  created_at bigint DEFAULT extract(epoch from now()) * 1000,
  updated_at bigint DEFAULT extract(epoch from now()) * 1000,
  uploaded_at bigint,
  encountered_count integer DEFAULT 0,
  viewed_count integer DEFAULT 0,
  encountered_last_updated_at bigint,
  viewed_last_updated_at bigint,
  first_encountered_at bigint,
  first_viewed_at bigint,
  user_id uuid REFERENCES auth.users(id) ON DELETE CASCADE
);

ALTER TABLE wip_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own wip items"
  ON wip_items FOR SELECT
  TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own wip items"
  ON wip_items FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own wip items"
  ON wip_items FOR UPDATE
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own wip items"
  ON wip_items FOR DELETE
  TO authenticated
  USING (auth.uid() = user_id);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_wip_items_user_id ON wip_items(user_id);
CREATE INDEX IF NOT EXISTS idx_wip_items_category ON wip_items(category);
CREATE INDEX IF NOT EXISTS idx_wip_items_encountered_count ON wip_items(encountered_count);
CREATE INDEX IF NOT EXISTS idx_wip_items_viewed_count ON wip_items(viewed_count);
CREATE INDEX IF NOT EXISTS idx_wip_items_created_at ON wip_items(created_at);
