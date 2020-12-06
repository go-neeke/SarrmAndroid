package com.android.sarrm.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.savedstate.SavedStateRegistryOwner
import com.android.sarrm.R
import com.android.sarrm.databinding.FragmentReplySettingBinding
import com.android.sarrm.view.factories.ViewModelFactory
import com.android.sarrm.view.models.ReplySettingViewModel

class ReplySettingFragment : Fragment() {
    private lateinit var binding: FragmentReplySettingBinding
    private val replySettingViewModel by viewModels<ReplySettingViewModel> {
        ViewModelFactory(
            this,
            requireActivity()
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
            false,
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

    fun Fragment.getViewModelStoreOwner(): SavedStateRegistryOwner = try {
        requireActivity()
    } catch (e: IllegalStateException) {
        this
    }
}