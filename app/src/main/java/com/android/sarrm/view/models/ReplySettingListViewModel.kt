package com.android.sarrm.view.models

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.sarrm.R
import com.android.sarrm.converts.ViewConverter
import com.android.sarrm.data.db.ReplySettingRealmDao
import com.android.sarrm.data.models.DateModel
import com.android.sarrm.data.models.RealmLiveData
import com.android.sarrm.data.models.RepeatType
import com.android.sarrm.data.models.ReplySetting
import io.realm.Realm
import java.util.*
import com.orhanobut.logger.Logger
import io.realm.RealmModel
import io.realm.RealmResults
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year

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

}