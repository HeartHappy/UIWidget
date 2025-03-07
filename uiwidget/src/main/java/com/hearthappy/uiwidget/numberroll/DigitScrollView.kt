package com.hearthappy.uiwidget.numberroll

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.dp2px
import com.hearthappy.uiwidget.utils.sp2px

/**
 * Created Date: 2025/3/6
 * @author ChenRui
 * ClassDescription：数字滚动控件
 */
class DigitScrollView : View {
    private var digitImages: List<Bitmap> = mutableListOf() // 存储数字图片
    private var digitCount = 9 // 默认显示的数字位数
    private var textColor = ContextCompat.getColor(context, R.color.color_title)
    private var textSize = 12f //文本字体大小
    private var outlineColor: Int = -1 //文本描边颜色
    private var outlineWidth: Float = 2f //文本描边宽
    private var digitImageWidth = 0f
    private var digitImageHeight = 0f
    private var digitFillMode = 0 //数字填充模式：默认不填充


    private var width = 0
    private var height: Int = 0 // 控件宽高
    private var currentValue = 0 // 当前值
    private var textPaint: Paint = Paint() // 绘图工具
    private var outlinePaint: Paint = Paint()
    private var animator: ValueAnimator? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { // 获取自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DigitScrollView)
        val resourceIdArray = typedArray.getResourceIdArray(R.styleable.DigitScrollView_nr_digit_images)
        digitImageWidth = typedArray.getDimension(R.styleable.DigitScrollView_nr_digit_image_width, 17f.dp2px())
        digitImageHeight = typedArray.getDimension(R.styleable.DigitScrollView_nr_digit_image_height, 23f.dp2px())
        digitImages = resourceIdArray.map { Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, it), digitImageWidth.toInt(), digitImageHeight.toInt(), true) }
        textColor = typedArray.getColor(R.styleable.DigitScrollView_nr_text_color, textColor)
        outlineColor = typedArray.getColor(R.styleable.DigitScrollView_nr_text_outline_color, outlineColor)
        textSize = typedArray.getDimension(R.styleable.DigitScrollView_nr_text_size, textSize.sp2px())
        outlineWidth = typedArray.getDimension(R.styleable.DigitScrollView_nr_text_outline_width, outlineWidth.sp2px())
        digitCount = typedArray.getInteger(R.styleable.DigitScrollView_nr_digit_count, digitCount)
        digitFillMode = typedArray.getInt(R.styleable.DigitScrollView_nr_fill_mode, 0)
        typedArray.recycle()
        initPaint()
    }

    private fun TypedArray.getResourceIdArray(index: Int): MutableList<Int> {
        val imageResourceIds = mutableListOf<Int>()
        val attrResourceId = getResourceId(index, 0)
        if (attrResourceId != 0) {
            val array = resources.obtainTypedArray(attrResourceId)
            for (i in 0 until array.length()) {
                val drawableResId = array.getResourceId(i, 0)
                if (drawableResId != 0) imageResourceIds.add(drawableResId)
            }
            array.recycle()
        }
        return imageResourceIds
    }

    private fun initPaint() {
        textPaint.apply {
            isAntiAlias = true
            color = this@DigitScrollView.textColor
            textSize = this@DigitScrollView.textSize
        }
        outlinePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = outlineWidth
            textSize = this@DigitScrollView.textSize //            textAlign = Paint.Align.CENTER
            color = outlineColor
            maskFilter = BlurMaskFilter(outlineWidth, BlurMaskFilter.Blur.NORMAL)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // 前面填充0
        val valueStr = if (digitFillMode == 1) String.format("%0" + digitCount + "d", currentValue) else currentValue.toString()
        if (digitImages.isNotEmpty()) {
            val averageWidth = width / digitCount
            for (i in 0 until digitCount) {
                canvas.drawBitmap(digitImages[Character.getNumericValue(valueStr[i])], i * averageWidth + (averageWidth - digitImageWidth) / 2f, (height - digitImageHeight) / 2f, textPaint)
            }
        } else {
            val textWidth = textPaint.measureText(valueStr)
            val x = (width - textWidth) / 2
            val y = height / 2 + textPaint.textSize / 3
            canvas.drawText(valueStr, x, y, outlinePaint)
            canvas.drawText(valueStr, x, y, textPaint)
        }
    }

    fun setValue(value: Int) {
        currentValue = value
        if (digitFillMode == 0) digitCount = currentValue.toString().length
        invalidate() // 重绘
    }

    fun getValue() = currentValue

    fun setDigitImagesSize(digitImageWidth: Float, digitImageHeight: Float) {
        this.digitImageWidth = digitImageWidth
        this.digitImageHeight = digitImageHeight
        invalidate()
    }

    fun rollToValue(targetValue: Int) {
        if (checkAnimatorComplete(targetValue)) return
        animator = ValueAnimator.ofInt(currentValue, targetValue)
        animator?.setDuration(1000)
        animator?.addUpdateListener { animation ->
            currentValue = animation.animatedValue as Int
            if (digitFillMode == 0 && currentValue == targetValue) digitCount = currentValue.toString().length
            invalidate()
        }
        animator?.start()
    }

    private fun checkAnimatorComplete(targetValue: Int): Boolean {
        return animator?.run {
            if (isRunning) {
                currentValue = targetValue
                invalidate()
                end()
                true
            } else false
        } ?: false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
    }

    companion object {
        private const val TAG = "DigitScrollView"
    }

}