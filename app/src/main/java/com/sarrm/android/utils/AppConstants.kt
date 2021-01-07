package com.sarrm.android.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.sarrm.android.R

interface AppConstants {

    enum class ReplyTargetType(val value: Int) {
        ALL(0),
        MY_CONTACT_LIST(1),
        TARGET_NUMBER(2);
    }

    enum class RepeatType(val value: Int) {
        DAILY(0),
        ONLY_WEEKDAY(1),
        ONLY_WEEKEND(2),
        SPECIFIC_DAY(3);
    }

    enum class ListMode(val value: Int) {
        NONE(0),
        SELECTION(1),
    }

    enum class PickerType(val value: Int) {
        START(1),
        END(2);
    }

    enum class PermissionType(val value: Int) {
        READ_CALL_LOG(1) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun permission(): Array<String> {
                return arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ANSWER_PHONE_CALLS
                )
            }

            override fun permissionAlertTitle(context:Context): String {
                return context.getString(R.string.read_call_log_alert_title)
            }

            override fun permissionAlertMessage(context:Context): String {
                return context.getString(R.string.read_call_log_alert_message)
            }

            override fun permissionRequestCode(): Int {
                return 1001
            }
        },
        SEND_SMS(2) {
            override fun permission(): Array<String> {
                return arrayOf(Manifest.permission.SEND_SMS)
            }

            override fun permissionAlertTitle(context:Context): String {
                return context.getString(R.string.send_sms_alert_title)
            }

            override fun permissionAlertMessage(context:Context): String {
                return context.getString(R.string.send_sms_alert_message)
            }

            override fun permissionRequestCode(): Int {
                return 1002
            }
        },
        READ_CONTACTS(3) {
            override fun permission(): Array<String> {
                return arrayOf(Manifest.permission.READ_CONTACTS)
            }

            override fun permissionAlertTitle(context:Context): String {
                return context.getString(R.string.read_contacts_alert_title)
            }

            override fun permissionAlertMessage(context:Context): String {
                return context.getString(R.string.read_contacts_alert_message)
            }

            override fun permissionRequestCode(): Int {
                return 1003
            }
        };

        abstract fun permission(): Array<String>
        abstract fun permissionAlertTitle(context: Context): String
        abstract fun permissionAlertMessage(context:Context): String
        abstract fun permissionRequestCode(): Int
    }
}