package com.android.sarrm.view.models

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
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
    val isSelsectedSpecificDay = MutableLiveData<Boolean>()   // 특정요일
    val isOverWeek = MutableLiveData<Boolean>()

    var startDate = Date()
    val startDateString = MutableLiveData<String>().default(ViewConverter.dateToString(startDate))

    var endDate = Date()
    val endDateString = MutableLiveData<String>().default(ViewConverter.dateToString(endDate))

    val calendar: Calendar = Calendar.getInstance()

    var startHour = calendar.get(Calendar.HOUR_OF_DAY)
    var startMinute = calendar.get(Calendar.MINUTE)
    var endHour = startHour + 1
    var endMinute = startMinute

    val startTimeString = MutableLiveData<String>().default(
        String.format(
            "%s : %d",
            ViewConverter.getAMPM(startHour),
            startMinute
        )
    )
    val endTimeString = MutableLiveData<String>().default(
        String.format(
            "%s : %d",
            ViewConverter.getAMPM(endHour),
            endMinute
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
            list.add(DateModel(index + 1, item, false))
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

    fun openTimePicker(type: Int) {
        val pickerHour = if (type == 0) startHour else endHour
        val pickerMinute = if (type == 0) startMinute else endMinute
        val timePickerDialog = TimePickerDialog(
            activity,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            TimePickerDialog.OnTimeSetListener { timePicker, i, i2 -> onTimeSet(type, i, i2) },
            pickerHour,
            pickerMinute,
            false
        )
        timePickerDialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }

    fun openDatePicker(type: Int) {
        val pickerYear =
            if (type == 0) ViewConverter.getYear(startDate) else ViewConverter.getYear(endDate)
        val pickerMonth =
            if (type == 0) ViewConverter.getMonth(startDate) else ViewConverter.getMonth(endDate)
        val pickerDay =
            if (type == 0) ViewConverter.getDay(startDate) else ViewConverter.getDay(endDate)
        val datePickerDialog = DatePickerDialog(
            activity,
            DatePickerDialog.OnDateSetListener { datePicker, i, i2, i3 ->
                onDateSet(
                    type,
                    i,
                    i2,
                    i3
                )
            },
            pickerYear,
            pickerMonth,
            pickerDay
        )
        datePickerDialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

    fun saveReplySetting() {
        if (name.value.isNullOrEmpty()) {
            Toast.makeText(activity, "설정할 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (replyTarget == 2 && phoneNumber.value.isNullOrEmpty()) {
            Toast.makeText(activity, "특정 번호 지정시 핸드폰 입력은 필수입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val startDate = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
            }

        val endDate = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, endHour)
                set(Calendar.MINUTE, endMinute)
            }

        val timeForhour = (60 * 60 * 1000).toLong() /// here 24*60*60*1000 =24 hours i.e 1 day

        val startTime = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
            }

        val endTime = Calendar.getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, endHour)
                set(Calendar.MINUTE, endMinute)
            }

        if (startTime.timeInMillis > endTime.timeInMillis) {
            Toast.makeText(activity, "시작시간이 종료시간을 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (startTime.timeInMillis > endTime.timeInMillis - timeForhour) {
            Toast.makeText(activity, "최소 설정가능한 시간은 1시간입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDate.timeInMillis > endDate.timeInMillis) {
            Toast.makeText(activity, "시작날짜가 종료날짜를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val checkedDayList = dateList.filter { it.ischecked }.map(DateModel::id)

        if (isSelsectedSpecificDay.value == true && checkedDayList.isNullOrEmpty()) {
            Toast.makeText(activity, "하나 이상의 요일을 지정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val realm: Realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val newReplySetting = realm.createObject(ReplySetting::class.java)
            newReplySetting.name = name.value.toString()
            newReplySetting.replyTarget = replyTarget
            newReplySetting.phoneNumber = phoneNumber.value.toString()
            newReplySetting.message = message.value.toString()
            newReplySetting.startDate = startDate.timeInMillis
            newReplySetting.endDate = endDate.timeInMillis
            newReplySetting.startHour = startHour
            newReplySetting.startMinute = startMinute
            newReplySetting.endHour = endHour
            newReplySetting.endMinute = endMinute
            newReplySetting.repeatType =
                repeatTypeList.filter { it.ischecked }.map(RepeatType::id)[0]
            newReplySetting.dayList.addAll(checkedDayList)
        }
        realm.close()

        Toast.makeText(activity, "저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun onDateSet(p0: Int, p1: Int, p2: Int, p3: Int) {
        Logger.d("onDateSet %d %d", p1, p2, p3)
        val calendar = Calendar.getInstance()
            .apply {
                set(Calendar.YEAR, p1)
                set(Calendar.MONTH, p2)
                set(Calendar.DAY_OF_MONTH, p3)
            }

        if (p0 == 1) {
            startDateString.value = ViewConverter.dateToString(calendar.time)
            startDate = calendar.time
        } else {
            endDateString.value = ViewConverter.dateToString(calendar.time)
            endDate = calendar.time
        }

        val timeForweek =
            (6 * 24 * 60 * 60 * 1000).toLong() /// here 24*60*60*1000 =24 hours i.e 1 day

        isOverWeek.value = endDate.time - startDate.time > timeForweek      // 일주일 이상일 경우 반복 설정 가능
    }

    private fun onTimeSet(p0: Int, p1: Int, p2: Int) {
        Logger.d("onTimeSet %d %d", p1, p2)
        if (p0 == 1) {
            startTimeString.value =
                String.format("%s : %d", ViewConverter.getAMPM(p1), p2)
            startHour = p1
            startMinute = p2
        } else {
            endTimeString.value =
                String.format("%s : %d", ViewConverter.getAMPM(p1), p2)
            endHour = p1
            endMinute = p2
        }
    }
}