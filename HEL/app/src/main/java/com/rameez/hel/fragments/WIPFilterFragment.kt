// package com.rameez.hel.fragments

// import android.annotation.SuppressLint
// import android.app.TimePickerDialog
// import android.os.Bundle
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import androidx.core.widget.doAfterTextChanged
// import com.rameez.hel.utils.animatePress
// import com.rameez.hel.utils.setupAutoRepeat
// import android.widget.AdapterView
// import android.widget.ArrayAdapter
// import android.widget.Toast
// import androidx.fragment.app.Fragment
// import androidx.fragment.app.activityViewModels
// import androidx.navigation.fragment.findNavController
// import androidx.recyclerview.widget.GridLayoutManager
// import com.rameez.hel.R
// import com.rameez.hel.adapter.CategoryAdapter
// import com.rameez.hel.adapter.CustomTagsAdapter
// import com.rameez.hel.databinding.FragmentWIPFilterBinding
// import com.rameez.hel.viewmodel.SharedViewModel
// import com.rameez.hel.viewmodel.WIPViewModel
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.launch
// import java.text.ParseException
// import java.text.SimpleDateFormat
// import java.util.Calendar
// import java.util.Date
// import java.util.Locale

// class WIPFilterFragment : Fragment() {

//     private lateinit var mBinding: FragmentWIPFilterBinding
//     private lateinit var customTagAdapter: CustomTagsAdapter
//     private val wipViewModel: WIPViewModel by activityViewModels()
//     private val customTags = mutableListOf<String>()
//     private lateinit var filteredCategoryList: MutableList<String>
//     private lateinit var filteredTagsList: MutableList<String>
//     private var filteredReadCount: Float? = null
//     private var filteredViewedCount: Float? = null
//     private val sharedViewModel: SharedViewModel by activityViewModels()
//     private val categoryAdapter = CategoryAdapter()
//     private val categoriesList = arrayListOf<String>()
//     private var readOperator: String? = null
//     private var viewedOperator: String? = null
//     private var isFirstTime = false
//     private var filteredWIP: String? = null
//     private var filteredMeaning: String? = null
//     private var filteredSampleSen: String? = null

//     // DATE/TIME format used across UI
//     private val UI_TS_FORMAT = "dd/MM/yyyy HH:mm:ss"
//     private val sdf = SimpleDateFormat(UI_TS_FORMAT, Locale.getDefault())

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         customTagAdapter = CustomTagsAdapter()
//     }

//     override fun onCreateView(
//         inflater: LayoutInflater,
//         container: ViewGroup?,
//         savedInstanceState: Bundle?
//     ): View? {
//         if (::mBinding.isInitialized.not()) {
//             isFirstTime = true
//             mBinding = FragmentWIPFilterBinding.inflate(layoutInflater, container, false)
//         }
//         return mBinding.root
//     }

//     // show combined Date -> Time -> Seconds picker (uses your util for selecting seconds)
//     private fun showDateTimePicker(onResult: (Long) -> Unit) {
//         val c = Calendar.getInstance()

//         val datePicker = android.app.DatePickerDialog(
//             requireContext(),
//             { _, year, month, day ->
//                 val timePicker = TimePickerDialog(
//                     requireContext(),
//                     { _, hour, minute ->
//                         // seconds selector (your util)
//                         val secondsPicker = com.rameez.hel.utils.TimePicker()
//                         secondsPicker.setTitle("Select Seconds")
//                         // don't show hours/minutes in seconds picker
//                         secondsPicker.includeHours = false
//                         secondsPicker.setOnTimeSetOption("Set") { _, _, seconds ->
//                             c.set(year, month, day, hour, minute, seconds)
//                             onResult(c.timeInMillis)
//                         }
//                         secondsPicker.show(requireActivity().supportFragmentManager, "sec_picker")
//                     },
//                     c.get(Calendar.HOUR_OF_DAY),
//                     c.get(Calendar.MINUTE),
//                     true
//                 )
//                 timePicker.show()
//             },
//             c.get(Calendar.YEAR),
//             c.get(Calendar.MONTH),
//             c.get(Calendar.DAY_OF_MONTH)
//         )
//         datePicker.show()
//     }

