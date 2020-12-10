package com.android.sarrm.view.models

import android.app.Activity
import android.app.TimePickerDialog
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sarrm.R
import com.android.sarrm.converts.ViewConverter
import com.android.sarrm.data.models.DateModel
import com.android.sarrm.data.models.RepeatType
import com.android.sarrm.data.models.ReplySetting
import io.realm.Realm
import java.util.*
import com.orhanobut.logger.Logger


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
    val isSelsectedSpecificDay = MutableLiveData<Boolean>()

    val calendar: Calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val startDateString = MutableLiveData<String>().default(
        String.format(
            "%s : %d",
            ViewConverter.getAMPM(hour),
            minute
        )
    )
    val endDateString = MutableLiveData<String>().default(
        String.format(
            "%s : %d",
            ViewConverter.getAMPM(hour + 2),
            minute
        )
    )

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
            list.add(DateModel(index+1, item, false))
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
            isSelectedPhoneNumber.value = position == 2     // 번호 지정 선택시 핸드폰 번호 입력란 보이도록
        }
    }

    val onSelectedSpecificDayListener =
        CompoundButton.OnCheckedChangeListener { view, isChecked ->
            repeatTypeList[view?.id!!]?.ischecked = isChecked
            if (view.id == 3) isSelsectedSpecificDay.value = isChecked      // 특정 요일 지정시 요일 선택 보이도록
        }

    fun openDateTimePicker(type: Int) {
//        DateTimePicker(activity, object : DateTimePicker.ICustomDateTimeListener {
//            @SuppressLint("BinaryOperationInTimber")
//            override fun onSet(dateSelected: Date) {
//                Logger.d(dateSelected)
//
//                if (type == 1) startDate = dateSelected else endDate = dateSelected
//                startDateString.value = ViewConverter.dateToString(startDate)
//                endDateString.value = ViewConverter.dateToString(endDate)
//}
//
//            override fun onCancel() {
//            }
//        }).apply {
//            set24HourFormat(false)//24hr format is off
//            setMaxMinDisplayDate(
//                minDate = Calendar.getInstance().apply {
//                    add(
//                        Calendar.MINUTE,
//                        5
//                    )
//                }.timeInMillis,//min date is 5 min after current time
//                maxDate = Calendar.getInstance()
//                    .apply { add(Calendar.YEAR, 1) }.timeInMillis//max date is next 1 year
//            )
//            setMaxMinDisplayedTime(5)//min time is 5 min after current time
//            setDate(Calendar.getInstance())//date and time will show in dialog is current time and date. We can change this according to our need
//            showDialog()
//        }

        val pickerHour = if (type == 0) hour else hour + 2
        val timePickerDialog = TimePickerDialog(
            activity,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            TimePickerDialog.OnTimeSetListener { timePicker, i, i2 -> onTimeSet(type, i, i2) },
            pickerHour,
            minute,
            false
        )
        timePickerDialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }


    fun saveReplySetting() {
        val realm: Realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val newReplySetting = realm.createObject(ReplySetting::class.java)
            newReplySetting.name = name.value.toString()
            newReplySetting.phoneNumber = phoneNumber.value.toString()
            newReplySetting.message = message.value.toString()
//            newReplySetting.startDate = startDate.time
//            newReplySetting.endDate = endDate.time
            newReplySetting.repeatType =
                repeatTypeList.filter { it.ischecked }.map(RepeatType::id)[0]
            val checkedDayList = dateList.filter { it.ischecked }.map(DateModel::id)
            newReplySetting.dayList.addAll(checkedDayList)
        }
        realm.close()
    }

    private fun onTimeSet(p0: Int, p1: Int, p2: Int) {
        Logger.d("onTimeSet %d %d", p1, p2)
        if (p0 == 1) startDateString.value =
            String.format("%s : %d", ViewConverter.getAMPM(p1), p2) else endDateString.value =
            String.format("%s : %d", ViewConverter.getAMPM(p1), p2)
    }
}