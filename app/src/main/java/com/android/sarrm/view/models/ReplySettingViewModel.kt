package com.android.sarrm.view.models

import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.*
import com.android.sarrm.data.db.ReplySettingDatabaseDao
import com.android.sarrm.data.models.ReplySetting
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReplySettingViewModel(private val dataSource: ReplySettingDatabaseDao) : ViewModel() {
    // Two-way databinding, exposing MutableLiveData
    val name = MutableLiveData<String>()
    val replyTarget = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val repeatType = MutableLiveData<String>()
    val date = MutableLiveData<String>()
    val hour = MutableLiveData<Int>()
    val minute = MutableLiveData<Int>()
    val message = MutableLiveData<String>()
    val isSelectedPhoneNumber = MutableLiveData<Boolean>()
    val isSelectedRepeatSpecific = MutableLiveData<Boolean>()
    val sfsfsf =MutableLiveData<Int>()

    val onSelectedReplyTargetListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            replyTarget.value = parent?.getItemAtPosition(position) as String
            isSelectedPhoneNumber.value = position == 2
        }
    }

    val onSelectedRepeatTypeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            repeatType.value = parent?.getItemAtPosition(position) as String
            isSelectedRepeatSpecific.value = position == 2
        }
    }

    val onSelectedDayListener =
        ChipGroup.OnCheckedChangeListener { group, checkedId -> sfsfsf.value=checkedId }
//        ChipGroup.OnCheckedChangeListener { group, checkedId -> Log.d("AGA", checkedId.toString()) }
//    val onSelectedDayddListener = InverseBindingListener { Log.d("AGA", "fdfdfd") }

    fun onStyleChange(chipGroup: ChipGroup, id: Int) {
//        Log.e(Thread.currentThread().name, "style_change: $id")
        sfsfsf.value=id
//        onOrderChange?.invoke(id)
    }

    fun saveReplySetting() {
        val currentName = name.value.toString()
        val currentReplyTarget = replyTarget.value.toString()
        val currentDate = date.value.toString()
        val currentHour = hour.value.toString()
        val currentMinute = minute.value.toString()
        val currentMessage = message.value.toString()
        val currentsfsfsf = sfsfsf.value.toString()

        Log.d("Viewmodel", currentName)
        Log.d("Viewmodel", currentReplyTarget)
        Log.d("Viewmodel", currentDate)
        Log.d("Viewmodel", currentHour)
        Log.d("Viewmodel", currentMinute)
        Log.d("Viewmodel", currentMessage)
        Log.d("Viewmodel", currentsfsfsf)
//        if (currentName.isEmpty() || currentLocation.isEmpty()) {
//            _snackbarText.value = "Please fill out Name and Location"
//            return
//        }

//        insertNewReplySetting(
//            ReplySetting(
//                currentName,
//                currentReplyTarget,
//                currentDate,
//                currentHour,
//                currentMinute,
//                currentMessage
//            )
//        )
    }


    private fun insertNewReplySetting(replySetting: ReplySetting) {
        viewModelScope.launch {
            insertReplySetting(replySetting)
            resetFields()
        }

    }

    private suspend fun insertReplySetting(replySetting: ReplySetting) {
        withContext(Dispatchers.IO) {
            dataSource.insert(replySetting)
        }
    }

    private fun resetFields() {
        name.value = ""
        replyTarget.value = ""
        date.value = ""
        hour.value = 0
        minute.value = 0
        message.value = ""
    }
}