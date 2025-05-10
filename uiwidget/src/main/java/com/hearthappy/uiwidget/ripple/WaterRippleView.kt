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
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.CarouselController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.properties.Delegates
import kotlin.random.Random

class WaterRippleView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), LifecycleEventObserver {
    private var currentViewHolder: ViewHolder? = null
    private var targetViewHolder: ViewHolder? = null
    private var preloadPreviousViewHolder: ViewHolder? = null
    private var preloadNextViewHolder: ViewHolder? = null
    private var waterRippleAdapter: Adapter by Delegates.notNull()

    // 回收池（优化性能）
    private val viewHolderPool = LinkedList<ViewHolder>()

    private val lifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val carouselController = CarouselController()

    // 添加数据观察者
    private val dataSetObserver = object : DataSetObserver {
        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyDataSetChanged()
        }
    }

    //协程域生命周期监听
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
    private val combinedPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    // 状态变量
    private var currentPosition = 0
    private var targetPosition = 1
    private var rippleRadius = 0f
    private var rippleCenterX = 0f
    private var rippleCenterY = 0f
    private var maxRadius = 0f
    private var isClickDistrict: Pair<Boolean, Boolean> by Delegates.notNull()
    private var isSliding = false

    // 动画控制
    private val clipPath = Path()
    private val edgePath = Path()
    private var rippleAnimator: ValueAnimator? = null

    // 配置参数
    private var rippleDuration = 2000L
    private var carouselInterval = 5000L
    private var enableSwipe = false
    private var enableLoopModel = false // 启用无限循环
    private var enableRippleEffect = true //启用水波纹增强效果
    private var enableAutoCarousel = false //启动自动轮播

    private var onWaterRippleListener: WaterRippleListener? = null
    private var onSelectedEndListener: ((position: Int, itemCount: Int) -> Unit)? = null
    private var onSelectedStartListener: ((position: Int, itemCount: Int) -> Unit)? = null
    private var onLongPressListener: ((position: Int) -> Unit)? = null
    private var onDoubleClickListener: ((position: Int) -> Unit)? = null


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterRippleView)
        enableSwipe = typedArray.getBoolean(R.styleable.WaterRippleView_wrv_enable_swipe, enableSwipe)
        enableLoopModel = typedArray.getBoolean(R.styleable.WaterRippleView_wrv_enable_loop_model, enableLoopModel)
        enableRippleEffect = typedArray.getBoolean(R.styleable.WaterRippleView_wrv_enable_ripple_effect, enableRippleEffect)
        enableAutoCarousel = typedArray.getBoolean(R.styleable.WaterRippleView_wrv_enable_auto_carousel, enableAutoCarousel)
        typedArray.recycle()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private fun initView() {
        if (waterRippleAdapter.getItemCount() == 0) return
        currentViewHolder = onCreateViewHolder().also {
            addView(it.itemView)
            waterRippleAdapter.onBindViewHolder(it, 0)
            onSelectedEndListener?.invoke(0, waterRippleAdapter.getItemCount())
            onSelectedStartListener?.invoke(0, waterRippleAdapter.getItemCount())
        }
        preloadAdjacentViews()

    }

    private fun preloadAdjacentViews() { // 预加载下一个（偏移+1，使用默认添加位置）
        preloadView(offset = 1, getHolder = { preloadNextViewHolder }, setHolder = { preloadNextViewHolder = it })
        preloadView(offset = -1, getHolder = { preloadPreviousViewHolder }, setHolder = { preloadPreviousViewHolder = it }, addViewIndex = 0)
    }

    /**
     * addViewIndex: -1表示添加到末尾，0表示添加到最底层
     * @param offset Int
     * @param getHolder Function0<ViewHolder?>
     * @param setHolder Function1<ViewHolder, Unit>
     * @param addViewIndex Int
     */
    private fun preloadView(offset: Int, getHolder: () -> ViewHolder?, setHolder: (ViewHolder) -> Unit, addViewIndex: Int = -1) {
        if (waterRippleAdapter.getItemCount() == 0) return
        val position = calculatePosition(currentPosition + offset, waterRippleAdapter.getItemCount())

        if (position == -1) return
        val currentHolder = getHolder()
        if (currentHolder == null) {
            val newHolder = onCreateViewHolder().apply {
                itemView.alpha = 0f // 根据索引决定添加位置
                if (addViewIndex == -1) addView(itemView)
                else addView(itemView, addViewIndex)
                waterRippleAdapter.onBindViewHolder(this, position)
            }
            setHolder(newHolder)
        } else {
            waterRippleAdapter.onBindViewHolder(currentHolder, position)
        }

    }

    private fun calculatePosition(position: Int, itemCount: Int): Int {
        return if (enableLoopModel) (position + itemCount) % itemCount else position.takeIf { it in 0 until itemCount } ?: -1
    }

    private fun onCreateViewHolder(): ViewHolder {
        return viewHolderPool.pollLast() ?: waterRippleAdapter.onCreateViewHolder(this)
    }

    private fun onRecycleViewHolder(holder: ViewHolder) {
        if (holder != preloadPreviousViewHolder && holder != preloadNextViewHolder) {
            viewHolderPool.offer(holder)
            removeView(holder.itemView)
        }
    }


    fun setAdapter(adapter: Adapter) {
        this.waterRippleAdapter = adapter.apply { registerDataSetObserver(dataSetObserver) }
        initView()
    }

    //数据刷新方法
    fun notifyDataSetChanged() {
        val newCount = waterRippleAdapter.getItemCount()

        //清理缓存ViewHolder
        preloadNextViewHolder?.let {
            onRecycleViewHolder(it)
            preloadNextViewHolder = null
        }
        preloadAdjacentViews()
        onSelectedStartListener?.invoke(currentPosition, newCount)
        onSelectedEndListener?.invoke(currentPosition, newCount) //        invalidate()
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


    private fun prepareToNext(onReady: (Int) -> Unit = {}) {
        val itemCount = waterRippleAdapter.getItemCount()
        if (itemCount == 0) return
        targetPosition = calculatePosition(currentPosition + 1, itemCount)
        if (targetPosition == -1) return
        if (targetPosition >= itemCount - 1) onWaterRippleListener?.onLoadMore()
        startTransition(true, onReady)
    }

    private fun prepareToPrevious(onReady: (Int) -> Unit = {}) {
        val itemCount = waterRippleAdapter.getItemCount()
        if (itemCount == 0) return
        targetPosition = calculatePosition(currentPosition - 1, itemCount)
        if (targetPosition == -1) return
        startTransition(false, onReady)

    }

    private fun startTransition(toNext: Boolean, onReady: (Int) -> Unit) {
        cancelRunningAnimations()
        targetViewHolder = if (toNext) preloadNextViewHolder.also { preloadNextViewHolder = null } else preloadPreviousViewHolder.also { preloadPreviousViewHolder = null } ?: onCreateViewHolder().apply {
            itemView.alpha = 0f
            addView(itemView)
            waterRippleAdapter.onBindViewHolder(this, targetPosition)
        }
        onReady(targetPosition)
    }

    /**
     * 创建水波纹动画
     * @param rippleDuration Long
     * @param startValue Float
     * @param endValue Float
     */
    private fun rippleAnimator(rippleDuration: Long, startValue: Float, endValue: Float) {
        rippleAnimator = ValueAnimator.ofFloat(startValue, endValue).apply {
            duration = rippleDuration
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                rippleRadius = anim.animatedValue as Float
                val progress = rippleRadius / endValue // 同步更新透明度 // 计算淡入阶段的局部进度（0~1）
                currentViewHolder?.itemView?.alpha = 1 - progress
                targetViewHolder?.itemView?.alpha = progress.coerceIn(0f, 1f)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    swapImages()
                    onSelectedEndListener?.invoke(targetPosition, waterRippleAdapter.getItemCount())
                    carouselController.resume()
                }

                override fun onAnimationStart(animation: Animator?) {
                    currentPosition = targetPosition
                    onSelectedStartListener?.invoke(targetPosition, waterRippleAdapter.getItemCount())
                    preloadAdjacentViews()
                    carouselController.pause()
                }
            })
            start()
        }
    }

    /**
     * 重置视图动画
     */
    private fun resetViewAnimator() {
        ValueAnimator.ofFloat(rippleRadius, 0f).apply {
            duration = 200
            addUpdateListener {
                rippleRadius = it.animatedValue as Float
                val progress = rippleRadius / maxRadius
                currentViewHolder?.itemView?.alpha = 1 - progress
                targetViewHolder?.itemView?.alpha = progress
                invalidate()
            }
            doOnEnd { preloadAdjacentViews() }
            start()
        }
    }

    /**
     * 侧滑更新视图
     * @param e1 MotionEvent
     * @param e2 MotionEvent
     */
    private fun slideUpdate(e1: MotionEvent, e2: MotionEvent) {
        val distance = calculateStraightLineDistance(e1, e2)
        if (distance > SWIPE_DISTANCE_THRESHOLD) {
            isSliding = true
            rippleRadius = distance.toFloat()
            val progress = rippleRadius / maxRadius // 同步更新透明度 // 计算淡入阶段的局部进度（0~1）
            currentViewHolder?.itemView?.alpha = 1 - progress
            targetViewHolder?.itemView?.alpha = progress //                        Log.d(TAG, "onScroll: $progress")
            invalidate()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas) // 确保子View正常绘制
        onDrawChild(canvas)
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
        targetViewHolder?.itemView?.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    /**
     * 水波纹增强效果
     * @param canvas Canvas
     */
    private fun drawRippleEffect(canvas: Canvas) {
        if (rippleRadius <= 1f) return

        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null) // 第一步：绘制当前图片（带透明度）
        targetViewHolder?.itemView?.draw(canvas)

        // 第二步：创建组合蒙版（裁剪+渐变）
        combinedPaint.apply {
            shader = RadialGradient(rippleCenterX, rippleCenterY, rippleRadius, intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK), floatArrayOf(0f, 0.75f, 1f), // 渐变范围调整
                Shader.TileMode.CLAMP)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        // 第三步：绘制下一张图片（带双重效果）
        currentViewHolder?.itemView?.draw(canvas) // 应用组合蒙版
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
            edgePath.lineTo(rippleCenterX + (rippleRadius + offsetX) * cos(radian).toFloat(), rippleCenterY + (rippleRadius + offsetY) * sin(radian).toFloat())
        }
        edgePath.close()
        return edgePath
    }


    private fun swapImages() {
        currentViewHolder?.let { onRecycleViewHolder(it) }
        currentViewHolder = targetViewHolder
        targetViewHolder = null
        currentViewHolder?.itemView?.alpha = 1f
        rippleRadius = 0f
    }

    private fun cancelRunningAnimations() {
        rippleAnimator?.cancel()
    }

    // 手势处理
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handlerTouchEvent = gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> carouselController.pause()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onSwipeUp()
                carouselController.resume()
            }
        }
        return handlerTouchEvent || super.onTouchEvent(event)
    }


    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                onDoubleClickListener?.invoke(currentPosition)
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                onSingleTapDown(e)
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                onLongPressListener?.invoke(currentPosition)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!isSliding) onSingleTapUp(e.x)
                return true
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (isReasonable(targetPosition)) slideUpdate(e1, e2)
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean = false
        })
    }

    /**
     * 单击按下或侧滑按下
     * @param e MotionEvent
     */
    private fun onSingleTapDown(e: MotionEvent) {
        rippleCenterX = e.x
        rippleCenterY = e.y.coerceIn(0f, height.toFloat())
        isClickDistrict = isClickDistrict(e.x)
        carouselController.pause()
        when {
            isClickDistrict.first -> prepareToNext()
            isClickDistrict.second -> prepareToPrevious()
        }
    }


    /**
     * 处理滑动松开
     */
    private fun onSwipeUp() {
        if (isSliding) {
            if (isRippleOverOneThird(rippleRadius, maxRadius)) {
                rippleAnimator(200, rippleRadius, maxRadius)
            } else {
                resetViewAnimator()
            }
            isSliding = false
        }
    }


    /**
     * 单击松开
     * @param x Float
     */
    private fun onSingleTapUp(x: Float) {
        if (isReasonable(targetPosition)) {
            val district = isClickDistrict(x)
            when {
                district.first -> rippleAnimator(rippleDuration, 0f, maxRadius)
                district.second -> rippleAnimator(rippleDuration, 0f, maxRadius)
                else -> return // 中间区域不响应
            }
        }
    }

    /**
     * 判断水波纹半径是否超过最大半径的三分之一
     * @param rippleRadius 当前水波纹半径
     * @param maxRadius 水波纹最大半径
     * @return true：rippleRadius > maxRadius/3；false：未超过
     */
    private fun isRippleOverOneThird(rippleRadius: Float, maxRadius: Float): Boolean { // 避免maxRadius为0导致除零错误（可选防御性判断）
        if (maxRadius <= 0) return false
        return rippleRadius > (maxRadius / 3f)
    }

    private fun isReasonable(targetPosition: Int): Boolean {
        return targetPosition in 0 until waterRippleAdapter.getItemCount()
    }

    /**
     * 判断点击区域
     * @param x Float
     * @return Pair<Boolean, Boolean> 右侧点击、左侧点击
     */
    private fun isClickDistrict(x: Float): Pair<Boolean, Boolean> {
        return (x > width * SPLIT_THRESHOLD) to (x < width * (1 - SPLIT_THRESHOLD))
    }

    private fun getRandomPoint(): PointF {
        return PointF(Random.nextInt(1, width - 1).toFloat(), Random.nextInt(1, height - 1).toFloat())
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
        waterRippleAdapter.unregisterDataSetObserver(dataSetObserver)
    }

    private fun startCarousel() {
        carouselController.start(lifecycleScope, carouselInterval) {
            withContext(Dispatchers.Main) {
                val point = getRandomPoint()
                rippleCenterX = point.x
                rippleCenterY = point.y
                prepareToNext { rippleAnimator(rippleDuration, 0f, maxRadius) }
            }
        }
    }


    fun onLoadMoreListener(listener: () -> Unit) {
        onWaterRippleListener = object : WaterRippleListener {
            override fun onLoadMore() = listener.invoke()
        }
    }

    fun onSelectedEndListener(onSelected: (position: Int, itemCount: Int) -> Unit) {
        onSelectedEndListener = onSelected
    }

    fun onSelectedStartListener(onSelected: (position: Int, itemCount: Int) -> Unit) {
        onSelectedStartListener = onSelected
    }

    fun setOnLongPressListener(onLongPress: (Int) -> Unit) {
        this.onLongPressListener = onLongPress
    }

    fun setOnDoubleClickListener(onDoubleClick: (Int) -> Unit) {
        this.onDoubleClickListener = onDoubleClick
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
        if (enableAutoCarousel) startCarousel()
        else carouselController.cancel()
    }

    interface WaterRippleListener {
        fun onLoadMore()
    }

    // 数据变化监听接口
    interface DataSetObserver {
        fun onChanged()
        fun onItemRangeChanged(positionStart: Int, itemCount: Int)
    }

    // ViewHolder 抽象类
    abstract class ViewHolder(view: View) {
        val itemView: View = view
    }

    // Adapter 接口
    abstract class Adapter {

        // 创建 ViewHolder
        abstract fun onCreateViewHolder(parent: ViewGroup): ViewHolder

        // 绑定数据
        abstract fun onBindViewHolder(holder: ViewHolder, position: Int)

        // 数据总数（新增）
        abstract fun getItemCount(): Int


        private val observers = mutableListOf<DataSetObserver>()

        // 注册观察者
        internal fun registerDataSetObserver(observer: DataSetObserver) {
            observers.add(observer)
        }

        // 注销观察者
        internal fun unregisterDataSetObserver(observer: DataSetObserver) {
            observers.remove(observer)
        }

        // 通知数据变化
        protected fun notifyDataSetChanged() {
            observers.forEach { it.onChanged() }
        }

        // 其他必要通知方法
        protected fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
            observers.forEach { it.onItemRangeChanged(positionStart, itemCount) }
        }
    }

    companion object {
        private var SPLIT_THRESHOLD = 0.5f // 左右区域划分比例
        private const val SWIPE_DISTANCE_THRESHOLD = 100f
        private const val SWIPE_RIGHT = 0
        private const val SWIPE_LEFT = 1
        private const val SWIPE_DOWN = 2
        private const val SWIPE_UP = 3
        private const val TAG = "WaterRippleView"


        /**
         * 计算手势滑动的水平和垂直距离
         * @param e1 手势起点事件（按下时的 MotionEvent）
         * @param e2 手势终点事件（抬起时的 MotionEvent）
         * @return 包含水平距离（x）和垂直距离（y）的 Pair（绝对值）
         */
        fun calculateSwipeDistance(e1: MotionEvent, e2: MotionEvent): Pair<Float, Float> {
            val horizontalDistance = e2.x - e1.x  // 水平方向实际差值（可能为负）
            val verticalDistance = e2.y - e1.y    // 垂直方向实际差值（可能为负）

            // 返回绝对值（实际滑动长度）
            return abs(horizontalDistance) to abs(verticalDistance)
        }

        /**
         * 计算手势滑动的直线距离（欧几里得距离）
         * @param e1 手势起点事件
         * @param e2 手势终点事件
         * @return 直线滑动距离（像素）
         */
        fun calculateStraightLineDistance(e1: MotionEvent, e2: MotionEvent): Double {
            val dx = e2.x - e1.x
            val dy = e2.y - e1.y
            return sqrt((dx * dx + dy * dy).toDouble())
        }
    }
}