//     private fun formatTimestamp(ms: Long?): String {
//         if (ms == null || ms == 0L) return ""
//         return try {
//             sdf.format(Date(ms))
//         } catch (e: Exception) {
//             ""
//         }
//     }

//     private fun parseTimestamp(text: String?): Long? {
//         if (text.isNullOrBlank()) return null
//         return try {
//             sdf.parse(text)?.time
//         } catch (e: ParseException) {
//             null
//         }
//     }

//     @SuppressLint("DiscouragedApi")
//     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//         super.onViewCreated(view, savedInstanceState)

//         filteredCategoryList = sharedViewModel.categoryList
//         filteredTagsList = sharedViewModel.tagsList
//         filteredReadCount = sharedViewModel.readCount
//         filteredViewedCount = sharedViewModel.viewedCount
//         readOperator = sharedViewModel.readOperator
//         viewedOperator = sharedViewModel.viewedOperator
//         filteredWIP = sharedViewModel.filteredWord
//         filteredMeaning = sharedViewModel.filteredMeaning
//         filteredSampleSen = sharedViewModel.filteredSampleSen

//         // initialize UI fields
//         mBinding.etReadCount.text = null
//         mBinding.etViewedCount.text = null
//         mBinding.etTimer.text = ""
//         setUpRecyclerView()

//         wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { list ->
//             // populate category/tag lists
//             list.forEach { wipItem ->
//                 val lowerCaseCategory = wipItem.category?.lowercase(Locale.ROOT)
//                 if (wipItem.category !in categoriesList && lowerCaseCategory !in categoriesList.map { it.lowercase(Locale.ROOT) }) {
//                     wipItem.category?.let { categoriesList.add(it) }
//                 }
//                 wipItem.customTag?.filterNot { it.isEmpty() }?.forEach { customTag ->
//                     val lower = customTag.lowercase(Locale.ROOT)
//                     if (customTag !in customTags && lower !in customTags.map { it.lowercase(Locale.ROOT) }) {
//                         customTags.add(customTag)
//                     }
//                 }
//             }
//             readOperatorSetup()
//             viewedOperatorSetup()
//             categoryAdapter.submitList(categoriesList.toList())
//             customTagAdapter.submitList(customTags.toList())
//         }

//         mBinding.apply {
//             // Timer picker
//             btnTimer.setOnClickListener {
//                 showDateTimePicker { millis ->
//                     etTimer.text = formatTimestamp(millis)
//                     val c = Calendar.getInstance().apply { timeInMillis = millis }
//                     sharedViewModel.selectedHours = c.get(Calendar.HOUR_OF_DAY)
//                     sharedViewModel.selectedMins = c.get(Calendar.MINUTE)
//                     sharedViewModel.selectedSecs = c.get(Calendar.SECOND)
//                 }
//             }

//             // -------- READ COUNT --------
//             fun updateReadButtonsState() {
//                 val value = etReadCount.text.toString().toIntOrNull() ?: 0
//                 btnReadMinus.isEnabled = value > 0
//                 btnReadMinus.alpha = if (value > 0) 1f else 0.4f
//             }

//             btnReadMinus.setOnClickListener {
//                 it.animatePress()
//                 val v = etReadCount.text.toString().toIntOrNull() ?: 0
//                 etReadCount.setText((v - 1).coerceAtLeast(0).toString())
//                 updateReadButtonsState()
//             }

//             btnReadPlus.setOnClickListener {
//                 it.animatePress()
//                 val v = etReadCount.text.toString().toIntOrNull() ?: 0
//                 etReadCount.setText((v + 1).toString())
//                 updateReadButtonsState()
//             }

//             btnReadMinus.setupAutoRepeat {
//                 btnReadMinus.performClick()
//             }

//             btnReadPlus.setupAutoRepeat {
//                 btnReadPlus.performClick()
//             }

