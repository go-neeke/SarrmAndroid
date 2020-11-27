package com.android.sarrm.listener

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.sarrm.data.db.ReplySettingDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PhoneCallStateListener(private val context: Context) : PhoneStateListener() {
    private var ongoingCall = false

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                ongoingCall = true
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
                                .filter { reg -> incomingNumber.matches(reg) }

                            if (matches.count() > 0) {
                                endCall(incomingNumber)
                            }
                        }
                    }, { error ->
                    })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun endCall(number: String?) {
        val telecomManager = context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.endCall()
    }
}
