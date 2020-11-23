package com.android.sarrm.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.android.sarrm.data.db.ReplySettingDatabase
import com.android.sarrm.listeners.ITelephony
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/*
* 수신 전화를 감지하여 응답메시지 전송하는 BroadcastReceiver
* */
class IncomingCallReceiver : BroadcastReceiver() {
    private val LOG_TAG: String = IncomingCallReceiver::class.java.simpleName

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent?.action) {
            Log.e(
                LOG_TAG, String.format(
                    "IncomingCallReceiver called with incorrect intent action: %s",
                    intent?.action
                )
            )
            return
        }

        val newState: String? = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(LOG_TAG, String.format("Call state changed to %s", newState))

        if (TelephonyManager.EXTRA_STATE_RINGING == newState) {
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (phoneNumber == null) {
                Log.d(LOG_TAG, "Ignoring empty phone number broadcast receiver");
                return
            }
            Log.i(LOG_TAG, String.format("Incoming call from %s", phoneNumber))

            // 지정된 번호인지 검색
            val dao = ReplySettingDatabase.getInstance(context).dao
            Observable.fromCallable { dao.getAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        val matches = it.map { sp ->
                            sp.regex
                        }
                            .filter { reg -> phoneNumber.matches(reg) }

                        Log.i(LOG_TAG, "Matches $matches")
                        if (matches.count() > 0) {
                            endCall(context, phoneNumber)
                        }
                    }
                }, { error ->
                    Log.i(LOG_TAG, "Not able to fetch data\n$error")
                })
        }

    }

    @SuppressLint("MissingPermission")
    private fun endCall(context: Context?, number: String?) {
        if (Build.VERSION.SDK_INT >= 28) {
            val telecomManager = context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
        } else {
            val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val iTelephony = tm.javaClass.getDeclaredMethod("getITelephony")
            iTelephony.isAccessible = true
            val telephonyService = iTelephony.invoke(tm) as ITelephony
            telephonyService.endCall()
        }
    }

}