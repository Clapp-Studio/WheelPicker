package studio.clapp.wheelpicker.adapters

import studio.clapp.wheelpicker.extensions.formatLeadingZero
import kotlin.math.abs

class HourPickerAdapter : WheelAdapter() {

    override fun getValue(position: Int): String =
        if (position >= 0) (abs(position) % HOURS).formatLeadingZero()
        else (HOURS - 1 - abs(position + 1) % HOURS).formatLeadingZero()

    override fun getPosition(value: String): Int = value.toInt()

    override fun getTextWithMaximumLength(): String = HOURS.toString()

    private companion object {
        const val HOURS = 24
    }
}
