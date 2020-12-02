package com.android.sarrm.listener

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi

class PhoneCallStateListener(private val context: Context) : PhoneStateListener() {
    private var ongoingCall = false
    private val SMS_SENT_INTENT_FILTER = "com.yourapp.sms_send"
    private val SMS_DELIVERED_INTENT_FILTER = "com.yourapp.sms_delivered"

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        Log.e("PhoneCallStateListener", "onCallStateChanged")
        Log.e("PhoneCallStateListener", state.toString())
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.e("CALL_STATE_RINGING", "전화왔습니다.")
                ongoingCall = true
                // 지정된 번호인지 검색
//                val dao = ReplySettingDatabase.getInstance(context).dao
//                Observable.fromCallable { dao.getAll() }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//                        if (it != null) {
//                            val matches = it.map { sp ->
//                                sp.regex
//                            }
//                                .filter { reg -> incomingNumber.matches(reg) }
//
//                            if (matches.count() > 0) {
//                endCall(incomingNumber)
//                            }
//                        }
//                    }, { error ->
//                    })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun endCall(number: String?) {
        val telecomManager = context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.endCall()

        val message = "hey, this is my message";

        val sentPI: PendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(
                SMS_SENT_INTENT_FILTER
            ), 0
        );
        val deliveredPI: PendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(
                SMS_DELIVERED_INTENT_FILTER
            ), 0
        );

        val sms: SmsManager = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
    }
}
