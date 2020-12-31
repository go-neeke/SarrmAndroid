package com.sarrm.android.view.holder

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.sarrm.android.data.realm.ReplySetting
import com.sarrm.android.data.models.ReplySettingClicked
import com.sarrm.android.databinding.ItemReplySettingBinding
import com.sarrm.android.utils.AppConstants
import com.sarrm.android.view.models.ReplySettingListViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.sarrm.android.R

open class ReplySettingItemViewHolder(val binding: ItemReplySettingBinding) :
    RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(
        item: ReplySetting,
        itemClicks: PublishRelay<ReplySettingClicked>,
        selectionList: MutableLiveData<MutableList<Long>>,
        currentMode: MutableLiveData<AppConstants.ListMode>,
        replySettingListViewModel: ReplySettingListViewModel
    ) {
        with(binding) {
            replySetting = item
            binding.executePendingBindings()

            // 체크 박스 셋팅
            binding.checkbox.visibility =
                if (currentMode.value == AppConstants.ListMode.SELECTION) View.VISIBLE else View.GONE
            binding.checkbox.isChecked = if (currentMode.value == AppConstants.ListMode.NONE) false else binding.checkbox.isChecked

            binding.checkbox.setOnClickListener {
                var tempList= selectionList.value
                if ((it as CheckBox).isChecked) tempList!!.add(itemId) else tempList!!.remove(itemId)
                selectionList.value = tempList
            }

            // 클릭 이벤트
            binding.root.setOnClickListener {
                itemClicks.accept(ReplySettingClicked(item, itemId))
                currentMode.value = AppConstants.ListMode.NONE
            }

            // 롱클릭 이벤트
            binding.root.setOnLongClickListener {
                if (currentMode.value != AppConstants.ListMode.SELECTION)
                    currentMode.value = AppConstants.ListMode.SELECTION
                true
            }

            if (item.isOn) {
                binding.settingItemToggle.setImageDrawable(binding.root.resources.getDrawable(R.drawable.ic_baseline_toggle_on_24))
            } else {
                binding.settingItemToggle.setImageDrawable(binding.root.resources.getDrawable(R.drawable.ic_outline_toggle_off_24))
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