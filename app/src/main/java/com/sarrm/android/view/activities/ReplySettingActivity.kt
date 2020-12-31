package com.sarrm.android.view.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sarrm.android.R
import com.sarrm.android.databinding.*
import com.orhanobut.logger.Logger
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.sarrm.android.view.factories.ViewModelFactory
import com.sarrm.android.view.models.ReplySettingViewModel

class ReplySettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReplySettingBinding

    private val replySettingViewModel by viewModels<ReplySettingViewModel> {
        ViewModelFactory(
            this,
            this
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reply_setting)
        binding.titleMenuName = resources.getString(R.string.title_menu_name)
        binding.titleMenuReplyTarget = resources.getString(R.string.title_menu_reply_target)
        binding.titleMenuTime = resources.getString(R.string.title_menu_time)
        binding.titleMenuReplyMessage = resources.getString(R.string.title_menu_reply_message)
        binding.replySettingViewModel = replySettingViewModel
        binding.lifecycleOwner = this

        // replySettingId 가 있을 경우 수정모드
        val replySettingId: Long? = intent.getLongExtra("replySettingId", 0)
        if (replySettingId?.toInt() != 0) replySettingViewModel.start(replySettingId)
        Logger.d("replySettingId %s", replySettingId)
    }

}
