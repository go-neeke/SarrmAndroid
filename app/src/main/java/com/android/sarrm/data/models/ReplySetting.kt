package com.android.sarrm.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.sarrm.utils.AppConstants
import com.android.sarrm.utils.AppConstants.Companion.REPLY_SETTING_DATABASE
import java.util.*

@Entity(tableName = REPLY_SETTING_DATABASE)
data class ReplySetting (
    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "reply_target")
    var replyTarget: String = "",

    @ColumnInfo(name = "date")
    var date: String = "",

    @ColumnInfo(name = "time")
    var log: String = "",

    @ColumnInfo(name = "message")
    var message: String = "",

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
)