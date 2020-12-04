package com.android.sarrm.view.bindingAdapters

import androidx.databinding.*
import com.google.android.material.chip.ChipGroup

@InverseBindingMethods(InverseBindingMethod(type = ChipGroup::class, attribute = "app:checkedButton", method = "getCheckedChipId"))
class SarrmBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter("app:checkedButton")
        fun setCheckedChip(view: ChipGroup?, id: Int) {
            if (id != view?.checkedChipId) {
                view?.check(id)
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["app:onCheckedChanged", "app:checkedButtonAttrChanged"], requireAll = false)
        fun setChipsListeners(view: ChipGroup?, listener: ChipGroup.OnCheckedChangeListener?,
                              attrChange: InverseBindingListener?) {
            if (attrChange == null) {
                view?.setOnCheckedChangeListener(listener)
            } else {
                view?.setOnCheckedChangeListener { group, checkedId ->
                    listener?.onCheckedChanged(group, checkedId)
                    attrChange.onChange()
                }
            }
        }
    }
}
