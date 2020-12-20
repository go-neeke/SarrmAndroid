package com.android.sarrm.view.models

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.sarrm.data.db.ReplySettingRealmDao
import com.android.sarrm.data.models.ReplySetting
import io.realm.Realm
import io.realm.RealmList
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ReplyResultViewModel(
    private val activity: Activity
) : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val realmDao = ReplySettingRealmDao(realm)

    fun findReplyResutBySettingId(replySettingId: String): List<String>? {
        val realmLiveData = realmDao.findReplyResutBySettingId(replySettingId)

        // get your live data
        val allReslutList = Transformations.map(realmLiveData) { realmResult -> realmResult }

        return allReslutList.value
    }


    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

}