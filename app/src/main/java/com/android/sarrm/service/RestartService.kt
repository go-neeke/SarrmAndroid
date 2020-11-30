package com.android.sarrm.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi

class RestartService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        toast("Start Restart Service1")
        // 실제 서비스는 startService로 실행
        startService(Intent(this, PhoneCallService::class.java))
        stopSelf()

        return START_NOT_STICKY
    }
}