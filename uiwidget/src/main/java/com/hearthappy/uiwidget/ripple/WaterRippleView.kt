package com.hearthappy.uiwidget.ripple

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.image.RoundImageView
import com.hearthappy.uiwidget.utils.CarouselController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.properties.Delegates
import kotlin.random.Random

class WaterRippleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleEventObserver {
    private val currentImageView = RoundImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }
    private val nextImageView = RoundImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        alpha = 0f
    }
    private val list: MutableList<String> = mutableListOf()

    // 协程作用域（类似Activity的lifecycleScope）
    private val lifecycleScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val carouselController = CarouselController()


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                source.lifecycle.removeObserver(this)
                destroyScope()
            }

            else -> {}
        }
    }

    private fun destroyScope() {
        lifecycleScope.cancel()
    }

    private val ripplePath = Path()

    // 状态变量
    private var currentPosition = 0
    private var rippleRadius = 0f
    private var rippleCenterX = 0f
    private var rippleCenterY = 0f
    private var maxRadius = 0f
    private var animType = AnimType.RIPPLE

    // 动画控制
    private val clipPath = Path()
    private val edgePath = Path()
    private var rippleAnimator: ValueAnimator? = null
    private var waterRippleAdapter: WaterRippleAdapter by Delegates.notNull()
    private var onWaterRippleListener: WaterRippleListener? = null
    private var onSelectedListener: ((position: Int, itemCount: Int) -> Unit)? = null
    private var onLongPress: ((position: Int) -> Unit)? = null
    private var onDoubleClick: ((position: Int) -> Unit)? = null

    // 配置参数
    private var rippleDuration = 2000L
    private var carouselInterval = 5000L
    private var enableSwipe = true
    private var enableLoopModel = false // 启用无限循环
    private var enableRippleEffect = true //启用水波纹增强效果
    private var enableAutoCarousel = false //启动自动轮播
    private var cornerRadius = 0f
        set(value) {
            field = value
            currentImageView.setRadius(cornerRadius)
            nextImageView.setRadius(cornerRadius)
        }

    fun setRippleDuration(duration: Long) {
        rippleDuration = duration
    }

    fun setCarouselInterval(interval: Long) {
        carouselInterval = interval
    }

    fun setEnableSwipe(enable: Boolean) {
        enableSwipe = enable
    }

    fun setEnableLoopModel(enable: Boolean) {
        enableLoopModel = enable
    }

    fun setEnableRippleEffect(enable: Boolean) {
        enableRippleEffect = enable
    }

    fun setEnableAutoCarousel(enable: Boolean) {
        enableAutoCarousel = enable
    }

    fun setBorder(width: Float, @ColorInt color: Int) {
        if (width > 0f) {
            currentImageView.setBorder(width, color)
            nextImageView.setBorder(width, color)
        }
    }

    fun setOuterBorder(width: Float, @ColorInt color: Int) {
        currentImageView.setOuterBorder(width, color)
        nextImageView.setOuterBorder(width, color)
    }

    fun setInnerGlow(innerGlowColor: Int, innerGlowRadius: Float) {
        currentImageView.setInnerGlow(innerGlowRadius, innerGlowColor)
        nextImageView.setInnerGlow(innerGlowRadius, innerGlowColor)
    }


    private fun startCarousel() {
        carouselController.start(lifecycleScope, carouselInterval) {
            withContext(Dispatchers.Main) {
                val point = getRandomPoint()
                rippleCenterX = point.x
                rippleCenterY = point.y
                switchToNext()
            }
        }
    }


    init {
        addView(currentImageView)
        addView(nextImageView)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterRippleView)
        enableSwipe =
            typedArray.getBoolean(R.styleable.WaterRippleView_wrv_enable_swipe, enableSwipe)
        enableLoopModel = typedArray.getBoolean(
            R.styleable.WaterRippleView_wrv_enable_loop_model, enableLoopModel
        )
        enableRippleEffect = typedArray.getBoolean(
            R.styleable.WaterRippleView_wrv_enable_ripple_effect, enableRippleEffect
        )
        enableAutoCarousel = typedArray.getBoolean(
            R.styleable.WaterRippleView_wrv_enable_auto_carousel, enableAutoCarousel
        )
        cornerRadius =
            typedArray.getDimension(R.styleable.WaterRippleView_wrv_corner_radius, cornerRadius)
        val borderWidth = typedArray.getDimension(R.styleable.WaterRippleView_wrv_border_width, 0f)
        val borderColor =
            typedArray.getColor(R.styleable.WaterRippleView_wrv_border_color, Color.TRANSPARENT)
        carouselInterval = typedArray.getInteger(
            R.styleable.WaterRippleView_wrv_carousel_interval, carouselInterval.toInt()
        ).toLong()
        rippleDuration = typedArray.getInteger(
            R.styleable.WaterRippleView_wrv_ripple_duration, rippleDuration.toInt()
        ).toLong()
        setBorder(borderWidth, borderColor)
        val outerBorderWidth =
            typedArray.getDimension(R.styleable.WaterRippleView_wrv_outer_border_width, 0f)
        val outerBorderColor = typedArray.getColor(
            R.styleable.WaterRippleView_wrv_outer_border_color, Color.TRANSPARENT
        )
        setOuterBorder(outerBorderWidth, outerBorderColor)
        val innerGlowColor =
            typedArray.getColor(R.styleable.WaterRippleView_wrv_inner_glow_color, Color.TRANSPARENT)
        val innerGlowRadius =
            typedArray.getDimension(R.styleable.WaterRippleView_wrv_inner_glow_radius, 0f)
        setInnerGlow(innerGlowColor, innerGlowRadius)

        typedArray.recycle()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }


    fun initData(imageUrls: List<String>) {
        list.clear()
        list.addAll(imageUrls)
        if (list.isNotEmpty()) {
            waterRippleAdapter.onBindView(list[0], currentImageView, 0)
            onSelectedListener?.invoke(0, getItemCount())
        }
    }

    fun addData(newUrls: List<String>) {
        if (newUrls.isEmpty()) return
        val oldSize = list.size
        list.addAll(newUrls)
        onSelectedListener?.invoke(oldSize - 1, getItemCount())
    }

    fun setAdapter(adapter: WaterRippleAdapter) {
        this.waterRippleAdapter = adapter
    }

    fun getDataSource(): List<String> {
        return list
    }

    fun onLoadMoreListener(listener: () -> Unit) {
        onWaterRippleListener = object : WaterRippleListener {
            override fun onLoadMore() {
                listener.invoke()
            }
        }
    }

    fun onSelectedListener(onSelected: (position: Int, itemCount: Int) -> Unit) {
        onSelectedListener = onSelected
    }

    fun setOnLongPressListener(onLongPress: (Int) -> Unit) {
        this.onLongPress = onLongPress
    }

    fun setOnDoubleClickListener(onDoubleClick: (Int) -> Unit) {
        this.onDoubleClick = onDoubleClick
    }

    fun getItemCount(): Int {
        return list.size
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rippleCenterX = w / 2f
        rippleCenterY = h / 2f
        maxRadius = hypot(w.toFloat(), h.toFloat())
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        carouselController.setVisible(visibility == VISIBLE)
    }


    fun switchToNext() {
        if (list.isEmpty()) return
        val nextPos = if (enableLoopModel) (currentPosition + 1) % list.size else {
            if (currentPosition + 1 >= list.size) return
            currentPosition + 1
        }

        if (currentPosition >= list.size - 2) {
            onWaterRippleListener?.onLoadMore()
        }
        startTransition(nextPos, AnimType.RIPPLE)
    }

    fun switchToPrevious() {
        if (list.isEmpty()) return
        val prevPos = (currentPosition - 1).let {
            if (it < 0) list.lastIndex else it
        }
        startTransition(prevPos, AnimType.SLIDE_RIGHT)
    }

    private fun startTransition(targetPos: Int, type: AnimType) {
        cancelRunningAnimations()
        animType = type // 预加载下一张
        waterRippleAdapter.onBindView(list[targetPos], nextImageView, targetPos)
        when (type) {
            AnimType.RIPPLE -> startRippleAnimation(targetPos)
            else -> return // 其他动画类型暂不处理
        }
    }

    private fun startRippleAnimation(targetPos: Int) {
        rippleAnimator = ValueAnimator.ofFloat(0f, maxRadius).apply {
            duration = rippleDuration
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                rippleRadius = anim.animatedValue as Float
                val progress = rippleRadius / maxRadius // 同步更新透明度 // 计算淡入阶段的局部进度（0~1）
                currentImageView.alpha = 1 - progress
                nextImageView.alpha = progress.coerceIn(0f, 1f)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentPosition = targetPos
                    swapImages()
                    onSelectedListener?.invoke(targetPos, getItemCount())
                    carouselController.resume()
                }

                override fun onAnimationStart(animation: Animator?) {
                    carouselController.pause()
                }
            })
            start()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas) // 确保子View正常绘制
        if (animType == AnimType.RIPPLE && rippleAnimator?.isRunning == true) {
            onDrawChild(canvas)
        }
    }


    private fun onDrawChild(canvas: Canvas) {
        if (enableRippleEffect) drawRippleEffect(canvas) else drawRipple(canvas)
    }

    /**
     * 水波效果
     * @param canvas Canvas
     */
    private fun drawRipple(canvas: Canvas) {
        val saveCount = canvas.save()
        ripplePath.reset()
        ripplePath.addCircle(rippleCenterX, rippleCenterY, rippleRadius, Path.Direction.CW)
        canvas.clipPath(ripplePath)
        nextImageView.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    /**
     * 水波纹增强效果
     * @param canvas Canvas
     */
    private fun drawRippleEffect(canvas: Canvas) {
        if (rippleRadius <= 1f) return

        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // 第一步：绘制当前图片（带透明度）
        nextImageView.draw(canvas)

        // 第二步：创建组合蒙版（裁剪+渐变）
        val combinedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                rippleCenterX, rippleCenterY, rippleRadius, intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK), floatArrayOf(0f, 0.75f, 1f), // 渐变范围调整
                Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        // 第三步：绘制下一张图片（带双重效果）
        canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        currentImageView.draw(canvas)

        // 应用组合蒙版
        canvas.drawPath(createClipPath(), combinedPaint)

        canvas.restoreToCount(saveCount)
    }

    private fun createClipPath(): Path {
        return clipPath.apply {
            reset()
            addCircle(rippleCenterX, rippleCenterY, rippleRadius, Path.Direction.CW) // 添加边缘扰动效果（可选）
            addPath(createRippleEdgePath())
        }
    }

    private fun createRippleEdgePath(): Path {
        edgePath.reset()
        val angleStep = 5f
        val amplitude = rippleRadius * 0.01f // 波动幅度
        (0 until 360 step angleStep.toInt()).forEach { angle ->
            val radian = Math.toRadians(angle.toDouble())
            val offsetX = (amplitude * cos(radian)).toFloat()
            val offsetY = (amplitude * sin(radian)).toFloat()
            edgePath.lineTo(
                rippleCenterX + (rippleRadius + offsetX) * cos(radian).toFloat(), rippleCenterY + (rippleRadius + offsetY) * sin(radian).toFloat()
            )
        }
        edgePath.close()
        return edgePath
    }

    private fun drawRippleEffect1(canvas: Canvas) {
        val saveCount =
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null) // 先绘制当前图片（带透明度）
        currentImageView.draw(canvas) // 再绘制下一张图片（带透明度+裁剪）
        ripplePath.reset()
        ripplePath.addCircle(rippleCenterX, rippleCenterY, rippleRadius, Path.Direction.CW)
        canvas.clipPath(ripplePath)
        nextImageView.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun swapImages() {
        if (currentImageView.drawable == nextImageView.drawable) return
        val temp = currentImageView.drawable
        currentImageView.setImageDrawable(nextImageView.drawable)
        nextImageView.setImageDrawable(temp)
        currentImageView.alpha = 1f
        nextImageView.alpha = 0f
        rippleRadius = 0f
    }

    private fun cancelRunningAnimations() {
        rippleAnimator?.cancel()
    }

    // 手势处理
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> carouselController.pause()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> carouselController.resume()
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                onDoubleClick?.invoke(currentPosition)
                return true
            }

            override fun onDown(e: MotionEvent): Boolean = true
            override fun onLongPress(e: MotionEvent?) {
                onLongPress?.invoke(currentPosition)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean = false

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (!enableSwipe) return false
                val deltaX = e2.x - e1.x
                return when {
                    deltaX > SWIPE_THRESHOLD -> {
                        switchToNext()
                        true
                    }

                    deltaX < -SWIPE_THRESHOLD -> {
                        switchToPrevious()
                        true
                    }

                    else -> false
                }
            }
        })
    }

    private fun handleTap(x: Float, y: Float) {
        rippleCenterX = x.coerceIn(0f, width.toFloat())
        rippleCenterY = y.coerceIn(0f, height.toFloat())
        switchToNext()
    }

    private fun getRandomPoint(): PointF {
        return PointF(
            Random.nextInt(1, width - 1).toFloat(), Random.nextInt(1, height - 1).toFloat()
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (enableAutoCarousel) startCarousel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelRunningAnimations()
        destroyScope()
        carouselController.cancel()
    }

    private enum class AnimType { RIPPLE, SLIDE_LEFT, SLIDE_RIGHT }

    interface WaterRippleAdapter {
        fun onBindView(data: String, imageView: ImageView, position: Int)
    }

    interface WaterRippleListener {
        fun onLoadMore()
    }


    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val TAG = "WaterRippleView"
    }
}