package com.android.sarrm.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.sarrm.utils.AppConstants
import com.android.sarrm.utils.AppConstants.Companion.REPLY_SETTING_DATABASE
import java.util.*

@Entity(tableName = REPLY_SETTING_DATABASE)
data class ReplySetting (
    val name: String,

    val replyTarget: AppConstants.ReplyTargetType,

    val time: Date,

    val message: String,

    val phoneNumber: String,

    @PrimaryKey
    val regex: Regex
)