//             etReadCount.doAfterTextChanged {
//                 filteredReadCount = it?.toString()?.trim()?.toFloatOrNull()
//                 updateReadButtonsState()
//             }



//             // -------- VIEWED COUNT --------
//             fun updateViewedButtonsState() {
//                 val value = etViewedCount.text.toString().toIntOrNull() ?: 0
//                 btnViewedMinus.isEnabled = value > 0
//                 btnViewedMinus.alpha = if (value > 0) 1f else 0.4f
//             }

//             btnViewedMinus.setOnClickListener {
//                 it.animatePress()
//                 val v = etViewedCount.text.toString().toIntOrNull() ?: 0
//                 etViewedCount.setText((v - 1).coerceAtLeast(0).toString())
//                 updateViewedButtonsState()
//             }

//             btnViewedPlus.setOnClickListener {
//                 it.animatePress()
//                 val v = etViewedCount.text.toString().toIntOrNull() ?: 0
//                 etViewedCount.setText((v + 1).toString())
//                 updateViewedButtonsState()
//             }

//             btnViewedMinus.setupAutoRepeat {
//                 btnViewedMinus.performClick()
//             }

//             btnViewedPlus.setupAutoRepeat {
//                 btnViewedPlus.performClick()
//             }


//             etViewedCount.doAfterTextChanged {
//                 filteredViewedCount = it?.toString()?.trim()?.toFloatOrNull()
//                 updateViewedButtonsState()
//             }



//             btnClearFilters.setOnClickListener {
//                 filteredCategoryList.clear()
//                 filteredTagsList.clear()
//                 filteredReadCount = null
//                 filteredViewedCount = null
//                 // clear UI timestamp fields
//                 etCreatedFrom.text = ""
//                 etCreatedTo.text = ""
//                 etUpdatedFrom.text = ""
//                 etUpdatedTo.text = ""
//                 etUploadedFrom.text = ""
//                 etUploadedTo.text = ""
//                 etWIP.text = null
//                 etMeaning.text = null
//                 etSampleSen.text = null
//                 etLimit.text = null
//                 etReadCount.text = null
//                 etViewedCount.text = null
//                 switchMaterial.isChecked = false
//                 Toast.makeText(requireContext(), "All filters are cleared", Toast.LENGTH_SHORT).show()
//                 findNavController().navigateUp()
//             }

//             // timestamp field taps -> show date/time
//             etCreatedFrom.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
//             etCreatedTo.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
//             etUpdatedFrom.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
//             etUpdatedTo.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
//             etUploadedFrom.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
//             etUploadedTo.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }

//             btnApplyFilter.setOnClickListener {
//                 // collect numeric & text filters
//                 filteredReadCount = etReadCount.text?.toString()?.trim()?.toFloatOrNull()
//                 filteredViewedCount = etViewedCount.text?.toString()?.trim()?.toFloatOrNull()
//                 filteredWIP = etWIP.text?.toString()?.trim()
//                 filteredMeaning = etMeaning.text?.toString()?.trim()
//                 filteredSampleSen = etSampleSen.text?.toString()?.trim()

//                 // parse timestamp ranges
//                 val createdFrom = parseTimestamp(etCreatedFrom.text?.toString())
//                 val createdTo = parseTimestamp(etCreatedTo.text?.toString())
//                 val updatedFrom = parseTimestamp(etUpdatedFrom.text?.toString())
//                 val updatedTo = parseTimestamp(etUpdatedTo.text?.toString())
//                 val uploadedFrom = parseTimestamp(etUploadedFrom.text?.toString())
//                 val uploadedTo = parseTimestamp(etUploadedTo.text?.toString())

//                 // get limit
//                 val limit = etLimit.text?.toString()?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0

//                 wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { data ->
//                     // Filter on background thread
//                     CoroutineScope(Dispatchers.IO).launch {
//                         var filteredData = data

