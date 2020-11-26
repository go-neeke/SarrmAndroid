package com.android.sarrm.view.activities

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.sarrm.R
import com.android.sarrm.databinding.*
import com.android.sarrm.receiver.PhoneCallReceiver
import kotlinx.android.synthetic.main.layout_menu_reply_target.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

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

        // 메뉴 설정
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.titleMenuName = resources.getString(R.string.title_menu_name)
        binding.titleMenuReplyTarget =resources.getString(R.string.title_menu_reply_target)
        binding.titleMenuTime = resources.getString(R.string.title_menu_time)
        binding.titleMenuReplyMessage = resources.getString(R.string.title_menu_reply_message)


        setReplyTargetSpinner()

        // 퍼미션 체크
        checkPermissions()
    }

    private fun setReplyTargetSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.reply_target_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReplyTarget.adapter = adapter
        spinnerReplyTarget.onItemSelectedListener = this
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // broadcastReceiver 등록
            registerReceiver()
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
                registerReceiver()
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
                registerReceiver()
            }

        }
    }

    private fun registerReceiver() {
        this.registerReceiver(
            PhoneCallReceiver(),
            IntentFilter("android.intent.action.PHONE_STATE")
        )
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
        registerReceiver()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val text: String = parent?.getItemAtPosition(position).toString()
    }
}