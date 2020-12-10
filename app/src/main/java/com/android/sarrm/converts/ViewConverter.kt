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
        val format = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)
        return format.format(value)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic fun stringToDate(value: String): Date {
        // Converts String to long.
        val format = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)
        return format.parse(value)
    }

    @JvmStatic fun getAMPM(value: Int): String {
        Logger.d("getAMPM %d", value)
        if(value < 12) {
            return String.format("오전 %d", value);
        } else {
            return String.format("오후 %d", value);
        }
    }

}