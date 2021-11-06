package studio.clapp.wheelpicker.extensions

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import studio.clapp.wheelpicker.dialog.TimePickerDialog

fun TextView.applyProperties(textProperties: TimePickerDialog.TextProperties) {
    setTextColor(ContextCompat.getColor(context, textProperties.textColorRes))
    textSize = resources.getDimension(textProperties.textSizeRes)
    if (textProperties.textFontRes != 0) {
        typeface = ResourcesCompat.getFont(context, textProperties.textFontRes)
    }
}