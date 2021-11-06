package studio.clapp.wheelpicker.extensions

import android.view.ViewGroup
import studio.clapp.wheelpicker.WheelPicker
import studio.clapp.wheelpicker.dialog.TimePickerDialog

fun WheelPicker.applyProperties(wheelPickerProperties: TimePickerDialog.WheelPickerProperties) {
    setSelectedTextColor(wheelPickerProperties.selectedTextColorRes)
    setUnselectedTextColor(wheelPickerProperties.textColorRes)
    setWheelItemCount(wheelPickerProperties.wheelItemCount)
    setSelectedTextScale(wheelPickerProperties.selectedTextScale)
    setTextSize(wheelPickerProperties.textSize)
    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
        height = getSuggestedMinHeight()
        topMargin = resources.getDimension(wheelPickerProperties.marginTopRes).toInt()
    }
}