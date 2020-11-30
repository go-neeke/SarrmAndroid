package com.android.sarrm.view.models

import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.*
import com.android.sarrm.data.db.ReplySettingDatabaseDao
import com.android.sarrm.data.models.ReplySetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReplySettingViewModel(private val dataSource: ReplySettingDatabaseDao) : ViewModel() {
    // Two-way databinding, exposing MutableLiveData
    val name = MutableLiveData<String>()
    val replyTarget = MutableLiveData<String>()
    val date = MutableLiveData<String>()
    val hour = MutableLiveData<Int>()
    val minute = MutableLiveData<Int>()
    val message = MutableLiveData<String>()

    val onSelectedReplyTargetListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            replyTarget.value = parent?.getItemAtPosition(position) as String
        }
    }

    val onSelectedRepeatTimeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            replyTarget.value = parent?.getItemAtPosition(position) as String
        }
    }

    fun saveReplySetting() {
        val currentName = name.value.toString()
        val currentReplyTarget = replyTarget.value.toString()
        val currentDate = date.value.toString()
        val currentHour = hour.value.toString()
        val currentMinute = minute.value.toString()
        val currentMessage = message.value.toString()


        Log.d("Viewmodel", currentName)
        Log.d("Viewmodel", currentReplyTarget)
        Log.d("Viewmodel", currentDate)
        Log.d("Viewmodel", currentHour)
        Log.d("Viewmodel", currentMinute)
        Log.d("Viewmodel", currentMessage)
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