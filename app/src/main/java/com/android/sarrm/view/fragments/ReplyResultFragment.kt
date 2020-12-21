package com.android.sarrm.view.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.android.sarrm.R
import com.android.sarrm.data.models.ReplyResult
import com.android.sarrm.view.factories.ViewModelFactory
import com.android.sarrm.databinding.FragmentReplyResultBinding
import com.android.sarrm.view.adapters.ReplyResultListAdapter
import com.android.sarrm.view.models.ReplyResultViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import com.jakewharton.rxrelay2.PublishRelay
import com.orhanobut.logger.Logger
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult

class ReplyResultFragment : Fragment() {

    private lateinit var binding: FragmentReplyResultBinding

    private val threadIds = BehaviorRelay.create<Long>()
    private val onDestroys = PublishRelay.create<Any>()
    private val emailThreadPage by lazy { requireView().parent as ExpandablePageLayout }

    private val replyResultViewModel by viewModels<ReplyResultViewModel> {
        ViewModelFactory(
            this,
            requireActivity()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reply_result,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.replyResultViewModel = replyResultViewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedState: Bundle?) {
        super.onViewCreated(view, savedState)

        if (savedState != null) {
            onRestoreInstanceState(savedState)
        }

        threadIds
            .map { it }
            .takeUntil(onDestroys)
            .onErrorReturnItem(0)
            .subscribe { render(it) }

        emailThreadPage.pullToCollapseInterceptor = { downX, downY, upwardPull ->
            if (binding.emailthreadScrollableContainer.globalVisibleRect().contains(downX, downY)
                    .not()
            ) {
                InterceptResult.IGNORED

            } else {
                val directionInt = if (upwardPull) +1 else -1
                val canScrollFurther =
                    binding.emailthreadScrollableContainer.canScrollVertically(directionInt)
                when {
                    canScrollFurther -> InterceptResult.INTERCEPTED
                    else -> InterceptResult.IGNORED
                }
            }
        }

        emailThreadPage.addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
            override fun onPageCollapsed() {
                binding.emailthreadScrollableContainer.scrollTo(0, 0)
            }
        })
    }

    override fun onDestroyView() {
        onDestroys.accept(Any())
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (threadIds.hasValue()) {
            outState.putLong("thread_id", threadIds.value)
        }
        super.onSaveInstanceState(outState)
    }

    private fun onRestoreInstanceState(savedState: Bundle) {
        val retainedThreadId: Long? = savedState.getLong("thread_id")
        if (retainedThreadId != null) {
            threadIds.accept(retainedThreadId)
        }
    }

    fun populate(threadId: Long) {
        threadIds.accept(threadId)
        Logger.d("populate %s", threadId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun render(replySettingId: Long) {
        val adapter = ReplyResultListAdapter()
        replyResultViewModel.findReplyResutBySettingId(replySettingId)
            .observe(viewLifecycleOwner, {
                it?.let {
                    adapter.replyResultList = it
                    binding.emptyView.visibility =
                        (if (it.isEmpty()) View.VISIBLE else View.GONE)

                    Logger.d("it %s")
                }
            })

        Logger.d("replySettingId %d", replySettingId)

        binding.emailthreadCollapse.setOnClickListener { requireActivity().onBackPressed() }
        binding.recyclerViewBeer.adapter = adapter
    }
}

private fun View.globalVisibleRect(): RectF {
    val rect = Rect()
    getGlobalVisibleRect(rect)
    return RectF(
        rect.left.toFloat(),
        rect.top.toFloat(),
        rect.right.toFloat(),
        rect.bottom.toFloat()
    )
}