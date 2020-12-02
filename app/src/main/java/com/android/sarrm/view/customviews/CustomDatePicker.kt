package com.android.sarrm.view.customviews

import android.R
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.NumberPicker
import java.lang.reflect.Field


/**
 * A subclass of [android.widget.DatePicker] that uses
 * reflection to allow for customization of the default blue
 * dividers.
 *
 * @author jjobes
 */
class CustomDatePicker(context: Context?, attrs: AttributeSet?) :
    DatePicker(context, attrs) {
    companion object {
        private const val TAG = "CustomDatePicker"
    }

    init {
        var idClass: Class<*>? = null
        var numberPickerClass: Class<*>? = null
        var selectionDividerField: Field? = null
        var monthField: Field? = null
        var dayField: Field? = null
        var yearField: Field? = null
        var monthNumberPicker: NumberPicker? = null
        var dayNumberPicker: NumberPicker? = null
        var yearNumberPicker: NumberPicker? = null
        try {
            // Create an instance of the id class
            idClass = Class.forName("com.android.internal.R\$id")

            // Get the fields that store the resource IDs for the month, day and year NumberPickers
            monthField = idClass.getField("month")
            dayField = idClass.getField("day")
            yearField = idClass.getField("year")

            // Use the resource IDs to get references to the month, day and year NumberPickers
            monthNumberPicker = findViewById<View>(monthField.getInt(null)) as NumberPicker
            dayNumberPicker = findViewById<View>(dayField.getInt(null)) as NumberPicker
            yearNumberPicker = findViewById<View>(yearField.getInt(null)) as NumberPicker
            numberPickerClass = Class.forName("android.widget.NumberPicker")

            // Set the value of the mSelectionDivider field in the month, day and year NumberPickers
            // to refer to our custom drawables
            selectionDividerField = numberPickerClass.getDeclaredField("mSelectionDivider")
            selectionDividerField.isAccessible = true
            selectionDividerField[monthNumberPicker] =
                resources.getDrawable(R.drawable.divider_horizontal_bright)
            selectionDividerField[dayNumberPicker] =
                resources.getDrawable(R.drawable.divider_horizontal_bright)
            selectionDividerField[yearNumberPicker] =
                resources.getDrawable(R.drawable.divider_horizontal_bright)
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "ClassNotFoundException in CustomDatePicker", e)
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "NoSuchFieldException in CustomDatePicker", e)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "IllegalAccessException in CustomDatePicker", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException in CustomDatePicker", e)
        }
    }
}