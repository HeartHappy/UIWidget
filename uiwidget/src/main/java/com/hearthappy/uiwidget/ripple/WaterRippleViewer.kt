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
import android.util.LruCache
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.animation.addListener
import kotlin.math.sqrt
import kotlin.properties.Delegates

/**
 * Created Date: 2025/4/28
 * @author ChenRui
 * ClassDescription：自定义水波纹切换控件
 */
class WaterRippleViewer @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var currentPosition = 0 // 当前张的下标
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
    private var mBottomMatrix: Matrix by Delegates.notNull() // 上一个矩阵
    private var mBottomBitmap: Bitmap? = null // 上一个Bitmap
    private var mCurrentBitmap: Bitmap? = null  // 当前Bitmap
    private var mNextBitmap: Bitmap? = null // 下一个Bitmap
    private var urls: MutableList<String> = mutableListOf() // Url集合
    private var isClick = false // 是否点击

    private var mCircleAnim: ValueAnimator? = null // 圆形动画
    private var mRightAnim: ValueAnimator? = null // 侧滑动画
    private var isAlphaShow = true // 显示图片是否执行渐变动画
    private var waterAnimDuration = 2000L // 圆形动画执行时长
    private var slideAnimDuration = 1000L // 圆形动画执行时长
    private var scrollX = 0f // 滑动偏移量
    private var animatorType = AnimatorType.ANIM_CIRCLE // 动画类型，用于处理动画类型，根据手势动作执行不同动画圆形滑动还是侧滑动画时使用
    private var isEnableSliding = true //默认启用侧滑查看

    // 图片缓存
    private val imageCache: LruCache<String, Bitmap> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount / 1024
            }
        }
    }

    private var onImageListener: OnImageListener? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var scaleType = ScaleType.CENTER_CROP

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
            setAdapter(urls)
        }
    }

    fun addData(newUrls: List<String>) {
        if (newUrls.isNotEmpty()) {
            val oldSize = urls.size
            urls.addAll(newUrls) // 预加载新添加的第一张图片（原最后一张的下一张）
            if (oldSize < urls.size) {
                loadImage(newUrls[0], oldSize) { bitmap, e ->
                    if (e == null && bitmap != null) {
                        mNextBitmap = bitmap // 新下一张图片
                    }
                }
            }
            onImageListener?.onPreSelected(currentPosition)
            onImageListener?.onSelected(currentPosition)
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
        when (animatorType) {
            AnimatorType.ANIM_CIRCLE -> {
                if (mBottomBitmap != null) { // 渐变消失动画
                    mPaint!!.alpha = computerAlpha()
                    mBottomMatrix = getMatrix(mBottomBitmap, scaleType)
                    canvas.drawBitmap(mBottomBitmap!!, mBottomMatrix, mPaint)
                }

                if (isClick && mCurrentBitmap != null) {
                    mPaint!!.reset()
                    mPath!!.reset()
                    if (isAlphaShow) {
                        mPaint!!.alpha = computerAlphaFromScratch()
                    }
                    mPath!!.addCircle(rx, ry, radius, Path.Direction.CW)
                    canvas.clipPath(mPath!!)
                    val currentMatrix = getMatrix(mCurrentBitmap, scaleType)
                    canvas.drawBitmap(mCurrentBitmap!!, currentMatrix, mPaint)
                }
            }
            AnimatorType.ANIM_RIGHT_SLIDE, AnimatorType.ANIM_LEFT_SLIDE -> drawSlideEffect(canvas)
        }
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
                matrix.postTranslate((widgetWidth - bitmap.width * scale) / 2, (widgetHeight - bitmap.height * scale) / 2)
            }
            ScaleType.FIT_CENTER -> {
                val widthRatio = widgetWidth / bitmap!!.width.toFloat()
                val heightRatio = widgetHeight / bitmap.height.toFloat()
                val scale = if (widthRatio < heightRatio) widthRatio else heightRatio
                matrix.postScale(scale, scale)
                matrix.postTranslate((widgetWidth - bitmap.width * scale) / 2, (widgetHeight - bitmap.height * scale) / 2)
            }
            ScaleType.MATRIX -> { // 这里可以根据需要实现自定义矩阵逻辑
            }
        }
        return matrix
    }

    /**
     * 绘制侧滑效果
     *
     * @param canvas
     */
    private fun drawSlideEffect(canvas: Canvas) {
        if (mBottomBitmap != null) {
            mBottomMatrix = getMatrix(mBottomBitmap, scaleType)
            mBottomMatrix.postTranslate(-widgetWidth + scrollX, 0f)
            canvas.drawBitmap(mBottomBitmap!!, mBottomMatrix, mPaint)

            val currentMatrix = getMatrix(mCurrentBitmap, scaleType)
            currentMatrix.postTranslate(scrollX, 0f)
            canvas.drawBitmap(mCurrentBitmap!!, currentMatrix, mPaint)
        }
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

    /**
     * 变化圆动画
     */
    private fun startAnim() {
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
        mRightAnim = ValueAnimator.ofFloat(start, end)
        mRightAnim?.setDuration(slideAnimDuration)
        mRightAnim?.addUpdateListener { animation ->
            scrollX = animation.animatedValue as Float
            invalidate()
        }
        mRightAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                scrollX = 0f
                when (animatorType) {
                    AnimatorType.ANIM_RIGHT_SLIDE -> mCurrentBitmap = mBottomBitmap
                    AnimatorType.ANIM_LEFT_SLIDE -> mBottomBitmap = mCurrentBitmap
                    else -> Unit
                } // 执行完毕，准备上一个数据
                onImageListener?.onSelected(currentPosition)
                prePrevData()
                preNextData()
            }

            override fun onAnimationStart(animation: Animator?) {
                onImageListener?.onPreSelected(currentPosition)
            }
        })
        mRightAnim?.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean { // 将事件交给GestureDetector处理（可以判断手势滑动方向）
        mGestureDetector.onTouchEvent(event)
        return true
    }

    private var mGestureDetector: GestureDetector = GestureDetector(getContext(), object : GestureDetector.OnGestureListener {
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

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            Log.i(TAG, "onScroll: $distanceX")
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            Log.i(TAG, "onLongPress: ")
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean { // 切换操作时，若果向右动画或圆形动画在执行则不操作下一个
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

    /**
     * 切换上一个，左滑触发
     */
    private fun switchPrePosition() {
        if (currentPosition - 1 >= 0) { // 成功开始请求，切换类型
            this.animatorType = AnimatorType.ANIM_RIGHT_SLIDE
            --currentPosition // 如果当前这个和上一个相同，说明准备的上一条数据还没好
            if (mCurrentBitmap != null && mCurrentBitmap!!.sameAs(mBottomBitmap)) {
                Log.i(TAG, "switchPrePosition: 网络加载")
                loadImage(urls[currentPosition], currentPosition) { bitmap, e ->
                    if (e != null) {
                        failHandler(e)
                    } else {
                        mBottomBitmap = bitmap
                        startSlideAnim(0f, widgetWidth)
                    }
                }
            } else {
                Log.i(TAG, "switchPrePosition: 本地加载") // 准备上一个
                startSlideAnim(0f, widgetWidth)
            }
        } else {
            Log.i(TAG, "switchNextPosition: 已是第一张：$currentPosition")
        }
    }

    /**
     * 切换下一个Position
     */
    private fun switchNextPosition(animatorType: AnimatorType) { // 当前是最后一张且没有更多数据时，提示并返回
        if (currentPosition == urls.size - 1 && onLoadMoreListener == null) { //            Toast.makeText(context, "无更多数据", Toast.LENGTH_SHORT).show()
            return
        }

        // 加载更多触发时机调整为倒数第一张
        val isNeedLoadMore = currentPosition == urls.size - 2 && urls.isNotEmpty()

        if (currentPosition + 1 < urls.size) {
            this.animatorType = animatorType
            if (mCurrentBitmap != null) {
                mBottomBitmap = mCurrentBitmap // 保存当前图片为底层
            }
            ++currentPosition

            // 加载更多逻辑：在滑动到倒数第二张时触发
            if (isNeedLoadMore) {
                onLoadMoreListener?.onLoadMore()
            }

            // 获取新的下一张图片（考虑加载更多后的新数据）
            val nextUrl = urls.getOrNull(currentPosition + 1)
            if (nextUrl != null) { // 预加载下一张图片（包括加载更多后的新数据）
                loadImage(nextUrl, currentPosition + 1) { bitmap, e ->
                    if (e == null && bitmap != null) {
                        mNextBitmap = bitmap // 始终预加载下一张
                    }
                }
            }

            // 处理当前图片加载
            val currentUrl = urls[currentPosition]
            loadImage(currentUrl, currentPosition) { bitmap, e ->
                if (e != null) {
                    failHandler(e)
                    return@loadImage
                }
                mCurrentBitmap = bitmap
                selAnimType(animatorType)
            }
        } else {
            Log.i(TAG, "已是最后一张：$currentPosition")
        }
    }

    /**
     * 根据动画类型选择绘制不同动画
     *
     * @param animatorType
     */
    private fun selAnimType(animatorType: AnimatorType) {
        when (animatorType) {
            AnimatorType.ANIM_CIRCLE -> {
                isClick = true
                startAnim()
            }
            AnimatorType.ANIM_LEFT_SLIDE -> startSlideAnim(widgetWidth, 0f)
            else -> Unit
        }
    }

    /**
     * 传入集合,并加载第一张
     *
     * @param urls url集合
     */
    private fun setAdapter(urls: List<String>) {
        loadImage(urls[currentPosition], currentPosition) { bitmap, e ->
            if (e != null) {
                failHandler(e)
            } else {
                bitmap?.let {
                    mBottomBitmap = it
                    mBottomMatrix = getMatrix(mBottomBitmap, scaleType)
                    invalidate() // 请求成功准备下一张
                    preNextData()
                }
                onImageListener?.onPreSelected(currentPosition)
                onImageListener?.onSelected(currentPosition)
            }
        }
    }

    /**
     * 预加载下个数据
     */
    private fun preNextData() {
        if (currentPosition + 1 < urls.size) {
            loadImage(urls[currentPosition + 1], currentPosition + 1) { bitmap, e ->
                if (e != null) {
                    clearBitmap()
                } else {
                    mNextBitmap = bitmap
                }
            }
        }
    }

    /**
     * 请求上一个数据
     */
    private fun prePrevData() {
        if (currentPosition - 1 >= 0) {
            loadImage(urls[currentPosition - 1], currentPosition - 1) { bitmap, e ->
                if (e != null) {
                    failHandler(e)
                } else {
                    mBottomBitmap = bitmap
                }
            }
        }
    }

    /**
     * 加载图片，优先从缓存获取，若没有则调用用户自定义的加载器
     */
    private fun loadImage(url: String, position: Int, callback: (Bitmap?, Exception?) -> Unit) {
        val cachedBitmap = imageCache.get(url)
        if (cachedBitmap != null) {
            callback(cachedBitmap, null)
        } else {
            onImageListener?.onBindView(url, position, object : ImageLoadCallback {
                override fun onSuccess(bitmap: Bitmap) {
                    imageCache.put(url, bitmap)
                    callback(bitmap, null)
                }

                override fun onFailed(e: Exception) {
                    callback(null, e)
                }
            })
        }
    }

    /**
     * 失败处理
     *
     * @param e
     */
    private fun failHandler(e: Exception?) {
        Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
        clearBitmap()
        invalidate()
        e?.printStackTrace()
    }

    private fun clearBitmap() {
        mBottomBitmap?.recycle()
        mBottomBitmap = null
        mCurrentBitmap?.recycle()
        mCurrentBitmap = null
        mNextBitmap?.recycle()
        mNextBitmap = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        imageCache.evictAll() //        clearBitmap()
    }


    interface OnImageListener {
        //加载图片回调，用户自行将bitmap通过callback函数返回
        fun onBindView(url: String, position: Int, callback: ImageLoadCallback)

        //动画结束时回调
        fun onSelected(position: Int)

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
        const val TAG: String = "WaveImageView"
        private const val FLIP_DISTANCE: Float = 100f // 滑动距离
    }
}