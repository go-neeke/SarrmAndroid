package com.android.sarrm.view.binding

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatRadioButton
import android.widget.RadioGroup
import androidx.core.view.marginRight
import androidx.databinding.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.android.sarrm.R
import com.android.sarrm.data.models.DateModel
import com.android.sarrm.data.models.RepeatType
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.orhanobut.logger.Logger

class ViewBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter(value = ["chipList"], requireAll = true)
        fun bindChips(group: ChipGroup, list: MutableList<DateModel>?) {
            group.removeAllViews()
            if (list.isNullOrEmpty()) return
            for ((index, item) in list.withIndex()) {
                val inflater: LayoutInflater = group.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val chip = inflater.inflate(R.layout.layout_chip, group, false) as Chip
                chip.id = index
                chip.text = item.date
                chip.setOnCheckedChangeListener { view, isChecked ->
                    item.ischecked = isChecked
                }

                group.addView(chip)
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["radioList"], requireAll = true)
        fun bindRadios(group: RadioGroup, list: MutableList<RepeatType>?) {
            group.removeAllViews()
            if (list.isNullOrEmpty()) return
            for ((index, item) in list.withIndex()) {
                val radio = AppCompatRadioButton(group.context)
                var params: RadioGroup.LayoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                );

                params.weight = 1.0f

                radio.layoutParams = params
                radio.id = index
                radio.text = item.type
                radio.setOnCheckedChangeListener { view, isChecked ->
                    item.ischecked = isChecked
                }

                group.addView(radio)
            }
        }
    }
}
