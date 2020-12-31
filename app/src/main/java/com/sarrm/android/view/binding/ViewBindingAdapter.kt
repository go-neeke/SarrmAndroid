package com.sarrm.android.view.binding

import android.content.Context
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.databinding.*
import com.sarrm.android.R
import com.sarrm.android.data.models.DateModel
import com.sarrm.android.data.models.RepeatType
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class ViewBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter(value = ["chipList"], requireAll = true)
        fun bindChips(group: ChipGroup, list: MutableList<DateModel>?) {
            group.removeAllViews()
            if (list.isNullOrEmpty()) return

            // 요일 셋팅
            for ((index, item) in list.withIndex()) {
                val inflater: LayoutInflater =
                    group.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val chip = inflater.inflate(R.layout.layout_chip, group, false) as Chip
                chip.id = index
                chip.text = item.date
                chip.isChecked = item.ischecked
                chip.setOnCheckedChangeListener { view, isChecked ->
                    item.ischecked = isChecked
                }

                group.addView(chip)
            }
        }

        @JvmStatic
        @BindingAdapter("app:radioList", "app:onSelectedSpecificDayListener", requireAll = true)
        fun bindRadios(
            group: RadioGroup,
            list: MutableList<RepeatType>?,
            onSelectedSpecificDayListener: CompoundButton.OnCheckedChangeListener
        ) {
            group.removeAllViews()
            if (list.isNullOrEmpty()) return

            // 반복 설정 셋팅
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
                radio.isChecked = item.ischecked
                radio.setOnCheckedChangeListener(onSelectedSpecificDayListener)

                group.addView(radio)
            }
        }
    }

}
