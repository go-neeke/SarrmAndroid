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
    var startDate: Long = 0
    var endDate: Long = 0
    var repeatType: Int = 0
    var dayList: RealmList<Int> = RealmList<Int>()
    var message: String = ""

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
}