//                         // text filters
//                         if (!filteredWIP.isNullOrBlank()) {
//                             filteredData = filteredData.filter { it.wip?.contains(filteredWIP!!, ignoreCase = true) == true }
//                         }
//                         if (!filteredMeaning.isNullOrBlank()) {
//                             filteredData = filteredData.filter { it.meaning?.contains(filteredMeaning!!, ignoreCase = true) == true }
//                         }
//                         if (!filteredSampleSen.isNullOrBlank()) {
//                             filteredData = filteredData.filter { it.sampleSentence?.contains(filteredSampleSen!!, ignoreCase = true) == true }
//                         }

//                         // category filter
//                         if (filteredCategoryList.isNotEmpty()) {
//                             val lowerFilteredCats = filteredCategoryList.map { it.lowercase(Locale.ROOT) }
//                             filteredData = filteredData.filter { it.category?.lowercase(Locale.ROOT) in lowerFilteredCats }
//                         }

//                         // tag filter
//                         if (filteredTagsList.isNotEmpty()) {
//                             val lowerTags = filteredTagsList.map { it.lowercase(Locale.ROOT) }
//                             filteredData = filteredData.filter { wipItem ->
//                                 wipItem.customTag?.any { tag -> tag.lowercase(Locale.ROOT) in lowerTags } ?: false
//                             }
//                         }

//                         // read/view numeric operators
//                         if (filteredReadCount != null) {
//                             filteredData = when (readOperator) {
//                                 "=" -> filteredData.filter { it.readCount == filteredReadCount }
//                                 ">" -> filteredData.filter { (it.readCount ?: 0f) > filteredReadCount!! }
//                                 "<" -> filteredData.filter { (it.readCount ?: 0f) < filteredReadCount!! }
//                                 else -> filteredData
//                             }
//                         }

//                         if (filteredViewedCount != null) {
//                             filteredData = when (viewedOperator) {
//                                 "=" -> filteredData.filter { it.displayCount == filteredViewedCount }
//                                 ">" -> filteredData.filter { (it.displayCount ?: 0f) > filteredViewedCount!! }
//                                 "<" -> filteredData.filter { (it.displayCount ?: 0f) < filteredViewedCount!! }
//                                 else -> filteredData
//                             }
//                         }

//                         // Timestamp range filters: created / updated / uploaded
//                         if (createdFrom != null || createdTo != null) {
//                             filteredData = filteredData.filter { item ->
//                                 val ts = item.createdAt
//                                 timestampInRange(ts, createdFrom, createdTo)
//                             }
//                         }

//                         if (updatedFrom != null || updatedTo != null) {
//                             filteredData = filteredData.filter { item ->
//                                 val ts = item.updatedAt
//                                 timestampInRange(ts, updatedFrom, updatedTo)
//                             }
//                         }

//                         if (uploadedFrom != null || uploadedTo != null) {
//                             filteredData = filteredData.filter { item ->
//                                 val ts = item.uploadedAt
//                                 timestampInRange(ts, uploadedFrom, uploadedTo)
//                             }
//                         }

//                         // apply timer behavior (if timer provided, keep entire filteredData, otherwise apply limit logic)
//                         if (etTimer.text.isNotBlank()) {
//                             sharedViewModel.filteredWipsList = filteredData.toMutableList()
//                         } else {
//                             if (limit > 0) {
//                                 sharedViewModel.filteredWipsList = filteredData.shuffled().take(limit).toMutableList()
//                             } else {
//                                 sharedViewModel.filteredWipsList = filteredData.toMutableList()
//                             }
//                         }

//                         // pass Read Aloud flag
//                         sharedViewModel.isReadAloud = switchMaterial.isChecked

//                         // navigate on main thread
//                         CoroutineScope(Dispatchers.Main).launch {
//                             findNavController().navigate(R.id.carouselFragment)
//                         }
//                     }
//                 }
//             } // btnApplyFilter click
//         } // mBinding.apply

