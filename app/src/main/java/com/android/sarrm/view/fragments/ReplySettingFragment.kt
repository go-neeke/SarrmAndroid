package com.android.sarrm.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.sarrm.R
import com.android.sarrm.data.db.ReplySettingDatabase
import com.android.sarrm.databinding.FragmentReplySettingBinding
import com.android.sarrm.view.factories.ViewModelFactory
import com.android.sarrm.view.models.ReplySettingViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_menu_reply_target.*

class ReplySettingFragment : Fragment() {
    private lateinit var binding: FragmentReplySettingBinding
    private val replySettingViewModel by viewModels<ReplySettingViewModel> {
        ViewModelFactory(
            ReplySettingDatabase.getInstance(this.requireContext()).dao,
            this
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reply_setting,
            container,
            false
        )

        binding.titleMenuName = resources.getString(R.string.title_menu_name)
        binding.titleMenuReplyTarget =resources.getString(R.string.title_menu_reply_target)
        binding.titleMenuTime = resources.getString(R.string.title_menu_time)
        binding.titleMenuReplyMessage = resources.getString(R.string.title_menu_reply_message)
        binding.replySettingViewModel = replySettingViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}