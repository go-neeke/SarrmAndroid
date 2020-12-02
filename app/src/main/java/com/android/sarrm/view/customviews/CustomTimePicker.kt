package com.android.sarrm.view.customviews

import android.R
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TimePicker
import java.lang.reflect.Field


/**
 * A subclass of [android.widget.TimePicker] that uses
 * reflection to allow for customization of the default blue
 * dividers.
 *
 * @author jjobes
 */
class CustomTimePicker(context: Context?, attrs: AttributeSet?) :
    TimePicker(context, attrs) {
    companion object {
        private const val TAG = "CustomTimePicker"
    }

    init {
        var idClass: Class<*>? = null
        var numberPickerClass: Class<*>? = null
        var selectionDividerField: Field? = null
        var hourField: Field? = null
        var minuteField: Field? = null
        var amPmField: Field? = null
        var hourNumberPicker: NumberPicker? = null
        var minuteNumberPicker: NumberPicker? = null
        var amPmNumberPicker: NumberPicker? = null
        try {
            // Create an instance of the id class
            idClass = Class.forName("com.android.internal.R\$id")

            // Get the fields that store the resource IDs for the hour, minute and amPm NumberPickers
            hourField = idClass.getField("hour")
            minuteField = idClass.getField("minute")
            amPmField = idClass.getField("amPm")

            // Use the resource IDs to get references to the hour, minute and amPm NumberPickers
            hourNumberPicker = findViewById<View>(hourField.getInt(null)) as NumberPicker
            minuteNumberPicker = findViewById<View>(minuteField.getInt(null)) as NumberPicker
            amPmNumberPicker = findViewById<View>(amPmField.getInt(null)) as NumberPicker
            numberPickerClass = Class.forName("android.widget.NumberPicker")

            // Set the value of the mSelectionDivider field in the hour, minute and amPm NumberPickers
            // to refer to our custom drawables
            selectionDividerField = numberPickerClass.getDeclaredField("mSelectionDivider")
            selectionDividerField.isAccessible = true
            selectionDividerField[hourNumberPicker] =
                resources.getDrawable(R.drawable.divider_horizontal_bright)
            selectionDividerField[minuteNumberPicker] =
                resources.getDrawable(R.drawable.divider_horizontal_bright)
            selectionDividerField[amPmNumberPicker] =
                resources.getDrawable(R.drawable.divider_horizontal_bright)
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "ClassNotFoundException in CustomTimePicker", e)
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "NoSuchFieldException in CustomTimePicker", e)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "IllegalAccessException in CustomTimePicker", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException in CustomTimePicker", e)
        }
    }
}