package com.hearthappy.uiwidget.image

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.hearthappy.uiwidget.R
import kotlin.math.max

/**
 * Created Date: 5/4/25
 * @author ChenRui
 * ClassDescription：自定义圆角ImageView
 */
class RoundImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // 圆角半径（左上、右上、右下、左下）
    private val radii = FloatArray(8)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()
    private val borderPath = Path()
    private val outerBorderPath = Path()
    private val innerGlowPath = Path()
    private var borderWidth = 0f
    private var borderColor = Color.TRANSPARENT

    // 新增：外边框
    private var outerBorderWidth = 0f
    private var outerBorderColor = Color.TRANSPARENT
    private val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    // 新增：内发光
    private var innerGlowColor = Color.TRANSPARENT
    private var innerGlowRadius = 0f
    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE // 关键修改：从 FILL 改为 STROKE
    }

    // 新增：颜色滤镜和灰度
    private var colorFilterColor = Color.TRANSPARENT
    private var isGrayscale = false
    private val grayscaleMatrix by lazy {
        ColorMatrix().apply {
            setSaturation(0f) // 饱和度设为0实现灰度
        }
    }

    //混合模式
    private var porterDuffMode = PorterDuff.Mode.MULTIPLY


    init { // 从XML属性初始化
        context.obtainStyledAttributes(attrs, R.styleable.RoundImageView).apply { // 统一圆角
            val radius = getDimension(R.styleable.RoundImageView_radius, 0f)
            if (radius > 0) {
                setRadius(radius)
            } else { // 独立圆角
                setRadius(
                    topLeft = getDimension(R.styleable.RoundImageView_radiusTopLeft, 0f), topRight = getDimension(R.styleable.RoundImageView_radiusTopRight, 0f), bottomRight = getDimension(R.styleable.RoundImageView_radiusBottomRight, 0f), bottomLeft = getDimension(R.styleable.RoundImageView_radiusBottomLeft, 0f)
                )
            }
            borderWidth = getDimension(R.styleable.RoundImageView_borderWidth, borderWidth)
            borderColor =
                getColor(R.styleable.RoundImageView_borderColor, Color.TRANSPARENT) // 解析外边框
            outerBorderWidth =
                getDimension(R.styleable.RoundImageView_outerBorderWidth, outerBorderWidth)
            outerBorderColor =
                getColor(R.styleable.RoundImageView_outerBorderColor, Color.TRANSPARENT)
            innerGlowColor = getColor(R.styleable.RoundImageView_innerGlowColor, Color.TRANSPARENT)
            innerGlowRadius =
                getDimension(R.styleable.RoundImageView_innerGlowRadius, innerGlowRadius)
            colorFilterColor = getColor(R.styleable.RoundImageView_colorFilter, Color.TRANSPARENT)
            isGrayscale = getBoolean(R.styleable.RoundImageView_grayscale, false)
            porterDuffMode =
                convertPorterDuffMode(getInt(R.styleable.RoundImageView_porterDuffMode, 13))
            recycle()
        }

        // 边框画笔配置
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor

        // 配置外边框画笔
        outerBorderPaint.strokeWidth = outerBorderWidth
        outerBorderPaint.color = outerBorderColor

        // 配置内发光画笔（模糊效果）
        if (innerGlowRadius > 0 && innerGlowColor != Color.TRANSPARENT) {
            innerGlowPaint.maskFilter = BlurMaskFilter(innerGlowRadius, BlurMaskFilter.Blur.NORMAL)
            innerGlowPaint.color = innerGlowColor
            innerGlowPaint.strokeWidth = innerGlowRadius
        } // 应用初始滤镜和灰度
        updateColorFilter() // 优化设置（必须关闭硬件加速）
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath()
        updateBorderPath()
        updateOuterBorderPath() // 新增：外边框路径更新
        updateInnerGlowPath() // 新增：更新内发光路径
    }

    override fun draw(canvas: Canvas) { // 使用离屏缓冲避免边缘锯齿
        //        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        val padding = outerBorderWidth
        val saveCount =
            canvas.saveLayer(-padding, -padding, width + padding, height + padding, null) // 1. 绘制外边框（在最底层）
        if (outerBorderWidth > 0) {
            canvas.drawPath(outerBorderPath, outerBorderPaint)
        }

        // 2. 应用内容裁剪（考虑内外边框）
        canvas.clipPath(clipPath)

        // 3. 绘制原始内容
        super.draw(canvas)

        // 4. 绘制内发光（在内容之上）
        if (innerGlowRadius > 0 && innerGlowColor != Color.TRANSPARENT) {
            canvas.drawPath(innerGlowPath, innerGlowPaint)
        }

        // 5. 绘制内边框（在最顶层）
        if (borderWidth > 0) {
            canvas.drawPath(borderPath, borderPaint)
        }

        canvas.restoreToCount(saveCount)
    }

    fun setRadius(radius: Float) {
        setRadius(radius, radius, radius, radius)
    }


    // 更新裁剪路径
    private fun updateClipPath() {
        clipPath.reset() // 计算总偏移量（取最大边框宽度）
        val totalInset =
            max(borderWidth, outerBorderWidth) //        val borderWidth = //            if (borderWidth > 0) borderWidth else if (outerBorderWidth > 0) outerBorderWidth else 0f
        clipPath.addRoundRect(
            RectF(totalInset, totalInset, width - totalInset, height - totalInset), radii, Path.Direction.CW
        )
    }

    // 新增：更新外边框路径
    private fun updateOuterBorderPath() {
        val halfWidth = outerBorderWidth
        outerBorderPath.reset()
        outerBorderPath.addRoundRect(
            RectF(
                halfWidth, halfWidth, width - halfWidth, height - halfWidth
            ), radii, Path.Direction.CW
        )
    }

    // 更新边框路径
    private fun updateBorderPath() {
        borderPath.reset()
        borderPath.addRoundRect(
            RectF(
                borderWidth, borderWidth, width - borderWidth, height - borderWidth
            ), radii, Path.Direction.CW
        )
    }

    //内发光路径更新方法
    private fun updateInnerGlowPath() {
        innerGlowPath.reset() // 向内缩进，确保发光效果仅出现在边缘附近
        //        val totalBorder = borderWidth + outerBorderWidth
        //        val inset = totalBorder + innerGlowRadius  // 保持与边框间距
        val inset = innerGlowRadius  // 可根据效果调整缩进量
        innerGlowPath.addRoundRect(
            RectF(
                inset, inset, width - inset, height - inset
            ), radii.map { it - inset }.toFloatArray(), Path.Direction.CW
        )
    }

    // 新增：统一更新颜色滤镜
    private fun updateColorFilter() {
        colorFilter = when {
            isGrayscale -> { // 灰度优先于颜色滤镜
                ColorMatrixColorFilter(grayscaleMatrix)
            }

            colorFilterColor != Color.TRANSPARENT -> { // 叠加颜色滤镜（PorterDuff.MODE_SRC_ATOP）
                PorterDuffColorFilter(colorFilterColor, porterDuffMode)
            }

            else -> {
                null
            }
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

    // 动态设置边框
    fun setBorder(width: Float, @ColorInt color: Int) {
        borderWidth = width.coerceAtLeast(0f)
        borderColor = color
        borderPaint.strokeWidth = width
        borderPaint.color = color
        updateClipPath()
        updateBorderPath()
        invalidate()
    }

    fun setOuterBorder(width: Float, @ColorInt color: Int) {
        if (width > 0f && color != Color.TRANSPARENT) {
            outerBorderWidth = width
            outerBorderColor = color
            outerBorderPaint.strokeWidth = width
            outerBorderPaint.color = color
            updateOuterBorderPath()
            invalidate()
        }
    }

    fun setInnerGlow(radius: Float, @ColorInt color: Int) {
        if (radius > 0f && color != Color.TRANSPARENT) {
            innerGlowRadius = radius
            innerGlowColor = color
            innerGlowPaint.maskFilter = BlurMaskFilter(innerGlowRadius, BlurMaskFilter.Blur.NORMAL)
            innerGlowPaint.color = innerGlowColor
            innerGlowPaint.strokeWidth = radius // 动态调整描边宽度
            invalidate()
        }
    }

    // 动态设置圆角（支持动画）
    fun setRadius(
        topLeft: Float = radii[0], topRight: Float = radii[2], bottomRight: Float = radii[4], bottomLeft: Float = radii[6]
    ) {
        radii.apply { // 左上角（索引0和1）
            set(0, topLeft)
            set(1, topLeft)

            // 右上角（索引2和3）
            set(2, topRight)
            set(3, topRight)

            // 右下角（索引4和5）
            set(4, bottomRight)
            set(5, bottomRight)

            // 左下角（索引6和7）
            set(6, bottomLeft)
            set(7, bottomLeft)
        }
        updateClipPath()
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

    companion object {
        private const val TAG = "RoundImageView"
    }
}