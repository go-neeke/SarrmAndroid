package com.sarrm.android.data.models

import com.sarrm.android.data.realm.ReplySetting

data class ReplySettingClicked(
    val replySettingItem: ReplySetting,
    val itemId: Long
)