//         // adapters callbacks
//         customTagAdapter.onCheckBoxClicked = { tagValue, isChecked, _ ->
//             if (isChecked) filteredTagsList.add(tagValue) else filteredTagsList.remove(tagValue)
//         }
//         categoryAdapter.onCheckBoxClicked = { tagValue, isChecked, _ ->
//             if (isChecked) filteredCategoryList.add(tagValue) else filteredCategoryList.remove(tagValue)
//         }
//     }

//     private fun setUpRecyclerView() {
//         mBinding.apply {
//             rvList.layoutManager = GridLayoutManager(requireContext(), 3)
//             rvList.adapter = customTagAdapter
//             categoryRv.layoutManager = GridLayoutManager(requireContext(), 3)
//             categoryRv.adapter = categoryAdapter
//         }
//     }

//     private fun readOperatorSetup() {
//         val readCountsStr = mutableListOf("=", "<", ">")
//         readCountsStr.add(0, "")
//         val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, readCountsStr)
//         mBinding.readSpinner.setSelection(0)
//         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//         mBinding.readSpinner.adapter = spinnerAdapter
//         mBinding.readSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//             override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                 if (position != 0) {
//                     readOperator = parent.getItemAtPosition(position).toString()
//                 }
//             }
//             override fun onNothingSelected(parent: AdapterView<*>) {}
//         }
//     }

//     private fun viewedOperatorSetup() {
//         val viewedCountsStr = mutableListOf("=", "<", ">")
//         viewedCountsStr.add(0, "")
//         val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, viewedCountsStr)
//         mBinding.viewedSpinner.setSelection(0)
//         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//         mBinding.viewedSpinner.adapter = spinnerAdapter
//         mBinding.viewedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//             override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                 if (position != 0) {
//                     viewedOperator = parent.getItemAtPosition(position).toString()
//                 }
//             }
//             override fun onNothingSelected(parent: AdapterView<*>) {}
//         }
//     }

//     // helper: check if single timestamp ts is inside optional range [from, to]
//     private fun timestampInRange(ts: Long?, from: Long?, to: Long?): Boolean {
//         if (ts == null) return false
//         from?.let { if (ts < it) return false }
//         to?.let { if (ts > it) return false }
//         return true
//     }

//     // show date-time and set provided TextView with formatted timestamp
//     private fun showDateTimePickerAndSetField(tv: android.widget.TextView) {
//         showDateTimePicker { millis ->
//             tv.text = formatTimestamp(millis)
//         }
//     }
// }

