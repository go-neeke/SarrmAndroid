package com.android.sarrm.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    fun toSimpleString(date: Date) : String {
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }
}