package com.android.sarrm.converts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.InverseMethod
import com.android.sarrm.data.models.ReplySetting
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ViewConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun dateToString(value: LocalDate): String {
        // Converts long to String.
//        val format = SimpleDateFormat("yyyy년\nMM월 dd일", Locale.KOREA)
//        return format.format(value)
        return value.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }

    @JvmStatic
    fun getHourStr(value: Int): String {
        return if (value < 12) String.format(
            "오전 %s",
            getNumberStr(value)
        ) else String.format("오후 %s", getNumberStr(value - 12))
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
        val targetStr= if (replySetting.replyTarget == 0) "모든 전화에" else if (replySetting.replyTarget == 1) "내 연락처만" else replySetting.phoneNumber
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