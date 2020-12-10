package com.android.sarrm.view.customviews

import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.sarrm.R
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*

class DateTimePicker(
    private var activity: Activity?,
    customDateTimeListener: ICustomDateTimeListener
) : View.OnClickListener {
    private var datePicker: CalendarView? = null
    private var timePicker: TimePicker? = null
    private var viewSwitcher: ViewSwitcher? = null

    private var btnSetDate: Button? = null
    private var btnSetTime: Button? = null
    private var btnSet: Button? = null
    private var btnCancel: Button? = null

    private var calendarDate: Calendar? = null

    private var iCustomDateTimeListener: ICustomDateTimeListener? = null

    private val dialog: Dialog

    private var is24HourView = true
    private var isAutoDismiss = true


    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDayOfMonth: Int = 0
    private var selectedMinute: Int = 0
    private var selectedHour: Int = 0

    private var maxDateInMillis: Long? = null
    private var minDateInMillis: Long? = null
    private var maxTimeInMinute: Int? = null
    private var minTimeInMinute: Int? = null

    private val dateTimePickerLayout: View
        get() {
            val linearMatchWrap = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val linearWrapWrap = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val relativeWrapWrap = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            val frameMatchWrap = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

            val buttonParams =
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)

            val linearMain = LinearLayout(activity)
            linearMain.layoutParams = linearMatchWrap
            linearMain.orientation = LinearLayout.VERTICAL

            val linearChild = LinearLayout(activity)
            linearChild.layoutParams = linearWrapWrap
            linearChild.orientation = LinearLayout.VERTICAL

            val linearTop = LinearLayout(activity)
            linearTop.layoutParams = linearMatchWrap

            btnSetDate = Button(activity)
            btnSetDate?.apply {
                layoutParams = buttonParams
                text = context.getString(R.string.setDate)
                id = SET_DATE
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                background = context.getDrawable(R.color.colorTransparent)
                setOnClickListener(this@DateTimePicker)
            }

            btnSetTime = Button(activity)
            btnSetTime?.apply {
                layoutParams = buttonParams
                text = activity?.getString(R.string.setTime)
                id = SET_TIME
                setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                background = context.getDrawable(R.color.colorTransparent)
                setOnClickListener(this@DateTimePicker)
            }

            linearTop.addView(btnSetDate)
            linearTop.addView(btnSetTime)

            viewSwitcher = ViewSwitcher(activity)
            viewSwitcher!!.layoutParams = frameMatchWrap

            datePicker = CalendarView(activity!!)
            timePicker = TimePicker(activity)
            timePicker!!.setIs24HourView(true)
            hideKeyboardInputInTimePicker(
                activity?.resources?.configuration?.orientation!!,
                timePicker!!
            )

            timePicker!!.setOnTimeChangedListener { view, hourOfDay, minute ->
//                 updateTime(hourOfDay, minute)
            }

            datePicker!!.setOnDateChangeListener { datePicker, year, month, dayOfMonth ->
                updateDate(
                    year,
                    month,
                    dayOfMonth
                )
            }

            val datePickerParent = RelativeLayout(activity)
            datePickerParent.layoutParams = relativeWrapWrap
            datePickerParent.gravity = Gravity.CENTER
            datePickerParent.addView(datePicker)

            val timePickerParent = RelativeLayout(activity)
            timePickerParent.layoutParams = relativeWrapWrap
            timePickerParent.gravity = Gravity.CENTER
            timePickerParent.addView(timePicker)

            viewSwitcher!!.addView(datePickerParent)
            viewSwitcher!!.addView(timePickerParent)

            val linearBottom = LinearLayout(activity)
            linearBottom.layoutParams = linearMatchWrap

            btnSet = Button(activity)
            btnSet?.apply {
                layoutParams = buttonParams
                text = activity?.getString(R.string.set)
                id = SET
                isAllCaps = false
                setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                background = context.getDrawable(R.color.colorTransparent)
                setOnClickListener(this@DateTimePicker)
            }

            btnCancel = Button(activity)
            btnCancel?.apply {
                layoutParams = buttonParams
                text = activity?.getString(R.string.cancel)
                id = CANCEL
                isAllCaps = false
                setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                background = context.getDrawable(R.color.colorTransparent)
                setOnClickListener(this@DateTimePicker)
            }

            linearBottom.addView(btnCancel)
            linearBottom.addView(btnSet)

            linearChild.addView(linearTop)
            linearChild.addView(viewSwitcher)
            linearChild.addView(linearBottom)
//
            linearMain.addView(linearChild)

            return linearMain
        }

    private fun updateTime(hourOfDay: Int, minute: Int) {
//        if (minTimeInMinute != null) {
//            val calendar = Calendar.getInstance()
//            calendar.set(
//                selectedYear,
//                selectedMonth,
//                selectedDayOfMonth,
//                hourOfDay,
//                minute
//            )
//
//            if (calendar.timeInMillis - Calendar.getInstance().timeInMillis >= minTimeInMinute!! * 60 * 1000) {
//                selectedHour = hourOfDay
//                selectedMinute = minute
//            } else {
//                selectedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//                selectedMinute = Calendar.getInstance().get(Calendar.MINUTE) + minTimeInMinute!!
//            }
//        } else {
            selectedHour = hourOfDay
            selectedMinute = minute
//        }

        updateDisplayedTime()
    }

    private fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        selectedYear = year
        selectedMonth = month
        selectedDayOfMonth = dayOfMonth
    }

    init {
        iCustomDateTimeListener = customDateTimeListener

        dialog = Dialog(activity!!)
        dialog.setOnDismissListener { resetData() }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogView = dateTimePickerLayout
        dialog.setContentView(dialogView)
    }

    fun showDialog() {
        if (!dialog.isShowing) {
            if (calendarDate == null)
                calendarDate = Calendar.getInstance()

            val hourOfDay = calendarDate!!.get(Calendar.HOUR_OF_DAY)
            val minute = calendarDate!!.get(Calendar.MINUTE)

            updateTime(hourOfDay, minute)

            updateDisplayedDate()

            dialog.show()
            btnSetDate!!.performClick()

            dialog.setOnCancelListener {
                if (activity != null)
                    activity = null
            }
        }
    }

    private fun updateDisplayedDate() {
        datePicker?.setDate(
            calendarDate?.timeInMillis!!
        )

        maxDateInMillis?.let {
            datePicker?.maxDate = maxDateInMillis as Long
        }
        minDateInMillis?.let {
            datePicker?.minDate = minDateInMillis as Long
        }
    }

    private fun updateDisplayedTime() {
        timePicker?.apply {
            setIs24HourView(is24HourView)
            currentMinute = selectedMinute
            currentHour = selectedHour
        }
    }

    fun setMaxMinDisplayDate(minDate: Long? = null, maxDate: Long? = null) {
        minDate?.let {
            minDateInMillis = minDate
        }
        maxDate?.let {
            maxDateInMillis = maxDate
        }
    }

    fun setMaxMinDisplayedTime(minTimeMinute: Int? = null, maxTimeMinute: Int? = null) {
        minTimeMinute?.let {
            minTimeInMinute = minTimeMinute
        }
        maxTimeMinute?.let {
            maxTimeInMinute = maxTimeMinute
        }
    }

    fun setAutoDismiss(isAutoDismiss: Boolean) {
        this.isAutoDismiss = isAutoDismiss
    }

    fun dismissDialog() {
        if (!dialog.isShowing)
            dialog.dismiss()
    }

    fun setDate(calendar: Calendar?) {
        if (calendar != null)
            calendarDate = calendar
    }

    fun setDate(date: Date?) {
        if (date != null) {
            calendarDate = Calendar.getInstance()
            calendarDate!!.time = date
        }
    }

    fun setDate(year: Int, month: Int, day: Int) {
        if (month in 0..11 && day < 32 && day >= 0 && year > 100 && year < 3000) {
            calendarDate = Calendar.getInstance()
            calendarDate!!.set(year, month, day)
        }

    }

    fun setTimeIn24HourFormat(hourIn24Format: Int, minute: Int) {
        if (hourIn24Format in 0..23 && minute >= 0 && minute < 60) {
            if (calendarDate == null)
                calendarDate = Calendar.getInstance()

            calendarDate!!.set(
                calendarDate!!.get(Calendar.YEAR),
                calendarDate!!.get(Calendar.MONTH),
                calendarDate!!.get(Calendar.DAY_OF_MONTH), hourIn24Format,
                minute
            )

            is24HourView = true
        }
    }

    fun setTimeIn12HourFormat(_hourIn12Format: Int, minute: Int, isAM: Boolean) {
        var hourIn12Format = _hourIn12Format
        if (hourIn12Format in 1..12 && minute >= 0
            && minute < 60
        ) {
            if (hourIn12Format == 12)
                hourIn12Format = 0

            var hourIn24Format = hourIn12Format

            if (!isAM)
                hourIn24Format += 12

            if (calendarDate == null)
                calendarDate = Calendar.getInstance()

            calendarDate!!.set(
                calendarDate!!.get(Calendar.YEAR),
                calendarDate!!.get(Calendar.MONTH),
                calendarDate!!.get(Calendar.DAY_OF_MONTH), hourIn24Format,
                minute
            )

            is24HourView = false
        }
    }

    fun set24HourFormat(is24HourFormat: Boolean) {
        is24HourView = is24HourFormat
    }

    interface ICustomDateTimeListener {
        fun onSet(dateSelected: Date)

        fun onCancel()
    }

    override fun onClick(v: View) {
        when (v.id) {
            SET_DATE -> {
                btnSetTime!!.isEnabled = true
                btnSetDate!!.isEnabled = false
                btnSetDate!!.setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                btnSetTime!!.setTextColor(ContextCompat.getColor(activity!!, R.color.colorBlack))

                if (viewSwitcher!!.displayedChild !== 0) {
                    viewSwitcher!!.showPrevious()
                }
            }

            SET_TIME -> {
                btnSetTime!!.isEnabled = false
                btnSetDate!!.isEnabled = true
                btnSetDate!!.setTextColor(ContextCompat.getColor(activity!!, R.color.colorBlack))
                btnSetTime!!.setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))

                if (viewSwitcher!!.displayedChild !== 1) {
                    viewSwitcher!!.showNext()
                }
            }

            SET -> {
                if (iCustomDateTimeListener != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        updateTime(timePicker!!.hour, timePicker!!.minute)
                    } else {
                        updateTime(timePicker!!.currentHour, timePicker!!.currentMinute)
                    }
                    calendarDate!!.set(
                        selectedYear,
                        selectedMonth,
                        selectedDayOfMonth,
                        selectedHour,
                        selectedMinute
                    )

                    iCustomDateTimeListener!!.onSet(dateSelected = calendarDate!!.time)
                    Logger.d("Calendar pick %d %d", selectedHour,selectedMinute)
                }
                if (dialog.isShowing && isAutoDismiss)
                    dialog.dismiss()
                if (activity != null)
                    activity = null
            }

            CANCEL -> {
                if (iCustomDateTimeListener != null)
                    iCustomDateTimeListener!!.onCancel()
                if (dialog.isShowing)
                    dialog.dismiss()
                if (activity != null)
                    activity = null
            }
        }
    }

    private fun getMonthFullName(monthNumber: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthNumber)

        val simpleDateFormat = SimpleDateFormat("MMMM")
        simpleDateFormat.calendar = calendar

        return simpleDateFormat.format(calendar.time)
    }

    private fun getMonthShortName(monthNumber: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthNumber)

        val simpleDateFormat = SimpleDateFormat("MMM")
        simpleDateFormat.calendar = calendar

        return simpleDateFormat.format(calendar.time)
    }

    private fun getWeekDayFullName(weekDayNumber: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber)

        val simpleDateFormat = SimpleDateFormat("EEEE")
        simpleDateFormat.calendar = calendar

        return simpleDateFormat.format(calendar.time)
    }

    private fun getWeekDayShortName(weekDayNumber: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber)

        val simpleDateFormat = SimpleDateFormat("EE")
        simpleDateFormat.calendar = calendar

        return simpleDateFormat.format(calendar.time)
    }

    private fun getHourIn12Format(hour24: Int): Int {
        return when {
            hour24 == 0 -> 12
            hour24 <= 12 -> hour24
            else -> hour24 - 12
        }
    }

    private fun getAMPM(calendar: Calendar): String {
        return if (calendar.get(Calendar.AM_PM) == Calendar.AM)
            "am"
        else
            "pm"
    }

    private fun resetData() {
        calendarDate = null
        is24HourView = true
    }

    companion object {
        /**
         * @param date       date in String
         * @param fromFormat format of your **date** eg: if your date is 2011-07-07
         * 09:09:09 then your format will be **yyyy-MM-dd hh:mm:ss**
         * @param toFormat   format to which you want to convert your **date** eg: if
         * required format is 31 July 2011 then the toFormat should be
         * **d MMMM yyyy**
         * @return formatted date
         */
        private const val SET_DATE = 100
        private const val SET_TIME = 101
        private const val SET = 102
        private const val CANCEL = 103

        fun convertDate(_date: String, fromFormat: String, toFormat: String): String {
            var date = _date
            try {
                var simpleDateFormat = SimpleDateFormat(fromFormat)
                val d = simpleDateFormat.parse(date)
                val calendar = Calendar.getInstance()
                calendar.time = d

                simpleDateFormat = SimpleDateFormat(toFormat)
                simpleDateFormat.calendar = calendar
                date = simpleDateFormat.format(calendar.time)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return date
        }

        fun pad(integerToPad: Int): String {
            return if (integerToPad >= 10 || integerToPad < 0)
                integerToPad.toString()
            else
                "0$integerToPad"
        }
    }

    private fun hideKeyboardInputInTimePicker(orientation: Int, timePicker: TimePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ((timePicker.getChildAt(0) as LinearLayout).getChildAt(4) as LinearLayout).getChildAt(
                        0
                    ).visibility = View.GONE
                } else {
                    (((timePicker.getChildAt(0) as LinearLayout).getChildAt(2) as LinearLayout).getChildAt(
                        2
                    ) as LinearLayout).getChildAt(0).visibility = View.GONE
                }
            } catch (ex: Exception) {
            }
        }
    }
}