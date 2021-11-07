package studio.clapp.wheelpicker.adapters

import studio.clapp.wheelpicker.WheelPicker

abstract class WheelAdapter {

    var picker: WheelPicker? = null

    /**
     * Update wheel picker.
     */
    fun notifyDataSetChanged() {
        picker?.setAdapter(this)
        picker?.requestLayout()
    }

    open fun getSize(): Int = -1

    open fun getMinValidIndex(): Int? = null

    open fun getMaxValidIndex(): Int? = null

    /**
     * Get the value at the specified position.
     */
    abstract fun getValue(position: Int): String

    /**
     * Get position of the specified value.
     */
    abstract fun getPosition(value: String): Int

    /**
     * Get the text with potential maximum print length for support "WRAP_CONTENT" attribute.
     * If not sure, return empty("") string, in that case "WRAP_CONTENT" will behavior like "MATCH_PARENT".
     */
    abstract fun getTextWithMaximumLength(): String
}
