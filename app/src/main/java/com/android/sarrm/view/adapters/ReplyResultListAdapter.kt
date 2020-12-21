package com.android.sarrm.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.sarrm.data.models.ReplyResult
import com.android.sarrm.databinding.ItemReplyResultBinding

class ReplyResultListAdapter() : RecyclerView.Adapter<ReplyResultListAdapter.ViewHolder>() {

    var replyResultList : List<ReplyResult> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(
        replyResultList[position],
    )

    class ViewHolder(val binding: ItemReplyResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReplyResult) {
            with(binding) {
                replyResult = item
                binding.executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemReplyResultBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

    }

    override fun getItemCount() = replyResultList.size
}