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
import com.android.sarrm.converts.ViewConverter
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
import java.time.DayOfWeek
import java.util.*

class PhoneCallStateListener(private val context: Context) : PhoneStateListener() {
    private val SMS_SENT_INTENT_FILTER = "com.yourapp.sms_send"
    private val SMS_DELIVERED_INTENT_FILTER = "com.yourapp.sms_delivered"

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        Logger.e("PhoneCallStateListener onCallStateChanged")
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Logger.e("PhoneCallStateListener %s", incomingNumber)
                Realm.getDefaultInstance().use { realm ->
                    realm.where(ReplySetting::class.java).findAll().forEach {
                        Logger.d("ReplySetting name %s", it.toString())
                        checkRepeatType(it, incomingNumber)
                    }
                }
            }
        }
    }

    private fun checkRepeatType(replySetting: ReplySetting, incomingNumber: String) {
        val startDate = replySetting.startDate
        val endDate = replySetting.endDate
        val repeatType = replySetting.repeatType
        val dayList = replySetting.dayList

        val current = Calendar.getInstance()

        if (current.timeInMillis in startDate..endDate) {
            val timeForweek =
                (6 * 24 * 60 * 60 * 1000).toLong() /// here 24*60*60*1000 =24 hours i.e 1 day

            if (endDate - startDate > timeForweek) {
                Logger.d("일주일이상임")
                val currentDay = current.get(Calendar.DAY_OF_WEEK) - 1
                // 일주일 이상일 경우, repeat type 체크
                when (repeatType) {
                    3 -> if (dayList.all { it == currentDay }) {
                        Logger.d("특정 요일 지정 current day = %s",currentDay)
                        checkReplyTarget(
                            replySetting,
                            incomingNumber
                        )
                    }
                    2 -> if (currentDay == 1 || currentDay == 7) {
                        Logger.d("주말에만 current day = %s",currentDay)
                        checkReplyTarget(
                            replySetting,
                            incomingNumber
                        )
                    }
                    1 -> if (currentDay in 2..6) {
                        Logger.d("주중에만 current day = %s",currentDay)
                        checkReplyTarget(
                            replySetting,
                            incomingNumber
                        )
                    }
                    0 -> checkReplyTarget(
                        replySetting,
                        incomingNumber
                    )
                }
            } else {
                Logger.d("일주일 이하")
                // 아닐 경우,
                checkReplyTarget(replySetting, incomingNumber)
            }
        }
    }

    private fun checkReplyTarget(replySetting: ReplySetting, incomingNumber: String) {
        val replyTarget = replySetting.replyTarget
        val phoneNumber = replySetting.phoneNumber

        if (replyTarget == 0) {
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

    @SuppressLint("MissingPermission")
    private fun endCall(number: String?) {
        Logger.e("PhoneCallStateListener endCall")
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

        val message = "hey, this is my message";

        Logger.d("endCall send SMS 11 %s", number)
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

        } catch (e: Exception) {
            Logger.e(e.toString())
        }
    }
}
