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
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.utils.AppConstants
import com.android.sarrm.utils.CheckNumberContacts
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import java.lang.Exception
import java.util.*

class PhoneCallStateListener(private val context: Context) : PhoneStateListener() {
    private var ongoingCall = false
    private val SMS_SENT_INTENT_FILTER = "com.yourapp.sms_send"
    private val SMS_DELIVERED_INTENT_FILTER = "com.yourapp.sms_delivered"

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCallStateChanged(state: Int, incomingNumber: String) {




        Log.e("PhoneCallStateListener", "onCallStateChanged")
//        Log.e("PhoneCallStateListener", state.toString())
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.e("PhoneCallStateListener",incomingNumber)
                Realm.getDefaultInstance().use { realm ->
                    realm.where(ReplySetting::class.java).findAll().forEach {
                        Logger.d("ReplySetting name %s", it.toString())
                        testCode(it,incomingNumber)
                    }
                }

//                Log.e("CALL_STATE_RINGING", "전화왔습니다.")
//                ongoingCall = true
//                // 지정된 번호인지 검색
//                val dao = ReplySettingDatabase.getInstance(context).dao
//                Observable.fromCallable { dao.getAll() }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//                        if (it != null) {
////                            testCode(it)
//                        }
//                    }, { error ->
//                    })
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun testCode(replySetting: ReplySetting, incomingNumber:String) {
        val replyTarget = replySetting.replyTarget
        val phoneNumber = replySetting.phoneNumber
        val startDate = replySetting.startDate
        val endDate = replySetting.endDate

        val current = Calendar.getInstance().timeInMillis

        if (current in replySetting.startDate..replySetting.endDate) {
            Logger.d("startDate %d, endDate %d, current %d", replySetting.startDate, replySetting.endDate, current)

            if (replySetting.replyTarget == 0) {
                Logger.d("무조건 end call -> receive sms")
                endCall(incomingNumber)
            } else if (replyTarget == 1) {
                if (CheckNumberContacts.isFromContacts(context, incomingNumber)) {
                    Logger.d("연락처 검색완료 -> receive sms")
                    endCall(incomingNumber)
                } else {
                    Logger.d("연락처 없음")
                }
            } else {
                if (phoneNumber === incomingNumber) {
                    endCall(incomingNumber)
                    Logger.d("전화온 번호와 db에 저장된 번호가 동일하면 -> receive sms, 아니면 return")
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun endCall(number: String?) {
        Log.e("PhoneCallStateListener","endCall")
        val telecomManager = context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.endCall()

        val message = "hey, this is my message";

        Logger.d("endCall send SMS 11 %s",number)
        try {
            val sentPI: PendingIntent = PendingIntent.getBroadcast(
                context, 0, Intent(
                    SMS_SENT_INTENT_FILTER
                ), 0
            );
            Logger.d("endCall send SMS 22 %s", number)
            val deliveredPI: PendingIntent = PendingIntent.getBroadcast(
                context, 0, Intent(
                    SMS_DELIVERED_INTENT_FILTER
                ), 0
            );

            Logger.d("endCall send SMS 33 %s", number)

            val sms: SmsManager = SmsManager.getDefault();
            sms.sendTextMessage(number, null, message, sentPI, deliveredPI);

        } catch(e:Exception) {
             Logger.e(e.toString())
        }
    }
}
