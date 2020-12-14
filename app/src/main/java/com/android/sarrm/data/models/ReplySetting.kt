package com.android.sarrm.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.sarrm.utils.AppConstants
import com.android.sarrm.utils.AppConstants.Companion.REPLY_SETTING_DATABASE
import io.realm.RealmList
import io.realm.RealmObject
import java.util.*
import kotlin.collections.ArrayList

open class ReplySetting : RealmObject() {
    var name: String = ""
    var replyTarget: Int = 0
    var phoneNumber: String = ""
    var startYear: Int = 0
    var startMonth: Int = 0
    var startDay: Int = 0
    var endYear: Int = 0
    var endMonth: Int = 0
    var endDay: Int = 0
    var startHour: Int = 0
    var startMinute: Int = 0
    var endHour: Int = 0
    var endMinute: Int = 0
    var repeatType: Int = 0
    var dayList: RealmList<Int> = RealmList<Int>()
    var message: String = ""

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
}