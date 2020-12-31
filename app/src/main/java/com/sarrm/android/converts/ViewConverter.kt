package com.sarrm.android.converts

import android.os.Build
import androidx.annotation.RequiresApi
import com.sarrm.android.R
import com.sarrm.android.application.SarrmApplication
import com.sarrm.android.data.realm.ReplySetting
import com.sarrm.android.utils.AppConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object ViewConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun dateToString(value: LocalDate): String {
        return value.format(DateTimeFormatter.ofPattern("yyyy - MM - dd"));
    }

    @JvmStatic
    fun getHourStr(value: Int): String {
        return if (value < 12) String.format(
            SarrmApplication.getInstance().getString(R.string.am),
            getNumberStr(value)
        ) else String.format(
            SarrmApplication.getInstance().getString(R.string.pm),
            getNumberStr(value - 12)
        )
    }

    @JvmStatic
    fun getNumberStr(value: Int): String {
        return String.format("%02d", value)
    }

    @JvmStatic
    fun getTime(hour: Int, minute: Int): String {
        return String.format(
            "%s : %s",
            getHourStr(hour),
            getNumberStr(minute)
        )
    }

    @JvmStatic
    fun getYear(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date;
        return calendar.get(Calendar.YEAR)
    }

    @JvmStatic
    fun getMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date;
        return calendar.get(Calendar.MONTH)
    }

    @JvmStatic
    fun getDay(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date;
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    @JvmStatic
    fun getReplyTargetStr(replySetting: ReplySetting): String {
        val replyTargetList =
            SarrmApplication.getInstance().resources.getStringArray(R.array.reply_target_array);
        val targetStr =
            if (replySetting.replyTarget == AppConstants.ReplyTargetType.TARGET_NUMBER.ordinal) replySetting.phoneNumber else replyTargetList[replySetting.replyTarget]
        return String.format("To. %s", targetStr)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun getDateStr(replySetting: ReplySetting): String {
        val startLocalDate =
            LocalDate.of(replySetting.startYear, replySetting.startMonth, replySetting.startDay)
        val endLocalDate =
            LocalDate.of(replySetting.endYear, replySetting.endMonth, replySetting.endDay)
        return String.format(
            "%s ~ %s\n%s ~ %s",
            dateToString(startLocalDate),
            dateToString(endLocalDate),
            getTime(replySetting.startHour, replySetting.startMinute),
            getTime(replySetting.endHour, replySetting.endMinute),
        )
    }
}