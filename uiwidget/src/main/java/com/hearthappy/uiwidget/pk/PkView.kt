package com.hearthappy.uiwidget.pk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.dp2px
import com.hearthappy.uiwidget.utils.sp2px
import kotlin.properties.Delegates

/**
 * Created Date: 2025/2/7
 * @author ChenRui
 * ClassDescription：PK进度条控件
 */
class PkView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var textHorizontalPadding = 0
    private var textSize = 12f
    private var textOutlineWidth = 0f
    private var textColor = Color.WHITE
    private var textOutlineColor =  Color.TRANSPARENT
    private var redSquareColor = Color.RED
    private var blueSquareColor = Color.BLUE
    private var redGradientColorStart = Color.TRANSPARENT
    private var redGradientColorEnd = Color.TRANSPARENT
    private var blueGradientColorStart = Color.TRANSPARENT
    private var blueGradientColorEnd = Color.TRANSPARENT
    private var progressHeight = 0f
    private var cornerRadius = 0f

    private var iconBitmap by Delegates.notNull<Bitmap>()
    private var redValue = 0f
    private var blueValue = 0f
    private var totalValue = 0f
    private var iconMatrix = Matrix()
    private var redRect = RectF()
    private var blueRect = RectF()

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

    // 在类成员变量中定义描边 Paint 和填充 Paint
    private val textStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE      // 描边模式
        textAlign = Paint.Align.CENTER
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PkView)
        val resourceId = typedArray.getResourceId(R.styleable.PkView_pk_icon, R.mipmap.ic_pk)
        iconBitmap = BitmapFactory.decodeResource(resources, resourceId)
        textHorizontalPadding = typedArray.getDimension(R.styleable.PkView_pk_text_horizontal_padding, 0f).toInt()
        textSize = typedArray.getDimension(R.styleable.PkView_pk_text_size, textSize.sp2px())
        textColor = typedArray.getColor(R.styleable.PkView_pk_text_color, Color.WHITE)
        redSquareColor = typedArray.getColor(R.styleable.PkView_pk_red_square_solid_color, Color.RED)
        redGradientColorStart = typedArray.getColor(R.styleable.PkView_pk_red_gradient_color_start, Color.RED)
        redGradientColorEnd = typedArray.getColor(R.styleable.PkView_pk_red_gradient_color_end, Color.RED)
        blueSquareColor = typedArray.getColor(R.styleable.PkView_pk_blue_square_solid_color, Color.BLUE)
        blueGradientColorStart = typedArray.getColor(R.styleable.PkView_pk_blue_gradient_color_start, Color.BLUE)
        blueGradientColorEnd = typedArray.getColor(R.styleable.PkView_pk_blue_gradient_color_end, Color.BLUE)
        progressHeight = typedArray.getDimension(R.styleable.PkView_pk_progress_height, 0f)
        cornerRadius = typedArray.getDimension(R.styleable.PkView_pk_round_radius, cornerRadius.dp2px())
        textOutlineWidth = typedArray.getDimension(R.styleable.PkView_pk_text_outline_width,textOutlineWidth)
        textOutlineColor = typedArray.getColor(R.styleable.PkView_pk_text_outline_color,textOutlineColor)
        typedArray.recycle()
        initPaint()
    }


    private fun setGradientColor(paint: Paint, startColor: Int, endColor: Int, rect: RectF) {
        if (startColor != Color.TRANSPARENT || endColor != Color.TRANSPARENT) {
            paint.shader = LinearGradient(rect.left, rect.top,    // 渐变起点：左边界
                rect.right, rect.top,  // 渐变终点：右边界（水平方向）
                startColor, endColor,            // 红色渐变颜色
                Shader.TileMode.CLAMP        // 渐变填充模式
            )
        }
    }

    fun setBlueSquareGradientColor(startColor: Int, endColor: Int) {
        setGradientColor(bluePaint, startColor, endColor, blueRect)
        invalidate()
    }

    fun setRedSquareGradientColor(startColor: Int, endColor: Int) {
        setGradientColor(redPaint, startColor, endColor, redRect)
        invalidate()
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
        textStrokePaint.apply {
            strokeWidth = textOutlineWidth                // 描边宽度（根据需求调整）
            color = textOutlineColor             // 描边颜色
            textSize = textPaint.textSize   // 同步文字大小
            typeface = textPaint.typeface   // 同步字体
            maskFilter = BlurMaskFilter(textOutlineWidth, BlurMaskFilter.Blur.NORMAL)
        }
    }


    fun setValues(red: Float, blue: Float) {
        redValue = red
        blueValue = blue
        totalValue = red + blue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        // 2. 计算可用区域（减去 padding）
        val availableWidth = width.toFloat() - paddingLeft - paddingRight
        val availableHeight = height.toFloat() - paddingTop - paddingBottom

        // 3. 计算红色部分的宽度（基于可用宽度）
        val redWidth = if (totalValue == 0f) {
            paddingLeft + availableWidth / 2  // 居中分割
        } else {
            paddingLeft + (redValue / totalValue) * availableWidth
        }

        // 4. 调整矩形位置（加入 padding）
        val rectY = paddingTop + if (progressHeight == 0f) {
            availableHeight / 2
        } else {
            (availableHeight - progressHeight) / 2
        }

        // 红色矩形（左侧圆角）
        redRect.set(paddingLeft.toFloat(), rectY, redWidth, rectY + progressHeight) // 核心修改1：为红色 Paint 设置水平渐变
        setGradientColor(redPaint, redGradientColorStart, redGradientColorEnd, redRect)
        drawRoundedRect(canvas, redRect, redPaint, cornerRadius, true)

        // 蓝色矩形（右侧圆角）
        blueRect.set(redWidth, rectY, width.toFloat() - paddingRight, rectY + progressHeight) // 核心修改2：为蓝色 Paint 设置水平渐变
        setGradientColor(bluePaint, blueGradientColorStart, blueGradientColorEnd, blueRect)
        drawRoundedRect(canvas, blueRect, bluePaint, cornerRadius, false)

        // 5. 调整文字位置（加入 padding）
        // 红色文字
        val redTextWidth = textPaint.measureText(redValue.toString()) // 先绘制描边
        canvas.drawText(redValue.toInt().toString(), paddingLeft + redTextWidth / 2 + textHorizontalPadding, height / 2 + textPaint.textSize / 3, textStrokePaint)
        canvas.drawText(redValue.toInt().toString(), paddingLeft + redTextWidth / 2 + textHorizontalPadding, height / 2 + textPaint.textSize / 3, textPaint)

        // 蓝色文字
        val blueTextWidth = textPaint.measureText(blueValue.toString())
        canvas.drawText(blueValue.toInt().toString(), width.toFloat() - paddingRight - blueTextWidth / 2 - textHorizontalPadding, height / 2 + textPaint.textSize / 3, textStrokePaint)
        canvas.drawText(blueValue.toInt().toString(), width.toFloat() - paddingRight - blueTextWidth / 2 - textHorizontalPadding, height / 2 + textPaint.textSize / 3, textPaint)

        // 6. 调整图标位置（避免超出 padding 区域）
        val iconX = redWidth - (iconBitmap.width / 2)
        val iconY = paddingTop + (availableHeight - iconBitmap.height) / 2f  // 垂直居中
        val scaleFactor = (availableHeight - paddingTop - paddingBottom) / iconBitmap.height.toFloat() // 基于可用高度缩放

        iconMatrix.reset()
        iconMatrix.postScale(scaleFactor, scaleFactor, iconBitmap.width / 2f, iconBitmap.height / 2f)
        iconMatrix.postTranslate(iconX.coerceIn(paddingLeft.toFloat(), width.toFloat() - paddingRight - iconBitmap.width), iconY)
        canvas.drawBitmap(iconBitmap, iconMatrix, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun drawRoundedRect(canvas: Canvas, rect: RectF, paint: Paint, radius: Float, isLeft: Boolean) {
        val path = Path() // 定义四个角的圆角半径（顺序：左上、右上、右下、左下）
        val radii = if (isLeft) { // 左侧圆角：左上和左下为 radius，其他为 0
            floatArrayOf(radius, radius,  // 左上
                0f, 0f,          // 右上
                0f, 0f,          // 右下
                radius, radius    // 左下
            )
        } else { // 右侧圆角：右上和右下为 radius，其他为 0
            floatArrayOf(0f, 0f,          // 左上
                radius, radius,  // 右上
                radius, radius,  // 右下
                0f, 0f           // 左下
            )
        } // 创建带自定义圆角的路径
        path.addRoundRect(rect, radii, Path.Direction.CW)
        canvas.drawPath(path, paint) // 绘制路径
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