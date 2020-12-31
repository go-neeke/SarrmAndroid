package com.sarrm.android.view.models

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sarrm.android.data.db.ReplySettingRealmDao
import com.sarrm.android.data.realm.ReplyResult
import io.realm.Realm

@RequiresApi(Build.VERSION_CODES.O)
class ReplyResultViewModel(
    private val activity: Activity
) : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val realmDao = ReplySettingRealmDao(realm)

    fun findReplyResultBySettingId(replySettingId: Long): LiveData<MutableList<ReplyResult>> {
        val realmLiveData = realmDao.findReplyResultBySettingId(replySettingId)

        // get your live data
        return Transformations.map(realmLiveData) { realmResult -> realm.copyFromRealm(realmResult) }

    }


    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

}