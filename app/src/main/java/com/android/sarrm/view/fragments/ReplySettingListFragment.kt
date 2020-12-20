package com.android.sarrm.view.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment

class ReplySettingListFragment : Fragment() {

//    private lateinit var binding: FragmentReplySettingListBinding
//    private val onDestroy = PublishRelay.create<Any>()
//    private lateinit var adapter : ReplySettingListAdapter
//
//    private val replySettingListViewModel by viewModels<ReplySettingListViewModel> {
//        ViewModelFactory(
//            this,
//            requireActivity()
//        )
//    }
//
//    private val mainActivityViewModel by viewModels<MainActivityViewModel> {
//        ViewModelFactory(
//            this,
//            requireActivity()
//        )
//    }
//
//    override fun onDestroy() {
//        onDestroy.accept(Any())
//        super.onDestroy()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//
//        setupThreadPage()
//        setupFab()
//
//        binding = DataBindingUtil.inflate(
//            inflater,
//            R.layout.fragment_reply_setting_list,
//            container,
//            false
//        )
//        binding.lifecycleOwner = this
//
//        setupThreadList()
//
//        binding.inboxFab.setOnClickListener {
//            this.findNavController().navigate(
//                ReplySettingListFragmentDirections.actionReplySettingListFragmentToReplySettingFragment(
//                    null
//                )
//            )
//            replySettingListViewModel.onNewReplySettingNavigated()
//        }
//
//
//        replySettingListViewModel.navigateToReplySettingDetail.observe(
//            viewLifecycleOwner,
//            Observer { plantId ->
//                plantId?.let {
//                    this.findNavController().navigate(
//                        ReplySettingListFragmentDirections.actionReplySettingListFragmentToReplySettingFragment(
//                            plantId
//                        )
//                    )
//
//                    replySettingListViewModel.onNewReplySettingNavigated()
//                }
//            })
//
//        return binding.root
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("CheckResult")
//    private fun setupThreadList() {
//        adapter = ReplySettingListAdapter(ClickListener { plantId ->
//            replySettingListViewModel.onReplySettingClicked(plantId)
//        })
//
//        binding.replySettingList.adapter = adapter
//        binding.replySettingList.dimPainter = DimPainter.listAndPage(
//            listColor = Color.WHITE,
//            listAlpha = 0.75F,
//            pageColor = Color.WHITE,
//            pageAlpha = 0.65f
//        )
//        binding.replySettingList.itemExpandAnimator = ItemExpandAnimator.split()
//        binding.inboxEmailThreadPage.pullToCollapseThresholdDistance = dp(90)
//
//        replySettingListViewModel.allReplySettingList.observe(viewLifecycleOwner, Observer {
//            it?.let {
//                adapter.submitList(it)
//            }
//        })
//
////        threadsAdapter.submitList(EmailRepository.threads())
////        recyclerView.adapter = threadsAdapter
//
//        adapter.itemClicks
//            .takeUntil(onDestroy)
//            .subscribe {
//                binding.replySettingList.expandItem(it.itemId)
//            }
//    }
//
//    @SuppressLint("CheckResult")
//    private fun setupThreadPage() {
//        val fragmentManager2: FragmentManager? = fragmentManager
//        val fragmentTransaction2: FragmentTransaction = fragmentManager2!!.beginTransaction()
//
//        var threadFragment = fragmentManager2.findFragmentById(binding.inboxEmailThreadPage.id) as ReplySettingResultFragment?
//        if (threadFragment == null) {
//            threadFragment = ReplySettingResultFragment()
//        }
//
//        supportFragmentManager
//            .beginTransaction()
//            .replace(emailPageLayout.id, threadFragment)
//            .commitNowAllowingStateLoss()
//
//        adapter.itemClicks
//            .map { it.replySettingItem.id }
//            .takeUntil(onDestroy)
//            .subscribe {
//                threadFragment.populate(it)
//            }
//    }
//
//    private fun setupFab() {
//        val avd = { iconRes: Int -> AppCompatResources.getDrawable(
//            this,
//            iconRes
//        ) as AnimatedVectorDrawable
//        }
//        fab.setImageDrawable(avd(R.drawable.avd_edit_to_reply_all))
//
//        emailPageLayout.addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
//            override fun onPageAboutToExpand(expandAnimDuration: Long) {
//                val icon = avd(R.drawable.avd_edit_to_reply_all)
//                fab.setImageDrawable(icon)
//                icon.start()
//            }
//
//            override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
//                val icon = avd(R.drawable.avd_reply_all_to_edit)
//                fab.setImageDrawable(icon)
//                icon.start()
//            }
//        })
//    }
}

private fun Fragment.dp(value: Int): Int {
    val metrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), metrics).toInt()
}