package com.sarrm.android.view.models

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sarrm.android.data.db.ReplySettingRealmDao
import com.sarrm.android.data.realm.ReplySetting
import io.realm.Realm

@RequiresApi(Build.VERSION_CODES.O)
class ReplySettingListViewModel(
    private val activity: Activity
) : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val realmDao = ReplySettingRealmDao(realm)

    val mRealmLiveData = realmDao.getAllReplySetting()

    // get your live data
    val allReplySettingList: LiveData<List<ReplySetting>> =
        Transformations.map(mRealmLiveData) { realmResult ->
            realm.copyFromRealm(realmResult)
        }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    fun settingToggle(id: Long, isOn: Boolean) {
        realmDao.settingToggle(id, isOn)
    }

    fun deleteReplySettingList(deleteList: MutableList<Long>) {
        for (item in deleteList) {
            realmDao.deleteReplySettingById(item)
        }
    }
}