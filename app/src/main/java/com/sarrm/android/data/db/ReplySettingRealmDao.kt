package com.sarrm.android.data.db

import androidx.lifecycle.LiveData
import com.sarrm.android.data.realm.RealmListLiveData
import com.sarrm.android.data.realm.RealmResultLiveData
import com.sarrm.android.data.realm.ReplyResult
import com.sarrm.android.data.realm.ReplySetting
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults


class ReplySettingRealmDao(val realm: Realm) {
    fun <T : RealmModel> RealmResults<T>.realmResultsasLiveData() = RealmResultLiveData<T>(this)
    fun <T : ReplyResult> RealmList<T>.realmListasLiveData() = RealmListLiveData<T>(this)

    // 모든 응답 설정 가져오기
    fun getAllReplySetting(): LiveData<RealmResults<ReplySetting>> {
        return realm.where(ReplySetting::class.java).findAllAsync().realmResultsasLiveData()
    }

    // id로 응답 설정 찾기
    fun findReplySettingById(replySettingId: Long): ReplySetting? {
        return realm.where(ReplySetting::class.java).findAllAsync().find { setting -> setting.id == replySettingId }
    }

    // id로 result 찾기
    fun findReplyResultBySettingId(replySettingId: Long): RealmListLiveData<ReplyResult> {
        val resplySetting = realm.where(ReplySetting::class.java).findAllAsync()
            .find { setting -> setting.id == replySettingId }
        return resplySetting!!.replyResult.realmListasLiveData()
    }

    // on/off toggle
    fun settingToggle(replySettingId: Long, isOn: Boolean) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.isOn = isOn
        realm?.commitTransaction()
    }

    // 삭제
    fun deleteReplySettingById(replySettingId: Long) {
        val replySetting = findReplySettingById(replySettingId)
        realm?.beginTransaction()
        replySetting?.deleteFromRealm()
        realm?.commitTransaction()
    }
}