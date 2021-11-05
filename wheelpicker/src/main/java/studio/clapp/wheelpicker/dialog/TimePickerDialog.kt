package studio.clapp.wheelpicker.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import studio.clapp.wheelpicker.adapters.HourPickerAdapter
import studio.clapp.wheelpicker.adapters.MinutePickerAdapter
import studio.clapp.wheelpicker.databinding.DialogTimePickerBinding
import com.airbnb.paris.extensions.style
import com.google.android.material.bottomsheet.BottomSheetDialog
import studio.clapp.wheelpicker.R

class TimePickerDialog(
    context: Context,
    @StyleRes theme: Int,
    private val title: String,
    private val onPickedListener: ((Int, Int) -> Unit)?,
    @ColorRes private val buttonBackgroundColor: Int,
    @ColorRes private val buttonTextColor: Int,
    @StyleRes private val textStyle: Int,
    @StyleRes private val titleStyle: Int,
    private val selectedTime: Pair<Int, Int>,
    private val buttonText: String
) : BottomSheetDialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DialogTimePickerBinding.inflate(layoutInflater)

        val hourPickerAdapter = HourPickerAdapter()
        val minutePickerAdapter = MinutePickerAdapter()

        with(binding) {
            with(dialogTimePickerTvTitle) {
                text = title
                style(titleStyle)
            }
            with(dialogTimePickerMbPick) {
                setOnClickListener {
                    onPickedListener?.invoke(
                        dialogTimePickerHourPicker.getCurrentItem().toInt(),
                        dialogTimePickerMinutePicker.getCurrentItem().toInt()
                    )
                    dismiss()
                }
                text = buttonText
                setBackgroundColor(ContextCompat.getColor(context, buttonBackgroundColor))
                setTextColor(ContextCompat.getColor(context, buttonTextColor))
            }

            with(dialogTimePickerHourPicker) {
                setAdapter(hourPickerAdapter)
                setOnUpListener { behavior.isDraggable = true }
                setOnDownListener { behavior.isDraggable = false }
                scrollToValue(selectedTime.first.toString())
                viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        setPadding(measuredWidth / 3, 0, 0, 0)
                        dialogTimePickerTvHour.setPadding(
                            (paddingLeft + minimumWidth * getSelectedTextScale()).toInt(),
                            dialogTimePickerTvHour.textSize.toInt(),
                            0,
                            0
                        )
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })

                dialogTimePickerTvHour.style(textStyle)
            }

            with(dialogTimePickerMinutePicker) {
                setAdapter(minutePickerAdapter)
                setOnUpListener { behavior.isDraggable = true }
                setOnDownListener { behavior.isDraggable = false }
                scrollToValue(selectedTime.second.toString())
                viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        setPadding(0, 0, measuredWidth / 3, 0)
                        dialogTimePickerTvMinute.setPadding(
                            0,
                            dialogTimePickerTvMinute.textSize.toInt(),
                            ((paddingRight - (minimumWidth * getSelectedTextScale() - minimumWidth) * 1.5).toInt()),
                            0
                        )
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
                dialogTimePickerTvMinute.style(textStyle)
            }
            setContentView(root)
        }
    }

    class Builder(private val context: Context) {

        private var title: String = context.getString(R.string.title)

        private var buttonText: String = context.getString(R.string.pick)

        @StyleRes
        private var theme: Int = R.style.BottomSheetDialog_Theme

        @ColorRes
        private var buttonBackgroundColor: Int = R.color.button_tint_primary

        @ColorRes
        private var buttonTextColor: Int = R.color.text_color_button

        @StyleRes
        private var textStyle: Int = R.style.TimePicker_Text

        @StyleRes
        private var titleStyle: Int = R.style.TimePicker_Title

        private var onPickedListener: ((Int, Int) -> Unit)? = null

        private var selectedTime: Pair<Int, Int> = Pair(0, 0)

        fun setTitle(title: String) = apply {
            this.title = title
        }

        fun setTitle(@StringRes titleRes: Int) = apply {
            this.title = context.getString(titleRes)
        }

        fun setButtonText(buttonText: String) = apply {
            this.buttonText = buttonText
        }

        fun setButtonText(@StringRes buttonTextRes: Int) = apply {
            this.buttonText = context.getString(buttonTextRes)
        }

        fun setTheme(@StyleRes theme: Int) = apply {
            this.theme = theme
        }

        fun setButtonBackgroundColor(@ColorRes buttonBackgroundColor: Int) = apply {
            this.buttonBackgroundColor = buttonBackgroundColor
        }

        fun setButtonTextColor(@ColorRes buttonTextColor: Int) = apply {
            this.buttonTextColor = buttonTextColor
        }

        fun setTextStyle(@StyleRes textTheme: Int) = apply {
            this.textStyle = textTheme
        }

        fun setTitleStyle(@StyleRes titleStyle: Int) = apply {
            this.titleStyle = titleStyle
        }

        fun setOnPickedListener(onPickedListener: (Int, Int) -> Unit) = apply {
            this.onPickedListener = onPickedListener
        }

        fun setSelectedTime(selectedHours: Int, selectedMinutes: Int) = apply {
            this.selectedTime = Pair(selectedHours, selectedMinutes)
        }

        fun build() = TimePickerDialog(
            context,
            theme,
            title,
            onPickedListener,
            buttonBackgroundColor,
            buttonTextColor,
            textStyle,
            titleStyle,
            selectedTime,
            buttonText
        )
    }
}