package com.rameez.hel.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.rameez.hel.R
import com.rameez.hel.repository.SupabaseWIPRepository
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import com.rameez.hel.viewmodel.WIPViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class WIPFilterFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var wipViewModel: WIPViewModel

    // Existing UI elements
    private lateinit var etFilterWord: EditText
    private lateinit var etFilterMeaning: EditText
    private lateinit var etFilterSampleSen: EditText
    private lateinit var etReadCount: EditText
    private lateinit var spinnerReadOperator: Spinner
    private lateinit var etViewedCount: EditText
    private lateinit var spinnerViewedOperator: Spinner
    private lateinit var btnApplyFilters: Button
    private lateinit var btnClearFilters: Button

    // NEW UI elements for encountered count
    private lateinit var etEncounteredCount: EditText
    private lateinit var spinnerEncounteredOperator: Spinner
    private lateinit var etEncounteredLastUpdatedFrom: EditText
    private lateinit var etEncounteredLastUpdatedTo: EditText
    private lateinit var spinnerEncounteredLastUpdatedOperator: Spinner

    // NEW UI elements for viewed count (extended)
    private lateinit var etViewedCountValue: EditText
    private lateinit var spinnerViewedCountOperator: Spinner
    private lateinit var etViewedLastUpdatedFrom: EditText
    private lateinit var etViewedLastUpdatedTo: EditText
    private lateinit var spinnerViewedLastUpdatedOperator: Spinner

    // NEW UI elements for first encountered timestamp
    private lateinit var etFirstEncounteredFrom: EditText
    private lateinit var etFirstEncounteredTo: EditText
    private lateinit var spinnerFirstEncounteredOperator: Spinner

    // NEW UI elements for first viewed timestamp
    private lateinit var etFirstViewedFrom: EditText
    private lateinit var etFirstViewedTo: EditText
    private lateinit var spinnerFirstViewedOperator: Spinner

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wip_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val repository = SupabaseWIPRepository()
        val factory = WIPViewModelFactory(repository)
        wipViewModel = ViewModelProvider(this, factory)[WIPViewModel::class.java]

        // Initialize existing UI elements
        etFilterWord = view.findViewById(R.id.etFilterWord)
        etFilterMeaning = view.findViewById(R.id.etFilterMeaning)
        etFilterSampleSen = view.findViewById(R.id.etFilterSampleSen)
        etReadCount = view.findViewById(R.id.etReadCount)
        spinnerReadOperator = view.findViewById(R.id.spinnerReadOperator)
        etViewedCount = view.findViewById(R.id.etViewedCount)
        spinnerViewedOperator = view.findViewById(R.id.spinnerViewedOperator)
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters)
        btnClearFilters = view.findViewById(R.id.btnClearFilters)

        // Initialize NEW UI elements
        etEncounteredCount = view.findViewById(R.id.etEncounteredCount)
        spinnerEncounteredOperator = view.findViewById(R.id.spinnerEncounteredOperator)
        etEncounteredLastUpdatedFrom = view.findViewById(R.id.etEncounteredLastUpdatedFrom)
        etEncounteredLastUpdatedTo = view.findViewById(R.id.etEncounteredLastUpdatedTo)
        spinnerEncounteredLastUpdatedOperator = view.findViewById(R.id.spinnerEncounteredLastUpdatedOperator)

        etViewedCountValue = view.findViewById(R.id.etViewedCountValue)
        spinnerViewedCountOperator = view.findViewById(R.id.spinnerViewedCountOperator)
        etViewedLastUpdatedFrom = view.findViewById(R.id.etViewedLastUpdatedFrom)
        etViewedLastUpdatedTo = view.findViewById(R.id.etViewedLastUpdatedTo)
        spinnerViewedLastUpdatedOperator = view.findViewById(R.id.spinnerViewedLastUpdatedOperator)

        etFirstEncounteredFrom = view.findViewById(R.id.etFirstEncounteredFrom)
        etFirstEncounteredTo = view.findViewById(R.id.etFirstEncounteredTo)
        spinnerFirstEncounteredOperator = view.findViewById(R.id.spinnerFirstEncounteredOperator)

        etFirstViewedFrom = view.findViewById(R.id.etFirstViewedFrom)
        etFirstViewedTo = view.findViewById(R.id.etFirstViewedTo)
        spinnerFirstViewedOperator = view.findViewById(R.id.spinnerFirstViewedOperator)

        setupSpinners()
        setupDatePickers()
        setupButtons()
    }

    private fun setupSpinners() {
        val operators = arrayOf("Select", ">", "<", "=", ">=", "<=")
        val dateOperators = arrayOf("Select", "between", "before", "after")

        // Existing spinners
        spinnerReadOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, operators)
        spinnerViewedOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, operators)

        // NEW spinners
        spinnerEncounteredOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, operators)
        spinnerEncounteredLastUpdatedOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dateOperators)
        
        spinnerViewedCountOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, operators)
        spinnerViewedLastUpdatedOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dateOperators)
        
        spinnerFirstEncounteredOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dateOperators)
        spinnerFirstViewedOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dateOperators)
    }

    private fun setupDatePickers() {
        // Encountered Last Updated date pickers
        etEncounteredLastUpdatedFrom.setOnClickListener {
            showDatePicker { timestamp ->
                etEncounteredLastUpdatedFrom.setText(dateFormat.format(Date(timestamp)))
                etEncounteredLastUpdatedFrom.tag = timestamp
            }
        }

        etEncounteredLastUpdatedTo.setOnClickListener {
            showDatePicker { timestamp ->
                etEncounteredLastUpdatedTo.setText(dateFormat.format(Date(timestamp)))
                etEncounteredLastUpdatedTo.tag = timestamp
            }
        }

        // Viewed Last Updated date pickers
        etViewedLastUpdatedFrom.setOnClickListener {
            showDatePicker { timestamp ->
                etViewedLastUpdatedFrom.setText(dateFormat.format(Date(timestamp)))
                etViewedLastUpdatedFrom.tag = timestamp
            }
        }

        etViewedLastUpdatedTo.setOnClickListener {
            showDatePicker { timestamp ->
                etViewedLastUpdatedTo.setText(dateFormat.format(Date(timestamp)))
                etViewedLastUpdatedTo.tag = timestamp
            }
        }

        // First Encountered date pickers
        etFirstEncounteredFrom.setOnClickListener {
            showDatePicker { timestamp ->
                etFirstEncounteredFrom.setText(dateFormat.format(Date(timestamp)))
                etFirstEncounteredFrom.tag = timestamp
            }
        }

        etFirstEncounteredTo.setOnClickListener {
            showDatePicker { timestamp ->
                etFirstEncounteredTo.setText(dateFormat.format(Date(timestamp)))
                etFirstEncounteredTo.tag = timestamp
            }
        }

        // First Viewed date pickers
        etFirstViewedFrom.setOnClickListener {
            showDatePicker { timestamp ->
                etFirstViewedFrom.setText(dateFormat.format(Date(timestamp)))
                etFirstViewedFrom.tag = timestamp
            }
        }

        etFirstViewedTo.setOnClickListener {
            showDatePicker { timestamp ->
                etFirstViewedTo.setText(dateFormat.format(Date(timestamp)))
                etFirstViewedTo.tag = timestamp
            }
        }
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
        btnApplyFilters.setOnClickListener {
            applyFilters()
        }

        btnClearFilters.setOnClickListener {
            clearFilters()
        }
    }

    private fun applyFilters() {
        // Get existing filter values
        val word = etFilterWord.text.toString().takeIf { it.isNotEmpty() }
        val meaning = etFilterMeaning.text.toString().takeIf { it.isNotEmpty() }
        val sampleSentence = etFilterSampleSen.text.toString().takeIf { it.isNotEmpty() }
        
        val readCount = etReadCount.text.toString().toFloatOrNull()
        val readOperator = spinnerReadOperator.selectedItem.toString().takeIf { it != "Select" }
        
        val viewedCount = etViewedCount.text.toString().toFloatOrNull()
        val viewedOperator = spinnerViewedOperator.selectedItem.toString().takeIf { it != "Select" }

        // Get NEW filter values - Encountered Count
        val encounteredCount = etEncounteredCount.text.toString().toIntOrNull()
        val encounteredOperator = spinnerEncounteredOperator.selectedItem.toString().takeIf { it != "Select" }
        
        val encounteredLastUpdatedFrom = etEncounteredLastUpdatedFrom.tag as? Long
        val encounteredLastUpdatedTo = etEncounteredLastUpdatedTo.tag as? Long
        val encounteredLastUpdatedOperator = spinnerEncounteredLastUpdatedOperator.selectedItem.toString().takeIf { it != "Select" }

        // Get NEW filter values - Viewed Count (extended)
        val viewedCountValue = etViewedCountValue.text.toString().toIntOrNull()
        val viewedCountOperator = spinnerViewedCountOperator.selectedItem.toString().takeIf { it != "Select" }
        
        val viewedLastUpdatedFrom = etViewedLastUpdatedFrom.tag as? Long
        val viewedLastUpdatedTo = etViewedLastUpdatedTo.tag as? Long
        val viewedLastUpdatedOperator = spinnerViewedLastUpdatedOperator.selectedItem.toString().takeIf { it != "Select" }

        // Get NEW filter values - First Encountered
        val firstEncounteredFrom = etFirstEncounteredFrom.tag as? Long
        val firstEncounteredTo = etFirstEncounteredTo.tag as? Long
        val firstEncounteredOperator = spinnerFirstEncounteredOperator.selectedItem.toString().takeIf { it != "Select" }

        // Get NEW filter values - First Viewed
        val firstViewedFrom = etFirstViewedFrom.tag as? Long
        val firstViewedTo = etFirstViewedTo.tag as? Long
        val firstViewedOperator = spinnerFirstViewedOperator.selectedItem.toString().takeIf { it != "Select" }

        // Store in SharedViewModel
        sharedViewModel.filteredWord = word
        sharedViewModel.filteredMeaning = meaning
        sharedViewModel.filteredSampleSen = sampleSentence
        sharedViewModel.readCount = readCount
        sharedViewModel.readOperator = readOperator
        sharedViewModel.viewedCount = viewedCount
        sharedViewModel.viewedOperator = viewedOperator
        
        // Store NEW filters
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

        // Apply filters using ViewModel
        wipViewModel.getFilteredWIPs(
            categories = sharedViewModel.categoryList.takeIf { it.isNotEmpty() },
            tags = sharedViewModel.tagsList.takeIf { it.isNotEmpty() },
            readCount = readCount,
            readOperator = readOperator,
            viewedCount = viewedCount,
            viewedOperator = viewedOperator,
            word = word,
            meaning = meaning,
            sampleSentence = sampleSentence,
            encounteredCount = encounteredCount,
            encounteredOperator = encounteredOperator,
            encounteredLastUpdatedFrom = encounteredLastUpdatedFrom,
            encounteredLastUpdatedTo = encounteredLastUpdatedTo,
            encounteredLastUpdatedOperator = encounteredLastUpdatedOperator,
            viewedCountValue = viewedCountValue,
            viewedCountOperator = viewedCountOperator,
            viewedLastUpdatedFrom = viewedLastUpdatedFrom,
            viewedLastUpdatedTo = viewedLastUpdatedTo,
            viewedLastUpdatedOperator = viewedLastUpdatedOperator,
            firstEncounteredFrom = firstEncounteredFrom,
            firstEncounteredTo = firstEncounteredTo,
            firstEncounteredOperator = firstEncounteredOperator,
            firstViewedFrom = firstViewedFrom,
            firstViewedTo = firstViewedTo,
            firstViewedOperator = firstViewedOperator
        )

        Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show()
    }

    private fun clearFilters() {
        // Clear existing fields
        etFilterWord.text.clear()
        etFilterMeaning.text.clear()
        etFilterSampleSen.text.clear()
        etReadCount.text.clear()
        spinnerReadOperator.setSelection(0)
        etViewedCount.text.clear()
        spinnerViewedOperator.setSelection(0)

        // Clear NEW fields
        etEncounteredCount.text.clear()
        spinnerEncounteredOperator.setSelection(0)
        etEncounteredLastUpdatedFrom.text.clear()
        etEncounteredLastUpdatedFrom.tag = null
        etEncounteredLastUpdatedTo.text.clear()
        etEncounteredLastUpdatedTo.tag = null
        spinnerEncounteredLastUpdatedOperator.setSelection(0)

        etViewedCountValue.text.clear()
        spinnerViewedCountOperator.setSelection(0)
        etViewedLastUpdatedFrom.text.clear()
        etViewedLastUpdatedFrom.tag = null
        etViewedLastUpdatedTo.text.clear()
        etViewedLastUpdatedTo.tag = null
        spinnerViewedLastUpdatedOperator.setSelection(0)

        etFirstEncounteredFrom.text.clear()
        etFirstEncounteredFrom.tag = null
        etFirstEncounteredTo.text.clear()
        etFirstEncounteredTo.tag = null
        spinnerFirstEncounteredOperator.setSelection(0)

        etFirstViewedFrom.text.clear()
        etFirstViewedFrom.tag = null
        etFirstViewedTo.text.clear()
        etFirstViewedTo.tag = null
        spinnerFirstViewedOperator.setSelection(0)

        // Clear SharedViewModel
        sharedViewModel.clearAllFilters()

        // Reload all data
        wipViewModel.getWIPs()

        Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show()
    }
}

