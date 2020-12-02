package com.android.sarrm.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.android.sarrm.utils.CheckNumberContacts


class MainActivity : AppCompatActivity() {

    private lateinit var mIntent: Intent
    private lateinit var binding: ActivityMainBinding

    val PERMISSION_REQ_CODE = 1234
    val PERMISSIONS_PHONE_BEFORE_P = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS
    )
    val PERMISSIONS_AFTER_P = arrayOf(
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // 퍼미션 체크
        checkPermissions()

        testCode()
    }

    private fun testCode() {
        val replyTarget = AppConstants.ReplyTargetType.MY_CONTACT_LIST
        val phoneNumber = "010-3258-9414"
        val startDate = Calendar.getInstance()
            .apply {
                set(Calendar.YEAR, 2020)
                set(Calendar.MONTH, 12)
                set(Calendar.DAY_OF_MONTH, 3)
                set(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 10)
            }.timeInMillis

        val endDate = Calendar.getInstance()
            .apply {
                set(Calendar.YEAR, 2020)
                set(Calendar.MONTH, 12)
                set(Calendar.DAY_OF_MONTH, 3)
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 10)
            }.timeInMillis

        val current = Calendar.getInstance()
            .apply {
                set(Calendar.YEAR, 2020)
                set(Calendar.MONTH, 12)
                set(Calendar.DAY_OF_MONTH, 3)
                set(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 10)
            }.timeInMillis

        if (current in startDate..endDate) {
            Logger.d("startDate %d, endDate %d, current %d", startDate, endDate, current)

            if (replyTarget == AppConstants.ReplyTargetType.ALL) {
                Logger.d("무조건 end call -> receive sms")
            } else if (replyTarget == AppConstants.ReplyTargetType.MY_CONTACT_LIST) {
                if (CheckNumberContacts.isFromContacts(this, phoneNumber)){
                    Logger.d("연락처 검색완료 -> receive sms")
                } else {
                    Logger.d("연락처 없음")
                }
            } else {
                Logger.d("전화온 번호와 db에 저장된 번호가 동일하면 -> receive sms, 아니면 return")
            }


        }


    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // broadcastReceiver 등록
            startPhoneCallService()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED
            )
                requestPermissions(PERMISSIONS_PHONE_BEFORE_P, PERMISSION_REQ_CODE)
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
                || checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED
            )
                requestPermissions(PERMISSIONS_AFTER_P, PERMISSION_REQ_CODE)
            else {
                // broadcastReceiver 등록
                startPhoneCallService()
            }

        }
    }

    private fun startPhoneCallService() {
        val serviceClass = PhoneCallService::class.java
        mIntent = Intent(applicationContext, serviceClass)
        if (!this.isServiceRunning(serviceClass)) {
            // App 실행 시 서비스(GpsService) 시작
            // App 실행 시 foreground 이므로 startService 로 호출
            startService(mIntent)
        }
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
                        if ((checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_DENIED)
                            || (!Settings.canDrawOverlays(this))
                        )
                            startPhoneCallService()

                } else {
//                    Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            stopService(mIntent)
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            Log.e("isServiceRunning", "Service is running")
            return true
        }
    }
    return false
}