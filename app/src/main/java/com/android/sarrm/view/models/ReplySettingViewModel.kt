package com.android.sarrm.view.models

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.android.sarrm.R
import com.android.sarrm.converts.ViewConverter
import com.android.sarrm.data.db.ReplySettingRealmDao
import com.android.sarrm.data.models.DateModel
import com.android.sarrm.data.models.RepeatType
import com.android.sarrm.data.models.ReplySetting
import com.orhanobut.logger.Logger
import io.realm.Realm
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class ReplySettingViewModel(
    private val activity: Activity,
) : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val realmDao = ReplySettingRealmDao(realm)

    private var replySettingId: String? = null

    private val _navigateToReplySettingList = MutableLiveData<Boolean>()
    val navigateToReplySettingList: LiveData<Boolean>
        get() = _navigateToReplySettingList

    val isExistReplySetting = MutableLiveData<Boolean>()   // 특정요일

    // Two-way databinding, exposing MutableLiveData
    val name = MutableLiveData<String>()    // 설정이름
    val phoneNumber = MutableLiveData<String>()    // 폰번호
    val message = MutableLiveData<String>()   // 응답 메시지

    val isSelectedPhoneNumber = MutableLiveData<Boolean>()   // 응답 대상 Spinner 선택
    val isSelsectedSpecificDay = MutableLiveData<Boolean>()   // 특정요일
    val isOverWeek = MutableLiveData<Boolean>()

    var startLocalDate = LocalDate.now()
    val startDateString =
        MutableLiveData<String>().default(ViewConverter.dateToString(startLocalDate))

    var endLocalDate = LocalDate.now()
    val endDateString = MutableLiveData<String>().default(ViewConverter.dateToString(endLocalDate))

    var startLocalTime = LocalTime.now()
    var endLocalTime = startLocalTime.plusHours(1)

    val startTimeString = MutableLiveData<String>().default(
        String.format(
            "%s : %d",
            ViewConverter.getAMPM(startLocalTime.hour),
            startLocalTime.minute
        )
    )
    val endTimeString = MutableLiveData<String>().default(
        String.format(
            "%s : %d",
            ViewConverter.getAMPM(endLocalTime.hour),
            endLocalTime.minute
        )
    )

    var dayList =
        initSettingDayList(null)
    var repeatTypeList = initRepeatList(null)

    val replyTarget = MutableLiveData<Int>()

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }


    fun start(replySettingId: String?) {
        this.replySettingId = replySettingId;

        if (replySettingId == null) {
            this.isExistReplySetting.value = false
            return
        }

        viewModelScope.launch {
            getReplySettingFromRealm(replySettingId)
        }
    }

    private fun getReplySettingFromRealm(id: String) {
        val replySettingData = realmDao.findReplySettingById(id)

        Logger.d("getReplySettingFromRealm %s", replySettingData.toString())

        this.isExistReplySetting.value = true

        this.name.value = replySettingData?.name
        this.phoneNumber.value = replySettingData?.phoneNumber
        this.message.value = replySettingData?.message
        this.startLocalDate =
            LocalDate.of(
                replySettingData?.startYear!!,
                replySettingData?.startMonth!!,
                replySettingData?.startDay!!
            )
        this.startDateString.value = ViewConverter.dateToString(startLocalDate)
        this.endLocalDate = LocalDate.of(
            replySettingData?.endYear!!,
            replySettingData?.endMonth!!,
            replySettingData?.endDay!!
        )
        this.endDateString.value = ViewConverter.dateToString(endLocalDate)
        this.startLocalTime = LocalTime.of(
            replySettingData?.startHour!!,
            replySettingData?.startMinute!!
        )
        this.startTimeString.value = String.format(
            "%s : %d",
            ViewConverter.getAMPM(startLocalTime.hour),
            startLocalTime.minute
        )
        this.endLocalTime = LocalTime.of(replySettingData?.endHour!!, replySettingData?.endMinute!!)
        this.endTimeString.value = String.format(
            "%s : %d",
            ViewConverter.getAMPM(endLocalTime.hour),
            endLocalTime.minute
        )

        this.replyTarget.value = replySettingData.replyTarget

        val list: ArrayList<Int> = ArrayList()
        list.addAll(replySettingData.dayList)
        this.dayList = initSettingDayList(list)

        this.repeatTypeList = initRepeatList(replySettingData.repeatType)

        val baseLocalDate = startLocalDate.plusDays(6)
        isOverWeek.value = endLocalDate.isAfter(baseLocalDate)
        isSelsectedSpecificDay.value = replySettingData.dayList.isNotEmpty()
    }

    fun initSettingDayList(selectedDayList: ArrayList<Int>?): MutableList<DateModel> {
        var dateList = activity.resources.getStringArray(R.array.setting_date_array)
        val list: MutableList<DateModel> = ArrayList()

        for ((index, item) in dateList.withIndex()) {
            list.add(DateModel(index, item, selectedDayList?.any{it == index} ?: false))
        }

        return list
    }

    fun initRepeatList(selectedRepeatType: Int?): MutableList<RepeatType> {
        var typeList = activity.resources.getStringArray(R.array.repeat_type_array)
        val list: MutableList<RepeatType> = ArrayList()

        for ((index, item) in typeList.withIndex()) {
            list.add(RepeatType(index, item, if (selectedRepeatType != null) index == selectedRepeatType else index == 0))
        }

        return list
    }

    val onSelectedReplyTargetListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            replyTarget.value = position
            isSelectedPhoneNumber.value = position == 2     // 번호 지정 선택시 핸드폰 번호 입력란 보이도록
        }
    }

    val onSelectedSpecificDayListener =
        CompoundButton.OnCheckedChangeListener { view, isChecked ->
            repeatTypeList[view?.id!!]?.ischecked = isChecked
            if (view.id == 3) isSelsectedSpecificDay.value = isChecked      // 특정 요일 지정시 요일 선택 보이도록
        }

    fun openTimePicker(type: Int) {
        val pickerHour = if (type == 1) startLocalTime.hour else endLocalTime.hour
        val pickerMinute = if (type == 1) startLocalTime.minute else endLocalTime.minute
        val timePickerDialog = TimePickerDialog(
            activity,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            TimePickerDialog.OnTimeSetListener { timePicker, i, i2 -> onTimeSet(type, i, i2) },
            pickerHour,
            pickerMinute,
            false
        )
        timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }

    fun openDatePicker(type: Int) {
        val pickerYear =
            if (type == 1) startLocalDate.year else endLocalDate.year
        val pickerMonth =
            if (type == 1) startLocalDate.monthValue else endLocalDate.monthValue
        val pickerDay =
            if (type == 1) startLocalDate.dayOfMonth else endLocalDate.dayOfMonth

        Logger.d(
            "DatePicker %d, %d, %d",
            startLocalDate.year,
            startLocalDate.monthValue,
            startLocalDate.dayOfMonth
        )
        val datePickerDialog = DatePickerDialog(
            activity,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            DatePickerDialog.OnDateSetListener { datePicker, i, i2, i3 ->
                onDateSet(
                    type,
                    i,
                    i2,
                    i3
                )
            },
            pickerYear,
            pickerMonth - 1,
            pickerDay
        )
        datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

    fun deleteReplySetting() {
        realmDao.deleteReplySettingById(replySettingId!!)
        viewModelScope.launch {
            _navigateToReplySettingList.value = true
        }
    }

    fun saveReplySetting() {
        if (name.value.isNullOrEmpty()) {
            Toast.makeText(activity, "설정할 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (replyTarget.value == 2 && phoneNumber.value.isNullOrEmpty()) {
            Toast.makeText(activity, "특정 번호 지정시 핸드폰 입력은 필수입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (startLocalTime.isAfter(endLocalTime)) {
            Toast.makeText(activity, "시작시간이 종료시간을 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val baseTime = LocalTime.of(startLocalTime.hour + 1, startLocalTime.minute)
        if (endLocalTime.isBefore(baseTime)) {
            Toast.makeText(activity, "최소 설정가능한 시간은 1시간입니다.", Toast.LENGTH_SHORT).show()
            return
        }


        if (startLocalDate.isAfter(endLocalDate)) {
            Toast.makeText(activity, "시작날짜가 종료날짜를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val checkedDayList = dayList.filter { it.ischecked }.map(DateModel::id)

        if (isSelsectedSpecificDay.value == true && checkedDayList.isNullOrEmpty()) {
            Toast.makeText(activity, "하나 이상의 요일을 지정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val realm: Realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val newReplySetting = realm.createObject(ReplySetting::class.java)
            newReplySetting.name = name.value.toString()
            newReplySetting.replyTarget = replyTarget.value!!.toInt()
            newReplySetting.phoneNumber = phoneNumber.value.toString()
            newReplySetting.message =
                if (message.value.isNullOrEmpty()) "지금은 전화를 받을 수 없습니다. 나중에 연락드리겠습니다." else message.value.toString()
            newReplySetting.startYear = startLocalDate.year
            newReplySetting.startMonth = startLocalDate.monthValue
            newReplySetting.startDay = startLocalDate.dayOfMonth
            newReplySetting.endYear = endLocalDate.year
            newReplySetting.endMonth = endLocalDate.monthValue
            newReplySetting.endDay = endLocalDate.dayOfMonth
            newReplySetting.startHour = startLocalTime.hour
            newReplySetting.startMinute = startLocalTime.minute
            newReplySetting.endHour = endLocalTime.hour
            newReplySetting.endMinute = endLocalTime.minute
            newReplySetting.repeatType = repeatTypeList.filter { it.ischecked }.map(RepeatType::id)[0]
            newReplySetting.dayList.addAll(checkedDayList)
        }
        realm.close()

        Toast.makeText(activity, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        this.init()

        viewModelScope.launch {
            _navigateToReplySettingList.value = true
        }
    }

    private fun init() {
        name.value = ""
        phoneNumber.value = ""
        message.value = ""
        replyTarget.value = 0

        isSelectedPhoneNumber.value = false
        isSelsectedSpecificDay.value = false
        isOverWeek.value = false

        startLocalDate = LocalDate.now()
        startDateString.value = ViewConverter.dateToString(startLocalDate)

        endLocalDate = LocalDate.now()
        endDateString.value = ViewConverter.dateToString(endLocalDate)

        startLocalTime = LocalTime.now()
        endLocalTime = startLocalTime.plusHours(1)

        startTimeString.value =
            String.format(
                "%s : %d",
                ViewConverter.getAMPM(startLocalTime.hour),
                startLocalTime.minute
            )

        endTimeString.value =
            String.format(
                "%s : %d",
                ViewConverter.getAMPM(endLocalTime.hour),
                endLocalTime.minute
            )

        this.dayList = initSettingDayList(null)
        this.repeatTypeList = initRepeatList(null)
    }

    private fun onDateSet(p0: Int, p1: Int, p2: Int, p3: Int) {
        Logger.d("onDateSet %d %d %d", p1, p2, p3)
        if (p0 == 1) {
            startLocalDate = LocalDate.of(p1, p2 + 1, p3)
            startDateString.value = ViewConverter.dateToString(startLocalDate)
        } else {
            endLocalDate = LocalDate.of(p1, p2 + 1, p3)
            endDateString.value = ViewConverter.dateToString(endLocalDate)
        }

        val baseLocalDate = startLocalDate.plusDays(6)
        isOverWeek.value = endLocalDate.isAfter(baseLocalDate)      // 일주일 이상일 경우 반복 설정 가능
    }

    private fun onTimeSet(p0: Int, p1: Int, p2: Int) {
        Logger.d("onTimeSet %d %d", p1, p2)
        if (p0 == 1) {
            startTimeString.value =
                String.format("%s : %d", ViewConverter.getAMPM(p1), p2)
            startLocalTime = LocalTime.of(p1, p2)
        } else {
            endTimeString.value =
                String.format("%s : %d", ViewConverter.getAMPM(p1), p2)
            endLocalTime = LocalTime.of(p1, p2)
        }
    }
}