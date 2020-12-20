package com.android.sarrm.view.models

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.sarrm.data.db.ReplySettingRealmDao
import com.android.sarrm.data.models.ReplySetting
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

    private val _navigateToReplySettingDetail = MutableLiveData<String>()
    val navigateToReplySettingDetail: LiveData<String>
        get() = _navigateToReplySettingDetail

    fun onReplySettingClicked(id: String) {
        _navigateToReplySettingDetail.value = id
    }

    fun onNewReplySettingNavigated() {
        _navigateToReplySettingDetail.value = null
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    fun settingToggle(id: String, isOn: Boolean) {
        realmDao.settingToggle(id, isOn)
    }
}