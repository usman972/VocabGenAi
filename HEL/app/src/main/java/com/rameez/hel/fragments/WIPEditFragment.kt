package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rameez.hel.R
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.FragmentWIPEditBinding
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel

class WIPEditFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPEditBinding
    private val wipViewModel: WIPViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var tagsList = arrayListOf<String>()
    private var readOperator: String? = null

    private var editingId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWIPEditBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingId = arguments?.getInt("wip_id")

        readOperator = sharedViewModel.readOperator
        setupCategorySpinner()

        if (editingId != null) {
            loadExistingWIP(editingId!!)
        } else {
            mBinding.tvHeading.text = "Add WIP"
        }

        mBinding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.btnSave.setOnClickListener {
            saveWIP()
        }

        mBinding.btnAddTag.setOnClickListener {
            addTag()
        }

        mBinding.ivDeleteTags.setOnClickListener {
            if (editingId != null) {
                val bundle = Bundle()
                bundle.putInt("wip_id", editingId!!)
                findNavController().navigate(R.id.deleteTagsFragment, bundle)
            }
        }
    }

    private fun loadExistingWIP(id: Int) {
        wipViewModel.getWIPById(id)?.observe(viewLifecycleOwner) { it ->
            if (it == null) return@observe

            mBinding.tvHeading.text = "Edit WIP"
            mBinding.etWord.setText(it.wip)
            mBinding.etMeaning.setText(it.meaning)
            mBinding.etSampleSentence.setText(it.sampleSentence)
            mBinding.etCategory.setText(it.category)
            mBinding.tvTags.text = it.customTag?.joinToString(", ")

            mBinding.etRadCount.setText(it.readCount?.toInt()?.toString() ?: "0")
            mBinding.etViewedCount.setText(it.displayCount?.toInt()?.toString() ?: "0")

            if (!it.customTag.isNullOrEmpty()) {
                tagsList = ArrayList(it.customTag!!)
                mBinding.ivDeleteTags.visibility = View.VISIBLE
            } else {
                mBinding.ivDeleteTags.visibility = View.GONE
            }
        }
    }

    private fun saveWIP() {
        val wip = mBinding.etWord.text.toString().trim()
        val meaning = mBinding.etMeaning.text.toString().trim()
        val sampleSentence = mBinding.etSampleSentence.text.toString().trim()
        val category = mBinding.etCategory.text.toString().trim()
        val readCount = mBinding.etRadCount.text.toString().toFloatOrNull() ?: 0f
        val viewCount = mBinding.etViewedCount.text.toString().toFloatOrNull() ?: 0f
        val tags = mBinding.tvTags.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (wip.isBlank() || category.isBlank()) {
            Toast.makeText(requireContext(), "WIP and Category cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingId != null) {
            wipViewModel.updateWIP(
                id = editingId!!,
                category = category,
                wip = wip,
                meaning = meaning,
                sampleSentence = sampleSentence,
                customTag = tags,
                readCount = readCount,
                viewedCount = viewCount
            )
            Toast.makeText(requireContext(), "WIP updated", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { allItems ->
            if (allItems == null) return@observe

            val sr = allItems.size + 1f

            val newItem = WIPModel(
                sr = sr,
                category = category,
                wip = wip,
                meaning = meaning,
                sampleSentence = sampleSentence,
                customTag = tags,
                readCount = readCount,
                displayCount = viewCount,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                uploadedAt = null
            )

            wipViewModel.insertWIP(newItem)
            sharedViewModel.isWipAdded = true

            Toast.makeText(requireContext(), "New WIP added", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun addTag() {
        val newTag = mBinding.etTag.text.toString().trim()
        if (newTag.isBlank()) return
        tagsList.add(newTag)
        mBinding.tvTags.text = tagsList.joinToString(", ")
        mBinding.etTag.setText("")
        mBinding.ivDeleteTags.visibility = View.VISIBLE
    }

    private fun setupCategorySpinner() {
        val list = mutableListOf("", "Word", "Idiom", "Phrase")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, list)
        mBinding.readSpinner.adapter = adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        mBinding.readSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    val selected = parent.getItemAtPosition(position).toString()
                    readOperator = selected
                    mBinding.etCategory.setText(selected)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
