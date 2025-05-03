package com.hearthappy.uiwidget.ripple

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.addListener
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Created Date: 2025/4/28
 * @author ChenRui
 * ClassDescription：自定义水波纹切换控件
 */
class WaterRippleViewer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var radius = 0f // 圆半径
    private var mMaxRadius = 0 // 最大半径
    private var rx = 0f // 圆X坐标
    private var ry = 0f // 圆Y坐标
    private var mFirstX = 0f // 点击起点X
    private var mFirstY = 0f // 点击起点Y
    private var mPaint: Paint? = null
    private var widgetWidth = 0f // 控件宽
    private var widgetHeight = 0f // 控件高
    private var mPath: Path? = null
    private var urls: MutableList<String> = mutableListOf() // Url集合

    private var mCircleAnim: ValueAnimator? = null // 圆形动画
    private var mRightAnim: ValueAnimator? = null // 侧滑动画
    private var scrollX = 0f // 滑动偏移量
    private var isViewVisible = false

    /*自定义属性*/
    private var isAlphaShow = true // 显示图片是否执行渐变动画
    private var slideAnimDuration = 1000L // 圆形动画执行时长
    private var waterAnimDuration = 2000L // 圆形动画执行时长
    private var animatorType = AnimatorType.ANIM_CIRCLE // 动画类型，用于处理动画类型，根据手势动作执行不同动画圆形滑动还是侧滑动画时使用
    private var isEnableSliding = true //默认启用侧滑查看
    private var interval = 5000L //轮播间隔
    private val bitmapLoader: BitmapLoader by lazy { BitmapLoader(context) }
    private var onImageListener: OnImageListener? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var scaleType = ScaleType.CENTER_CROP
    private var placeholder: Bitmap? = null

    // 替换原有的三个 Bitmap 变量
    private val bitmapState = BitmapState()

    // 当前显示位置
    private var currentPosition = 0
        set(value) {
            field = value
            bitmapState.updatePosition(value)
            loadAdjacentBitmaps() // 位置变化时预加载
        }

    //=== 核心修改点2：优化图片加载逻辑 ===//
    private fun loadAdjacentBitmaps() { // 预加载前后各一张
        loadBitmap(currentPosition - 1, isPreload = true)
        loadBitmap(currentPosition + 1, isPreload = true)
    }

    enum class ScaleType {
        CENTER_CROP, FIT_CENTER, MATRIX
    }

    enum class AnimatorType {
        ANIM_CIRCLE, ANIM_RIGHT_SLIDE, ANIM_LEFT_SLIDE
    }


    fun setImageLoader(onImageListener: OnImageListener) {
        this.onImageListener = onImageListener
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    fun setScaleType(scaleType: ScaleType) {
        this.scaleType = scaleType
        invalidate()
    }

    fun setEnableSliding(enableSliding: Boolean) {
        isEnableSliding = enableSliding
    }

    fun getItemCount(): Int {
        return urls.size
    }

    fun setDuration(waterAnimDuration: Long = 2000L, slideAnimDuration: Long = 1000L) {
        this.waterAnimDuration = waterAnimDuration
        this.slideAnimDuration = slideAnimDuration
    }

    fun initData(urls: List<String>) {
        this.urls = urls.toMutableList()
        if (urls.isNotEmpty()) {
            loadBitmap(currentPosition)
            loadAdjacentBitmaps()
        }
    }

    fun addData(newUrls: List<String>) {
        if (newUrls.isNotEmpty()) {
            urls.addAll(newUrls)
            currentPosition = currentPosition.coerceAtMost(urls.lastIndex)
        }
    }


    private fun initPath() {
        mPath = Path()
    }

    private fun initPaint() {
        mPaint = Paint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widgetWidth = w.toFloat()
        widgetHeight = h.toFloat()
        mMaxRadius = sqrt((w * w + h * h).toDouble()).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmapState.get(currentPosition) == null) {
            drawPlaceholder(canvas)
            return
        }
        when (animatorType) {
            AnimatorType.ANIM_CIRCLE -> drawRippleEffect(canvas)
            AnimatorType.ANIM_RIGHT_SLIDE, AnimatorType.ANIM_LEFT_SLIDE -> drawSlideEffect(canvas)
        }
    }

    /**
     * 水波纹效果
     * @param canvas Canvas
     */
    private fun drawRippleEffect(canvas: Canvas) { // 当前页
        // 绘制前一张图片（渐变消失）
        bitmapState.get(currentPosition - 1)?.let { prevBitmap ->
            mPaint?.alpha = computerAlpha() // 计算透明度
            val matrix = getMatrix(prevBitmap, scaleType)
            canvas.drawBitmap(prevBitmap, matrix, mPaint)
        }

        // 绘制当前图片（水波纹效果）
        bitmapState.get(currentPosition)?.let { currentBitmap ->
            mPaint?.reset()
            mPath?.reset()

            if (isAlphaShow) {
                mPaint?.alpha = computerAlphaFromScratch()
            }

            mPath?.addCircle(rx, ry, radius, Path.Direction.CW)
            canvas.clipPath(mPath!!)
            val currentMatrix = getMatrix(currentBitmap, scaleType)
            canvas.drawBitmap(currentBitmap, currentMatrix, mPaint)
        }
    }

    /**
     * 侧滑效果
     * @param canvas Canvas
     */
    private fun drawSlideEffect(canvas: Canvas) {
        bitmapState.get(currentPosition - 1)?.let { previous ->
            val matrix = getMatrix(previous, scaleType).apply {
                postTranslate(-width + scrollX, 0f)
            }
            canvas.drawBitmap(previous, matrix, mPaint)
        }

        bitmapState.get(currentPosition)?.let { current ->
            val matrix = getMatrix(current, scaleType).apply {
                postTranslate(scrollX, 0f)
            }
            canvas.drawBitmap(current, matrix, mPaint)
        }
    }

    /**
     * 预加载
     * @param canvas Canvas
     */
    private fun drawPlaceholder(canvas: Canvas) {
        placeholder?.let { canvas.drawBitmap(it, 0f, 0f, mPaint) }
    }


    /**
     * 图片渲染方式
     * @param bitmap Bitmap?
     * @param scaleType ScaleType
     * @return Matrix
     */
    private fun getMatrix(bitmap: Bitmap?, scaleType: ScaleType): Matrix {
        val matrix = Matrix()
        when (scaleType) {
            ScaleType.CENTER_CROP -> {
                val widthRatio = widgetWidth / bitmap!!.width.toFloat()
                val heightRatio = widgetHeight / bitmap.height.toFloat()
                val scale = if (widthRatio > heightRatio) widthRatio else heightRatio
                matrix.postScale(scale, scale)
                matrix.postTranslate(
                    (widgetWidth - bitmap.width * scale) / 2,
                    (widgetHeight - bitmap.height * scale) / 2
                )
            }

            ScaleType.FIT_CENTER -> {
                val widthRatio = widgetWidth / bitmap!!.width.toFloat()
                val heightRatio = widgetHeight / bitmap.height.toFloat()
                val scale = if (widthRatio < heightRatio) widthRatio else heightRatio
                matrix.postScale(scale, scale)
                matrix.postTranslate(
                    (widgetWidth - bitmap.width * scale) / 2,
                    (widgetHeight - bitmap.height * scale) / 2
                )
            }

            ScaleType.MATRIX -> { // 这里可以根据需要实现自定义矩阵逻辑
            }
        }
        return matrix
    }


    /**
     * 计算透明值（从有到无）
     *
     * @return 0~255的值
     */
    private fun computerAlpha(): Int { // 透明度计算公式(0~1)：（总长-移动距离)/总长 --->(mMaxRadius-radius)/mMaxRadius;
        // 透明度计算公式(0~255)：剩余距离（等于：总长-移动半径）/总长=alpha/255; alpha=radius/mMaxRadius* 255;
        return ((mMaxRadius - radius) / mMaxRadius * 255).toInt()
    }

    /**
     * 计算透明值（从无到有）
     *
     * @return 0~255的值
     */
    private fun computerAlphaFromScratch(): Int { // 透明度计算公式(0~1)：（总长-移动距离)/总长 --->(mMaxRadius-radius)/mMaxRadius;
        // 透明度计算公式(0~255)：剩余距离（等于：总长-移动半径）/总长=alpha/255; alpha=radius/mMaxRadius* 255;
        return (radius / mMaxRadius * 255).toInt()
    }


    private fun startAnim(animatorType: AnimatorType) {
        when (animatorType) {
            AnimatorType.ANIM_CIRCLE -> { // 确保下一张已加载
                if (bitmapState.get(currentPosition + 1) == null) {
                    loadBitmap(currentPosition + 1)
                }
                startCircleAnim()
            }

            AnimatorType.ANIM_RIGHT_SLIDE -> { // 确保上一张已加载
                if (bitmapState.get(currentPosition - 1) == null) {
                    loadBitmap(currentPosition - 1)
                }
                startSlideAnim(widgetWidth, 0f)
            }

            AnimatorType.ANIM_LEFT_SLIDE -> {
                if (bitmapState.get(currentPosition + 1) == null) {
                    loadBitmap(currentPosition + 1)
                }
                startSlideAnim(0f, widgetWidth)
            }
        }
    }

    /**
     * 变化圆动画
     */
    private fun startCircleAnim() {

        mCircleAnim = ValueAnimator.ofFloat(0f, mMaxRadius.toFloat())
        mCircleAnim?.setDuration(waterAnimDuration)
        mCircleAnim?.addUpdateListener { animation ->
            radius = animation.animatedValue as Float
            invalidate()
        }
        mCircleAnim?.addListener(onEnd = {
            onImageListener?.onSelected(currentPosition)
        }, onStart = {
            onImageListener?.onPreSelected(currentPosition)
        })
        mCircleAnim?.start()
    }

    /**
     * 向右执行动画
     */
    private fun startSlideAnim(start: Float, end: Float) {
        mRightAnim = ValueAnimator.ofFloat(start, end).apply {
            duration = slideAnimDuration
            addUpdateListener { animation ->
                scrollX = animation.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) { // 动画开始前预加载必要资源
                    when (animatorType) {
                        AnimatorType.ANIM_RIGHT_SLIDE -> loadBitmap(currentPosition - 1)
                        AnimatorType.ANIM_LEFT_SLIDE -> loadBitmap(currentPosition + 1)
                        else -> Unit
                    }
                    onImageListener?.onPreSelected(currentPosition)
                }

                override fun onAnimationEnd(animation: Animator) {
                    scrollX = 0f

                    // 根据动画类型更新位置
                    when (animatorType) {
                        AnimatorType.ANIM_RIGHT_SLIDE -> { // 左滑切换到下一张（currentPosition 已在外部更新）
                            updateDisplay()
                        }

                        AnimatorType.ANIM_LEFT_SLIDE -> { // 右滑切换到上一张（currentPosition 已在外部更新）
                            updateDisplay()
                        }

                        else -> Unit
                    }

                    // 清理非可见区域资源（由 BitmapState 自动处理）
                    onImageListener?.onSelected(currentPosition)
                }
            })
            start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean { // 将事件交给GestureDetector处理（可以判断手势滑动方向）
        mGestureDetector.onTouchEvent(event)
        return true
    }

    private var mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean {
                mFirstX = e.x
                mFirstY = e.y
                return false
            }

            override fun onShowPress(e: MotionEvent) {
                Log.i(TAG, "onShowPress: ")
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean { // 如果向右动画执行完了，才操作下面
                if (mRightAnim != null && mRightAnim!!.isRunning) {
                    return true
                } // 如果点击时，动画还在运行则清理动画
                isRunningClearAnim // 按下，初始化圆心
                initCenterCoordinates(mFirstX, mFirstY) // 交换上下层
                switchNextPosition(AnimatorType.ANIM_CIRCLE)
                return false
            }

            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                Log.i(TAG, "onScroll: $distanceX")
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                Log.i(TAG, "onLongPress: ")
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean { // 切换操作时，若果向右动画或圆形动画在执行则不操作下一个
                if (mRightAnim != null && mRightAnim!!.isRunning || mCircleAnim != null && mCircleAnim!!.isRunning) {
                    return true
                }

                if (e1.x - e2.x > FLIP_DISTANCE && isEnableSliding) {
                    Log.i(TAG, "向左滑...")
                    switchNextPosition(AnimatorType.ANIM_LEFT_SLIDE)
                    return true
                }
                if (e2.x - e1.x > FLIP_DISTANCE && isEnableSliding) {
                    switchPrePosition()
                    Log.i(TAG, "向右滑...")
                    return true
                }
                if (e1.y - e2.y > FLIP_DISTANCE) {
                    Log.i(TAG, "向上滑...")
                    return true
                }
                if (e2.y - e1.y > FLIP_DISTANCE) {
                    Log.i(TAG, "向下滑...")
                    return true
                }
                return false
            }
        })

    init {
        initPaint()
        initPath()
    }

    /**
     * 初始化圆心坐标
     *
     * @param firstX 按下的X
     * @param firstY 按下的Y
     */
    private fun initCenterCoordinates(firstX: Float, firstY: Float) {
        rx = firstX
        ry = firstY
    }

    private val isRunningClearAnim: Unit
        /**
         * 如果点击时，动画还在运行则清理动画
         */
        get() {
            if (mCircleAnim != null && mCircleAnim!!.isRunning) {
                mCircleAnim!!.end()
                Log.i(TAG, "onTouchEvent: 动画没结束")
            }
        }

    private fun loadBitmap(
        position: Int, isPreload: Boolean = false, onComplete: (() -> Unit)? = null
    ) {
        if (position !in urls.indices) return
        if (bitmapState.get(position) != null) return
        bitmapLoader.loadBitmap(urls[position]) { bitmap ->
            bitmap?.let {
                bitmapState.put(position, it)
                if (!isPreload) {
                    updateDisplay()
                }
                onComplete?.invoke()
            }
        }
    }

    private fun updateDisplay() { // 触发重绘并更新矩阵
        invalidate()
        onImageListener?.onSelected(currentPosition)
    }


    //=== 核心修改点4：优化切换逻辑 ===//
    private fun switchNextPosition(animatorType: AnimatorType) {
        if (currentPosition == urls.lastIndex && onLoadMoreListener == null) return

        // 触发加载更多
        if (currentPosition >= urls.size - 2) {
            onLoadMoreListener?.onLoadMore()
        }

        currentPosition = (currentPosition + 1).coerceAtMost(urls.lastIndex)
        startAnim(animatorType)
    }

    private fun switchPrePosition() {
        currentPosition = (currentPosition - 1).coerceAtLeast(0)
        startAnim(AnimatorType.ANIM_RIGHT_SLIDE)
    }


    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        isViewVisible = visibility == VISIBLE
        when (visibility) {
            VISIBLE -> { // 加载可见区域内容
                loadBitmap(currentPosition)
                loadAdjacentBitmaps()
            }

            else -> { // 保留当前位内容，清理其他
                bitmapState.trimCache()
            }
        }
    }

    /**
     * 设置轮播间隔和滚动持续时间
     * @param interval Long 间隔时长
     */
    fun setCarouseInterval(interval: Long) {
        this.interval = interval
    }

    fun startAutoCarouse() {
        postDelayed(carouselTask, interval)
    }

    private val carouselTask = object : Runnable {
        override fun run() {
            if (isViewVisible && bitmapState.get(currentPosition) != null) {
                Log.d(TAG, "run: $currentPosition")
                if (currentPosition >= Int.MAX_VALUE) {
                    currentPosition = 0
                }
                val randomPoint = getRandomPoint(widgetWidth.toInt(), widgetHeight.toInt())
                initCenterCoordinates(
                    randomPoint.first.toFloat(), randomPoint.second.toFloat()
                ) // 交换上下层
                switchNextPosition(AnimatorType.ANIM_CIRCLE)
            }
            postDelayed(this, interval)
        }

    }

    private fun stopAutoCarouse() {
        removeCallbacks(carouselTask)
    }

    /**
     * 生成随机的 x 和 y 坐标
     * @param spaceWidth Int
     * @param spaceHeight Int
     * @return Pair<Int, Int>
     */
    fun getRandomPoint(spaceWidth: Int, spaceHeight: Int): Pair<Int, Int> {
        val randomX = Random.nextInt(1, spaceWidth - 1)
        val randomY = Random.nextInt(1, spaceHeight - 1)
        return Pair(randomX, randomY)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (urls.isNotEmpty()) { // 预加载当前及下一张
            loadBitmap(currentPosition, isPreload = true)
            loadBitmap(currentPosition + 1, isPreload = true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoCarouse() //        clearBitmap()
        bitmapState.destroy() // 彻底释放资源
    }


    interface OnImageListener {
        //加载图片回调，用户自行将bitmap通过callback函数返回
        fun onBindView(url: String, position: Int, callback: ImageLoadCallback)

        //动画结束时回调
        fun onSelected(position: Int)

        //预选中时回调
        fun onPreSelected(position: Int)
    }

    interface ImageLoadCallback {
        fun onSuccess(bitmap: Bitmap)
        fun onFailed(e: Exception)
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    companion object {
        const val TAG: String = "WaterRippleViewer"
        private const val FLIP_DISTANCE: Int = 100 // 滑动距离
    }
}