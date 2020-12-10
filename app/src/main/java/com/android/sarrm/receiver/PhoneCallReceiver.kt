package com.android.sarrm.receiver


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.os.PowerManager
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.sarrm.R
import com.android.sarrm.application.SarrmApplication
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.listener.ITelephony
import com.android.sarrm.receiver.AlarmReceiver
import com.android.sarrm.listener.PhoneCallStateListener
import com.android.sarrm.utils.CheckNumberContacts
import com.android.sarrm.view.activities.MainActivity
import com.orhanobut.logger.Logger
import io.realm.Realm
import java.lang.Exception
import java.util.*


class PhoneCallReceiver() : BroadcastReceiver() {
    private val SMS_SENT_INTENT_FILTER = "com.yourapp.sms_send"
    private val SMS_DELIVERED_INTENT_FILTER = "com.yourapp.sms_delivered"

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent?.action) {
            Logger.e(
                "IncomingCallReceiver called with incorrect intent action: %s",
                intent?.action

            )
            return
        }

        val newState: String? = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Logger.d("Call state changed to %s", newState)


        if (TelephonyManager.EXTRA_STATE_RINGING == newState) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (incomingNumber == null) {
                Logger.d("Ignoring empty phone number broadcast receiver");
                return
            }

            Logger.d("PhoneCallStateListener %s", incomingNumber)
            Realm.getDefaultInstance().use { realm ->
                realm.where(ReplySetting::class.java).findAll().forEach {
                    Logger.d("ReplySetting name %s", it.toString())
                    checkRepeatType(context, it, incomingNumber!!)
                }
            }
        }
    }

    private fun checkRepeatType(
        context: Context,
        replySetting: ReplySetting,
        incomingNumber: String
    ) {
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
                        Logger.d("특정 요일 지정 current day = %s", currentDay)
                        checkReplyTarget(
                            context,
                            replySetting,
                            incomingNumber
                        )
                    }
                    2 -> if (currentDay == 1 || currentDay == 7) {
                        Logger.d("주말에만 current day = %s", currentDay)
                        checkReplyTarget(
                            context,
                            replySetting,
                            incomingNumber
                        )
                    }
                    1 -> if (currentDay in 2..6) {
                        Logger.d("주중에만 current day = %s", currentDay)
                        checkReplyTarget(
                            context,
                            replySetting,
                            incomingNumber
                        )
                    }
                    0 -> checkReplyTarget(
                        context,
                        replySetting,
                        incomingNumber
                    )
                }
            } else {
                Logger.d("일주일 이하")
                // 아닐 경우,
                checkReplyTarget(context, replySetting, incomingNumber)
            }
        }
    }

    private fun checkReplyTarget(
        context: Context,
        replySetting: ReplySetting,
        incomingNumber: String
    ) {
        val replyTarget = replySetting.replyTarget
        val phoneNumber = replySetting.phoneNumber

        if (replyTarget == 0) {
            Logger.d("무조건 end call -> receive sms")
            endCall(context, incomingNumber,replySetting.message)
        } else if (replyTarget == 1) {
            if (CheckNumberContacts.isFromContacts(context, incomingNumber)) {
                Logger.d("연락처 검색완료 -> receive sms")
                endCall(context, incomingNumber,replySetting.message)
            } else {
                Logger.d("연락처 없음")
            }
        } else {
            if (phoneNumber === incomingNumber) {
                endCall(context, incomingNumber,replySetting.message)
                Logger.d("전화온 번호와 db에 저장된 번호가 동일하면 -> receive sms, 아니면 return")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun endCall(context: Context, number: String?, message:String) {
        Logger.d("PhoneCallStateListener endCall")
        if (Build.VERSION.SDK_INT >= 28) {
            val telecomManager =
                context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
        } else {
            val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val iTelephony = tm.javaClass.getDeclaredMethod("getITelephony")
            iTelephony.isAccessible = true
            val telephonyService = iTelephony.invoke(tm) as ITelephony
            telephonyService.endCall()
        }

        val messageStr = "hey, this is my message";

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
            sms.sendTextMessage(number, null, messageStr, sentPI, deliveredPI);

        } catch (e: Exception) {
            Logger.d(e.toString())
        }
    }
}