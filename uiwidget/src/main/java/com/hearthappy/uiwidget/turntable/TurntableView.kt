package com.hearthappy.uiwidget.turntable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.utils.SizeUtils
import java.util.Timer
import java.util.TimerTask
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Created Date: 2024/12/3
 * @author ChenRui
 * ClassDescription：自定义转盘 VIEW
 */
class TurntableView : View {

    private var bgrBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.bg_turntable_default)
    private var selectBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.bg_turntable_select)
    private var textColor = ContextCompat.getColor(context, R.color.color_title)
    private var textSize = 12f
    private var isShowHighlight = true //选中区域高亮
    private var numSectors = 12 //等分数量
    private var textOffsetY = textSize //文本偏移，根据外圆向内偏移距离
    private var iconPositionPercent = 0.7f //距离圆心位置 1在最外边缘
    private var iconSize = 30f //图标大小
    private var startSpeed = 0.35f // 控制转盘开始速度，值越大开始的速度越快
    private var decelerationRate = 0.001f // 慢下来的速率，值越小停下得越慢
    private var minRotationNumber = 5
    private var isResultCenter = false //转盘结束时居中显示，正对12点方向
    private var outlineColor: Int = -1 //文本描边颜色
    private var outlineWidth: Float = 2f //文本描边宽
    private var angleOffsetArray = intArrayOf()
    private var angleOffsetRange = intArrayOf()


    private val lotteryBoxSet = mutableSetOf<MultipleLottery>()
    private val lotteryBoxList = mutableListOf<MultipleLottery>()
    private var iconBitmaps = listOf<Bitmap>()
    private var titles = listOf<String>()

    var onSingleDrawEndListener: ((Int, String?) -> Unit)? = null // 单抽回调，返回：索引，标题
    var onMoreDrawEndListener: ((List<MultipleLottery>) -> Unit)? = null // 多抽回调，返回：索引，和抽中次数的集合
    var onEndListener: TurntableCallback? = null

    private var isFinishLottery = false //是否开始抽奖
    private var isMultipleDraw = false //是否连续抽奖
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private val indexPaint = Paint()
    private val titlePaint = Paint()
    private val pathPaint = Paint()
    private var outlinePaint: Paint = Paint()
    private val path = Path()
    private var scaleFactor: Float = 1f
    private var scaledWidth: Float = 0f
    private var scaledHeight: Float = 0f
    private var sectorAngle = 360f / numSectors
    private val bgrMatrix = Matrix() // 控制转盘的旋转
    private val selectMatrix = Matrix() // 控制转盘的旋转
    private val mutableSelectMatrix = Matrix()
    private val iconMatrix = Matrix()
    private var currentAngle = 0f // 当前旋转的角度
    private var selectIndex = 0 //记录选中的index，作为角度计算基准
    private var randomOffsetAngle = 0f


    // 旋转剩余的弧度
    private var timer: Timer? = null
    private var rotationRadian: Float = 0f //旋转弧度，持续变化
    private var totalRotationRadian: Float = 0f //旋转总弧度
    private val startingPoint = -90 //默认是在3点方向绘制，-90度让起点在12点方向执行



    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TurntableView)
        val bgrResourceId = attributes.getResourceId(R.styleable.TurntableView_tv_bgr, R.mipmap.bg_turntable_default)
        val bgrSelectResourceId = attributes.getResourceId(R.styleable.TurntableView_tv_bgr_select, R.mipmap.bg_turntable_select)
        numSectors = attributes.getInteger(R.styleable.TurntableView_tv_equal_number, numSectors)
        textColor = attributes.getColor(R.styleable.TurntableView_tv_text_color, textColor)
        outlineColor = attributes.getColor(R.styleable.TurntableView_tv_text_outline_color, outlineColor)
        textSize = attributes.getDimension(R.styleable.TurntableView_tv_text_size, SizeUtils.sp2px(context, textSize).toFloat())
        outlineWidth = attributes.getDimension(R.styleable.TurntableView_tv_text_outline_width, SizeUtils.sp2px(context, outlineWidth).toFloat())
        textOffsetY = attributes.getDimension(R.styleable.TurntableView_tv_text_offset_y, SizeUtils.dp2px(context, textOffsetY).toFloat())
        iconSize = attributes.getDimension(R.styleable.TurntableView_tv_icon_size, SizeUtils.dp2px(context, iconSize).toFloat())
        iconPositionPercent = attributes.getFloat(R.styleable.TurntableView_tv_icon_position_percent, iconPositionPercent)
        isShowHighlight = attributes.getBoolean(R.styleable.TurntableView_tv_show_highlight, isShowHighlight)
        isResultCenter = attributes.getBoolean(R.styleable.TurntableView_tv_show_result_center, isResultCenter)
        startSpeed = attributes.getFloat(R.styleable.TurntableView_tv_start_speed, startSpeed)
        decelerationRate = attributes.getFloat(R.styleable.TurntableView_tv_deceleration_rate, decelerationRate)
        minRotationNumber = attributes.getInteger(R.styleable.TurntableView_tv_min_rotation_number, minRotationNumber)
        val angleOffsetArrayResId = attributes.getResourceId(R.styleable.TurntableView_tv_angle_offset_array, 0)
        val angleOffsetRangeResId = attributes.getResourceId(R.styleable.TurntableView_tv_angle_offset_range, 0)
        if (angleOffsetArrayResId != 0) angleOffsetArray = resources.getIntArray(angleOffsetArrayResId)
        if (angleOffsetRangeResId != 0) angleOffsetRange = resources.getIntArray(angleOffsetRangeResId)

        bgrBitmap = BitmapFactory.decodeResource(resources, bgrResourceId)
        selectBitmap = BitmapFactory.decodeResource(resources, bgrSelectResourceId)
        sectorAngle = 360f / numSectors
        initPaint()
        attributes.recycle()

    }

    private fun initPaint() {
        indexPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = this@TurntableView.textColor
            textSize = this@TurntableView.textSize
            textAlign = Paint.Align.CENTER
            this.strokeWidth = 8f
        }
        titlePaint.apply {
            isAntiAlias = true
            color = this@TurntableView.textColor
            textSize = this@TurntableView.textSize
            textAlign = Paint.Align.CENTER
        }
        pathPaint.apply {
            isAntiAlias = true
            color = Color.GREEN
            style = Paint.Style.STROKE
            this.strokeWidth = 2f
        }

        //描边画笔
        outlinePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = outlineWidth
            textSize = this@TurntableView.textSize
            textAlign = Paint.Align.CENTER
            color = outlineColor
            maskFilter = BlurMaskFilter(outlineWidth, BlurMaskFilter.Blur.NORMAL)
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleFactor = calculateScaleFactor(bgrBitmap.width, bgrBitmap.height, width, height)
        selectBitmap = scaleBitmapToCircleRadius(selectBitmap, (width / 2).toFloat())
        scaledWidth = (bgrBitmap.width * scaleFactor)
        scaledHeight = (bgrBitmap.height * scaleFactor)

        // 将图片绘制到中心
        centerX = width / 2f
        centerY = height / 2f
        radius = min(centerX, centerY)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // Draw background

        bgrMatrix.setScale(scaleFactor, scaleFactor)
        bgrMatrix.postRotate(currentAngle, centerX, centerY)

        //绘制背景
        canvas.drawBitmap(bgrBitmap, bgrMatrix, null)

        //绘制文本标题
        drawTexts(canvas)

        //绘制图标
        drawDefaultIcons(canvas)
        //是选中区域高亮
        drawHighlight(canvas)
    }

    private fun drawDefaultIcons(canvas: Canvas) {
        //绘制默认图标
        if (iconBitmaps.isEmpty()) {
            val bitmaps = mutableListOf<Bitmap>()
            for (i in 0 until numSectors) {
                bitmaps.add(BitmapFactory.decodeResource(resources, R.mipmap.ic_apple))
            }
            drawIcons(canvas, bitmaps)
        }else{
            //绘制Icon
            drawIcons(canvas, iconBitmaps)
        }
    }

    private fun drawTextsOrIndex(canvas: Canvas, rect: RectF) {
        if (titles.isEmpty()) {
            for (index in 0 until numSectors) {
                val startAngle = index * sectorAngle + startingPoint + currentAngle - sectorAngle / 2
                path.reset()
                path.addArc(rect, startAngle, sectorAngle)
                if (outlineColor != -1) canvas.drawTextOnPath(index.toString(), path, 0f, 0f, outlinePaint)
                canvas.drawTextOnPath(index.toString(), path, 0f, 0f, titlePaint)
            }
        }else{
            titles.forEachIndexed { index, text ->
                val startAngle = index * sectorAngle + startingPoint + currentAngle - sectorAngle / 2
                path.reset()
                path.addArc(rect, startAngle, sectorAngle)

                // 先绘制描边
                if (outlineColor != -1) canvas.drawTextOnPath(text, path, 0f, 0f, outlinePaint)

                canvas.drawTextOnPath(text, path, 0f, 0f, titlePaint)
            }
        }
    }

    private fun drawHighlight(canvas: Canvas) {
        if (isFinishLottery && isShowHighlight) { //多抽选中
            if (isMultipleDraw) {
                lotteryBoxList.forEach {
                    mutableSelectMatrix.reset()
                    mutableSelectMatrix.setTranslate((width / 2).toFloat() - selectBitmap.width / 2, 0f)
                    mutableSelectMatrix.postRotate(getRelativeAngle(selectIndex, it.index) + randomOffsetAngle, centerX, centerY)
                    canvas.drawBitmap(selectBitmap, mutableSelectMatrix, null)
                }
            } else { //单抽显示选中
                selectMatrix.setTranslate((width / 2).toFloat() - selectBitmap.width / 2, 0f) // 将Matrix应用到Drawable或Bitmap上
                selectMatrix.postRotate(randomOffsetAngle, centerX, centerY)
                canvas.drawBitmap(selectBitmap, selectMatrix, null)
            }
        }
    }

    private fun drawTexts(canvas: Canvas) {
        val rect = RectF(paddingLeft.toFloat() + textOffsetY, paddingTop.toFloat() + textOffsetY, width.toFloat() - paddingEnd - textOffsetY, height.toFloat() - paddingBottom - textOffsetY) //        val startAngle = -105
        //绘制文本
        drawTextsOrIndex(canvas, rect)

    }

    private fun drawIcons(canvas: Canvas, bitmaps: List<Bitmap>) {

        canvas.save()
        canvas.rotate(currentAngle, centerX, centerY)

        bitmaps.forEachIndexed { index, iconBitmap ->
            val angle = sectorAngle * index + startingPoint
            val x = centerX + (radius * iconPositionPercent) * cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = centerY + (radius * iconPositionPercent) * sin(Math.toRadians(angle.toDouble())).toFloat()
            iconMatrix.reset()
            val scaleFactor = iconSize / max(iconBitmap.width.toFloat(), iconBitmap.height.toFloat())
            iconMatrix.postScale(scaleFactor, scaleFactor, iconBitmap.width / 2f, iconBitmap.height / 2f)
            iconMatrix.postTranslate(x - iconBitmap.width / 2f, y - iconBitmap.height / 2f)
            iconMatrix.postRotate(angle - startingPoint, x, y)
            canvas.drawBitmap(iconBitmap, iconMatrix, indexPaint)
        }
        canvas.restore()
    }

    /**
     * 获取相对角度
     * @param index Int
     * @return Float
     */
    private fun getRelativeAngle(relativeIndex: Int, index: Int): Float {
        val indexDiff = index - relativeIndex
        return when {
            index == 0 -> (12 + indexDiff) * sectorAngle
            indexDiff >= 0 -> (indexDiff * sectorAngle)
            indexDiff < 0 && index >= 0 -> (indexDiff + 12) * sectorAngle
            else -> throw IllegalArgumentException("无效的索引值")
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow() // 释放Bitmap资源，避免内存泄漏
        for (iconBitmap in iconBitmaps) {
            iconBitmap.recycle()
        }
    }

    private fun singleStart(index: Int, isMultipleDraw: Boolean = false) {
        this.isFinishLottery = false
        this.isMultipleDraw = isMultipleDraw
        createRotationAnimator(index)
    }

    /**
     * 随机单抽
     *
     * 随机生成抽中的索引，范围0 - 11,即numSectors设置数量之间"
     */
    fun startSingleDraw() {
        val randomIndex: Int = Random.nextInt(numSectors)
        singleStart(randomIndex)
    }


    /**
     * 随机连抽-默认10连
     * @param number Int
     */
    fun startMultipleDraws(number: Int = 10) {
        handlerMultipleData(number)
        multipleStart()
    }

    /**
     * 指定单抽
     * @param index Int
     */
    fun specifySingleDraw(index: Int) {
        singleStart(index)
    }

    /**
     * 指定抽中多个
     */
    fun specifyMultipleDraws(indexList: List<Int>) {
        handlerSpecifyMultipleData(indexList)
        multipleStart()
    }

    //根据最大的数量排序
    private fun multipleStart() {
        val multipleLottery = lotteryBoxList.maxBy { it.title.toInt() }
        singleStart(multipleLottery.index, true)
    }

    private fun handlerSpecifyMultipleData(indexList: List<Int>) {
        lotteryBoxSet.clear()
        lotteryBoxList.clear()

        for (index in indexList) {
            val find = lotteryBoxSet.find { it.index == index }
            find?.apply {
                val oldNumber = this.number
                lotteryBoxSet.remove(this)
                this.number = oldNumber + 1
                lotteryBoxSet.add(this)
            } ?: let {
                lotteryBoxSet.add(MultipleLottery(index, 1, titles[index]))
            }
        }

        lotteryBoxList.addAll(lotteryBoxSet.toList())
    }

    /**
     * 处理连抽数据
     * @param number Int
     */
    private fun handlerMultipleData(number: Int) {
        lotteryBoxSet.clear()
        lotteryBoxList.clear()

        for (i in 0 until number) {
            val randomIndex = Random.nextInt(12)
            val find = lotteryBoxSet.find { it.index == randomIndex }
            find?.apply {
                val oldNumber = this.number
                lotteryBoxSet.remove(this)
                this.number = oldNumber + 1
                lotteryBoxSet.add(this)
            } ?: let {
                lotteryBoxSet.add(MultipleLottery(randomIndex, 1, titles[randomIndex]))
            }
        }
        lotteryBoxList.addAll(lotteryBoxSet.toList())
    }


    private fun createRotationAnimator(index: Int) {
        post { //            setLayerType(LAYER_TYPE_HARDWARE, null)
            currentAngle = 0f
            stopTimer()
            //目标角度
            val turnAngle = calculateTurnAngle(index, sectorAngle) // 生成一个随机的偏移角度，范围在 -10 到 10 度之间
            val rotationRadianValue = calculateRotationRadian(if (isResultCenter) turnAngle else turnAngle.run { plus(calculateOffsetAngle()) }) // 初始化旋转角度为0，准备开始新的旋转过程
            totalRotationRadian = rotationRadianValue
            rotationRadian = rotationRadianValue
            startRotationTimer(index) {onEndTask(it)}
        }
    }

    private fun onEndTask(it: Int) {
        if (isMultipleDraw) {
            onMoreDrawEndListener?.invoke(lotteryBoxList)
            onEndListener?.onMoreDrawEndListener(lotteryBoxList)
        } else {
            onSingleDrawEndListener?.invoke(it, titles[it])
            onEndListener?.onSingleDrawEndListener(it, titles[it])
        }
    }

    //计算结果，随机偏移角度
    private fun calculateOffsetAngle(): Float {
        randomOffsetAngle = when {
            angleOffsetArray.isNotEmpty() -> angleOffsetArray.random().toFloat()
            angleOffsetRange.isNotEmpty() -> Random.nextInt(IntRange(angleOffsetRange.first(), angleOffsetRange.last())).toFloat()
            else -> (Math.random() * sectorAngle - sectorAngle / 2).toFloat()
        }
        return randomOffsetAngle
    }


    // 计算单次旋转角度（考虑了物品索引、每个物品对应的角度以及随机角度）
    private fun calculateTurnAngle(index: Int, anglePerItem: Float, randomAngle: Float = 360f * minRotationNumber): Float {
        return 360 - (index * anglePerItem) + randomAngle
    }

    // 根据单次旋转角度和总圈数计算总的旋转弧度
    private fun calculateRotationRadian(turnAngle: Float): Float {
        return (turnAngle + 360 * minRotationNumber) * (PI / 180f).toFloat()
    }

    // 启动定时器，用于定时更新转盘的旋转动画
    private fun startRotationTimer(i: Int, block: (Int) -> Unit) {
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    post { rotationAnimation(i, block) }
                }
            }, 0, (1000 / 60).toLong())
        }
    }

    // 定时器每次触发时执行的动画逻辑，用于更新转盘的旋转角度实现旋转及缓慢停止效果
    private fun rotationAnimation(index: Int, block: (Int) -> Unit) {
        val progressRatio = rotationRadian / totalRotationRadian
        val perAngle = max(decelerationRate, progressRatio * startSpeed)
        if (rotationRadian >= perAngle) {
            rotationRadian -= perAngle
            updateRotation(perAngle)
        }
        if (rotationRadian < perAngle) {
            stopTimer()
            isFinishLottery = true
            selectIndex = index //选中index,多抽时根据基准设置其他位置
            invalidate()
            block(index)
        }
    }

    // 根据每次的角度增量更新转盘的当前旋转角度
    private fun updateRotation(perAngle: Float) {
        currentAngle += perAngle * (180f / PI.toFloat())
        invalidate()
    }


    private fun stopTimer() {
        setLayerType(LAYER_TYPE_NONE, null)
        timer?.cancel()
        timer = null
    }


    /**
     * 计算图片缩放比例
     * @param imageWidth Int
     * @param imageHeight Int
     * @param viewWidth Int
     * @param viewHeight Int
     * @return Float
     */
    private fun calculateScaleFactor(imageWidth: Int, imageHeight: Int, viewWidth: Int, viewHeight: Int): Float {
        val widthRatio = viewWidth.toFloat() / imageWidth
        val heightRatio = viewHeight.toFloat() / imageHeight
        return minOf(widthRatio, heightRatio)
    }

    /**
     * 选中光标缩放后得bitmap
     * @param bitmap Bitmap
     * @param circleRadius Float
     * @return Bitmap
     */
    private fun scaleBitmapToCircleRadius(bitmap: Bitmap, circleRadius: Float): Bitmap { // 获取原始图片的高度和宽度
        val originalHeight = bitmap.height.toFloat()

        // 确定目标高度（即圆的半径）
        val targetHeight = circleRadius

        // 计算缩放比例
        val scaleFactor = targetHeight / originalHeight

        // 创建缩放矩阵
        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)

        // 使用缩放矩阵创建新的Bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    fun setSourceData(iconBitmaps: List<Bitmap>, titles: List<String> = emptyList()) {
        this.iconBitmaps = iconBitmaps
        this.titles = titles
        invalidate()
    }

    fun <T> setSourceData(turntableImpl: ITurntableSource) {
        this.iconBitmaps = turntableImpl.icons()
        this.titles = turntableImpl.prices()
        invalidate()
    }


    companion object {
        private const val TAG = "TurntableView"
    }
}