package com.android.sarrm.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.sarrm.R
import com.android.sarrm.databinding.*
import com.android.sarrm.service.PhoneCallService
import com.android.sarrm.utils.AppConstants
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.layout_menu_reply_target.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.ContactsContract
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sarrm.data.models.ReplySettingClicked
import com.android.sarrm.receiver.PhoneCallReceiver
import com.android.sarrm.utils.CheckNumberContacts
import com.android.sarrm.view.adapters.ReplySettingListAdapter
import com.android.sarrm.view.factories.ViewModelFactory
import com.android.sarrm.view.fragments.ReplyResultFragment
import com.android.sarrm.view.fragments.ReplySettingListFragmentDirections
import com.android.sarrm.view.models.ReplySettingListViewModel
import com.jakewharton.rxrelay2.PublishRelay
import me.saket.inboxrecyclerview.animation.ItemExpandAnimator
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var mIntent: Intent
    private lateinit var binding: ActivityMainBinding

    private var replySettingId by Delegates.notNull<Long>()

    private val onDestroy = PublishRelay.create<Any>()
    private val adapter = ReplySettingListAdapter()

    private val replySettingListViewModel by viewModels<ReplySettingListViewModel> {
        ViewModelFactory(
            this,
            this
        )
    }

    val PERMISSION_REQ_CODE = 1234
    val PERMISSIONS_PHONE_BEFORE_P = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )
    val PERMISSIONS_AFTER_P = arrayOf(
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setupThreadList()
        setupThreadPage()
        setupFab()

        // 퍼미션 체크
        checkPermissions()
    }

    private fun checkPermissions() {
        Logger.d("checkPermissions")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // broadcastReceiver 등록
            startPhoneCallService()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_DENIED
            )
                requestPermissions(PERMISSIONS_PHONE_BEFORE_P, PERMISSION_REQ_CODE)
            else if (!Settings.canDrawOverlays(this))
                checkDrawOverlayPermission()
            else {
                // broadcastReceiver 등록
                startPhoneCallService()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            if (checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_DENIED
            )
                requestPermissions(PERMISSIONS_AFTER_P, PERMISSION_REQ_CODE)
            else if (!Settings.canDrawOverlays(this))
                checkDrawOverlayPermission()
            else {
                // broadcastReceiver 등록
                startPhoneCallService()
            }

        }
    }


    private fun startPhoneCallService() {
        Logger.d("startPhoneCallService")
//        this.registerReceiver(
//            PhoneCallReceiver(),
//            IntentFilter("android.intent.action.PHONE_STATE")
//        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkDrawOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, 12345)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startPhoneCallService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQ_CODE -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        if (!Settings.canDrawOverlays(this)) {
                            Logger.d(
                                "Settings.canDrawOverlays(this) %s",
                                Settings.canDrawOverlays(this)
                            )
                            checkDrawOverlayPermission()
                        } else startPhoneCallService()

                } else {
//                    Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    override fun onDestroy() {
        onDestroy.accept(Any())
        super.onDestroy()
        Logger.d("onDestroy")
        try {
//            stopService()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (binding.inboxEmailThreadPage.isExpandedOrExpanding) {
            binding.inboxRecyclerview.collapse()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("CheckResult")
    private fun setupThreadList() {
        binding.inboxRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.inboxRecyclerview.expandablePage = binding.inboxEmailThreadPage
        binding.inboxRecyclerview.dimPainter = DimPainter.listAndPage(
            listColor = Color.WHITE,
            listAlpha = 0.75F,
            pageColor = Color.WHITE,
            pageAlpha = 0.65f
        )
        binding.inboxRecyclerview.itemExpandAnimator = ItemExpandAnimator.split()
        binding.inboxEmailThreadPage.pullToCollapseThresholdDistance = dp(90)

        replySettingListViewModel.allReplySettingList.observeForever {
            it?.let {
                Logger.d("observeForever")
                adapter.submitList(it)
                binding.emptyView.visibility = (if (it.isEmpty()) View.VISIBLE else View.GONE)
            }
        }

        adapter.replySettingListViewModel = replySettingListViewModel
        binding.inboxRecyclerview.adapter = adapter

        adapter.itemClicks
            .takeUntil(onDestroy)
            .subscribe {
                binding.inboxRecyclerview.expandItem(it.itemId)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun setupThreadPage() {
        var replyResultFragment =
            supportFragmentManager.findFragmentById(binding.inboxEmailThreadPage.id) as ReplyResultFragment?
        if (replyResultFragment == null) {
            replyResultFragment = ReplyResultFragment()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.inboxEmailThreadPage.id, replyResultFragment)
            .commitNowAllowingStateLoss()

        adapter.itemClicks
            .map { it.replySettingItem.id }
            .takeUntil(onDestroy)
            .subscribe {
                replySettingId = it
                replyResultFragment.populate(it)
            }
    }

    private fun setupFab() {
        binding.inboxFab.setImageDrawable(resources.getDrawable(R.drawable.ic_add_24))

        binding.inboxFab.setOnClickListener {
            val intent = Intent(this, ReplySettingActivity::class.java)

            if (binding.inboxEmailThreadPage.isExpandedOrExpanding) {
                onBackPressed()

                intent.putExtra("replySettingId", replySettingId)
                startActivity(intent)
            } else {
                startActivity(intent)
            }
        }


        binding.inboxEmailThreadPage.addStateChangeCallbacks(object :
            SimplePageStateChangeCallbacks() {
            override fun onPageAboutToExpand(expandAnimDuration: Long) {
                binding.inboxFab.setImageDrawable(resources.getDrawable(R.drawable.ic_edit_24))
            }

            override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
                binding.inboxFab.setImageDrawable(resources.getDrawable(R.drawable.ic_add_24))
            }
        })
    }
}

private fun Context.dp(value: Int): Int {
    val metrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), metrics).toInt()
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            Logger.d("isServiceRunning", "Service is running")
            return true
        }
    }
    return false
}