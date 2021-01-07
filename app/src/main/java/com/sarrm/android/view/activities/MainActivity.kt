package com.sarrm.android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxrelay2.PublishRelay
import com.orhanobut.logger.Logger
import com.sarrm.android.R
import com.sarrm.android.databinding.*
import com.sarrm.android.receiver.PhoneCallReceiver
import com.sarrm.android.utils.AppConstants
import com.sarrm.android.view.adapters.ReplySettingListAdapter
import com.sarrm.android.view.factories.ViewModelFactory
import com.sarrm.android.view.fragments.ReplyResultFragment
import com.sarrm.android.view.models.ReplySettingListViewModel
import kotlinx.android.synthetic.main.layout_menu_reply_target.*
import me.saket.inboxrecyclerview.animation.ItemExpandAnimator
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var phoneCallReceiver = PhoneCallReceiver()

    private var replySettingId by Delegates.notNull<Long>()

    private val onDestroy = PublishRelay.create<Any>()

    private lateinit var adapter: ReplySettingListAdapter
    private lateinit var selectionList: MutableLiveData<MutableList<Long>>
    private lateinit var currentMode: MutableLiveData<AppConstants.ListMode>

    private lateinit var clockRotate: Animation
    private lateinit var antiClockRotate: Animation


    private val replySettingListViewModel by viewModels<ReplySettingListViewModel> {
        ViewModelFactory(
            this,
            this
        )
    }

    val PERMISSION_REQ_CODE = 1234
    val PERMISSIONS_PHONE_BEFORE_P = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.ANSWER_PHONE_CALLS
    )

    private var permissionDenied = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        // floating 버튼 애니메이션
        clockRotate = AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_clockwise
        )

        antiClockRotate = AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_anticlockwise
        )

        // ui 초기화
        setReplySettingList()
        setReplyResultPage()
        setFloatingButton()

        // 퍼미션 체크
        checkPermissions()
    }

    private fun checkPermissions() {
        Logger.d("checkPermissions")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            registerPhonecallReceiver()
        } else {
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
                showReadCallLogPermissionAlert(AppConstants.PermissionType.READ_CALL_LOG)
            else if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
                showReadCallLogPermissionAlert(AppConstants.PermissionType.READ_CONTACTS)
            else if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
                showReadCallLogPermissionAlert(AppConstants.PermissionType.SEND_SMS)
            else if (checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_DENIED)
                requestPermissions(PERMISSIONS_PHONE_BEFORE_P, PERMISSION_REQ_CODE)
            else if (!Settings.canDrawOverlays(this))
                checkDrawOverlayPermission()
            else registerPhonecallReceiver()
        }
    }

    private fun showReadCallLogPermissionAlert(permissionType: AppConstants.PermissionType) {
        showAlert(
            this,
            permissionType.permissionAlertTitle(this),
            permissionType.permissionAlertMessage(this),
            object : AlertCallbackListener {
                override fun onClickPositive() {
                    ActivityCompat.requestPermissions(
                        this@MainActivity, permissionType.permission(),
                        permissionType.permissionRequestCode()
                    )
                }

                override fun onClickNegative() {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.permission_not_granted),
                        Toast.LENGTH_SHORT
                    ).show();
                    permissionDenied = true
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkDrawOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, PERMISSION_REQ_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQ_CODE) {
            if (Settings.canDrawOverlays(this)) {
                registerPhonecallReceiver()
            }
        }
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
                        } else {
                            registerPhonecallReceiver()
                        }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_not_granted),
                        Toast.LENGTH_SHORT
                    ).show();
                    permissionDenied = true
                }
            }
            1000,
            1001,
            1002,
            1003 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_not_granted),
                        Toast.LENGTH_SHORT
                    ).show();
                    permissionDenied = true
                }
            }
        }
    }

    private fun registerPhonecallReceiver() {
        permissionDenied = false
        Logger.d("registerPhonecallReceiver")
        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        registerReceiver(phoneCallReceiver, filter)
    }

    override fun onDestroy() {
        onDestroy.accept(Any())
        unregisterReceiver(phoneCallReceiver);
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (binding.expandableResultPage.isExpandedOrExpanding) {
            // result page에서 back 키 눌렀을때 
            binding.recyclerviewList.collapse()
        } else if (currentMode.value == AppConstants.ListMode.SELECTION) {
            // 삭제 모드에서 back 키 눌렀을 때
            initSelection()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("CheckResult")
    private fun setReplySettingList() {
        // 선택된 삭제 리스트
        selectionList = MutableLiveData<MutableList<Long>>().apply {
            value = mutableListOf()
        }

        // 기본| 선택가능한 모드 (삭제)
        currentMode =
            MutableLiveData<AppConstants.ListMode>().default(AppConstants.ListMode.NONE)

        adapter = ReplySettingListAdapter(selectionList, currentMode)

        currentMode.observeForever {
            it?.let {
                if (currentMode.value == AppConstants.ListMode.SELECTION) {
                    // 모드 변경되었을 때 icon 변경해줌
                    binding.buttonFloating.startAnimation(clockRotate)
                    binding.buttonFloating.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_delete_outline_24))
                }
                adapter.notifyDataSetChanged()
            }
        }

        // use library
        binding.recyclerviewList.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewList.expandablePage = binding.expandableResultPage
        binding.recyclerviewList.dimPainter = DimPainter.listAndPage(
            listColor = Color.WHITE,
            listAlpha = 0.75F,
            pageColor = Color.WHITE,
            pageAlpha = 0.65f
        )
        binding.recyclerviewList.itemExpandAnimator = ItemExpandAnimator.split()
        binding.expandableResultPage.pullToCollapseThresholdDistance = dp(90)

        replySettingListViewModel.allReplySettingList.observeForever {
            it?.let {
                // reply setting list 셋팅
                adapter.submitList(it)
                binding.emptyView.visibility = (if (it.isEmpty()) View.VISIBLE else View.GONE)
            }
        }

        adapter.replySettingListViewModel = replySettingListViewModel
        binding.recyclerviewList.adapter = adapter

        // 아이템 클릭
        adapter.itemClicks
            .takeUntil(onDestroy)
            .subscribe {
                binding.recyclerviewList.expandItem(it.itemId)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun setReplyResultPage() {
        var replyResultFragment =
            supportFragmentManager.findFragmentById(binding.expandableResultPage.id) as ReplyResultFragment?
        if (replyResultFragment == null) {
            replyResultFragment = ReplyResultFragment()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.expandableResultPage.id, replyResultFragment)
            .commitNowAllowingStateLoss()

        // 선택된 reply setting의 result list 가져옴
        adapter.itemClicks
            .map { it.replySettingItem.id }
            .takeUntil(onDestroy)
            .subscribe {
                replySettingId = it
                replyResultFragment.populate(it)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setFloatingButton() {
        // floating button 설정
        binding.buttonFloating.setImageDrawable(resources.getDrawable(R.drawable.ic_add_24))
        binding.buttonFloating.setOnClickListener {
            if (currentMode.value == AppConstants.ListMode.SELECTION) {
                // 삭제 버튼 클릭시
                showAlert(
                    this,
                    "",
                    getString(R.string.alert_message),
                    object : AlertCallbackListener {
                        override fun onClickPositive() {
                            replySettingListViewModel.deleteReplySettingList(selectionList.value!!)
                            initSelection()
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.confirm_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onClickNegative() {
                        }
                    })
            } else {
                val intent = Intent(this, ReplySettingActivity::class.java)
                if (binding.expandableResultPage.isExpandedOrExpanding) {
                    // result page에서 클릭시 > 수정 가능
                    onBackPressed()
                    intent.putExtra("replySettingId", replySettingId)
                    startActivity(intent)
                } else {
                    if (permissionDenied) {
                        showAlert(
                            this,
                            getString(R.string.permission_grant_alert_title),
                            getString(R.string.permission_grant_alert_message),
                            object : AlertCallbackListener {
                                override fun onClickPositive() {
                                    checkPermissions()
                                }

                                override fun onClickNegative() {
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.permission_not_granted),
                                        Toast.LENGTH_SHORT
                                    ).show();
                                    permissionDenied = true
                                }
                            })
                    } else {
                        // list에서 클릭시
                        startActivity(intent)
                    }
                }
            }
        }

        binding.expandableResultPage.addStateChangeCallbacks(object :
            SimplePageStateChangeCallbacks() {
            override fun onPageAboutToExpand(expandAnimDuration: Long) {
                // floating button 이미지 셋팅 및 회전
                binding.buttonFloating.startAnimation(clockRotate)
                binding.buttonFloating.setImageDrawable(resources.getDrawable(R.drawable.ic_edit_24))
            }

            override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
                // floating button 이미지 셋팅 및 회전
                binding.buttonFloating.startAnimation(antiClockRotate)
                binding.buttonFloating.setImageDrawable(resources.getDrawable(R.drawable.ic_add_24))
            }
        })
    }

    private fun initSelection() {
        binding.buttonFloating.startAnimation(antiClockRotate)
        binding.buttonFloating.setImageDrawable(resources.getDrawable(R.drawable.ic_add_24))

        // 삭제 취소했을 때 > 초기화
        var tempList = selectionList.value
        tempList!!.clear()
        selectionList.value = tempList
        currentMode.value = AppConstants.ListMode.NONE
    }
}

private fun <T : Any?> MutableLiveData<T>.default(initialValue: T) =
    apply { setValue(initialValue) }


private fun Context.dp(value: Int): Int {
    val metrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), metrics).toInt()
}

private fun showAlert(
    context: Context,
    title: String,
    message: String,
    alertCallbackListener: AlertCallbackListener
) {
    val alertBuilder = AlertDialog.Builder(context)
    alertBuilder.setCancelable(true)
    alertBuilder.setTitle(title)
    alertBuilder.setMessage(message)
    alertBuilder.setPositiveButton(
        android.R.string.yes
    ) { dialog, which ->
        alertCallbackListener.onClickPositive()
    }
    alertBuilder.setNegativeButton(
        android.R.string.no
    ) { dialog, which ->
        alertCallbackListener.onClickNegative()
    }
    val alert = alertBuilder.create()
    alert.show()
}

private interface AlertCallbackListener {
    fun onClickPositive()
    fun onClickNegative()
}
