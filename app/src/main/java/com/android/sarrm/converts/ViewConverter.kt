package com.android.sarrm.converts

import android.os.Build
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.databinding.InverseMethod
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
}