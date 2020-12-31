package com.sarrm.android.view.models

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
import com.sarrm.android.R
import com.sarrm.android.converts.ViewConverter
import com.sarrm.android.data.db.ReplySettingRealmDao
import com.sarrm.android.data.models.DateModel
import com.sarrm.android.data.models.RepeatType
import com.sarrm.android.data.realm.ReplyResult
import com.sarrm.android.data.realm.ReplySetting
import com.orhanobut.logger.Logger
import com.sarrm.android.utils.AppConstants
import io.realm.Realm
import io.realm.RealmList
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.Exception


@RequiresApi(Build.VERSION_CODES.O)
class ReplySettingViewModel(
    private val activity: Activity,
) : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val realmDao = ReplySettingRealmDao(realm)

    private var replySettingId: Long? = null

    val isExistReplySetting = MutableLiveData<Boolean>()   // 특정요일

    // Two-way databinding, exposing MutableLiveData
    val name = MutableLiveData<String>()    // 설정이름
    val phoneNumber = MutableLiveData<String>()    // 폰번호
    val message = MutableLiveData<String>()   // 응답 메시지

    val isSelectedPhoneNumber = MutableLiveData<Boolean>()   // 응답 대상 Spinner 선택
    val isSelsectedSpecificDay = MutableLiveData<Boolean>()   // 특정요일
    val isOverWeek = MutableLiveData<Boolean>()   // 일주일 이상인지 체크

    var startLocalTime = LocalTime.now()   // 시작 시간
    var endLocalTime = startLocalTime.plusHours(1)   // 종료 시간

    val startTimeString = MutableLiveData<String>().default(
        ViewConverter.getTime(
            startLocalTime.hour,
            startLocalTime.minute
        )
    )
    val endTimeString = MutableLiveData<String>().default(
        ViewConverter.getTime(
            endLocalTime.hour,
            endLocalTime.minute
        )
    )

    var startLocalDate = LocalDate.now()   // 시작 날짜
    val startDateString =
        MutableLiveData<String>().default(ViewConverter.dateToString(startLocalDate))

    var endLocalDate =
        if (endLocalTime.hour == 0) LocalDate.now().plusDays(1) else LocalDate.now()   // 종료 날짜
    val endDateString = MutableLiveData<String>().default(ViewConverter.dateToString(endLocalDate))

    var dayList =
        initSettingDayList(null)   // 요일 리스트
    var repeatTypeList = initRepeatList(null)   // 반복 타입

    val replyTarget = MutableLiveData<Int>()    // 응답 대상

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }


    fun start(replySettingId: Long?) {
        this.replySettingId = replySettingId;

        if (replySettingId == null) {
            this.isExistReplySetting.value = false
            return
        }

        viewModelScope.launch {
            getReplySettingFromRealm(replySettingId)
        }
    }

    // 수정 모드 > realm에서 데이터 가져오기
    private fun getReplySettingFromRealm(id: Long) {
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
        this.startTimeString.value =
            ViewConverter.getTime(startLocalTime.hour, startLocalTime.minute)
        this.endLocalTime = LocalTime.of(replySettingData?.endHour!!, replySettingData?.endMinute!!)
        this.endTimeString.value = ViewConverter.getTime(endLocalTime.hour, endLocalTime.minute)

        this.replyTarget.value = replySettingData.replyTarget

        val list: ArrayList<Int> = ArrayList()
        list.addAll(replySettingData.dayList)
        this.dayList = initSettingDayList(list)

        this.repeatTypeList = initRepeatList(replySettingData.repeatType)

        val baseLocalDate = startLocalDate.plusDays(6)
        isOverWeek.value = endLocalDate.isAfter(baseLocalDate)
        isSelsectedSpecificDay.value = replySettingData.dayList.isNotEmpty()
    }

    // 요일 리스트 초기화
    fun initSettingDayList(selectedDayList: ArrayList<Int>?): MutableList<DateModel> {
        var dateList = activity.resources.getStringArray(R.array.setting_date_array)
        val list: MutableList<DateModel> = ArrayList()

        for ((index, item) in dateList.withIndex()) {
            list.add(DateModel(index, item, selectedDayList?.any { it == index } ?: false))
        }

        return list
    }

    // 반복 설정 초기화
    fun initRepeatList(selectedRepeatType: Int?): MutableList<RepeatType> {
        var typeList = activity.resources.getStringArray(R.array.repeat_type_array)
        val list: MutableList<RepeatType> = ArrayList()

        for ((index, item) in typeList.withIndex()) {
            list.add(
                RepeatType(
                    index,
                    item,
                    if (selectedRepeatType != null) index == selectedRepeatType else index == 0
                )
            )
        }

        return list
    }

    // 응답 대상 spinner listener
    val onSelectedReplyTargetListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            Logger.d("onItemSelected %d", position)
            replyTarget.value = position
            isSelectedPhoneNumber.value = position == AppConstants.ReplyTargetType.TARGET_NUMBER.ordinal     // 번호 지정 선택시 핸드폰 번호 입력란 보이도록
        }
    }

    // 요일 선택 chip list listener
    val onSelectedSpecificDayListener =
        CompoundButton.OnCheckedChangeListener { view, isChecked ->
            repeatTypeList[view?.id!!]?.ischecked = isChecked
            if (view.id == AppConstants.RepeatType.SPECIFIC_DAY.ordinal) isSelsectedSpecificDay.value = isChecked      // 특정 요일 지정시 요일 선택 보이도록
        }

    // time picker dialog
    fun openTimePicker(type: Int) {
        val pickerHour = if (type == AppConstants.PickerType.START.ordinal) startLocalTime.hour else endLocalTime.hour
        val pickerMinute = if (type == AppConstants.PickerType.START.ordinal) startLocalTime.minute else endLocalTime.minute
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

    // date picker dialog
    fun openDatePicker(type: Int) {
        val pickerYear =
            if (type == AppConstants.PickerType.START.ordinal) startLocalDate.year else endLocalDate.year
        val pickerMonth =
            if (type == AppConstants.PickerType.START.ordinal) startLocalDate.monthValue else endLocalDate.monthValue
        val pickerDay =
            if (type == AppConstants.PickerType.START.ordinal) startLocalDate.dayOfMonth else endLocalDate.dayOfMonth

        val datePickerDialog = DatePickerDialog(
            activity,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            DatePickerDialog.OnDateSetListener { datePicker, i, i2, i3 ->
                onDateSet(type, i, i2, i3)
            },
            pickerYear,
            pickerMonth - 1,
            pickerDay
        )
        datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

    // 응답 설정 삭제
    fun deleteReplySetting() {
        try {
            realmDao.deleteReplySettingById(replySettingId!!)
        } catch (e: Exception) {
            Toast.makeText(activity, activity.getString(R.string.error_message), Toast.LENGTH_SHORT)
                .show()
            Logger.e(e.toString())
        }

        viewModelScope.launch {
            Toast.makeText(
                activity,
                activity.getString(R.string.confirm_message),
                Toast.LENGTH_SHORT
            ).show()
            activity.onBackPressed()
        }
    }

    // 응답 설정 저장
    fun saveReplySetting() {
        try {
            if (name.value.isNullOrEmpty()) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.input_name_message),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (replyTarget.value == AppConstants.ReplyTargetType.TARGET_NUMBER.ordinal && phoneNumber.value.isNullOrEmpty()) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.input_phone_number_message),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (startLocalTime.isAfter(endLocalTime)) {
                if (endLocalTime.hour == 0 && startLocalDate.isBefore(endLocalDate)) {
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.check_time_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }

            val baseHour = if (startLocalTime.hour == 23) 0 else startLocalTime.hour + 1
            val baseTime = LocalTime.of(baseHour, startLocalTime.minute)
            if (endLocalTime.isBefore(baseTime)) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.set_time_message),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }


            if (startLocalDate.isAfter(endLocalDate)) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.check_date_message),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val checkedDayList = dayList.filter { it.ischecked }.map(DateModel::id)

            if (isSelsectedSpecificDay.value == true && checkedDayList.isNullOrEmpty()) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.set_day_message),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            Logger.d("replySettingId %d", replySettingId)
            realm.executeTransaction {
                val newReplySetting =
                    if (replySettingId == null || replySettingId?.toInt() == 0) realm.createObject(
                        ReplySetting::class.java
                    ) else realmDao.findReplySettingById(replySettingId!!) as ReplySetting
                newReplySetting.name = name.value.toString()
                newReplySetting.replyTarget = replyTarget.value!!.toInt()
                newReplySetting.phoneNumber = phoneNumber.value.toString()
                newReplySetting.message =
                    if (message.value.isNullOrEmpty()) activity.getString(R.string.default_sms_message) else message.value.toString()
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
                newReplySetting.repeatType =
                    repeatTypeList.filter { it.ischecked }.map(RepeatType::id)[0]
                newReplySetting.dayList.addAll(checkedDayList)
                newReplySetting.isOn = true
            }
        } catch (e: Exception) {
            Toast.makeText(activity, activity.getString(R.string.error_message), Toast.LENGTH_SHORT)
                .show()
            Logger.e(e.toString())
        }

        Toast.makeText(activity, activity.getString(R.string.saved_message), Toast.LENGTH_SHORT)
            .show()
        this.init()

        viewModelScope.launch {
            activity.onBackPressed()
        }
    }

    private fun init() {
        name.value = ""
        phoneNumber.value = ""
        message.value = ""
        replyTarget.value = AppConstants.ReplyTargetType.ALL.ordinal

        isSelectedPhoneNumber.value = false
        isSelsectedSpecificDay.value = false
        isOverWeek.value = false

        startLocalDate = LocalDate.now()
        startDateString.value = ViewConverter.dateToString(startLocalDate)

        endLocalDate = LocalDate.now()
        endDateString.value = ViewConverter.dateToString(endLocalDate)

        startLocalTime = LocalTime.now()
        endLocalTime = startLocalTime.plusHours(1)

        startTimeString.value = ViewConverter.getTime(startLocalTime.hour, startLocalTime.minute)
        endTimeString.value = ViewConverter.getTime(endLocalTime.hour, endLocalTime.minute)

        this.dayList = initSettingDayList(null)
        this.repeatTypeList = initRepeatList(null)
    }

    private fun onDateSet(type: Int, year: Int, month: Int, dayOfMonth: Int) {
        if (type == AppConstants.PickerType.START.ordinal) {
            startLocalDate = LocalDate.of(year, month + 1, dayOfMonth)
            startDateString.value = ViewConverter.dateToString(startLocalDate)
        } else {
            endLocalDate = LocalDate.of(year, month + 1, dayOfMonth)
            endDateString.value = ViewConverter.dateToString(endLocalDate)
        }

        val baseLocalDate = startLocalDate.plusDays(6)
        isOverWeek.value = endLocalDate.isAfter(baseLocalDate)      // 일주일 이상일 경우 반복 설정 가능
    }

    private fun onTimeSet(type: Int, hour: Int, minute: Int) {
        if (type == AppConstants.PickerType.START.ordinal) {
            startLocalTime = LocalTime.of(hour, minute)
            startTimeString.value =
                ViewConverter.getTime(startLocalTime.hour, startLocalTime.minute)
        } else {
            endLocalTime = LocalTime.of(hour, minute)
            endTimeString.value =
                ViewConverter.getTime(endLocalTime.hour, endLocalTime.minute)

            // 24시 일 경우, 하루를 더해줌
            endLocalDate = if (endLocalTime.hour == 0) endLocalDate.plusDays(1) else endLocalDate
            endDateString.value = ViewConverter.dateToString(endLocalDate)

            val baseLocalDate = startLocalDate.plusDays(6)
            isOverWeek.value = endLocalDate.isAfter(baseLocalDate)      // 일주일 이상일 경우 반복 설정 가능
        }
    }
}