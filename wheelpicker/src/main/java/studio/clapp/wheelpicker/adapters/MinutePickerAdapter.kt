package studio.clapp.wheelpicker.adapters

import studio.clapp.wheelpicker.extensions.checkSixtyMinutes
import studio.clapp.wheelpicker.extensions.formatLeadingZero
import kotlin.math.abs

class MinutePickerAdapter : WheelAdapter() {

    override fun getValue(position: Int): String =
        if (position >= 0) (abs(position) % FIVE_MINUTES * MINUTES / FIVE_MINUTES).formatLeadingZero() else (MINUTES - abs(
            position
        ) % FIVE_MINUTES * MINUTES / FIVE_MINUTES).checkSixtyMinutes().formatLeadingZero()

    override fun getPosition(value: String): Int = value.toInt() * MINUTES / FIVE_MINUTES

    override fun getTextWithMaximumLength(): String = MINUTES.toString()

    private companion object {
        const val MINUTES = 60
        const val FIVE_MINUTES = 12
    }
}
