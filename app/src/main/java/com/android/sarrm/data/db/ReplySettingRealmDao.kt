package com.android.sarrm.data.db

import androidx.lifecycle.LiveData
import com.android.sarrm.data.models.RealmListLiveData
import com.android.sarrm.data.models.RealmResultLiveData
import com.android.sarrm.data.models.ReplySetting
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults


class ReplySettingRealmDao(val realm: Realm) {
    fun <T : RealmModel> RealmResults<T>.realmResultsasLiveData() = RealmResultLiveData<T>(this)
    fun <T : String> RealmList<T>.realmListasLiveData() = RealmListLiveData<T>(this)

    fun getAllReplySetting(): LiveData<RealmResults<ReplySetting>> {
        return realm.where(ReplySetting::class.java).findAllAsync().realmResultsasLiveData()
    }

    fun findReplySettingById(replySettingId: String): ReplySetting? {
        return realm.where(ReplySetting::class.java).findAllAsync()
            .find { setting -> setting.id == replySettingId }
    }

    fun findReplyResutBySettingId(replySettingId: String): LiveData<RealmList<String>> {
        val resplySetting = realm.where(ReplySetting::class.java).findAllAsync()
            .find { setting -> setting.id == replySettingId }
        return resplySetting!!.resultResult.realmListasLiveData()
    }

    fun settingToggle(replySettingId: String, isOn: Boolean) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.isOn = isOn
        realm?.commitTransaction()
    }

    fun deleteReplySettingById(replySettingId: String) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.deleteFromRealm()
        realm?.commitTransaction()
    }

}