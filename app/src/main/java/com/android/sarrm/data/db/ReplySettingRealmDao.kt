package com.android.sarrm.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.android.sarrm.data.models.RealmLiveData
import com.android.sarrm.data.models.ReplySetting
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults


class ReplySettingRealmDao(val realm: Realm) {
    fun <T : RealmModel> RealmResults<T>.asLiveData() = RealmLiveData<T>(this)

    fun getAllReplySetting(): LiveData<RealmResults<ReplySetting>> {
        return realm.where(ReplySetting::class.java).findAllAsync().asLiveData()
    }

    fun findReplySettingById(replySettingId: String): ReplySetting? {
        return realm.where(ReplySetting::class.java).findAllAsync()
            .find { setting -> setting.id == replySettingId }
    }

    fun deleteReplySettingById(replySettingId: String) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.deleteFromRealm()
        realm?.commitTransaction()
    }

}