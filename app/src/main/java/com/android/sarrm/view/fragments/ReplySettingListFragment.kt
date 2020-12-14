package com.android.sarrm.view.fragments

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.android.sarrm.R
import com.android.sarrm.databinding.FragmentReplySettingListBinding
import com.android.sarrm.view.adapters.ClickListener
import com.android.sarrm.view.adapters.ReplySettingListAdapter
import com.android.sarrm.view.factories.ViewModelFactory
import androidx.lifecycle.Observer
import com.android.sarrm.view.models.ReplySettingListViewModel

class ReplySettingListFragment : Fragment() {

    private lateinit var binding: FragmentReplySettingListBinding

    private val replySettingListViewModel by viewModels<ReplySettingListViewModel> {
        ViewModelFactory(
            this,
            requireActivity()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reply_setting_list,
            container,
            false
        )
        binding.lifecycleOwner = this

        val adapter = ReplySettingListAdapter(ClickListener { plantId ->
            replySettingListViewModel.onReplySettingClicked(plantId)
        })

        binding.recyclerviewReplySettingList.adapter = adapter

        binding.buttonReplySettingNew.setOnClickListener {
            this.findNavController().navigate(
                ReplySettingListFragmentDirections.actionReplySettingListFragmentToReplySettingFragment(
                    null
                )
            )
            replySettingListViewModel.onNewReplySettingNavigated()
        }

        replySettingListViewModel.allReplySettingList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        replySettingListViewModel.navigateToReplySettingDetail.observe(
            viewLifecycleOwner,
            Observer { plantId ->
                plantId?.let {
                    this.findNavController().navigate(
                        ReplySettingListFragmentDirections.actionReplySettingListFragmentToReplySettingFragment(
                            plantId
                        )
                    )

                    replySettingListViewModel.onNewReplySettingNavigated()
                }
            })

        return binding.root
    }

}