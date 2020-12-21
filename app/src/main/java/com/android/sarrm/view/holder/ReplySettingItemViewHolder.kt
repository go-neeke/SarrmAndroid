package com.android.sarrm.view.holder

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.android.sarrm.R
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.data.models.ReplySettingClicked
import com.android.sarrm.databinding.ItemReplySettingBinding
import com.android.sarrm.view.models.ReplySettingListViewModel
import com.jakewharton.rxrelay2.PublishRelay

open class ReplySettingItemViewHolder(val binding: ItemReplySettingBinding) :
    RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(
        item: ReplySetting,
        itemClicks: PublishRelay<ReplySettingClicked>,
        replySettingListViewModel: ReplySettingListViewModel
    ) {
        with(binding) {
            replySetting = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                itemClicks.accept(ReplySettingClicked(item, itemId))
            }

            if (item.isOn) {
                binding.settingItemToggle.setImageDrawable(binding.root.resources.getDrawable(R.drawable.ic_baseline_toggle_on_24))
                Toast.makeText(binding.root.context, "자동 응답이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                binding.settingItemToggle.setImageDrawable(binding.root.resources.getDrawable(R.drawable.ic_outline_toggle_off_24))
                Toast.makeText(binding.root.context, "자동 응답이 비활성화되었습니다.", Toast.LENGTH_SHORT).show()
            }

            binding.settingItemToggle.setOnClickListener {
                replySettingListViewModel.settingToggle(item.id, !item.isOn)
            }
        }
    }

    companion object {
        fun from(parent: ViewGroup): ReplySettingItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemReplySettingBinding.inflate(layoutInflater, parent, false)
            return ReplySettingItemViewHolder(binding)
        }
    }

}