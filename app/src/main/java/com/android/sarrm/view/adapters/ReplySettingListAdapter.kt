package com.android.sarrm.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.databinding.ItemReplySettingBinding


class ReplySettingListAdapter(private val clickListener: ClickListener) : ListAdapter<ReplySetting, ReplySettingListAdapter.ViewHolder>(
    PlantDiffCallback()
) {
    private val COUNTDOWN_RUNNING_TIME = 500

    private var animationUp: Animation? = null
    private  var animationDown:Animation? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(
        getItem(position),
        clickListener
    )

    class ViewHolder(val binding: ItemReplySettingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReplySetting, clickListener: ClickListener) {
            with(binding) {
                replySetting = item
                binding.executePendingBindings()
                binding.clickListener = clickListener
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemReplySettingBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

    }
}
class PlantDiffCallback : DiffUtil.ItemCallback<ReplySetting>() {
    override fun areItemsTheSame(oldItem: ReplySetting, newItem: ReplySetting): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ReplySetting, newItem: ReplySetting): Boolean {
        return oldItem.equals(newItem)
    }
}

class ClickListener(val clickListener: (replySettingId: String) -> Unit) {
    fun onClickItem(replySetting: ReplySetting) = clickListener(replySetting.id)
}