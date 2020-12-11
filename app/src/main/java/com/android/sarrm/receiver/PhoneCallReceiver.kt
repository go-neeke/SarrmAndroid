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
import android.text.TextUtils
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
            Logger.e("IncomingCallReceiver called with incorrect intent action: %s", intent?.action)
            return
        }

        val newState: String? = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Logger.d("Call state changed to %s", newState)

        if (TelephonyManager.EXTRA_STATE_RINGING == newState) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (incomingNumber == null) {
                Logger.e("Ignoring empty phone number broadcast receiver");
                return
            }

            Logger.d("PhoneCallStateListener %s", incomingNumber)
            Realm.getDefaultInstance().use { realm ->
                realm.where(ReplySetting::class.java).findAll().forEach {
                    Logger.d("ReplySetting name %s", it.toString())
                    checkReplyTarget(context, it, incomingNumber!!)
                }
            }
        }
    }

//    private fun checkTime(
//        startDate:Long,
//        endDate:Long,
//    ): Boolean {
//        val startCalendar = longToCalendar(startDate)
//        val endCalendar = longToCalendar(endDate)
//        val current = Calendar.getInstance();
//
//        return current.get(Calendar.HOUR_OF_DAY) in startCalendar.get(Calendar.HOUR_OF_DAY)..endCalendar.get(
//            Calendar.HOUR_OF_DAY
//        )
//                && current.get(Calendar.MINUTE) in startCalendar.get(Calendar.MINUTE)..endCalendar.get(
//            Calendar.MINUTE
//        );
//    }

    private fun testCode(
        context: Context,
        incomingNumber: String
    ) {
        val startDate = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 10)
            }

        val endDate = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 10)
            }

        val current = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 50)
            }

        Logger.d("run testCode")

        if (current.timeInMillis in startDate.timeInMillis..endDate.timeInMillis) {
            val currentDay = current.get(Calendar.DAY_OF_WEEK)
            val repeatType: Int = 3
            val dayList: Array<Int> = arrayOf(2, 6)

            Logger.d("current day = %s", currentDay)

            when (repeatType) {
                3 -> if (dayList.any { it == currentDay }) {
                    Logger.d("특정 요일 지정")
                    endCall(
                        context,
                        incomingNumber,
                        "message"
                    )
                }
                2 -> if (currentDay == 1 || currentDay == 7) {
                    Logger.d("주말에만")
                    endCall(
                        context,
                        incomingNumber,
                        "message"
                    )
                }
                1 -> if (currentDay in 2..6) {
                    Logger.d("주중에만")
                    endCall(
                        context,
                        incomingNumber,
                        "message"
                    )
                }
                0 -> {
                    Logger.d("매일")
                    endCall(
                        context,
                        incomingNumber,
                        "message"
                    )
                }
            }
        }
    }

    private fun checkRepeatType(
        context: Context,
        replySetting: ReplySetting,
        incomingNumber: String
    ) {
//        testCode(context, incomingNumber)
//        return;

        val repeatType = replySetting.repeatType
        val dayList = replySetting.dayList

        val startTime = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, replySetting.startHour)
                set(Calendar.MINUTE, replySetting.startMinute)
            }

        val endTime = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, replySetting.endHour)
                set(Calendar.MINUTE, replySetting.endMinute)
            }

        val current = Calendar.getInstance()
//            .apply {
//                set(Calendar.HOUR_OF_DAY, 16)
//                set(Calendar.MINUTE, 5)
//            }

        Logger.d("current.timeInMillis %d",current.timeInMillis)
        Logger.d("replySetting.startDate %d",replySetting.startDate)
        Logger.d("replySetting.endDate %d",replySetting.endDate)
        Logger.d("startTime.timeInMillis %d",startTime.timeInMillis)
        Logger.d("endTime.timeInMillis %d",endTime.timeInMillis)

        if (current.timeInMillis in replySetting.startDate..replySetting.endDate
            && current.timeInMillis in startTime.timeInMillis..endTime.timeInMillis) {
            val currentDay = current.get(Calendar.DAY_OF_WEEK) - 1
            when (repeatType) {
                3 -> if (dayList.all { it == currentDay }) {
                    Logger.d("특정 요일 지정 current day = %s", currentDay)
                    endCall(
                        context,
                        incomingNumber,
                        replySetting.message
                    )
                }
                2 -> if (currentDay == 1 || currentDay == 7) {
                    Logger.d("주말에만 current day = %s", currentDay)
                    endCall(
                        context,
                        incomingNumber,
                        replySetting.message
                    )
                }
                1 -> if (currentDay in 2..6) {
                    Logger.d("주중에만 current day = %s", currentDay)
                    endCall(
                        context,
                        incomingNumber,
                        replySetting.message
                    )
                }
                0 -> {
                    Logger.d("매일")
                    endCall(
                        context,
                        incomingNumber,
                        replySetting.message
                    )
                }
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

        Logger.d("checkReplyTarget %d", replyTarget)

        when (replyTarget) {
            2 -> if (incomingNumber?.equals(phoneNumber)) checkRepeatType(
                context,
                replySetting,
                incomingNumber
            )
            1 -> if (CheckNumberContacts.isFromContacts(context, incomingNumber)) checkRepeatType(
                context,
                replySetting,
                incomingNumber
            )
            else -> checkRepeatType(context, replySetting, incomingNumber)
        }
    }

    @SuppressLint("MissingPermission")
    private fun endCall(context: Context, number: String?, message: String) {
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

        Logger.d("message.isNullOrEmpty() %s",TextUtils.isEmpty(message).toString())

        val messageStr = if (TextUtils.isEmpty(message) || message.equals("null")) "지금은 전화를 받을 수 없습니다. 잠시 후 전화드리겠습니다." else message
        Logger.d("messageStr %s",messageStr)


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