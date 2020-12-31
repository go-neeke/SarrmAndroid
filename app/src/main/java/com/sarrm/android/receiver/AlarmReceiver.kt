package com.sarrm.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sarrm.android.service.RestartService
import com.orhanobut.logger.Logger

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("startForegroundService RestartService")
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // O OS 이상이면 RestartService를 foreground service로 실행
            context.startForegroundService(Intent(context, RestartService::class.java))
        } else {
            // O OS 이전은 실행하고자 하는 Service를 바로 startService로 실행
            context.startService(Intent(context, RestartService::class.java))
        }
    }
}