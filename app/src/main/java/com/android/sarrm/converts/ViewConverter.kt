package com.android.sarrm.converts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.InverseMethod
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ViewConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic fun dateToString(value: LocalDate): String {
        // Converts long to String.
//        val format = SimpleDateFormat("yyyy년\nMM월 dd일", Locale.KOREA)
//        return format.format(value)
        return value.format(DateTimeFormatter.ofPattern("yyyy년\nMM월 dd일"));
    }

    @JvmStatic fun getAMPM(value: Int): String {
//        Logger.d("getAMPM %d", value)
        return if (value < 12) {
            String.format("오전 %d", value);
        } else {
            String.format("오후 %d", value);
        }
    }

    @JvmStatic fun getYear(date:Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date;
       return calendar.get(Calendar.YEAR)
    }

    @JvmStatic fun getMonth(date:Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date;
        return calendar.get(Calendar.MONTH)
    }

    @JvmStatic fun getDay(date:Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date;
        return calendar.get(Calendar.DAY_OF_MONTH)
    }
}