package com.hearthappy.uiwidget.indicator

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.dp2px
/**
 * Created Date: 2025/3/21
 * @author ChenRui
 * ClassDescription：指示器控件
 */
class IndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var indicatorWidth = 4f
    private var indicatorHeight = 4f
    private var selectedIndicatorWidth = 22f
    private var selectedIndicatorHeight = 4f
    private var indicatorRadius = 2f
    private var selectedIndicatorRadius = 2f
    private var unselectedColor = Color.GRAY
    private var selectedColor = Color.WHITE
    private var indicatorSpacing = 10f
    private var indicatorCount = 10
    private var selectedIndex = 0
    private var animatorInterpolator: Interpolator = AccelerateDecelerateInterpolator()

    private var animator: ValueAnimator? = null
    private var offsetX = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    init { // 默认选中第0个指示器
        context.theme.obtainStyledAttributes(attrs, R.styleable.IndicatorView, defStyleAttr, 0).apply {
            try {
                indicatorWidth = getDimension(R.styleable.IndicatorView_indicator_width, indicatorWidth.dp2px())
                indicatorHeight = getDimension(R.styleable.IndicatorView_indicator_height, indicatorHeight.dp2px())
                indicatorRadius = getDimension(R.styleable.IndicatorView_indicator_radius, indicatorRadius.dp2px())
                selectedIndicatorWidth = getDimension(R.styleable.IndicatorView_indicator_selected_width, selectedIndicatorWidth.dp2px())
                selectedIndicatorHeight = getDimension(R.styleable.IndicatorView_indicator_selected_height, selectedIndicatorHeight.dp2px())
                selectedIndicatorRadius = getDimension(R.styleable.IndicatorView_indicator_selected_radius, selectedIndicatorRadius.dp2px())
                indicatorSpacing = getDimension(R.styleable.IndicatorView_indicator_spacing, indicatorSpacing.dp2px())
                unselectedColor = getColor(R.styleable.IndicatorView_indicator_color, unselectedColor)
                selectedColor = getColor(R.styleable.IndicatorView_indicator_selected_color, selectedColor)
                indicatorCount = getInteger(R.styleable.IndicatorView_indicator_count, indicatorCount)
            } finally {
                recycle()
            }
        }
    }

    fun setIndicatorCount(count: Int) {
        indicatorCount = count
        requestLayout()
        invalidate()
    }

    fun setSelectedIndex(index: Int) {
        if (index < 0 || index >= indicatorCount || index == selectedIndex) return
        animator?.cancel()
        val startPosition = getIndicatorX(selectedIndex)
        val endPosition = getIndicatorX(index)
        animator = ValueAnimator.ofFloat(startPosition, endPosition).apply {
            duration = 300
            interpolator = animatorInterpolator
            addUpdateListener { animation ->
                offsetX = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
        selectedIndex = index
    }


    // 设置属性的方法
    fun setIndicatorWidth(width: Float) {
        indicatorWidth = width
        requestLayout()
        invalidate()
    }

    fun setIndicatorHeight(height: Float) {
        indicatorHeight = height
        requestLayout()
        invalidate()
    }

    fun setIndicatorRadius(radius: Float) {
        indicatorRadius = radius
        invalidate()
    }

    fun setSelectedIndicatorWidth(width: Float) {
        selectedIndicatorWidth = width
        requestLayout()
        invalidate()
    }

    fun setSelectedIndicatorHeight(height: Float) {
        selectedIndicatorHeight = height
        requestLayout()
        invalidate()
    }

    fun setSelectedIndicatorRadius(radius: Float) {
        selectedIndicatorRadius = radius
        invalidate()
    }

    fun setUnselectedColor(color: Int) {
        unselectedColor = color
        invalidate()
    }

    fun setSelectedColor(color: Int) {
        selectedColor = color
        invalidate()
    }

    fun setIndicatorSpacing(spacing: Float) {
        indicatorSpacing = spacing
        requestLayout()
        invalidate()
    }

    fun setAnimatorInterpolator(interpolator: Interpolator) {
        animatorInterpolator = interpolator
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = (indicatorCount - 1) * indicatorSpacing + (indicatorCount - 1) * indicatorWidth + selectedIndicatorWidth + paddingLeft + paddingRight
        val totalHeight = if (selectedIndicatorHeight > indicatorHeight) selectedIndicatorHeight else indicatorHeight + paddingTop + paddingBottom

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val measuredWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> totalWidth.toInt().coerceAtMost(widthSize)
            else -> totalWidth.toInt()
        }

        val measuredHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> totalHeight.toInt().coerceAtMost(heightSize)
            else -> totalHeight.toInt()
        }

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var startX = paddingLeft + (width - paddingLeft - paddingRight - ((indicatorCount - 1) * indicatorSpacing + (indicatorCount - 1) * indicatorWidth + selectedIndicatorWidth)) / 2f
        val centerY = paddingTop + (height - paddingTop - paddingBottom) / 2f

        for (i in 0 until indicatorCount) {
            if (i == selectedIndex) {
                paint.color = selectedColor
                canvas.drawRoundRect(offsetX + paddingLeft, centerY - selectedIndicatorHeight / 2, offsetX + selectedIndicatorWidth + paddingLeft, centerY + selectedIndicatorHeight / 2, selectedIndicatorRadius, selectedIndicatorRadius, paint)
                startX += selectedIndicatorWidth + indicatorSpacing
            } else {
                paint.color = unselectedColor
                rectF.set(startX, centerY - indicatorHeight / 2, startX + indicatorWidth, centerY + indicatorHeight / 2)
                canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, paint)
                startX += indicatorWidth + indicatorSpacing
            }
        }
    }

    private fun getIndicatorX(position: Int): Float {
        val totalWidth = (indicatorCount - 1) * (indicatorWidth + indicatorSpacing) + selectedIndicatorWidth+paddingLeft+paddingRight
        val startX = paddingLeft + (width - paddingLeft - paddingRight - totalWidth) / 2f
        return startX + position * (indicatorWidth + indicatorSpacing)
    }
}