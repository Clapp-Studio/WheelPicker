package studio.clapp.wheelpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import studio.clapp.wheelpicker.adapters.WheelAdapter
import java.util.*
import kotlin.math.abs


interface OnValueChangeListener {
    fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String)
}

class WheelPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mSelectorItemCount: Int
    private var mSelectorVisibleItemCount: Int
    private var mMinIndex: Int
    private var mMaxIndex: Int
    private var mMaxValidIndex: Int? = null
    private var mMinValidIndex: Int? = null

    private var mWheelMiddleItemIndex: Int
    private var mWheelVisibleItemMiddleIndex: Int
    private var mSelectorItemIndices: ArrayList<Int>
    private var mSelectorItemValidStatus: ArrayList<Boolean>
    private var mCurSelectedItemIndex = 0
    private var mWrapSelectorWheelPreferred: Boolean

    private var mTextPaint: Paint = Paint()
    private var mSelectedTextColor: Int
    private var mUnSelectedTextColor: Int
    private var mTextSize: Int
    private var mTextAlign: String

    private var mOverScroller: OverScroller? = null
    private var mVelocityTracker: VelocityTracker? = null
    private val mTouchSlop: Int
    private val mMaximumVelocity: Int
    private val mMinimumVelocity: Int
    private var mLastY: Float = 0f
    private var mIsDragging: Boolean = false
    private var mCurrentFirstItemOffset: Int = 0
    private var mInitialFirstItemOffset = Int.MIN_VALUE
    private var mTextGapHeight: Int = 0
    private var mItemHeight: Int = 0
    private var mTextHeight: Int = 0
    private var mPreviousScrollerY: Int = 0
    private var mOnValueChangeListener: OnValueChangeListener? = null
    private var mAdapter: WheelAdapter? = null
    private var mFadingEdgeEnabled = true
    private var mSelectedTextScale = 0.3f
    private lateinit var touchDownListener: () -> Unit
    private lateinit var touchUpListener: () -> Unit

    private enum class TextAlign {
        LEFT, CENTER, RIGHT
    }

    init {
        val attributesArray =
            context.obtainStyledAttributes(attrs, R.styleable.WheelPicker, defStyleAttr, 0)

        mSelectorItemCount =
            attributesArray.getInt(R.styleable.WheelPicker_wheelItemCount, DEFAULT_ITEM_COUNT) + 2
        mWheelMiddleItemIndex = (mSelectorItemCount - 1) / 2
        mSelectorVisibleItemCount = mSelectorItemCount - 2
        mWheelVisibleItemMiddleIndex = (mSelectorVisibleItemCount - 1) / 2
        mSelectorItemIndices = ArrayList(mSelectorItemCount)
        mSelectorItemValidStatus = ArrayList(mSelectorItemCount)

        mMinIndex = attributesArray.getInt(R.styleable.WheelPicker_min, Integer.MIN_VALUE)
        mMaxIndex = attributesArray.getInt(R.styleable.WheelPicker_max, Integer.MAX_VALUE)
        if (attributesArray.hasValue(R.styleable.WheelPicker_maxValidIndex))
            mMaxValidIndex = attributesArray.getInt(R.styleable.WheelPicker_maxValidIndex, 0)
        if (attributesArray.hasValue(R.styleable.WheelPicker_minValidIndex))
            mMinValidIndex = attributesArray.getInt(R.styleable.WheelPicker_minValidIndex, 0)
        mWrapSelectorWheelPreferred =
            attributesArray.getBoolean(R.styleable.WheelPicker_wrapSelectorWheel, false)
        mSelectedTextScale =
            attributesArray.getFloat(R.styleable.WheelPicker_selectedTextScale, 0.3f)

        mOverScroller = OverScroller(context, DecelerateInterpolator(2.5f))
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity =
            configuration.scaledMaximumFlingVelocity / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity

        mSelectedTextColor = attributesArray.getColor(
            R.styleable.WheelPicker_selectedTextColor,
            ContextCompat.getColor(context, R.color.text_color_time_picker)
        )
        mUnSelectedTextColor = attributesArray.getColor(
            R.styleable.WheelPicker_textColor,
            ContextCompat.getColor(context, R.color.text_color_time_picker)
        )
        mTextSize = attributesArray.getDimensionPixelSize(
            R.styleable.WheelPicker_textSize,
            DEFAULT_TEXT_SIZE
        )
        val textAlignInt = attributesArray.getInt(R.styleable.WheelPicker_align, 1)
        val textAlignArray = TextAlign.values()
        mTextAlign = textAlignArray[textAlignInt % textAlignArray.size].toString()
        mFadingEdgeEnabled =
            attributesArray.getBoolean(R.styleable.WheelPicker_fadingEdgeEnabled, true)

        mTextPaint.run {
            isAntiAlias = true
            isAntiAlias = true
            textSize = mTextSize.toFloat()
            textAlign = Paint.Align.valueOf(mTextAlign)
            style = Paint.Style.FILL_AND_STROKE
            val typefaceResId =
                attributesArray.getResourceId(R.styleable.WheelPicker_android_fontFamily, 0)
            if (typefaceResId != 0) {
                this.typeface = ResourcesCompat.getFont(context, typefaceResId)
            }
        }

        attributesArray.recycle()

        initializeSelectorWheelIndices()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try greedily to fit the max width and height.
        var lp: ViewGroup.LayoutParams? = layoutParams
        if (lp == null)
            lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        var width = calculateSize(suggestedMinimumWidth, lp.width, widthMeasureSpec)
        var height = calculateSize(suggestedMinimumHeight, lp.height, heightMeasureSpec)

        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        setMeasuredDimension(width, height)
    }

    override fun getSuggestedMinimumWidth(): Int {
        var suggested = super.getSuggestedMinimumHeight()
        if (mSelectorVisibleItemCount > 0) {
            suggested = suggested.coerceAtLeast(computeMaximumWidth())
        }
        return suggested
    }

    override fun getSuggestedMinimumHeight(): Int {
        var suggested = super.getSuggestedMinimumWidth()
        if (mSelectorVisibleItemCount > 0) {
            val fontMetricsInt = mTextPaint.fontMetricsInt
            val height = fontMetricsInt.descent - fontMetricsInt.ascent
            suggested = suggested.coerceAtLeast(height * mSelectorVisibleItemCount)
        }
        return suggested
    }

    override fun getMinimumWidth(): Int = suggestedMinimumWidth

    override fun getBottomFadingEdgeStrength(): Float = TOP_AND_BOTTOM_FADING_EDGE_STRENGTH

    override fun getTopFadingEdgeStrength(): Float = TOP_AND_BOTTOM_FADING_EDGE_STRENGTH

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVertical(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        onTouchEventVertical(event)
        return true
    }

    override fun scrollBy(x: Int, y: Int) {
        if (y == 0)
            return

        val gap = mTextGapHeight

        if (!mWrapSelectorWheelPreferred && y > 0
            && (mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinIndex
                    || (mMinValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinValidIndex!!))
        ) {
            if (mCurrentFirstItemOffset + y - mInitialFirstItemOffset < gap / 2)
                mCurrentFirstItemOffset += y
            else {
                mCurrentFirstItemOffset = mInitialFirstItemOffset + (gap / 2)
                if (!mOverScroller!!.isFinished && !mIsDragging) {
                    mOverScroller!!.abortAnimation()
                }
            }
            return
        }

        if (!mWrapSelectorWheelPreferred && y < 0
            && (mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxIndex
                    || (mMaxValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxValidIndex!!))
        ) {
            if (mCurrentFirstItemOffset + y - mInitialFirstItemOffset > -(gap / 2))
                mCurrentFirstItemOffset += y
            else {
                mCurrentFirstItemOffset = mInitialFirstItemOffset - (gap / 2)
                if (!mOverScroller!!.isFinished && !mIsDragging) {
                    mOverScroller!!.abortAnimation()
                }
            }
            return
        }

        mCurrentFirstItemOffset += y

        while (mCurrentFirstItemOffset - mInitialFirstItemOffset < -gap) {
            mCurrentFirstItemOffset += mItemHeight
            increaseSelectorsIndex()
            if (!mWrapSelectorWheelPreferred
                && (mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxIndex
                        || (mMaxValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxValidIndex!!))
            ) {
                mCurrentFirstItemOffset = mInitialFirstItemOffset
            }
        }

        while (mCurrentFirstItemOffset - mInitialFirstItemOffset > gap) {
            mCurrentFirstItemOffset -= mItemHeight
            decreaseSelectorsIndex()
            if (!mWrapSelectorWheelPreferred
                && (mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinIndex
                        || (mMinValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinValidIndex!!))
            ) {
                mCurrentFirstItemOffset = mInitialFirstItemOffset
            }
        }
        onSelectionChanged(mSelectorItemIndices[mWheelMiddleItemIndex])
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mOverScroller!!.computeScrollOffset()) {
            val x = mOverScroller!!.currX
            val y = mOverScroller!!.currY


            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = mOverScroller!!.startY
            }
            scrollBy(x, y - mPreviousScrollerY)
            mPreviousScrollerY = y
            invalidate()
        } else {
            if (!mIsDragging)
            //align item
                adjustItemVertical()
        }
    }

    fun getSuggestedMinHeight(): Int = suggestedMinimumHeight

    fun getSelectedTextScale(): Float = mSelectedTextScale

    fun setSelectedTextScale(mSelectedTextScale: Float) {
        this.mSelectedTextScale = mSelectedTextScale
    }

    fun setTextSize(@DimenRes mTextSizeRes: Int) {
        this.mTextSize = context.resources.getDimension(mTextSizeRes).toInt()
    }

    fun setOnUpListener(touchUpListener: () -> Unit) {
        this.touchUpListener = touchUpListener
    }

    fun setOnDownListener(touchDownListener: () -> Unit) {
        this.touchDownListener = touchDownListener
    }

    fun scrollTo(position: Int) {
        if (mCurSelectedItemIndex == position)
            return

        mCurSelectedItemIndex = position
        mSelectorItemIndices.clear()
        for (i in 0 until mSelectorItemCount) {
            var selectorIndex = mCurSelectedItemIndex + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheelPreferred) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            mSelectorItemIndices.add(selectorIndex)
        }

        invalidate()
    }

    fun setOnValueChangedListener(onValueChangeListener: OnValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener
    }

    fun smoothScrollTo(position: Int) {
        val realPosition = validatePosition(position)
        changeValueBySteps(realPosition - mCurSelectedItemIndex)
    }

    fun smoothScrollToValue(value: String) = smoothScrollTo(getPosition(value))

    fun scrollToValue(value: String) = scrollTo(getPosition(value))

    fun setUnselectedTextColor(@ColorRes colorId: Int) {
        mUnSelectedTextColor = ContextCompat.getColor(context, colorId)
        invalidate()
    }

    /**
     * Set user define adapter
     *
     * @adapter user define adapter
     * @indexRangeBasedOnAdapterSize specific if the picker's min~max range is based on adapter's size
     */
    fun setAdapter(adapter: WheelAdapter?, indexRangeBasedOnAdapterSize: Boolean = true) {
        mAdapter = adapter
        if (mAdapter == null) {
            initializeSelectorWheelIndices()
            invalidate()
            return
        }

        if (adapter!!.getSize() != -1 && indexRangeBasedOnAdapterSize) {
            mMaxIndex = adapter.getSize() - 1
            mMinIndex = 0
        }

        mMaxValidIndex = adapter.getMaxValidIndex()
        mMinValidIndex = adapter.getMinValidIndex()

        initializeSelectorWheelIndices()
        invalidate()

        mAdapter?.picker = this
    }

    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the {@link NumberPicker#getMinValue()} and
     * {@link NumberPicker#getMaxValue()} values.
     * <p>
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     * </p>
     * <p>
     * <strong>Note:</strong> If the number of items, i.e. the range (
     * {@link #getMaxValue()} - {@link #getMinValue()}) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     * </p>
     *
     * @param wrap Whether to wrap.
     */
    fun setWrapSelectorWheel(wrap: Boolean) {
        mWrapSelectorWheelPreferred = wrap
        invalidate()
    }

    /**
     * Gets whether the selector wheel wraps when reaching the min/max value.
     *
     * @return True if the selector wheel wraps.
     *
     * @see .getMinValue
     * @see .getMaxValue
     */
    fun getWrapSelectorWheel(): Boolean = mWrapSelectorWheelPreferred

    /**
     * Set how many visible item show in the picker
     */
    fun setWheelItemCount(count: Int) {
        mSelectorItemCount = count + 2
        mWheelMiddleItemIndex = (mSelectorItemCount - 1) / 2
        mSelectorVisibleItemCount = mSelectorItemCount - 2
        mWheelVisibleItemMiddleIndex = (mSelectorVisibleItemCount - 1) / 2
        mSelectorItemIndices = ArrayList(mSelectorItemCount)
        mSelectorItemValidStatus = ArrayList(mSelectorItemCount)
        reset()
        invalidate()
    }

    /**
     * Set color for current selected item
     */
    fun setSelectedTextColor(@ColorRes colorId: Int) {
        mSelectedTextColor = ContextCompat.getColor(context, colorId)
        invalidate()
    }

    fun getValue(position: Int): String = when {
        mAdapter != null -> mAdapter!!.getValue(position)
        else -> if (!mWrapSelectorWheelPreferred) {
            when {
                position > mMaxIndex -> ""
                position < mMinIndex -> ""
                else -> position.toString()
            }
        } else {
            getWrappedSelectorIndex(position).toString()
        }
    }

    fun setValue(value: String) = scrollToValue(value)

    fun setMaxValue(max: Int) {
        mMaxIndex = max
    }

    fun getMaxValue(): String = if (mAdapter != null) {
        mAdapter!!.getValue(mMaxIndex)
    } else {
        mMaxIndex.toString()
    }

    fun setMinValue(min: Int) {
        mMinIndex = min
    }

    fun setMinValidValue(minValid: Int?) {
        mMinValidIndex = minValid
    }

    fun setMaxValidValue(maxValid: Int?) {
        mMaxValidIndex = maxValid
    }

    fun getMinValue(): String = if (mAdapter != null) {
        mAdapter!!.getValue(mMinIndex)
    } else {
        mMinIndex.toString()
    }

    fun reset() {
        initializeSelectorWheelIndices()
        initializeSelectorWheel()
        invalidate()
    }

    fun getCurrentItem(): String = getValue(mCurSelectedItemIndex)

    fun getCurrentIndex(): Int = mCurSelectedItemIndex

    private fun isValidPosition(position: Int): Boolean = when {
        mMinValidIndex != null && position < mMinValidIndex!! -> false
        mMaxValidIndex != null && position > mMaxValidIndex!! -> false
        else -> true
    }

    private fun adjustItemVertical() {
        mPreviousScrollerY = 0
        var deltaY = mInitialFirstItemOffset - mCurrentFirstItemOffset

        if (abs(deltaY) > mItemHeight / 2) {
            deltaY += if (deltaY > 0)
                -mItemHeight
            else
                mItemHeight
        }

        if (deltaY != 0) {
            mOverScroller!!.startScroll(scrollX, scrollY, 0, deltaY, 800)
            postInvalidateOnAnimation()
        }
    }

    private fun recyclerVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun getItemHeight(): Int = height / (mSelectorItemCount - 2)

    private fun getGapHeight(): Int = getItemHeight() - computeTextHeight()

    private fun computeTextHeight(): Int {
        val metricsInt = mTextPaint.fontMetricsInt
        return abs(metricsInt.bottom + metricsInt.top)
    }

    private fun drawVertical(canvas: Canvas) {
        if (mSelectorItemIndices.size == 0)
            return
        val itemHeight = getItemHeight()

        val x = when (mTextPaint.textAlign) {
            Paint.Align.LEFT -> paddingLeft.toFloat()
            Paint.Align.CENTER -> ((right - left) / 2).toFloat()
            Paint.Align.RIGHT -> (right - left).toFloat() - paddingRight.toFloat()
            else -> ((right - left) / 2).toFloat()
        }

        var y = mCurrentFirstItemOffset.toFloat()

        var i = 0

        val topIndexDiffToMid = mWheelVisibleItemMiddleIndex
        val bottomIndexDiffToMid = mSelectorVisibleItemCount - mWheelVisibleItemMiddleIndex - 1
        val maxIndexDiffToMid = topIndexDiffToMid.coerceAtLeast(bottomIndexDiffToMid)

        while (i < mSelectorItemIndices.size) {
            var scale: Float

            val offsetToMiddle =
                abs(y - (mInitialFirstItemOffset + mWheelMiddleItemIndex * itemHeight).toFloat())

            scale = if (abs(itemHeight * maxIndexDiffToMid - offsetToMiddle) == 100f) {
                mSelectedTextScale * (itemHeight / 1.01f * maxIndexDiffToMid - offsetToMiddle) / (itemHeight * maxIndexDiffToMid) + 1
            } else {
                mSelectedTextScale * (itemHeight / ((100 + abs(itemHeight * maxIndexDiffToMid - offsetToMiddle) / 100f) / 100) * maxIndexDiffToMid - offsetToMiddle) / (itemHeight * maxIndexDiffToMid) + 1
            }
            scale /= 1.5f
            if (mSelectorItemValidStatus[i]) {
                if (offsetToMiddle < mItemHeight / 2) {
                    mTextPaint.color = mSelectedTextColor
                } else {
                    mTextPaint.color = mUnSelectedTextColor
                }
            } else {
                mTextPaint.color = ContextCompat.getColor(context, R.color.material_grey_300)
            }
            canvas.save()
            canvas.scale(scale, scale, x, y)
            canvas.drawText(getValue(mSelectorItemIndices[i]), x, y, mTextPaint)
            canvas.restore()

            y += itemHeight
            i++
        }
    }

    private fun getPosition(value: String): Int = when {
        mAdapter != null -> {
            validatePosition(mAdapter!!.getPosition(value))
        }
        else -> try {
            val position = value.toInt()
            validatePosition(position)
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun increaseSelectorsIndex() {
        for (i in 0 until (mSelectorItemIndices.size - 1)) {
            mSelectorItemIndices[i] = mSelectorItemIndices[i + 1]
            mSelectorItemValidStatus[i] = mSelectorItemValidStatus[i + 1]
        }
        var nextScrollSelectorIndex = mSelectorItemIndices[mSelectorItemIndices.size - 2] + 1
        if (mWrapSelectorWheelPreferred && nextScrollSelectorIndex > mMaxIndex) {
            nextScrollSelectorIndex = mMinIndex
        }
        mSelectorItemIndices[mSelectorItemIndices.size - 1] = nextScrollSelectorIndex
        mSelectorItemValidStatus[mSelectorItemIndices.size - 1] =
            isValidPosition(nextScrollSelectorIndex)
    }

    private fun decreaseSelectorsIndex() {
        for (i in mSelectorItemIndices.size - 1 downTo 1) {
            mSelectorItemIndices[i] = mSelectorItemIndices[i - 1]
            mSelectorItemValidStatus[i] = mSelectorItemValidStatus[i - 1]
        }
        var nextScrollSelectorIndex = mSelectorItemIndices[1] - 1
        if (mWrapSelectorWheelPreferred && nextScrollSelectorIndex < mMinIndex) {
            nextScrollSelectorIndex = mMaxIndex
        }
        mSelectorItemIndices[0] = nextScrollSelectorIndex
        mSelectorItemValidStatus[0] = isValidPosition(nextScrollSelectorIndex)
    }

    private fun changeValueBySteps(steps: Int) {
        mPreviousScrollerY = 0
        mOverScroller!!.startScroll(0, 0, 0, -mItemHeight * steps, SNAP_SCROLL_DURATION)
        invalidate()
    }

    private fun onSelectionChanged(current: Int) {
        val previous = mCurSelectedItemIndex
        mCurSelectedItemIndex = current
        if (previous != current) {
            notifyChange(previous, current)
        }
    }

    private fun getWrappedSelectorIndex(selectorIndex: Int): Int = when {
        selectorIndex > mMaxIndex -> {
            mMinIndex + (selectorIndex - mMaxIndex) % (mMaxIndex - mMinIndex + 1) - 1
        }
        selectorIndex < mMinIndex -> {
            mMaxIndex - (mMinIndex - selectorIndex) % (mMaxIndex - mMinIndex + 1) + 1
        }
        else -> {
            selectorIndex
        }
    }

    private fun notifyChange(previous: Int, current: Int) =
        mOnValueChangeListener?.onValueChange(this, getValue(previous), getValue(current))

    private fun validatePosition(position: Int): Int = if (!mWrapSelectorWheelPreferred) {
        when {
            mMaxValidIndex == null && position > mMaxIndex -> mMaxIndex
            mMaxValidIndex != null && position > mMaxValidIndex!! -> mMaxValidIndex!!
            mMinValidIndex == null && position < mMinIndex -> mMinIndex
            mMinValidIndex != null && position < mMinValidIndex!! -> mMinValidIndex!!
            else -> position
        }
    } else {
        getWrappedSelectorIndex(position)
    }

    private fun onTouchEventVertical(event: MotionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }

        mVelocityTracker?.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (this::touchDownListener.isInitialized) {
                    touchDownListener()
                }
                if (!mOverScroller!!.isFinished)
                    mOverScroller!!.forceFinished(true)

                mLastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaY = event.y - mLastY
                if (!mIsDragging && abs(deltaY) > mTouchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)

                    if (deltaY > 0) {
                        deltaY -= mTouchSlop
                    } else {
                        deltaY += mTouchSlop
                    }
                    mIsDragging = true
                }

                if (mIsDragging) {
                    scrollBy(0, deltaY.toInt())
                    invalidate()
                    mLastY = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (this::touchUpListener.isInitialized) {
                    touchUpListener()
                }
                if (mIsDragging) {
                    mIsDragging = false
                    parent?.requestDisallowInterceptTouchEvent(false)

                    mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val velocity = mVelocityTracker?.yVelocity?.toInt() ?: 0

                    mPreviousScrollerY = 0
                    mOverScroller?.fling(
                        scrollX,
                        scrollY,
                        0,
                        velocity,
                        0,
                        0,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE,
                        0,
                        (getItemHeight() * 0.7).toInt()
                    )
                    postInvalidateOnAnimation()
                    recyclerVelocityTracker()
                } else {
                    //click event
                    val y = event.y.toInt()
                    handlerClickVertical(y)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    mIsDragging = false
                }
                recyclerVelocityTracker()
            }
        }
    }

    private fun handlerClickVertical(y: Int) {
        val selectorIndexOffset = y / mItemHeight - mWheelVisibleItemMiddleIndex
        changeValueBySteps(selectorIndexOffset)
    }

    private fun computeMaximumWidth(): Int {
        mTextPaint.textSize = mTextSize * 1.3f
        if (mAdapter != null) {
            return if (mAdapter!!.getTextWithMaximumLength().isNotEmpty()) {
                val suggestedWith =
                    mTextPaint.measureText(mAdapter!!.getTextWithMaximumLength()).toInt()
                mTextPaint.textSize = mTextSize.toFloat()
                suggestedWith
            } else {
                val suggestedWith = mTextPaint.measureText("00").toInt()
                mTextPaint.textSize = mTextSize.toFloat()
                suggestedWith
            }
        }
        val widthForMinIndex = mTextPaint.measureText(mMinIndex.toString()).toInt()
        val widthForMaxIndex = mTextPaint.measureText(mMaxIndex.toString()).toInt()
        mTextPaint.textSize = mTextSize * 1.0f
        return if (widthForMinIndex > widthForMaxIndex)
            widthForMinIndex
        else
            widthForMaxIndex
    }

    private fun calculateSize(suggestedSize: Int, paramSize: Int, measureSpec: Int): Int {
        var result = 0
        val size = MeasureSpec.getSize(measureSpec)
        val mode = MeasureSpec.getMode(measureSpec)

        when (MeasureSpec.getMode(mode)) {
            MeasureSpec.AT_MOST ->
                result = when (paramSize) {
                    ViewGroup.LayoutParams.WRAP_CONTENT -> suggestedSize.coerceAtMost(size)
                    ViewGroup.LayoutParams.MATCH_PARENT -> size
                    else -> {
                        paramSize.coerceAtMost(size)
                    }
                }
            MeasureSpec.EXACTLY -> result = size
            MeasureSpec.UNSPECIFIED ->
                result =
                    if (paramSize == ViewGroup.LayoutParams.WRAP_CONTENT || paramSize == ViewGroup.LayoutParams
                            .MATCH_PARENT
                    )
                        suggestedSize
                    else {
                        paramSize
                    }
        }

        return result
    }

    private fun initializeSelectorWheel() {
        mItemHeight = getItemHeight()
        mTextHeight = computeTextHeight()
        mTextGapHeight = getGapHeight()

        val visibleMiddleItemPos =
            mItemHeight * mWheelVisibleItemMiddleIndex + (mItemHeight + mTextHeight) / 2
        mInitialFirstItemOffset = visibleMiddleItemPos - mItemHeight * mWheelMiddleItemIndex
        mCurrentFirstItemOffset = mInitialFirstItemOffset
    }

    private fun initializeFadingEdges() {
        isVerticalFadingEdgeEnabled = mFadingEdgeEnabled
        if (mFadingEdgeEnabled)
            setFadingEdgeLength((bottom - top - mTextSize) / 2)
    }

    private fun initializeSelectorWheelIndices() {
        mSelectorItemIndices.clear()
        mSelectorItemValidStatus.clear()

        mCurSelectedItemIndex = if (mMinValidIndex == null || mMinValidIndex!! < mMinIndex) {
            if (mMinIndex <= 0) {
                0
            } else {
                mMinIndex
            }
        } else {
            if (mMinValidIndex!! <= 0) {
                0
            } else {
                mMinValidIndex!!
            }
        }

        for (i in 0 until mSelectorItemCount) {
            var selectorIndex = mCurSelectedItemIndex + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheelPreferred) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            mSelectorItemIndices.add(selectorIndex)
            mSelectorItemValidStatus.add(isValidPosition(selectorIndex))
        }
    }

    private companion object {
        const val TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f
        const val SNAP_SCROLL_DURATION = 300
        const val SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 4
        const val DEFAULT_ITEM_COUNT = 3
        const val DEFAULT_TEXT_SIZE = 80
    }
}
