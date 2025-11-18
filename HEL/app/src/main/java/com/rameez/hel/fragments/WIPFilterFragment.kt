package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.rameez.hel.utils.animatePress
import com.rameez.hel.utils.setupAutoRepeat
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rameez.hel.R
import com.rameez.hel.adapter.CategoryAdapter
import com.rameez.hel.adapter.CustomTagsAdapter
import com.rameez.hel.databinding.FragmentWIPFilterBinding
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WIPFilterFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPFilterBinding
    private lateinit var customTagAdapter: CustomTagsAdapter
    private val wipViewModel: WIPViewModel by activityViewModels()
    private val customTags = mutableListOf<String>()
    private lateinit var filteredCategoryList: MutableList<String>
    private lateinit var filteredTagsList: MutableList<String>
    private var filteredReadCount: Float? = null
    private var filteredViewedCount: Float? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val categoryAdapter = CategoryAdapter()
    private val categoriesList = arrayListOf<String>()
    private var readOperator: String? = null
    private var viewedOperator: String? = null
    private var isFirstTime = false
    private var filteredWIP: String? = null
    private var filteredMeaning: String? = null
    private var filteredSampleSen: String? = null

    // DATE/TIME format used across UI
    private val UI_TS_FORMAT = "dd/MM/yyyy HH:mm:ss"
    private val sdf = SimpleDateFormat(UI_TS_FORMAT, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customTagAdapter = CustomTagsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (::mBinding.isInitialized.not()) {
            isFirstTime = true
            mBinding = FragmentWIPFilterBinding.inflate(layoutInflater, container, false)
        }
        return mBinding.root
    }

    // show combined Date -> Time -> Seconds picker (uses your util for selecting seconds)
    private fun showDateTimePicker(onResult: (Long) -> Unit) {
        val c = Calendar.getInstance()

        val datePicker = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        // seconds selector (your util)
                        val secondsPicker = com.rameez.hel.utils.TimePicker()
                        secondsPicker.setTitle("Select Seconds")
                        // don't show hours/minutes in seconds picker
                        secondsPicker.includeHours = false
                        secondsPicker.setOnTimeSetOption("Set") { _, _, seconds ->
                            c.set(year, month, day, hour, minute, seconds)
                            onResult(c.timeInMillis)
                        }
                        secondsPicker.show(requireActivity().supportFragmentManager, "sec_picker")
                    },
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun formatTimestamp(ms: Long?): String {
        if (ms == null || ms == 0L) return ""
        return try {
            sdf.format(Date(ms))
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseTimestamp(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        return try {
            sdf.parse(text)?.time
        } catch (e: ParseException) {
            null
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filteredCategoryList = sharedViewModel.categoryList
        filteredTagsList = sharedViewModel.tagsList
        filteredReadCount = sharedViewModel.readCount
        filteredViewedCount = sharedViewModel.viewedCount
        readOperator = sharedViewModel.readOperator
        viewedOperator = sharedViewModel.viewedOperator
        filteredWIP = sharedViewModel.filteredWord
        filteredMeaning = sharedViewModel.filteredMeaning
        filteredSampleSen = sharedViewModel.filteredSampleSen

        // initialize UI fields
        mBinding.etReadCount.text = null
        mBinding.etViewedCount.text = null
        mBinding.etTimer.text = ""
        setUpRecyclerView()

        wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { list ->
            // populate category/tag lists
            list.forEach { wipItem ->
                val lowerCaseCategory = wipItem.category?.lowercase(Locale.ROOT)
                if (wipItem.category !in categoriesList && lowerCaseCategory !in categoriesList.map { it.lowercase(Locale.ROOT) }) {
                    wipItem.category?.let { categoriesList.add(it) }
                }
                wipItem.customTag?.filterNot { it.isEmpty() }?.forEach { customTag ->
                    val lower = customTag.lowercase(Locale.ROOT)
                    if (customTag !in customTags && lower !in customTags.map { it.lowercase(Locale.ROOT) }) {
                        customTags.add(customTag)
                    }
                }
            }
            readOperatorSetup()
            viewedOperatorSetup()
            categoryAdapter.submitList(categoriesList.toList())
            customTagAdapter.submitList(customTags.toList())
        }

        mBinding.apply {
            // Timer picker
            btnTimer.setOnClickListener {
                showDateTimePicker { millis ->
                    etTimer.text = formatTimestamp(millis)
                    val c = Calendar.getInstance().apply { timeInMillis = millis }
                    sharedViewModel.selectedHours = c.get(Calendar.HOUR_OF_DAY)
                    sharedViewModel.selectedMins = c.get(Calendar.MINUTE)
                    sharedViewModel.selectedSecs = c.get(Calendar.SECOND)
                }
            }

            // -------- READ COUNT --------
            fun updateReadButtonsState() {
                val value = etReadCount.text.toString().toIntOrNull() ?: 0
                btnReadMinus.isEnabled = value > 0
                btnReadMinus.alpha = if (value > 0) 1f else 0.4f
            }

            btnReadMinus.setOnClickListener {
                it.animatePress()
                val v = etReadCount.text.toString().toIntOrNull() ?: 0
                etReadCount.setText((v - 1).coerceAtLeast(0).toString())
                updateReadButtonsState()
            }

            btnReadPlus.setOnClickListener {
                it.animatePress()
                val v = etReadCount.text.toString().toIntOrNull() ?: 0
                etReadCount.setText((v + 1).toString())
                updateReadButtonsState()
            }

            btnReadMinus.setupAutoRepeat {
                btnReadMinus.performClick()
            }

            btnReadPlus.setupAutoRepeat {
                btnReadPlus.performClick()
            }

            etReadCount.doAfterTextChanged {
                filteredReadCount = it?.toString()?.trim()?.toFloatOrNull()
                updateReadButtonsState()
            }



            // -------- VIEWED COUNT --------
            fun updateViewedButtonsState() {
                val value = etViewedCount.text.toString().toIntOrNull() ?: 0
                btnViewedMinus.isEnabled = value > 0
                btnViewedMinus.alpha = if (value > 0) 1f else 0.4f
            }

            btnViewedMinus.setOnClickListener {
                it.animatePress()
                val v = etViewedCount.text.toString().toIntOrNull() ?: 0
                etViewedCount.setText((v - 1).coerceAtLeast(0).toString())
                updateViewedButtonsState()
            }

            btnViewedPlus.setOnClickListener {
                it.animatePress()
                val v = etViewedCount.text.toString().toIntOrNull() ?: 0
                etViewedCount.setText((v + 1).toString())
                updateViewedButtonsState()
            }

            btnViewedMinus.setupAutoRepeat {
                btnViewedMinus.performClick()
            }

            btnViewedPlus.setupAutoRepeat {
                btnViewedPlus.performClick()
            }


            etViewedCount.doAfterTextChanged {
                filteredViewedCount = it?.toString()?.trim()?.toFloatOrNull()
                updateViewedButtonsState()
            }



            btnClearFilters.setOnClickListener {
                filteredCategoryList.clear()
                filteredTagsList.clear()
                filteredReadCount = null
                filteredViewedCount = null
                // clear UI timestamp fields
                etCreatedFrom.text = ""
                etCreatedTo.text = ""
                etUpdatedFrom.text = ""
                etUpdatedTo.text = ""
                etUploadedFrom.text = ""
                etUploadedTo.text = ""
                etWIP.text = null
                etMeaning.text = null
                etSampleSen.text = null
                etLimit.text = null
                etReadCount.text = null
                etViewedCount.text = null
                switchMaterial.isChecked = false
                Toast.makeText(requireContext(), "All filters are cleared", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }

            // timestamp field taps -> show date/time
            etCreatedFrom.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
            etCreatedTo.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
            etUpdatedFrom.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
            etUpdatedTo.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
            etUploadedFrom.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }
            etUploadedTo.setOnClickListener { showDateTimePickerAndSetField(it as android.widget.TextView) }

            btnApplyFilter.setOnClickListener {
                // collect numeric & text filters
                filteredReadCount = etReadCount.text?.toString()?.trim()?.toFloatOrNull()
                filteredViewedCount = etViewedCount.text?.toString()?.trim()?.toFloatOrNull()
                filteredWIP = etWIP.text?.toString()?.trim()
                filteredMeaning = etMeaning.text?.toString()?.trim()
                filteredSampleSen = etSampleSen.text?.toString()?.trim()

                // parse timestamp ranges
                val createdFrom = parseTimestamp(etCreatedFrom.text?.toString())
                val createdTo = parseTimestamp(etCreatedTo.text?.toString())
                val updatedFrom = parseTimestamp(etUpdatedFrom.text?.toString())
                val updatedTo = parseTimestamp(etUpdatedTo.text?.toString())
                val uploadedFrom = parseTimestamp(etUploadedFrom.text?.toString())
                val uploadedTo = parseTimestamp(etUploadedTo.text?.toString())

                // get limit
                val limit = etLimit.text?.toString()?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0

                wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { data ->
                    // Filter on background thread
                    CoroutineScope(Dispatchers.IO).launch {
                        var filteredData = data

                        // text filters
                        if (!filteredWIP.isNullOrBlank()) {
                            filteredData = filteredData.filter { it.wip?.contains(filteredWIP!!, ignoreCase = true) == true }
                        }
                        if (!filteredMeaning.isNullOrBlank()) {
                            filteredData = filteredData.filter { it.meaning?.contains(filteredMeaning!!, ignoreCase = true) == true }
                        }
                        if (!filteredSampleSen.isNullOrBlank()) {
                            filteredData = filteredData.filter { it.sampleSentence?.contains(filteredSampleSen!!, ignoreCase = true) == true }
                        }

                        // category filter
                        if (filteredCategoryList.isNotEmpty()) {
                            val lowerFilteredCats = filteredCategoryList.map { it.lowercase(Locale.ROOT) }
                            filteredData = filteredData.filter { it.category?.lowercase(Locale.ROOT) in lowerFilteredCats }
                        }

                        // tag filter
                        if (filteredTagsList.isNotEmpty()) {
                            val lowerTags = filteredTagsList.map { it.lowercase(Locale.ROOT) }
                            filteredData = filteredData.filter { wipItem ->
                                wipItem.customTag?.any { tag -> tag.lowercase(Locale.ROOT) in lowerTags } ?: false
                            }
                        }

                        // read/view numeric operators
                        if (filteredReadCount != null) {
                            filteredData = when (readOperator) {
                                "=" -> filteredData.filter { it.readCount == filteredReadCount }
                                ">" -> filteredData.filter { (it.readCount ?: 0f) > filteredReadCount!! }
                                "<" -> filteredData.filter { (it.readCount ?: 0f) < filteredReadCount!! }
                                else -> filteredData
                            }
                        }

                        if (filteredViewedCount != null) {
                            filteredData = when (viewedOperator) {
                                "=" -> filteredData.filter { it.displayCount == filteredViewedCount }
                                ">" -> filteredData.filter { (it.displayCount ?: 0f) > filteredViewedCount!! }
                                "<" -> filteredData.filter { (it.displayCount ?: 0f) < filteredViewedCount!! }
                                else -> filteredData
                            }
                        }

                        // Timestamp range filters: created / updated / uploaded
                        if (createdFrom != null || createdTo != null) {
                            filteredData = filteredData.filter { item ->
                                val ts = item.createdAt
                                timestampInRange(ts, createdFrom, createdTo)
                            }
                        }

                        if (updatedFrom != null || updatedTo != null) {
                            filteredData = filteredData.filter { item ->
                                val ts = item.updatedAt
                                timestampInRange(ts, updatedFrom, updatedTo)
                            }
                        }

                        if (uploadedFrom != null || uploadedTo != null) {
                            filteredData = filteredData.filter { item ->
                                val ts = item.uploadedAt
                                timestampInRange(ts, uploadedFrom, uploadedTo)
                            }
                        }

                        // apply timer behavior (if timer provided, keep entire filteredData, otherwise apply limit logic)
                        if (etTimer.text.isNotBlank()) {
                            sharedViewModel.filteredWipsList = filteredData.toMutableList()
                        } else {
                            if (limit > 0) {
                                sharedViewModel.filteredWipsList = filteredData.shuffled().take(limit).toMutableList()
                            } else {
                                sharedViewModel.filteredWipsList = filteredData.toMutableList()
                            }
                        }

                        // pass Read Aloud flag
                        sharedViewModel.isReadAloud = switchMaterial.isChecked

                        // navigate on main thread
                        CoroutineScope(Dispatchers.Main).launch {
                            findNavController().navigate(R.id.carouselFragment)
                        }
                    }
                }
            } // btnApplyFilter click
        } // mBinding.apply

        // adapters callbacks
        customTagAdapter.onCheckBoxClicked = { tagValue, isChecked, _ ->
            if (isChecked) filteredTagsList.add(tagValue) else filteredTagsList.remove(tagValue)
        }
        categoryAdapter.onCheckBoxClicked = { tagValue, isChecked, _ ->
            if (isChecked) filteredCategoryList.add(tagValue) else filteredCategoryList.remove(tagValue)
        }
    }

    private fun setUpRecyclerView() {
        mBinding.apply {
            rvList.layoutManager = GridLayoutManager(requireContext(), 3)
            rvList.adapter = customTagAdapter
            categoryRv.layoutManager = GridLayoutManager(requireContext(), 3)
            categoryRv.adapter = categoryAdapter
        }
    }

    private fun readOperatorSetup() {
        val readCountsStr = mutableListOf("=", "<", ">")
        readCountsStr.add(0, "")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, readCountsStr)
        mBinding.readSpinner.setSelection(0)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.readSpinner.adapter = spinnerAdapter
        mBinding.readSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    readOperator = parent.getItemAtPosition(position).toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun viewedOperatorSetup() {
        val viewedCountsStr = mutableListOf("=", "<", ">")
        viewedCountsStr.add(0, "")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, viewedCountsStr)
        mBinding.viewedSpinner.setSelection(0)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.viewedSpinner.adapter = spinnerAdapter
        mBinding.viewedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    viewedOperator = parent.getItemAtPosition(position).toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // helper: check if single timestamp ts is inside optional range [from, to]
    private fun timestampInRange(ts: Long?, from: Long?, to: Long?): Boolean {
        if (ts == null) return false
        from?.let { if (ts < it) return false }
        to?.let { if (ts > it) return false }
        return true
    }

    // show date-time and set provided TextView with formatted timestamp
    private fun showDateTimePickerAndSetField(tv: android.widget.TextView) {
        showDateTimePicker { millis ->
            tv.text = formatTimestamp(millis)
        }
    }
}
