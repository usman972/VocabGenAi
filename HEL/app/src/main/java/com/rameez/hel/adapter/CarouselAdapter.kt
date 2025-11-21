// package com.rameez.hel.adapter

// import android.annotation.SuppressLint
// import android.view.LayoutInflater
// import android.view.ViewGroup
// import androidx.recyclerview.widget.ListAdapter
// import androidx.recyclerview.widget.RecyclerView
// import com.rameez.hel.data.model.WIPModel
// import com.rameez.hel.databinding.CarouselItemBinding
// import java.text.SimpleDateFormat
// import java.util.Locale

// class CarouselAdapter :
//     ListAdapter<WIPModel, RecyclerView.ViewHolder>(CarouselDiffUtil()) {

//     var onItemClick: ((Int, Int) -> Unit)? = null

//     class CarouselDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<WIPModel>() {
//         override fun areItemsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
//             return oldItem.id == newItem.id
//         }

//         override fun areContentsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
//             return oldItem == newItem
//         }
//     }

//     inner class CarouselViewHolder(private val binding: CarouselItemBinding) :
//         RecyclerView.ViewHolder(binding.root) {

//         @SuppressLint("SetTextI18n")
//         fun bind(item: WIPModel) {

//             binding.apply {
//                 txtWordType.text = item.category
//                 txtWord.text = item.wip
//                 txtWordMeaning.text = item.meaning
//                 txtSampleSentence.text = item.sampleSentence
//                 txtTags.text = item.customTag?.joinToString(", ")

//                 txtViewCount.text = "Viewed ${item.displayCount?.toInt()} times"
//                 txtReadCount.text = "Encountered ${item.readCount?.toInt()} times"

//                 txtCreatedAt.text = "Created: ${formatTimestamp(item.createdAt)}"
//                 txtUpdatedAt.text = "Updated: ${formatTimestamp(item.updatedAt)}"

//                 wipCv.setOnClickListener {
//                     item.id?.let { it1 -> onItemClick?.invoke(it1, position) }
//                 }
//             }
//         }

//         private fun formatTimestamp(timestamp: Long?): String {
//             if (timestamp == null) return "--"
//             val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
//             return sdf.format(java.util.Date(timestamp))
//         }
//     }

//     override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//         (holder as CarouselViewHolder).bind(getItem(position))
//     }
// fun getWIPItem(position: Int): WIPModel {
//     return currentList[position]
// }


//     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//         return CarouselViewHolder(
//             CarouselItemBinding.inflate(
//                 LayoutInflater.from(parent.context),
//                 parent,
//                 false
//             )
//         )
//     }
// }

package com.rameez.hel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.R
import com.rameez.hel.model.WIPModel

class CarouselAdapter : ListAdapter<WIPModel, CarouselAdapter.WIPCardViewHolder>(WIPDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WIPCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wip_card, parent, false)
        return WIPCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: WIPCardViewHolder, position: Int) {
        val wipItem = getItem(position)
        holder.bind(wipItem)
    }

    inner class WIPCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtWord: TextView = itemView.findViewById(R.id.txtWord)
        private val txtWordMeaning: TextView = itemView.findViewById(R.id.txtWordMeaning)
        private val txtSampleSentence: TextView = itemView.findViewById(R.id.txtSampleSentence)
        private val txtWordType: TextView = itemView.findViewById(R.id.txtWordType)
        private val txtTags: TextView = itemView.findViewById(R.id.txtTags)

        fun bind(wipItem: WIPModel) {
            // Set word
            txtWord.text = wipItem.wip ?: "No word"

            // Set meaning
            txtWordMeaning.text = wipItem.meaning ?: "No meaning available"

            // Set sample sentence
            txtSampleSentence.text = wipItem.sampleSentence ?: "No sample sentence available"

            // Set category
            txtWordType.text = wipItem.category ?: "Uncategorized"

            // Set tags
            val tags = wipItem.customTag
            if (!tags.isNullOrEmpty()) {
                txtTags.text = "Tags: ${tags.joinToString(", ")}"
                txtTags.visibility = View.VISIBLE
            } else {
                txtTags.visibility = View.GONE
            }
        }
    }

    class WIPDiffCallback : DiffUtil.ItemCallback<WIPModel>() {
        override fun areItemsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }
    }
}
Now, let me also provide you with the Bolt Database Migration SQL that you need to run in your Bolt Database dashboard:

15. Bolt Database Migration SQL
Location: Run this in your Bolt Database Dashboard → SQL Editor


/*
  # WIP Items Table Migration

  1. New Tables
    - `wip_items`
      - `id` (uuid, primary key)
      - `wip` (text) - The word/phrase
      - `meaning` (text) - Definition/meaning
      - `sample_sentence` (text) - Example usage
      - `category` (text) - Word category
      - `custom_tag` (text) - Comma-separated tags
      - `read_count` (float) - Number of times read
      - `display_count` (float) - Legacy display count
      - `created_at` (bigint) - Creation timestamp
      - `updated_at` (bigint) - Last update timestamp
      - `uploaded_at` (bigint) - Upload timestamp
      - `encountered_count` (int) - Number of times manually encountered
      - `viewed_count` (int) - Number of times automatically viewed
      - `encountered_last_updated_at` (bigint) - Last time encountered count updated
      - `viewed_last_updated_at` (bigint) - Last time viewed count updated
      - `first_encountered_at` (bigint) - First time encountered
      - `first_viewed_at` (bigint) - First time viewed

  2. Security
    - Enable RLS on `wip_items` table
    - Add policies for authenticated users to manage their data
*/

-- Create the wip_items table
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
  encountered_count int DEFAULT 0,
  viewed_count int DEFAULT 0,
  encountered_last_updated_at bigint,
  viewed_last_updated_at bigint,
  first_encountered_at bigint,
  first_viewed_at bigint
);

-- Enable Row Level Security
ALTER TABLE wip_items ENABLE ROW LEVEL SECURITY;

-- Policy: Allow all authenticated users to read all wip_items
CREATE POLICY "Authenticated users can read all wip items"
  ON wip_items
  FOR SELECT
  TO authenticated
  USING (true);

-- Policy: Allow all authenticated users to insert wip_items
CREATE POLICY "Authenticated users can insert wip items"
  ON wip_items
  FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- Policy: Allow all authenticated users to update wip_items
CREATE POLICY "Authenticated users can update wip items"
  ON wip_items
  FOR UPDATE
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- Policy: Allow all authenticated users to delete wip_items
CREATE POLICY "Authenticated users can delete wip items"
  ON wip_items
  FOR DELETE
  TO authenticated
  USING (true);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_wip_items_category ON wip_items(category);
CREATE INDEX IF NOT EXISTS idx_wip_items_encountered_count ON wip_items(encountered_count);
CREATE INDEX IF NOT EXISTS idx_wip_items_viewed_count ON wip_items(viewed_count);
CREATE INDEX IF NOT EXISTS idx_wip_items_created_at ON wip_items(created_at);

