package com.android.sarrm.view.activities

import android.Manifest
import android.app.ActivityManager
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
import kotlinx.android.synthetic.main.layout_menu_reply_target.*


class MainActivity : AppCompatActivity()  {

    private  lateinit var mIntent: Intent
    private lateinit var binding: ActivityMainBinding

    val PERMISSION_REQ_CODE = 1234
    val PERMISSIONS_PHONE_BEFORE_P = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )
    val PERMISSIONS_AFTER_P = arrayOf(
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        // 퍼미션 체크
        checkPermissions()
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
                || checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED)
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
                || checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED)
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
        val serviceClass = PhoneCallService::class.java
        mIntent = Intent(applicationContext, serviceClass)
        if (!this.isServiceRunning(serviceClass)) {
            // App 실행 시 서비스(GpsService) 시작
            // App 실행 시 foreground 이므로 startService 로 호출
            startService(mIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 12345)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startPhoneCallService()
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