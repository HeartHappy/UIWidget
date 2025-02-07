package com.hearthappy.uiwidget.pk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.SizeUtils
import kotlin.properties.Delegates

/**
 * Created Date: 2025/2/7
 * @author ChenRui
 * ClassDescription：PK进度条控件
 */
class PkView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var textHorizontalPadding = 0
    private var textSize = 12f
    private var textColor = Color.WHITE
    private var redSquareColor = Color.RED
    private var blueSquareColor = Color.BLUE
    private var progressHeight = 0f
    private var roundRadius = 0f

    private var iconBitmap by Delegates.notNull<Bitmap>()
    private var redValue = 0f
    private var blueValue = 0f
    private var totalValue = 0f
    private var iconMatrix = Matrix()

    private val redPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val bluePaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PkView)
        val resourceId = typedArray.getResourceId(R.styleable.PkView_pk_icon, R.mipmap.ic_pk)
        iconBitmap = BitmapFactory.decodeResource(resources, resourceId)
        textHorizontalPadding = typedArray.getDimension(R.styleable.PkView_pk_text_horizontal_padding, 0f).toInt()
        textSize = typedArray.getDimension(R.styleable.PkView_pk_text_size, SizeUtils.sp2px(context, textSize).toFloat())
        textColor = typedArray.getColor(R.styleable.PkView_pk_text_color, Color.WHITE)
        redSquareColor = typedArray.getColor(R.styleable.PkView_pk_red_square_color, Color.RED)
        blueSquareColor = typedArray.getColor(R.styleable.PkView_pk_blue_square_color, Color.BLUE)
        progressHeight = typedArray.getDimension(R.styleable.PkView_pk_progress_height, 0f)
        roundRadius = typedArray.getDimension(R.styleable.PkView_pk_round_radius, SizeUtils.sp2px(context, roundRadius).toFloat())
        typedArray.recycle()
        initPaint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (progressHeight == 0f || progressHeight > h) progressHeight = h.toFloat()
    }

    private fun initPaint() {
        textPaint.apply {
            color = textColor
            textSize = this@PkView.textSize
        }
        redPaint.apply { color = redSquareColor }
        bluePaint.apply { color = blueSquareColor }
    }


    fun setValues(red: Float, blue: Float) {
        redValue = red
        blueValue = blue
        totalValue = red + blue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val redWidth = if (totalValue == 0f) width / 2 else (redValue / totalValue) * width

        val rectY = if (progressHeight == 0f) height / 2 else (height - progressHeight) / 2
        canvas.drawRect(0f, rectY, width, rectY + progressHeight, redPaint)
        canvas.drawRect(redWidth, rectY, width, rectY + progressHeight, bluePaint)

        val redTextWidth = textPaint.measureText(redValue.toString())
        canvas.drawText(redValue.toInt().toString(), redTextWidth / 2 + textHorizontalPadding, height / 2 + textPaint.textSize / 3, textPaint)

        val blueTextWidth = textPaint.measureText(blueValue.toString())
        canvas.drawText(blueValue.toInt().toString(), width - blueTextWidth / 2 - textHorizontalPadding, height / 2 + textPaint.textSize / 3, textPaint)

        val iconX = redWidth - (iconBitmap.width / 2)
        val iconY = (height - iconBitmap.height) / 2f
        val scaleFactor = height / iconBitmap.height.toFloat()
        iconMatrix.reset()
        iconMatrix.postScale(scaleFactor, scaleFactor, iconBitmap.width / 2f, iconBitmap.height / 2f)
        iconMatrix.postTranslate(if (iconX < 0) 0f else if (iconX > width) width else iconX, iconY)
        canvas.drawBitmap(iconBitmap, iconMatrix, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    fun setTextHorizontalPadding(textHorizontalPadding: Int) {
        this.textHorizontalPadding = textHorizontalPadding
        invalidate()
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        invalidate()
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        invalidate()
    }

    fun setRedSquareColor(redSquareColor: Int) {
        this.redSquareColor = redSquareColor
        invalidate()
    }

    fun setBlueSquareColor(blueSquareColor: Int) {
        this.blueSquareColor = blueSquareColor
        invalidate()
    }

    fun setProgressHeight(progressHeight: Float) {
        this.progressHeight = progressHeight
        invalidate()
    }
}