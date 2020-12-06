package com.android.sarrm.view.models

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sarrm.R
import com.android.sarrm.converts.ViewConverter
import com.android.sarrm.data.models.DateModel
import com.android.sarrm.data.models.RepeatType
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.view.customviews.DateTimePicker
import io.realm.Realm
import java.util.*


class ReplySettingViewModel(
    private val activity: Activity
) : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    // Two-way databinding, exposing MutableLiveData
    val name = MutableLiveData<String>()    // 설정이름
    val phoneNumber = MutableLiveData<String>()    // 폰번호
    val message = MutableLiveData<String>()   // 응답 메시지

    val isSelectedPhoneNumber = MutableLiveData<Boolean>()   // 응답 대상 Spinner 선택
    val isOverWeek = MutableLiveData<Boolean>()

    var startDate = Date()
    val startDateString = MutableLiveData<String>().default(ViewConverter.dateToString(startDate))

    var endDate = Date()
    val endDateString = MutableLiveData<String>().default(ViewConverter.dateToString(endDate))

    var dateList = getSettingDateList()
    var repeatTypeList = getRepeatList()

    var replyTarget: Int = 0

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

    fun getSettingDateList(): MutableList<DateModel> {
        var dateList = listOf(activity.resources.getStringArray(R.array.setting_date_array))
        val list: MutableList<DateModel> = ArrayList()

        for ((index, item) in dateList[0].withIndex()) {
            list.add(DateModel(index, item, false))
        }

        return list
    }

    fun getRepeatList(): MutableList<RepeatType> {
        var typeList = listOf(activity.resources.getStringArray(R.array.repeat_type_array))
        val list: MutableList<RepeatType> = ArrayList()

        for ((index, item) in typeList[0].withIndex()) {
            list.add(RepeatType(index, item, false))
        }

        return list
    }

    val onSelectedReplyTargetListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            replyTarget = position
            isSelectedPhoneNumber.value = position == 2
        }
    }

    fun openDateTimePicker(type: Int) {
        DateTimePicker(activity, object : DateTimePicker.ICustomDateTimeListener {
            @SuppressLint("BinaryOperationInTimber")
            override fun onSet(
                dialog: Dialog,
                calendarSelected: Calendar,
                dateSelected: Date,
                year: Int,
                monthFullName: String,
                monthShortName: String,
                monthNumber: Int,
                day: Int,
                weekDayFullName: String,
                weekDayShortName: String,
                hour24: Int,
                hour12: Int,
                min: Int,
                sec: Int,
                AM_PM: String
            ) {
                if (type == 1) startDate = dateSelected else endDate = dateSelected
                startDateString.value = ViewConverter.dateToString(startDate)
                endDateString.value = ViewConverter.dateToString(endDate)

                val timeForweek =
                    (6 * 24 * 60 * 60 * 1000).toLong() /// here 24*60*60*1000 =24 hours i.e 1 day

                isOverWeek.value = endDate.time - startDate.time > timeForweek
            }

            override fun onCancel() {
            }
        }).apply {
            set24HourFormat(false)//24hr format is off
            setMaxMinDisplayDate(
                minDate = Calendar.getInstance().apply {
                    add(
                        Calendar.MINUTE,
                        5
                    )
                }.timeInMillis,//min date is 5 min after current time
                maxDate = Calendar.getInstance()
                    .apply { add(Calendar.YEAR, 1) }.timeInMillis//max date is next 1 year
            )
            setMaxMinDisplayedTime(5)//min time is 5 min after current time
            setDate(Calendar.getInstance())//date and time will show in dialog is current time and date. We can change this according to our need
            showDialog()
        }
    }

    fun saveReplySetting() {
        val realm: Realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val newReplySetting = realm.createObject(ReplySetting::class.java)
            newReplySetting.name = name.value.toString()
            newReplySetting.phoneNumber = phoneNumber.value.toString()
            newReplySetting.message = message.value.toString()
            newReplySetting.startDate = startDate.time
            newReplySetting.endDate = endDate.time
            newReplySetting.repeatType =
                repeatTypeList.filter { it.ischecked }.map(RepeatType::id)[0]
            val checkedDayList = dateList.filter { it.ischecked }.map(DateModel::id)
            newReplySetting.dayList.addAll(checkedDayList)
        }
        realm.close()
    }
}