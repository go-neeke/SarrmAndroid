package com.sarrm.android.data.realm

import androidx.room.PrimaryKey
import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

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
    var replyResult: RealmList<ReplyResult> = RealmList<ReplyResult>()
    var isOn : Boolean = true

    @PrimaryKey
    var id: Long = UUID.randomUUID().mostSignificantBits
}