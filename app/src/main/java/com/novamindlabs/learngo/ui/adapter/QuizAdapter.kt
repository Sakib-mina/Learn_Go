package com.novamindlabs.learngo.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.ItemQuizListBinding

data class QuizCategory(
    val id: String,
    val title: String,
    val questionCountText: String = "20 Questions",
    val iconRes: Int,
    val startColorHex: String,
    val endColorHex: String,
    val isFree: Boolean,
    val isPopular: Boolean = false
)

class QuizAdapter(
    private val onItemClick: (QuizCategory) -> Unit
) : ListAdapter<QuizCategory, QuizAdapter.QuizViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemQuizListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuizCategory) {
            binding.apply {
                tvQuizTitle.text = item.title
                tvQuestionCount.text = item.questionCountText
                ivQuizIcon.setImageResource(item.iconRes)

                // Dynamic Gradient Background for Category Icon
                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.parseColor(item.startColorHex),
                        Color.parseColor(item.endColorHex)
                    )
                ).apply {
                    cornerRadius = dpToPx(12)
                }
                ivQuizIcon.background = gradient

                // Free vs Premium Badge Styling
                if (item.isFree) {
                    tvBadge.text = "Free"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_free)
                    tvBadge.setTextColor(Color.parseColor("#15803D"))
                } else {
                    tvBadge.text = "Premium"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_premium)
                    tvBadge.setTextColor(Color.parseColor("#854D0E"))
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

        private fun dpToPx(dp: Int): Float {
            val density = itemView.context.resources.displayMetrics.density
            return dp * density
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<QuizCategory>() {
        override fun areItemsTheSame(oldItem: QuizCategory, newItem: QuizCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QuizCategory, newItem: QuizCategory): Boolean {
            return oldItem == newItem
        }
    }
}