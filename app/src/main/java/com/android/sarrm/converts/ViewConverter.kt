package com.android.sarrm.converts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.InverseMethod
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*

object ViewConverter {
    @InverseMethod("stringToDate")
    @JvmStatic fun dateToString(value: Date): String {
        // Converts long to String.
        val format = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        return format.format(value)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic fun stringToDate(value: String): Date {
        // Converts String to long.
        val format = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        return format.parse(value)
    }

    @JvmStatic fun getAMPM(value: Int): String {
        Logger.d("getAMPM %d", value)
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