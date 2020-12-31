package com.sarrm.android.view.adapters

import android.os.Build
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sarrm.android.data.realm.ReplySetting
import com.sarrm.android.data.models.ReplySettingClicked
import com.sarrm.android.utils.AppConstants
import com.jakewharton.rxrelay2.PublishRelay
import com.sarrm.android.view.holder.ReplySettingItemViewHolder
import com.sarrm.android.view.models.ReplySettingListViewModel

class ReplySettingListAdapter(private val selectionList : MutableLiveData<MutableList<Long>>, private val  currentMode: MutableLiveData<AppConstants.ListMode>) :
    ListAdapter<ReplySetting, ReplySettingItemViewHolder>(
        ReplySettingDiffCallback()
    ) {
    lateinit var replySettingListViewModel: ReplySettingListViewModel
    val itemClicks = PublishRelay.create<ReplySettingClicked>()!!

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplySettingItemViewHolder {
        return ReplySettingItemViewHolder.from(parent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ReplySettingItemViewHolder, position: Int) = holder.bind(
        getItem(position),
        itemClicks,
        selectionList,
        currentMode,
        replySettingListViewModel
    )

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
}

class ReplySettingDiffCallback : DiffUtil.ItemCallback<ReplySetting>() {
    override fun areItemsTheSame(oldItem: ReplySetting, newItem: ReplySetting): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ReplySetting, newItem: ReplySetting): Boolean {
        return oldItem.equals(newItem)
    }
}
