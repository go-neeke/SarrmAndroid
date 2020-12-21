package com.android.sarrm.data.db

import androidx.lifecycle.LiveData
import com.android.sarrm.data.models.RealmListLiveData
import com.android.sarrm.data.models.RealmResultLiveData
import com.android.sarrm.data.models.ReplyResult
import com.android.sarrm.data.models.ReplySetting
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults


class ReplySettingRealmDao(val realm: Realm) {
    fun <T : RealmModel> RealmResults<T>.realmResultsasLiveData() = RealmResultLiveData<T>(this)
    fun <T : ReplyResult> RealmList<T>.realmListasLiveData() = RealmListLiveData<T>(this)

    fun getAllReplySetting(): LiveData<RealmResults<ReplySetting>> {
        return realm.where(ReplySetting::class.java).findAllAsync().realmResultsasLiveData()
    }

    fun findReplySettingById(replySettingId: Long): ReplySetting? {
        return realm.where(ReplySetting::class.java).findAllAsync()
            .find { setting -> setting.id == replySettingId }
    }

    fun findReplyResutBySettingId(replySettingId: Long): RealmListLiveData<ReplyResult> {
        val resplySetting = realm.where(ReplySetting::class.java).findAllAsync()
            .find { setting -> setting.id == replySettingId }
        return resplySetting!!.replyResult.realmListasLiveData()
    }

    fun settingToggle(replySettingId: Long, isOn: Boolean) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.isOn = isOn
        realm?.commitTransaction()
    }

    fun deleteReplySettingById(replySettingId: Long) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.deleteFromRealm()
        realm?.commitTransaction()
    }
}