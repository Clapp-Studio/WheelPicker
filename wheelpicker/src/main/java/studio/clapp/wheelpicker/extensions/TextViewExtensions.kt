package studio.clapp.wheelpicker.extensions

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import studio.clapp.wheelpicker.dialog.TimePickerDialog

fun TextView.applyProperties(labelProperties: TimePickerDialog.LabelProperties) {
    setTextColor(ContextCompat.getColor(context, labelProperties.textColorRes))
    textSize = resources.getDimension(labelProperties.textSizeRes)
    if (labelProperties.textFontRes != TimePickerDialog.TEXT_FONT_RES_DEFAULT) {
        typeface = ResourcesCompat.getFont(context, labelProperties.textFontRes)
    }
}
