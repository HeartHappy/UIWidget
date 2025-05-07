package com.hearthappy.uiwidget.image

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.dp2px
import com.hearthappy.uiwidget.utils.ext.getFloatArray
import kotlin.math.tan

/**
 * Created Date: 5/4/25
 * @author ChenRui
 * ClassDescription：自定义圆角ImageView
 */
class RoundImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    // 圆角半径（左上、右上、右下、左下）
    private val radii = FloatArray(8)
    private val clipPath = Path()
    private var isCircle = false
    private var blendDrawable: Drawable? = null

    //外边框
    private var borderWidth = 0f
    private var borderColor = Color.TRANSPARENT
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private var gradientBorderColors: IntArray? = null
    private var gradientBorderPositions: FloatArray? = null
    private var gradientBorderAngle: Float = 0f
    private var gradientType = 0
    private val borderMatrix by lazy { Matrix() }

    //内边框
    private var innerBorderWidth = 0f
    private var innerBorderColor = Color.TRANSPARENT
    private val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private var gradientInnerBorderColors: IntArray? = null
    private var gradientInnerBorderPositions: FloatArray? = null
    private var gradientInnerBorderAngle: Float = 0f
    private val innerBorderMatrix by lazy { Matrix() }


    // 新增：内发光
    private var innerGlowColor = Color.TRANSPARENT
    private var innerGlowRadius = 0f
    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    // 新增：颜色滤镜和灰度,饱和度设为0实现灰度
    private var colorFilterColor = Color.TRANSPARENT
    private var isGrayscale = false
    private val grayscaleMatrix by lazy { ColorMatrix().apply { setSaturation(0f) } }

    //混合模式
    private var colorBlendMode = PorterDuff.Mode.MULTIPLY
    private var layersBlendModel = PorterDuff.Mode.SRC_OVER

    // 新增混合图层布局参数
    private var blendWidth = 0
    private var blendHeight = 0
    private var blendGravity = Gravity.START or Gravity.TOP
    private val blendMargin = Rect()
    private val layersPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    //图层水印
    private var layersWatermarkOn = false
    private var layersHorSpacing = 40
    private var layersVerSpacing = 50


    init { // 从XML属性初始化
        context.obtainStyledAttributes(attrs, R.styleable.RoundImageView).apply {
            isCircle = getBoolean(R.styleable.RoundImageView_isCircle, false) // 统一圆角
            getDimension(R.styleable.RoundImageView_radius, 0f).takeIf { it > 0 }?.let { setRadiusPx(it, it, it, it) } ?: setRadiusPx(topLeft = getDimension(R.styleable.RoundImageView_radiusTopLeft, 0f), topRight = getDimension(R.styleable.RoundImageView_radiusTopRight, 0f), bottomRight = getDimension(R.styleable.RoundImageView_radiusBottomRight, 0f), bottomLeft = getDimension(R.styleable.RoundImageView_radiusBottomLeft, 0f))
            innerBorderWidth = getDimensionPixelSize(R.styleable.RoundImageView_innerBorderWidth, 0).toFloat()
            innerBorderColor = getColor(R.styleable.RoundImageView_innerBorderColor, Color.TRANSPARENT) // 解析外边框
            borderWidth = getDimensionPixelSize(R.styleable.RoundImageView_borderWidth, 0).toFloat()
            borderColor = getColor(R.styleable.RoundImageView_borderColor, Color.TRANSPARENT)
            innerGlowColor = getColor(R.styleable.RoundImageView_innerGlowColor, Color.TRANSPARENT)
            innerGlowRadius = getDimensionPixelSize(R.styleable.RoundImageView_innerGlowRadius, 0).toFloat()
            colorFilterColor = getColor(R.styleable.RoundImageView_colorFilter, Color.TRANSPARENT)
            isGrayscale = getBoolean(R.styleable.RoundImageView_grayscale, false)
            colorBlendMode = convertPorterDuffMode(getInt(R.styleable.RoundImageView_colorBlendMode, PorterDuff.Mode.MULTIPLY.ordinal))
            blendDrawable = getDrawable(R.styleable.RoundImageView_blendSrc)
            layersBlendModel = convertPorterDuffMode(getInt(R.styleable.RoundImageView_layersBlendMode, PorterDuff.Mode.SRC_OVER.ordinal)) // 混合图层布局参数
            blendWidth = getDimensionPixelSize(R.styleable.RoundImageView_blendWidth, blendWidth)
            blendHeight = getDimensionPixelSize(R.styleable.RoundImageView_blendHeight, blendHeight)
            blendGravity = getInteger(R.styleable.RoundImageView_blendGravity, Gravity.START or Gravity.TOP)

            val marginAll = getDimensionPixelSize(R.styleable.RoundImageView_blendMargin, 0)
            blendMargin.set(getDimensionPixelSize(R.styleable.RoundImageView_blendMarginLeft, marginAll), getDimensionPixelSize(R.styleable.RoundImageView_blendMarginTop, marginAll), getDimensionPixelSize(R.styleable.RoundImageView_blendMarginRight, marginAll), getDimensionPixelSize(R.styleable.RoundImageView_blendMarginBottom, marginAll))
            layersWatermarkOn = getBoolean(R.styleable.RoundImageView_layersWatermarkOn, false)
            layersHorSpacing = getDimensionPixelSize(R.styleable.RoundImageView_layersHorSpacing, layersHorSpacing)
            layersVerSpacing = getDimensionPixelSize(R.styleable.RoundImageView_layersVerSpacing, layersVerSpacing)

            // 解析读取渐变色属性
            getResourceId(R.styleable.RoundImageView_gradientBorderColors, 0).takeIf { it != 0 }?.let { gradientBorderColors = resources.getIntArray(it) }
            getResourceId(R.styleable.RoundImageView_gradientBorderPositions, 0).takeIf { it != 0 }?.let { resId -> gradientBorderPositions = resources.getFloatArray(resId) }
            gradientBorderAngle = getFloat(R.styleable.RoundImageView_gradientBorderAngle, 0f)

            getResourceId(R.styleable.RoundImageView_gradientInnerBorderColors, 0).takeIf { it != 0 }?.let { gradientInnerBorderColors = resources.getIntArray(it) }
            getResourceId(R.styleable.RoundImageView_gradientInnerBorderPositions, 0).takeIf { it != 0 }?.let { resId -> gradientInnerBorderPositions = resources.getFloatArray(resId) }
            gradientInnerBorderAngle = getFloat(R.styleable.RoundImageView_gradientInnerBorderAngle, 0f)
            gradientType = getInt(R.styleable.RoundImageView_gradientType, 0)

            recycle()
        }

        // 边框画笔配置
        if (isReasonable(innerBorderWidth, innerBorderColor)) {
            innerBorderPaint.strokeWidth = borderWidth + innerBorderWidth
            innerBorderPaint.color = innerBorderColor
        }

        // 配置外边框画笔
        if (isReasonable(borderWidth, borderColor)) {
            borderPaint.strokeWidth = borderWidth
            borderPaint.color = borderColor
        }

        // 配置内发光画笔（模糊效果）
        if (isReasonable(innerGlowRadius, innerGlowColor)) {
            innerGlowPaint.maskFilter = BlurMaskFilter(innerGlowRadius, BlurMaskFilter.Blur.NORMAL)
            innerGlowPaint.color = innerGlowColor
            innerGlowPaint.strokeWidth = borderWidth + innerBorderWidth + innerGlowRadius
        } //图层混合
        blendDrawable?.let { layersPaint.xfermode = PorterDuffXfermode(colorBlendMode) } // 应用初始滤镜和灰度
        updateColorFilter() // 优化设置（必须关闭硬件加速）
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath() // 尺寸变化时更新渐变
        updateGradient(gradientBorderColors, gradientBorderPositions, gradientBorderAngle, borderMatrix, borderWidth, borderPaint)
        updateGradient(gradientInnerBorderColors, gradientInnerBorderPositions, gradientInnerBorderAngle, innerBorderMatrix, innerBorderWidth, innerBorderPaint)
    }

    override fun draw(canvas: Canvas) {
        canvas.clipPath(clipPath) // 3. 绘制原始内容
        super.draw(canvas)
        drawBlendImage(canvas)
        if (isReasonable(innerGlowRadius, innerGlowColor)) canvas.drawPath(clipPath, innerGlowPaint)
        if (isReasonable(innerBorderWidth, innerBorderColor)) canvas.drawPath(clipPath, innerBorderPaint)
        if (isReasonable(borderWidth, borderColor)) canvas.drawPath(clipPath, borderPaint)
    }

    private fun drawBlendImage(canvas: Canvas) {
        blendDrawable?.let { drawable ->
            val (dw, dh) = getBlendDrawableSize(drawable)
            if (layersWatermarkOn) {
                drawLayersWatermark(dw, dh, drawable, canvas)
            } else {
                val destRect = getLayersBlendRect(dw, dh)
                drawable.bounds = destRect
                drawable.draw(canvas)
            }
        }
    }

    /**
     * 绘制图层水印
     * @param dw Int
     * @param dh Int
     * @param drawable Drawable
     * @param canvas Canvas
     */
    private fun drawLayersWatermark(dw: Int, dh: Int, drawable: Drawable, canvas: Canvas) {
        var currentTop = 0
        var isEvenRow = false
        while (currentTop < height) {
            var currentLeft = if (isEvenRow) layersHorSpacing else 0
            while (currentLeft < width) {
                val destRect = getRectWithOffset(currentLeft, currentTop, dw, dh)
                drawable.bounds = destRect
                drawable.draw(canvas)
                currentLeft += dw + layersHorSpacing
            }
            currentTop += dh + layersVerSpacing
            isEvenRow = !isEvenRow
        }
    }

    /**
     * 单个图层Rect
     * @param dw Int
     * @param dh Int
     * @return Rect
     */
    private fun getLayersBlendRect(dw: Int, dh: Int): Rect {
        var left = 0
        var top = 0
        when (blendGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> left = blendMargin.left
            Gravity.CENTER_HORIZONTAL -> left = (width - dw) / 2 + blendMargin.left - blendMargin.right
            Gravity.RIGHT -> left = width - dw - blendMargin.right
        }

        when (blendGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> top = blendMargin.top
            Gravity.CENTER_VERTICAL -> top = (height - dh) / 2 + blendMargin.top - blendMargin.bottom
            Gravity.BOTTOM -> top = height - dh - blendMargin.bottom
        }
        val right = left + dw
        val bottom = top + dh
        val destRect = Rect(left, top, right, bottom)
        return destRect
    }

    /**
     * 多图层，根据偏移量获取Rect
     * @param leftOffset Int
     * @param topOffset Int
     * @param dw Int
     * @param dh Int
     * @return Rect
     */
    private fun getRectWithOffset(leftOffset: Int, topOffset: Int, dw: Int, dh: Int): Rect {
        var left = 0
        var top = 0
        when (blendGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> left = blendMargin.left + leftOffset
            Gravity.CENTER_HORIZONTAL -> left = (width - dw) / 2 + blendMargin.left - blendMargin.right + leftOffset
            Gravity.RIGHT -> left = width - dw - blendMargin.right + leftOffset
        }

        when (blendGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> top = blendMargin.top + topOffset
            Gravity.CENTER_VERTICAL -> top = (height - dh) / 2 + blendMargin.top - blendMargin.bottom + topOffset
            Gravity.BOTTOM -> top = height - dh - blendMargin.bottom + topOffset
        }
        val right = left + dw
        val bottom = top + dh
        return Rect(left, top, right, bottom)
    }

    private fun updateGradient(colors: IntArray?, positions: FloatArray?, angle: Float, matrix: Matrix, borderWidth: Float, paint: Paint) {
        colors?.takeIf { it.size >= 2 }?.let { cs ->
            val halfBorder = borderWidth / 2
            val rect = RectF(halfBorder, halfBorder, width - halfBorder, height - halfBorder)

            val shader = when (gradientType) {
                0 -> { // 计算渐变起止点（关键修改）
                    val (startX, startY, endX, endY) = calculateGradientPoints(rect, angle)
                    LinearGradient(startX, startY, endX, endY, cs, positions, Shader.TileMode.CLAMP)
                }
                else -> {
                    val centerX = width / 2f
                    val centerY = height / 2f
                    SweepGradient(centerX, centerY, cs, positions).apply { // 通过Matrix旋转角度
                        matrix.preRotate(angle, centerX, centerY)
                        setLocalMatrix(matrix)
                    }
                }
            }
            paint.shader = shader
        }
    }

    /**
     * 根据角度计算渐变起止点坐标
     * @param rect 边框区域
     * @param angleDegrees 角度（0-360）
     */
    private fun calculateGradientPoints(rect: RectF, angleDegrees: Float): FloatArray {
        val angleRad = Math.toRadians(angleDegrees.toDouble()) // 根据角度调整坐标系（强制类型转换）
        val (dx, dy) = when {
            angleDegrees % 180 == 0f -> Pair(rect.width() / 2f, 0f) // 添加f后缀声明Float
            angleDegrees % 180 == 90f -> Pair(0f, rect.height() / 2f)
            else -> {
                val tan = tan(angleRad).toFloat() // Double转Float
                Pair(rect.width() / 2f, (rect.width() / (2 * tan)).coerceIn(-rect.height() / 2f, rect.height() / 2f))
            }
        }

        // 显式转换为Float类型
        val startX = rect.centerX() - dx
        val startY = rect.centerY() - dy
        val endX = rect.centerX() + dx
        val endY = rect.centerY() + dy

        return floatArrayOf(startX, startY, endX, endY)
    }


    fun setGradientInnerBorderAnger(angle: Float) {
        gradientInnerBorderAngle = angle
        updateGradient(gradientInnerBorderColors, gradientInnerBorderPositions, gradientInnerBorderAngle, innerBorderMatrix, innerBorderWidth, innerBorderPaint)
        invalidate()
    }

    fun setGradientInnerBorderColors(colors: IntArray, positions: FloatArray? = null) {
        gradientInnerBorderColors = colors
        gradientInnerBorderPositions = positions
        updateGradient(gradientInnerBorderColors, gradientInnerBorderPositions, gradientInnerBorderAngle, innerBorderMatrix, innerBorderWidth, innerBorderPaint)
        invalidate()
    }

    fun setGradientBorderAnger(angle: Float) {
        gradientBorderAngle = angle
        updateGradient(gradientBorderColors, gradientBorderPositions, gradientBorderAngle, borderMatrix, borderWidth, borderPaint)
        invalidate()
    }

    // 动态设置渐变
    fun setGradientBorderColors(colors: IntArray, positions: FloatArray? = null) {
        gradientBorderColors = colors
        gradientBorderPositions = positions
        updateGradient(gradientBorderColors, gradientBorderPositions, gradientBorderAngle, borderMatrix, borderWidth, borderPaint)
        invalidate()
    }

    private fun isReasonable(width: Float, color: Int): Boolean {
        return width > 0f && color != Color.TRANSPARENT
    }

    // 更新裁剪路径
    private fun updateClipPath() {
        clipPath.reset() // 计算总偏移量（取最大边框宽度）
        if (isCircle) {
            clipPath.addCircle(width / 2f, height / 2f, width / 2f, Path.Direction.CW)
        } else {
            clipPath.addRoundRect(RectF(paddingLeft.toFloat(), paddingTop.toFloat(), (width - paddingEnd).toFloat(), (height - paddingBottom).toFloat()), radii, Path.Direction.CW)
        }

    }

    // 新增：统一更新颜色滤镜
    private fun updateColorFilter() {
        colorFilter = when {
            isGrayscale -> { // 灰度优先于颜色滤镜
                ColorMatrixColorFilter(grayscaleMatrix)
            }

            colorFilterColor != Color.TRANSPARENT -> { // 叠加颜色滤镜（PorterDuff.MODE_SRC_ATOP）
                PorterDuffColorFilter(colorFilterColor, colorBlendMode)
            }

            else -> {
                null
            }
        }
    }

    fun animateGradientAngle(start: Float, end: Float, duration: Long) {
        ValueAnimator.ofFloat(start, end).apply {
            this.duration = duration
            addUpdateListener {
                gradientBorderAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // 计算混合图层尺寸
    private fun getBlendDrawableSize(drawable: Drawable): Pair<Int, Int> {
        return when {
            blendWidth > 0 && blendHeight > 0 -> Pair(blendWidth, blendHeight)
            blendWidth > 0 -> {
                val ratio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                Pair(blendWidth, (blendWidth / ratio).toInt())
            }
            blendHeight > 0 -> {
                val ratio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                Pair((blendHeight * ratio).toInt(), blendHeight)
            }
            else -> Pair(drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    // 动态设置颜色滤镜
    fun setViewColorFilter(@ColorInt color: Int) {
        colorFilterColor = color
        isGrayscale = false // 关闭灰度
        updateColorFilter()
        invalidate()
    }

    // 动态设置灰度模式
    fun setGrayscale(enabled: Boolean) {
        isGrayscale = enabled
        updateColorFilter()
        invalidate()
    }

    // 动态设置内边框
    fun setInnerBorder(width: Float, @ColorInt color: Int) {
        if (isReasonable(width, color)) {
            innerBorderWidth = width.dp2px()
            innerBorderColor = color
            innerBorderPaint.strokeWidth = borderWidth + innerBorderWidth
            innerBorderPaint.color = color
            invalidate()
        }
    }

    //设置外边框
    fun setBorder(width: Float, @ColorInt color: Int) {
        if (isReasonable(width, color)) {
            borderWidth = width.dp2px()
            borderColor = color
            borderPaint.strokeWidth = borderWidth
            borderPaint.color = color
            invalidate()
        }
    }

    fun setInnerGlow(radius: Float, @ColorInt color: Int) {
        if (radius > 0f && color != Color.TRANSPARENT) {
            innerGlowRadius = radius.dp2px()
            innerGlowColor = color
            innerGlowPaint.maskFilter = BlurMaskFilter(innerGlowRadius, BlurMaskFilter.Blur.NORMAL)
            innerGlowPaint.color = innerGlowColor
            innerGlowPaint.strokeWidth = borderWidth + innerBorderWidth + innerGlowRadius // 动态调整描边宽度
            invalidate()
        }
    }

    fun setColorBlendMode(mode: PorterDuff.Mode) {
        colorBlendMode = mode
        updateColorFilter()
        invalidate()
    }

    fun setLayersBlendMode(mode: PorterDuff.Mode) {
        layersBlendModel = mode
        layersPaint.xfermode = PorterDuffXfermode(colorBlendMode)
        invalidate()
    }

    // 新增设置方法
    fun setBlendSize(width: Int, height: Int) {
        blendWidth = width.dp2px()
        blendHeight = height.dp2px()
        invalidate()
    }

    fun setBlendGravity(gravity: Int) {
        blendGravity = gravity
        invalidate()
    }

    fun setBlendMargin(margin: Int) {
        setBlendMargin(margin, margin, margin, margin)
        invalidate()
    }

    fun setBlendMargin(left: Int, top: Int, right: Int, bottom: Int) {
        blendMargin.set(left.dp2px(), top.dp2px(), right.dp2px(), bottom.dp2px())
        invalidate()
    }

    fun setRadius(radius: Float) {
        setRadius(radius, radius, radius, radius)
    }

    // 动态设置圆角（支持动画）
    fun setRadius(topLeft: Float = radii[0], topRight: Float = radii[2], bottomRight: Float = radii[4], bottomLeft: Float = radii[6]) {
        setCorners(topLeft.dp2px(), topRight.dp2px(), bottomRight.dp2px(), bottomLeft.dp2px())
        updateClipPath()
        invalidate()
    }

    private fun setRadiusPx(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        setCorners(topLeft, topRight, bottomRight, bottomLeft)
        updateClipPath()
        invalidate()
    }


    fun setBlendResource(drawable: Drawable) {
        blendDrawable = drawable
        invalidate()
    }

    fun setLayersHorSpacing(spacing: Int) {
        layersHorSpacing = spacing
        invalidate()
    }

    fun setLayersVerSpacing(spacing: Int) {
        layersVerSpacing = spacing
        invalidate()
    }

    fun setLayersWatermarkOn(enable: Boolean) {
        layersWatermarkOn = enable
        invalidate()
    }

    /**
     * 转换PorterDuff模式
     * @param mode Int
     * @return PorterDuff.Mode
     */
    private fun convertPorterDuffMode(mode: Int): PorterDuff.Mode {
        return when (mode) {
            1 -> PorterDuff.Mode.SRC
            2 -> PorterDuff.Mode.DST
            3 -> PorterDuff.Mode.SRC_OVER
            4 -> PorterDuff.Mode.DST_OVER
            5 -> PorterDuff.Mode.SRC_IN
            6 -> PorterDuff.Mode.DST_IN
            7 -> PorterDuff.Mode.SRC_OUT
            8 -> PorterDuff.Mode.DST_OUT
            9 -> PorterDuff.Mode.SRC_ATOP
            10 -> PorterDuff.Mode.DST_ATOP
            11 -> PorterDuff.Mode.XOR
            12 -> PorterDuff.Mode.ADD
            13 -> PorterDuff.Mode.MULTIPLY
            14 -> PorterDuff.Mode.SCREEN
            15 -> PorterDuff.Mode.OVERLAY
            16 -> PorterDuff.Mode.DARKEN
            17 -> PorterDuff.Mode.LIGHTEN
            else -> PorterDuff.Mode.CLEAR
        }
    }

    // 扩展函数，用于设置四个角的半径
    private fun setCorners(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        radii[0] = topLeft
        radii[1] = topLeft
        radii[2] = topRight
        radii[3] = topRight
        radii[4] = bottomRight
        radii[5] = bottomRight
        radii[6] = bottomLeft
        radii[7] = bottomLeft
    }
}