package com.android.sarrm.receiver


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.android.sarrm.data.db.ReplySettingRealmDao
import com.android.sarrm.data.models.ReplyResult
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.listener.ITelephony
import com.android.sarrm.utils.CheckNumberContacts
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.kotlin.where
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


class PhoneCallReceiver() : BroadcastReceiver() {
    private val SMS_SENT_INTENT_FILTER = "com.yourapp.sms_send"
    private val SMS_DELIVERED_INTENT_FILTER = "com.yourapp.sms_delivered"

    @RequiresApi(Build.VERSION_CODES.O)
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
                realm.where(ReplySetting::class.java).equalTo("isOn", true).findAll().forEach {
                    Logger.d("ReplySetting name %s", it.toString())
                    checkReplyTarget(realm, context, it, incomingNumber!!)
                }
                realm.close()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkExistReplySetting(
        realm: Realm,
        context: Context,
        replySetting: ReplySetting,
        currentLocalDate: LocalDate,
        currentTime: LocalTime,
        incomingNumber: String
    ): Boolean {
        if (replySetting.replyTarget == 2) return false

        val query = realm.where<ReplySetting>()
        query.equalTo("isOn", true)
        query.equalTo("repeatType", replySetting.repeatType)
        val result = query.findAll()

        for (setting in result) {
            val startLocalDate =
                LocalDate.of(setting.startYear, setting.startMonth, setting.startDay)
            val endLocalDate =
                LocalDate.of(setting.endYear, setting.endMonth, setting.endDay)

            if (currentLocalDate.isAfter(startLocalDate) && currentLocalDate.isBefore(endLocalDate) ||
                currentLocalDate.isEqual(startLocalDate) || currentLocalDate.isEqual(endLocalDate)
            ) {
                val startTime = LocalTime.of(setting.startHour, setting.startMinute)
                val endTime = LocalTime.of(setting.endHour, setting.endMinute)

                if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                    val currentDay = currentLocalDate.dayOfWeek.value

                    when (setting.repeatType) {
                        3 -> if (setting.dayList.all { it == currentDay }) {
                            if (setting.phoneNumber == incomingNumber) return true
                            return false
                        }
                        2 -> if (currentDay == DayOfWeek.SATURDAY.value || currentDay == DayOfWeek.SUNDAY.value) {
                            if (setting.phoneNumber == incomingNumber) return true
                            return false
                        }
                        1 -> if (currentDay in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value) {
                            if (setting.phoneNumber == incomingNumber) return true
                            return false
                        }
                        0 -> {
                            if (setting.phoneNumber == incomingNumber) return true
                            return false
                        }
                    }
                }
            }
        }

        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkRepeatType(
        realm: Realm,
        context: Context,
        replySetting: ReplySetting,
        incomingNumber: String
    ) {
        val repeatType = replySetting.repeatType
        val dayList = replySetting.dayList

        val currentLocalDate = LocalDate.now()
        val startLocalDate =
            LocalDate.of(replySetting.startYear, replySetting.startMonth, replySetting.startDay)
        val endLocalDate =
            LocalDate.of(replySetting.endYear, replySetting.endMonth, replySetting.endDay)

        Logger.d("currentLocalDate %s", currentLocalDate.toString())
        Logger.d("startLocalDate %s", startLocalDate.toString())
        Logger.d("endLocalDate %s", endLocalDate.toString())

        // 현재 날짜가 시작날짜와 종료날짜사이에 존재하는지 체크
        if (currentLocalDate.isAfter(startLocalDate) && currentLocalDate.isBefore(endLocalDate) ||
            currentLocalDate.isEqual(startLocalDate) || currentLocalDate.isEqual(endLocalDate)
        ) {
            val currentTime = LocalTime.now()
            val startTime = LocalTime.of(replySetting.startHour, replySetting.startMinute)
            val endTime = LocalTime.of(replySetting.endHour, replySetting.endMinute)

            Logger.d("currentTime %s", currentTime.toString())
            Logger.d("startTime %s", startTime.toString())
            Logger.d("endTime %s", endTime.toString())

            // 현재 시간이 시작시간과 종료시간사이에 존재하는지 체크
            if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                val currentDay = currentLocalDate.dayOfWeek.value
                when (repeatType) {
                    3 -> if (dayList.all { it == currentDay }) {
                        Logger.d("특정 요일 지정 current day = %s", currentDay)
                        if (!checkExistReplySetting(
                                realm,
                                context,
                                replySetting,
                                currentLocalDate,
                                currentTime,
                                incomingNumber
                            )
                        ) endCall(
                            realm,
                            context,
                            incomingNumber,
                            replySetting
                        )
                    }
                    2 -> if (currentDay == DayOfWeek.SATURDAY.value || currentDay == DayOfWeek.SUNDAY.value) {
                        Logger.d("주말에만 current day = %s", currentDay)
                        if (!checkExistReplySetting(
                                realm,
                                context,
                                replySetting,
                                currentLocalDate,
                                currentTime,
                                incomingNumber
                            )
                        ) endCall(
                            realm,
                            context,
                            incomingNumber,
                            replySetting
                        )
                    }
                    1 -> if (currentDay in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value) {
                        Logger.d("주중에만 current day = %s", currentDay)
                        if (!checkExistReplySetting(
                                realm,
                                context,
                                replySetting,
                                currentLocalDate,
                                currentTime,
                                incomingNumber
                            )
                        ) endCall(
                            realm,
                            context,
                            incomingNumber,
                            replySetting
                        )
                    }
                    0 -> {
                        Logger.d("매일")
                        if (!checkExistReplySetting(
                                realm,
                                context,
                                replySetting,
                                currentLocalDate,
                                currentTime,
                                incomingNumber
                            )
                        ) endCall(
                            realm,
                            context,
                            incomingNumber,
                            replySetting
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkReplyTarget(
        realm: Realm,
        context: Context,
        replySetting: ReplySetting,
        incomingNumber: String
    ) {
        val replyTarget = replySetting.replyTarget
        val phoneNumber = replySetting.phoneNumber

        Logger.d("checkReplyTarget %d", replyTarget)

        when (replyTarget) {
            2 -> if (incomingNumber.equals(phoneNumber)) checkRepeatType(
                realm,
                context,
                replySetting,
                incomingNumber
            )
            1 -> if (CheckNumberContacts.isFromContacts(
                    context,
                    incomingNumber
                )
            ) checkRepeatType(
                realm,
                context,
                replySetting,
                incomingNumber
            )
            else -> checkRepeatType(realm, context, replySetting, incomingNumber)
        }
    }

    @SuppressLint("MissingPermission")
    private fun endCall(
        realm: Realm,
        context: Context,
        number: String?,
        replySetting: ReplySetting
    ) {
        val message = replySetting.message
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

        Logger.d("endCall send SMS 11 %s %s", number, message)
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

            realm.executeTransaction {
                val replyResult = realm.createObject(ReplyResult::class.java)
                replyResult.sendDate = Date()
                replyResult.phoneNumber = number!!
                replySetting.replyResult.add(replyResult)
            }
        } catch (e: Exception) {
            Logger.e(e.toString())
        }
    }
}