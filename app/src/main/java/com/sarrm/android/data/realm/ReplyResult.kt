package com.sarrm.android.data.realm

import io.realm.RealmObject
import java.util.*

open class ReplyResult : RealmObject() {
    var sendDate: Date = Date()
    var phoneNumber: String = ""
}