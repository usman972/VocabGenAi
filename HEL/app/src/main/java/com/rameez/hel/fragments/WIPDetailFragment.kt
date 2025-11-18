package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rameez.hel.R
import com.rameez.hel.SharedPref
import com.rameez.hel.databinding.FragmentWIPDetailBinding
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WIPDetailFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPDetailBinding
    private val wipViewModel: WIPViewModel by activityViewModels()
    private lateinit var textToSpeech: TextToSpeech
    private var word: String = ""
    private var id: Int = 0
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWIPDetailBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    // format includes seconds
    private fun formatDate(time: Long?): String {
        if (time == null || time == 0L) return "--"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
        return sdf.format(Date(time))
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id = arguments?.getInt("wip_id", 0) ?: 0

        wipViewModel.getWIPById(id)?.observe(viewLifecycleOwner) { it ->
            if (it == null) return@observe

            mBinding.apply {
                word = it.wip ?: ""
                txtWord.text = it.wip
                txtMeaning.text = it.meaning
                txtSampleSentence.text = it.sampleSentence
                txtCategory.text = it.category
                tvTags.text = it.customTag?.joinToString(", ")

                txtReadCount.text = "${it.readCount?.toInt() ?: 0} times"
                txtViewCount.text = "${it.displayCount?.toInt() ?: 0} times"

                // SHOW TIMESTAMPS
                txtCreatedAt.text = "Created:  ${formatDate(it.createdAt)}"
                txtUpdatedAt.text = "Updated:  ${formatDate(it.updatedAt)}"
                txtUploadedAt.text = "Uploaded: ${formatDate(it.uploadedAt)}"
            }

            // Update view count timestamp when user opens detail (increment done elsewhere)
            if (it.id != null && it.displayCount != null) {
                wipViewModel.updateViewedCount(it.id, viewCount = it.displayCount ?: 0f)

            }
        }

        mBinding.imgBack.setOnClickListener { findNavController().navigateUp() }

        mBinding.btnEdit.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("wip_id", id)
            findNavController().navigate(R.id.WIPEditFragment, bundle)
        }

        mBinding.tvDeleteWIP.setOnClickListener {
            wipViewModel.deleteWIPById(id)
            lifecycleScope.launch {
                sharedViewModel.isWIPDeleted = true
                findNavController().navigateUp()
            }
        }

        mBinding.dEncountered.setOnClickListener {
            wipViewModel.resetEncountered(id)
            mBinding.txtReadCount.text = "0"
        }

        mBinding.dViewed.setOnClickListener {
            wipViewModel.resetViewed(id)
            mBinding.txtViewCount.text = "0"
        }

        // Mark as uploaded (sets uploadedAt and updatedAt)
        mBinding.btnUpload.setOnClickListener {
            wipViewModel.markUploaded(id)
            // refresh will come from observer; optionally show a toast:
            // Toast.makeText(requireContext(), "Marked uploaded", Toast.LENGTH_SHORT).show()
        }

        // TextToSpeech
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TAG", "Initialization Success")
            } else {
                Log.d("TAG", "Initialization Failed")
            }
        }
        textToSpeech.language = Locale.US

        mBinding.ivSpeaker.setOnClickListener {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech.shutdown()
    }
}
