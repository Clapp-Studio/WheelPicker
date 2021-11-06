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
    private val labelProperties: LabelProperties,
    private val buttonProperties: ButtonProperties,
    private val onPickedListener: ((Int, Int) -> Unit)?,
    private val selectedTime: Pair<String, String>,
    private val labelsTextRes: Pair<Int, Int>
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
                if (titleProperties.textFontRes != TEXT_FONT_RES_DEFAULT) {
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
                        buttonProperties.backgroundColorRes
                    )
                )
                setTextColor(ContextCompat.getColor(context, buttonProperties.textColorRes))
                textSize = resources.getDimension(buttonProperties.textSizeRes)
                if (buttonProperties.textFontRes != TEXT_FONT_RES_DEFAULT) {
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
                        setPadding(
                            measuredWidth / WHEEL_PICKER_TEXT_PADDING_COEFFICIENT,
                            0,
                            0,
                            0
                        )
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
                applyProperties(labelProperties)
                setText(labelsTextRes.first)
            }

            with(dialogTimePickerTvMinute) {
                applyProperties(labelProperties)
                setText(labelsTextRes.second)
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
                        setPadding(
                            0,
                            0,
                            measuredWidth / WHEEL_PICKER_TEXT_PADDING_COEFFICIENT,
                            0
                        )
                        dialogTimePickerTvMinute.setPadding(
                            0,
                            dialogTimePickerTvMinute.textSize.toInt(),
                            ((paddingRight - (minimumWidth * getSelectedTextScale() - minimumWidth)
                                    * MINUTE_TEXT_PADDING_COEFFICIENT).toInt()),
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

        private var labelProperties = LabelProperties()

        private var buttonProperties = ButtonProperties()

        @StyleRes
        private var theme: Int = R.style.BottomSheetDialog_Theme

        private var onPickedListener: ((Int, Int) -> Unit)? = null

        private var selectedTime: Pair<String, String> = Pair("0", "0")

        private var labelsTextRes: Pair<Int, Int> = Pair(R.string.hours, R.string.minutes)

        /**
         * Set user define {@link TitleProperties} for dialog title.
         *
         * @param titleProperties user define title properties.
         */
        fun setTitleProperties(titleProperties: TitleProperties) = apply {
            this.titleProperties = titleProperties
        }

        /**
         * Set user define {@link WheelPickerProperties} for both dialog wheel pickers.
         *
         * @param wheelPickerProperties user define wheel picker properties.
         */
        fun setWheelPickerProperties(wheelPickerProperties: WheelPickerProperties) = apply {
            this.wheelPickerProperties = wheelPickerProperties
        }

        /**
         * Set user define {@link LabelProperties} for both dialog small labels near to wheel pickers.
         *
         * @param labelProperties user define label properties.
         */
        fun setLabelProperties(labelProperties: LabelProperties) = apply {
            this.labelProperties = labelProperties
        }

        /**
         * Set user define {@link ButtonProperties} for dialog button.
         *
         * @param buttonProperties user define wheel picker properties.
         */
        fun setButtonProperties(buttonProperties: ButtonProperties) = apply {
            this.buttonProperties = buttonProperties
        }

        /**
         * Set user define dialog theme.
         *
         * @param theme user define theme.
         */
        fun setTheme(@StyleRes theme: Int) = apply {
            this.theme = theme
        }

        /**
         * Set user define callback to get values of both wheel pickers.
         *
         * <p>First value means hours.
         *
         * <p>Second value means minutes.
         *
         * @param onPickedListener user define callback.
         */
        fun setOnPickedListener(onPickedListener: (Int, Int) -> Unit) = apply {
            this.onPickedListener = onPickedListener
        }

        /**
         * Set user define values of both wheel pickers, that will be shown at start.
         *
         * @param selectedHours user define value of first wheel picker.
         * @param selectedMinutes user define value of second wheel picker.
         */
        fun setSelectedTime(selectedHours: String, selectedMinutes: String) = apply {
            this.selectedTime = Pair(selectedHours, selectedMinutes)
        }

        /**
         * Set user define values of both labels, that will be shown at start.
         *
         * @param hoursRes text string resource of hour label.
         * @param minutesRes text string resource of minute label.
         */
        fun setLabelsTextResources(@StringRes hoursRes: Int, @StringRes minutesRes: Int) = apply {
            this.labelsTextRes = Pair(hoursRes, minutesRes)
        }

        /**
         * Get result of {@link TimePickerDialog.Builder}.
         *
         * @return {@link TimePickerDialog} instance.
         */
        fun build() = TimePickerDialog(
            context,
            theme,
            titleProperties,
            wheelPickerProperties,
            labelProperties,
            buttonProperties,
            onPickedListener,
            selectedTime,
            labelsTextRes
        )
    }

    /**
     * A data class that holds properties of {@link TimePickerDialog}`s title.
     *
     * @param titleRes text string resource.
     * @param textSizeRes text size dimen resource.
     * @param textColorRes text color resource.
     * @param marginTopRes top margin dimen resource.
     * @param textFontRes text font resource.
     */
    data class TitleProperties(
        @StringRes val titleRes: Int = R.string.title,
        @DimenRes val textSizeRes: Int = R.dimen.sp_12,
        @ColorRes val textColorRes: Int = R.color.text_color_time_picker,
        @DimenRes val marginTopRes: Int = R.dimen.dp_24,
        @FontRes val textFontRes: Int = TEXT_FONT_RES_DEFAULT
    )

    /**
     * A data class that holds properties of both {@link TimePickerDialog}`s wheel pickers.
     *
     * @param textSizeRes text size dimen resource.
     * @param selectedTextColorRes text color resource of selected item.
     * @param unselectedTextColorRes text color resource of unselected item.
     * @param marginTopRes top margin dimen resource.
     * @param wheelItemCount how much items will be visible to user.
     * @param selectedTextScale text scale of selected item.
     * @param textFontRes text font resource.
     */
    data class WheelPickerProperties(
        @DimenRes val textSizeRes: Int = R.dimen.sp_42,
        @ColorRes val selectedTextColorRes: Int = R.color.text_color_time_picker,
        @ColorRes val unselectedTextColorRes: Int = R.color.text_color_time_picker,
        @DimenRes val marginTopRes: Int = R.dimen.dp_16,
        val wheelItemCount: Int = WHEEL_ITEM_COUNT_DEFAULT,
        val selectedTextScale: Float = SELECTED_TEXT_SCALE_DEFAULT,
        @FontRes val textFontRes: Int = TEXT_FONT_RES_DEFAULT
    )

    /**
     * A data class that holds properties of both {@link TimePickerDialog}`s labels.
     *
     * @param textSizeRes text size dimen resource.
     * @param textColorRes text color resource.
     * @param textFontRes text font resource.
     */
    data class LabelProperties(
        @DimenRes val textSizeRes: Int = R.dimen.sp_10,
        @ColorRes val textColorRes: Int = R.color.text_color_time_picker,
        @FontRes val textFontRes: Int = TEXT_FONT_RES_DEFAULT
    )

    /**
     * A data class that holds properties of {@link TimePickerDialog}`s button.
     *
     * @param textRes text string resource.
     * @param textSizeRes text size dimen resource.
     * @param textColorRes text color resource.
     * @param cornerRadiusRes corner radius dimen resource.
     * @param paddingTopRes top padding dimen resource.
     * @param paddingBottomRes bottom padding dimen resource.
     * @param marginStartRes start margin dimen resource.
     * @param marginEndRes end margin dimen resource.
     * @param marginTopRes top margin dimen resource.
     * @param marginBottomRes bottom margin dimen resource.
     * @param backgroundColorRes background color resource.
     * @param textFontRes text font resource.
     */
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
        @ColorRes val backgroundColorRes: Int = R.color.button_tint_primary,
        @FontRes val textFontRes: Int = TEXT_FONT_RES_DEFAULT
    )

    companion object {
        private const val WHEEL_ITEM_COUNT_DEFAULT = 5
        private const val SELECTED_TEXT_SCALE_DEFAULT = 1.2f
        private const val WHEEL_PICKER_TEXT_PADDING_COEFFICIENT = 3
        private const val MINUTE_TEXT_PADDING_COEFFICIENT = 1.75f
        const val TEXT_FONT_RES_DEFAULT = 0
    }
}
