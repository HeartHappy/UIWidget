package com.hearthappy.uiwidget.indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.hearthappy.uiwidget.R

class IndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var indicatorWidth = 20f
    private var indicatorHeight = 20f
    private var selectedIndicatorWidth = 30f
    private var selectedIndicatorHeight = 30f
    private var indicatorRadius = 10f
    private var selectedIndicatorRadius = 15f
    private var unselectedColor = Color.GRAY
    private var selectedColor = Color.WHITE
    private var indicatorSpacing = 10f
    private var indicatorCount = 3
    private var selectedIndex = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    init { // 默认选中第0个指示器
        context.theme.obtainStyledAttributes(attrs, R.styleable.IndicatorView, defStyleAttr, 0).apply {
            try {
                indicatorWidth = getDimension(R.styleable.IndicatorView_indicatorWidth, indicatorWidth)
                indicatorHeight = getDimension(R.styleable.IndicatorView_indicatorHeight, indicatorHeight)
                indicatorRadius = getDimension(R.styleable.IndicatorView_indicatorCornerRadius, indicatorRadius)
                selectedIndicatorWidth = getDimension(R.styleable.IndicatorView_selectedIndicatorWidth, selectedIndicatorWidth)
                selectedIndicatorHeight = getDimension(R.styleable.IndicatorView_selectedIndicatorHeight, selectedIndicatorHeight)
                selectedIndicatorRadius = getDimension(R.styleable.IndicatorView_selectedIndicatorCornerRadius, selectedIndicatorRadius)
                indicatorSpacing = getDimension(R.styleable.IndicatorView_indicatorSpacing, indicatorSpacing)
                unselectedColor = getColor(R.styleable.IndicatorView_unselectedColor, unselectedColor)
                selectedColor = getColor(R.styleable.IndicatorView_selectedColor, selectedColor)
                indicatorCount = getInteger(R.styleable.IndicatorView_count, indicatorCount)
                selectedIndex = getInteger(R.styleable.IndicatorView_selectedIndex, selectedIndex)
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

    fun setSelectedIndex(index: Int) { //        if (index < 0 || index >= indicatorCount) return
        //        val startPosition = currentSelectedPosition
        //        val endPosition = calculateIndicatorPosition(index)
        //        val animator = ValueAnimator.ofFloat(startPosition, endPosition)
        //        animator.duration = 300
        //        animator.interpolator = AccelerateDecelerateInterpolator()
        //        animator.addUpdateListener { animation ->
        //            currentSelectedPosition = animation.animatedValue as Float
        //            invalidate()
        //        }
        //        animator.start()
        selectedIndex = index.coerceIn(0, indicatorCount - 1)
        invalidate() //        startIndicatorAnimation()
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


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = (indicatorCount - 1) * indicatorSpacing + (indicatorCount - 1) * indicatorWidth + selectedIndicatorWidth
        val totalHeight = if (selectedIndicatorHeight > indicatorHeight) selectedIndicatorHeight else indicatorHeight

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
        var startX = (width - ((indicatorCount - 1) * indicatorSpacing + (indicatorCount - 1) * indicatorWidth + selectedIndicatorWidth)) / 2f
        val centerY = height / 2f

        for (i in 0 until indicatorCount) {
            if (i == selectedIndex) {
                paint.color = selectedColor
                rectF.set(startX, centerY - selectedIndicatorHeight / 2, startX + selectedIndicatorWidth, centerY + selectedIndicatorHeight / 2)
                canvas.drawRoundRect(rectF, selectedIndicatorRadius, selectedIndicatorRadius, paint)
                startX += selectedIndicatorWidth + indicatorSpacing
            } else {
                paint.color = unselectedColor
                rectF.set(startX, centerY - indicatorHeight / 2, startX + indicatorWidth, centerY + indicatorHeight / 2)
                canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, paint)
                startX += indicatorWidth + indicatorSpacing
            }
        }
    }
}