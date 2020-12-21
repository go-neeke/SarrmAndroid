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

open class ReplyResult : RealmObject() {
    var sendDate: Date = Date()
    var phoneNumber: String = ""
}