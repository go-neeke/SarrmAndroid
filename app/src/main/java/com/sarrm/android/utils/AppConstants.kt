package com.sarrm.android.utils

interface AppConstants {

    enum class ReplyTargetType (val value: Int) {
        ALL(0),
        MY_CONTACT_LIST(1),
        TARGET_NUMBER(2);
    }

    enum class RepeatType (val value: Int) {
        DAILY(0),
        ONLY_WEEKDAY(1),
        ONLY_WEEKEND(2),
        SPECIFIC_DAY(3);
    }

    enum class ListMode (val value: Int) {
        NONE(0),
        SELECTION(1),
    }

    enum class PickerType (val value: Int) {
        START(1),
        END(2);
    }
}