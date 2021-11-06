package studio.clapp.wheelpicker.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import studio.clapp.wheelpicker.adapters.HourPickerAdapter
import studio.clapp.wheelpicker.adapters.MinutePickerAdapter
import studio.clapp.wheelpicker.databinding.DialogTimePickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import studio.clapp.wheelpicker.R
import studio.clapp.wheelpicker.extensions.applyProperties

class TimePickerDialog(
    context: Context,
    @StyleRes theme: Int,
    private val titleProperties: TitleProperties,
    private val wheelPickerProperties: WheelPickerProperties,
    private val textProperties: TextProperties,
    private val buttonProperties: ButtonProperties,
    private val onPickedListener: ((Int, Int) -> Unit)?,
    private val selectedTime: Pair<String, String>
) : BottomSheetDialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DialogTimePickerBinding.inflate(layoutInflater)

        val hourPickerAdapter = HourPickerAdapter()
        val minutePickerAdapter = MinutePickerAdapter()

        with(binding) {
            with(dialogTimePickerTvTitle) {
                setText(titleProperties.titleRes)
                setTextColor(ContextCompat.getColor(context, titleProperties.textColorRes))
                textSize = resources.getDimension(titleProperties.textSizeRes)
                if (titleProperties.textFontRes != 0) {
                    typeface = ResourcesCompat.getFont(context, titleProperties.textFontRes)
                }
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = resources.getDimension(titleProperties.marginTopRes).toInt()
                }
            }

            with(dialogTimePickerMbPick) {
                setOnClickListener {
                    onPickedListener?.invoke(
                        dialogTimePickerHourPicker.getCurrentItem().toInt(),
                        dialogTimePickerMinutePicker.getCurrentItem().toInt()
                    )
                    dismiss()
                }
                setText(buttonProperties.textRes)
                setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        buttonProperties.buttonBackgroundColorRes
                    )
                )
                setTextColor(ContextCompat.getColor(context, buttonProperties.textColorRes))
                textSize = resources.getDimension(buttonProperties.textSizeRes)
                if (buttonProperties.textFontRes != 0) {
                    typeface = ResourcesCompat.getFont(context, buttonProperties.textFontRes)
                }
                setPadding(
                    0,
                    resources.getDimension(buttonProperties.paddingTopRes).toInt(),
                    0,
                    resources.getDimension(buttonProperties.paddingBottomRes).toInt()
                )
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin = resources.getDimension(buttonProperties.marginStartRes).toInt()
                    topMargin = resources.getDimension(buttonProperties.marginTopRes).toInt()
                    rightMargin = resources.getDimension(buttonProperties.marginEndRes).toInt()
                    bottomMargin = resources.getDimension(buttonProperties.marginBottomRes).toInt()
                }
                setCornerRadiusResource(buttonProperties.cornerRadiusRes)
            }

            with(dialogTimePickerHourPicker) {
                setAdapter(hourPickerAdapter)
                setOnUpListener { behavior.isDraggable = true }
                setOnDownListener { behavior.isDraggable = false }
                applyProperties(wheelPickerProperties)
                scrollToValue(selectedTime.first)
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
            }

            with(dialogTimePickerTvHour) {
                applyProperties(textProperties)
                setText(textProperties.hoursRes)
            }

            with(dialogTimePickerTvMinute) {
                applyProperties(textProperties)
                setText(textProperties.minutesRes)
            }
            with(dialogTimePickerMinutePicker) {
                setAdapter(minutePickerAdapter)
                setOnUpListener { behavior.isDraggable = true }
                setOnDownListener { behavior.isDraggable = false }
                applyProperties(wheelPickerProperties)
                scrollToValue(selectedTime.second)
                viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        setPadding(0, 0, measuredWidth / 3, 0)
                        dialogTimePickerTvMinute.setPadding(
                            0,
                            dialogTimePickerTvMinute.textSize.toInt(),
                            ((paddingRight - (minimumWidth * getSelectedTextScale() - minimumWidth) * 1.75f).toInt()),
                            0
                        )
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
            setContentView(root)
        }
    }

    class Builder(private val context: Context) {

        private var titleProperties = TitleProperties()

        private var wheelPickerProperties = WheelPickerProperties()

        private var textProperties = TextProperties()

        private var buttonProperties = ButtonProperties()

        @StyleRes
        private var theme: Int = R.style.BottomSheetDialog_Theme

        private var onPickedListener: ((Int, Int) -> Unit)? = null

        private var selectedTime: Pair<String, String> = Pair("0", "0")

        fun setTitleProperties(titleProperties: TitleProperties) = apply {
            this.titleProperties = titleProperties
        }

        fun setWheelPickerProperties(wheelPickerProperties: WheelPickerProperties) = apply {
            this.wheelPickerProperties = wheelPickerProperties
        }

        fun setTextProperties(textProperties: TextProperties) = apply {
            this.textProperties = textProperties
        }

        fun setButtonProperties(buttonProperties: ButtonProperties) = apply {
            this.buttonProperties = buttonProperties
        }

        fun setTheme(@StyleRes theme: Int) = apply {
            this.theme = theme
        }

        fun setOnPickedListener(onPickedListener: (Int, Int) -> Unit) = apply {
            this.onPickedListener = onPickedListener
        }

        fun setSelectedTime(selectedHours: String, selectedMinutes: String) = apply {
            this.selectedTime = Pair(selectedHours, selectedMinutes)
        }

        fun build() = TimePickerDialog(
            context,
            theme,
            titleProperties,
            wheelPickerProperties,
            textProperties,
            buttonProperties,
            onPickedListener,
            selectedTime
        )
    }

    data class TitleProperties(
        @StringRes val titleRes: Int = R.string.title,
        @DimenRes val textSizeRes: Int = R.dimen.sp_12,
        @ColorRes val textColorRes: Int = R.color.text_color_time_picker,
        @DimenRes val marginTopRes: Int = R.dimen.dp_24,
        @FontRes val textFontRes: Int = 0
    )

    data class WheelPickerProperties(
        @DimenRes val textSize: Int = R.dimen.sp_42,
        @ColorRes val selectedTextColorRes: Int = R.color.text_color_time_picker,
        @ColorRes val textColorRes: Int = R.color.text_color_time_picker,
        @DimenRes val marginTopRes: Int = R.dimen.dp_16,
        val wheelItemCount: Int = 5,
        val selectedTextScale: Float = 1.2f,
        @FontRes val textFontRes: Int = 0
    )

    data class TextProperties(
        @StringRes val hoursRes: Int = R.string.hours,
        @StringRes val minutesRes: Int = R.string.minutes,
        @DimenRes val textSizeRes: Int = R.dimen.sp_10,
        @ColorRes val textColorRes: Int = R.color.text_color_time_picker,
        @FontRes val textFontRes: Int = 0
    )

    data class ButtonProperties(
        @StringRes val textRes: Int = R.string.pick,
        @DimenRes val textSizeRes: Int = R.dimen.sp_12,
        @ColorRes val textColorRes: Int = R.color.text_color_button,
        @DimenRes val cornerRadiusRes: Int = R.dimen.dp_8,
        @DimenRes val paddingTopRes: Int = R.dimen.dp_16,
        @DimenRes val paddingBottomRes: Int = R.dimen.dp_16,
        @DimenRes val marginStartRes: Int = R.dimen.dp_16,
        @DimenRes val marginEndRes: Int = R.dimen.dp_16,
        @DimenRes val marginTopRes: Int = R.dimen.dp_32,
        @DimenRes val marginBottomRes: Int = R.dimen.sp_24,
        @FontRes val textFontRes: Int = 0,
        @ColorRes val buttonBackgroundColorRes: Int = R.color.button_tint_primary
    )
}