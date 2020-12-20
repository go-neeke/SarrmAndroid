package com.android.sarrm.view.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.savedstate.SavedStateRegistryOwner
import com.android.sarrm.R
import com.android.sarrm.view.factories.ViewModelFactory
import com.android.sarrm.view.models.ReplySettingViewModel

class ReplySettingFragment : Fragment() {
//    private val args: ReplySettingFragmentArgs by navArgs()
//    private lateinit var binding: FragmentReplySettingBinding
//    private val replySettingViewModel by viewModels<ReplySettingViewModel> {
//        ViewModelFactory(
//            this,
//            requireActivity()
//        )
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        binding = DataBindingUtil.inflate(
//            inflater,
//            R.layout.activity_reply_setting,
//            container,
//            false,
//        )
//
//        binding.titleMenuName = resources.getString(R.string.title_menu_name)
//        binding.titleMenuReplyTarget = resources.getString(R.string.title_menu_reply_target)
//        binding.titleMenuTime = resources.getString(R.string.title_menu_time)
//        binding.titleMenuReplyMessage = resources.getString(R.string.title_menu_reply_message)
//        binding.replySettingViewModel = replySettingViewModel
//        binding.lifecycleOwner = this
//
//        replySettingViewModel.navigateToReplySettingList.observe(viewLifecycleOwner, Observer {
//            it?.let {
//                if (it) {
//                    this.findNavController()
//                        .navigate(ReplySettingFragmentDirections.actionReplySettingDetailFragmentToReplySettingListFragment())
//                    hideKeyboard()
//                }
//            }
//        })
//
//        return binding.root
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        replySettingViewModel.start(args.replySettingId)
//    }
//
//    fun Fragment.getViewModelStoreOwner(): SavedStateRegistryOwner = try {
//        requireActivity()
//    } catch (e: IllegalStateException) {
//        this
//    }
//
//    private fun hideKeyboard() {
//        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view?.windowToken, 0)
//    }
}