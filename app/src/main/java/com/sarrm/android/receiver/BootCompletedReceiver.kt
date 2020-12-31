package com.sarrm.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import com.sarrm.android.service.RestartService
import com.sarrm.android.view.activities.MainActivity
import com.orhanobut.logger.Logger

class BootCompletedReceiver : BroadcastReceiver() {

    private val TAG: String = "BootCompletedReceiver"

    override fun onReceive(context: Context, intent: Intent) {

        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action

        Logger.d( "Receive ACTION")
        if (action == null) {
            Logger.d( "action is null")
            return
        }

        if (TextUtils.equals(action, Intent.ACTION_BOOT_COMPLETED)) {
            Logger.d( "boot complete received")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, RestartService::class.java))
            } else {
                val i = Intent(context, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context!!.startActivity(i)
            }

        }
    }
}