package com.android.sarrm.view.adapters

import android.os.Build
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.data.models.ReplySettingClicked
import com.jakewharton.rxrelay2.PublishRelay
import com.android.sarrm.view.holder.ReplySettingItemViewHolder
import com.android.sarrm.view.models.ReplySettingListViewModel

class ReplySettingListAdapter() :
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
