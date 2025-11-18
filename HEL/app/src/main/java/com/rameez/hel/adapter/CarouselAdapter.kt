package com.rameez.hel.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.CarouselItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CarouselAdapter :
    ListAdapter<WIPModel, RecyclerView.ViewHolder>(CarouselDiffUtil()) {

    var onItemClick: ((Int, Int) -> Unit)? = null

    class CarouselDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<WIPModel>() {
        override fun areItemsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }
    }

    inner class CarouselViewHolder(private val binding: CarouselItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: WIPModel) {

            binding.apply {
                txtWordType.text = item.category
                txtWord.text = item.wip
                txtWordMeaning.text = item.meaning
                txtSampleSentence.text = item.sampleSentence
                txtTags.text = item.customTag?.joinToString(", ")

                txtViewCount.text = "Viewed ${item.displayCount?.toInt()} times"
                txtReadCount.text = "Encountered ${item.readCount?.toInt()} times"

                txtCreatedAt.text = "Created: ${formatTimestamp(item.createdAt)}"
                txtUpdatedAt.text = "Updated: ${formatTimestamp(item.updatedAt)}"

                wipCv.setOnClickListener {
                    item.id?.let { it1 -> onItemClick?.invoke(it1, position) }
                }
            }
        }

        private fun formatTimestamp(timestamp: Long?): String {
            if (timestamp == null) return "--"
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CarouselViewHolder).bind(getItem(position))
    }
fun getWIPItem(position: Int): WIPModel {
    return currentList[position]
}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CarouselViewHolder(
            CarouselItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}
