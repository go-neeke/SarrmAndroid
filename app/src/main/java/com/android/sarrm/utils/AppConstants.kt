package com.android.sarrm.utils

interface AppConstants {

    companion object {

        const val REPLY_SETTING_DATABASE: String = "reply_setting_table"

    }

    enum class ReplyTargetType (val value: Int) {
        ALL(0),
        MY_CONTACT_LIST(1),
        TARGET_NUMBER(2)
    }
}