package com.hearthappy.uiwidget.turntable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.hearthappy.uiwidget.R
import java.util.Timer
import java.util.TimerTask
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

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
    private var bgrRotation = 0f
    private var isShowIndex = false //是否显示索引
    private var isShowHighlight = true //选中区域高亮
    private var numSectors = 12 //等分数量
    private var iconVOffset = 0f //图标垂直偏移，正数向外
    private var radiusOffset = textSize //文本偏移，根据外圆向内偏移距离

    private val lotteryBoxSet = mutableSetOf<MultipleLottery>()
    private val lotteryBoxList = mutableListOf<MultipleLottery>()
    private var iconBitmaps = listOf<Bitmap>()
    private var titles = listOf<String>()

    var onSingleDrawEndListener: ((Int, String?) -> Unit)? = null // 单抽回调，返回：索引，标题
    var onMoreDrawEndListener: ((List<MultipleLottery>) -> Unit)? = null // 多抽回调，返回：索引，和抽中次数的集合

    private var isFinishLottery = false //是否开始抽奖
    private var isMultipleDraw = false //是否连续抽奖
    private var lastFinalAngle: Float = 0f  // 新增变量，用于记录上次动画结束的角度
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private val indexPaint = Paint()
    private val titlePaint = Paint()
    private val pathPaint = Paint()
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
    private val minRotationNumber = 5

    // 旋转剩余的弧度
    private var timer: Timer? = null
    private var rotationRadian: Float = 0f
    private var totalRotationRadian: Float = 0f

    // 控制转盘开始速度，值越大开始的速度越快
    private val startSpeed = 0.35f

    // 慢下来的速率，值越小停下得越慢
    private val decelerationRate = 0.001f // 开始转盘动画，传入要旋转到的物品索引


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TurntableView)
        val bgrResourceId = attributes.getResourceId(R.styleable.TurntableView_tv_bgr, R.mipmap.bg_turntable_default)
        val bgrSelectResourceId = attributes.getResourceId(R.styleable.TurntableView_tv_bgr_select, R.mipmap.bg_turntable_select)
        numSectors = attributes.getInteger(R.styleable.TurntableView_tv_equal_number, numSectors)
        textColor = attributes.getColor(R.styleable.TurntableView_tv_text_color, textColor)
        textSize = attributes.getDimension(R.styleable.TurntableView_tv_text_size, SizeUtils.sp2px(context, textSize).toFloat())
        radiusOffset = attributes.getDimension(R.styleable.TurntableView_tv_radius_offset, SizeUtils.dp2px(context, radiusOffset).toFloat())
        iconVOffset = attributes.getDimension(R.styleable.TurntableView_tv_icon_v_offset, SizeUtils.dp2px(context, iconVOffset).toFloat())
        bgrRotation = attributes.getFloat(R.styleable.TurntableView_tv_bgr_rotation, bgrRotation)
        isShowIndex = attributes.getBoolean(R.styleable.TurntableView_tv_show_index, isShowIndex)
        isShowHighlight = attributes.getBoolean(R.styleable.TurntableView_tv_show_highlight, isShowHighlight)
        bgrBitmap = BitmapFactory.decodeResource(resources, bgrResourceId)
        selectBitmap = BitmapFactory.decodeResource(resources, bgrSelectResourceId)
        sectorAngle=360f/numSectors
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
        radius = width / 2f
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // Draw background

        bgrMatrix.reset()
        iconMatrix.reset() //绘制背景使用，Matrix或其他方式应用缩放
        bgrMatrix.setScale(scaleFactor, scaleFactor)
        bgrMatrix.postRotate(currentAngle, centerX, centerY)


        canvas.drawBitmap(bgrBitmap, bgrMatrix, null)


        //绘制文本标题
        drawTexts(canvas)

        //绘制索引
        if (isShowIndex) {
            for (i in 0 until numSectors) {
                val textPosition = calculateTextPosition(centerX, centerY, radius, (i * sectorAngle) + currentAngle - 90 - sectorAngle/2, ((i + 1) * sectorAngle) + currentAngle - 90 - sectorAngle/2)
                canvas.drawText("$i", textPosition.first, textPosition.second, indexPaint)
            }
        }

        //绘制icon
        var startAngle = -(sectorAngle * 3 + sectorAngle / 2)
        iconBitmaps.forEachIndexed { index, iconBitmap ->
            val imageWidth = radius / 10 //计算半边扇形的角度 度=Math.PI/180 弧度=180/Math.PI
            val angle = ((startAngle + sectorAngle / 2) * Math.PI / 180).toFloat() //计算中心点的坐标
            val r = (radius / 8.0 * 5.0).toInt() + iconVOffset
            val x = (centerX + r * cos(angle.toDouble())).toFloat()
            val y = (centerY + r * sin(angle.toDouble())).toFloat() //设置绘制图片的范围 // 先平移到以转盘中心为原点的对应位置（考虑图标尺寸）
            iconMatrix.reset()
            iconMatrix.postTranslate(x - imageWidth, y - imageWidth) // 再根据角度进行旋转，使图标正确朝向扇形中心位置
            iconMatrix.postRotate(angle + currentAngle, centerX, centerY)
            canvas.drawBitmap(iconBitmap, iconMatrix, indexPaint)
            startAngle += sectorAngle
        }

        //是选中区域高亮
        if (isFinishLottery && isShowHighlight) { //多抽选中
            if (isMultipleDraw) {
                Log.d(TAG, "onDraw: =====================================")
                lotteryBoxList.forEach {
                    mutableSelectMatrix.reset()
                    mutableSelectMatrix.setTranslate((width / 2).toFloat() - selectBitmap.width / 2, 0f)
                    Log.d(TAG, "onDraw: selectIndex:${selectIndex},index:${it.index},number:${it.number}，${titles[it.index]}")
                    mutableSelectMatrix.postRotate(getRelativeAngle(selectIndex, it.index), centerX, centerY)
                    canvas.drawBitmap(selectBitmap, mutableSelectMatrix, null)
                }
            } else { //单抽显示选中
                selectMatrix.setTranslate((width / 2).toFloat() - selectBitmap.width / 2, 0f) // 将Matrix应用到Drawable或Bitmap上
                selectMatrix.postRotate(0f, centerX, centerY)
                canvas.drawBitmap(selectBitmap, selectMatrix, null)
            }
        }
    }

    private fun drawTexts(canvas: Canvas) {
        val rect = RectF(paddingLeft.toFloat() + radiusOffset, paddingTop.toFloat() + radiusOffset, width.toFloat() - paddingEnd - radiusOffset, height.toFloat() - paddingBottom - radiusOffset) //        val startAngle = -105

        titles.forEachIndexed { index, text ->
            val startAngle = index * sectorAngle - 90 + currentAngle - sectorAngle/2
            val sweepAngle = sectorAngle
            path.reset()
            path.addArc(rect, startAngle, sweepAngle)
            canvas.drawTextOnPath(text, path, 0f, 0f, titlePaint)
        }
    }

    /**
     * 获取相对角度
     * @param index Int
     * @return Float
     */
    private fun getRelativeAngle(relativeIndex: Int, index: Int): Float {
        val indexDiff = index - relativeIndex
        return when {
            indexDiff >= 0 -> (indexDiff * sectorAngle)
            indexDiff < 0 && index >= 0 -> (indexDiff + 12) * sectorAngle
            index == 0 -> (12 + indexDiff) * sectorAngle
            else -> throw IllegalArgumentException("无效的索引值")
        }
    }


    /**
     * 计算索引绘制位置
     * @param cx Float
     * @param cy Float
     * @param r Float
     * @param startAngle Float
     * @param endAngle Float
     * @param location Float 位置,相对中心得距离
     * @return Pair<Float, Float>
     */
    private fun calculateTextPosition(cx: Float, cy: Float, r: Float, startAngle: Float, endAngle: Float, location: Float = 0.7f): Pair<Float, Float> { // Convert angles from degrees to radians
        val midAngleRad = Math.toRadians(((startAngle + endAngle) / 2).toDouble())

        // Calculate the distance from the center where we want to place the text
        val textDistance = r * location // Adjust this ratio as needed

        // Calculate the position of the text based on the middle angle and the chosen distance
        val textX = cx + textDistance * cos(midAngleRad).toFloat()
        val textY = cy + textDistance * sin(midAngleRad).toFloat()

        return Pair(textX.toFloat(), textY.toFloat())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow() // 释放Bitmap资源，避免内存泄漏
        for (iconBitmap in iconBitmaps) {
            iconBitmap.recycle()
        }
    }

    /**
     * 单抽-  随机生成抽中的索引，范围0 - 11"
     */
    fun startSingleDraw(randomIndex: Int = Random.nextInt(12), isMultipleDraw: Boolean = false) {
        this.isFinishLottery = false
        this.isMultipleDraw = isMultipleDraw
        createRotationAnimator(randomIndex)
    }


    /**
     * 连抽-默认10连
     * @param number Int
     */
    fun startTenConsecutiveDraws(number: Int = 10) {
        handlerMultipleData(number)

        //根据最大的数量排序
        val multipleLottery = lotteryBoxList.maxBy { it.title.toInt() }
        startSingleDraw(multipleLottery.index, true)

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
        post {
            setLayerType(LAYER_TYPE_HARDWARE, null)
            currentAngle = 0f
            stopTimer()
            val turnAngle = calculateTurnAngle(index, sectorAngle)
            val rotationRadianValue = calculateRotationRadian(turnAngle) // 初始化旋转角度为0，准备开始新的旋转过程
            totalRotationRadian = rotationRadianValue
            rotationRadian = rotationRadianValue
            startRotationTimer(index){
                if (isMultipleDraw) {
                    onMoreDrawEndListener?.invoke(lotteryBoxList)
                } else {
                    onSingleDrawEndListener?.invoke(it, titles[it])
                }
            }
        }
    }


    // 计算单次旋转角度（考虑了物品索引、每个物品对应的角度以及随机角度）
    private fun calculateTurnAngle(index: Int, anglePerItem: Float, randomAngle: Float = 360 * 3f): Float {
        return 360 - (index * anglePerItem) + randomAngle
    }

    // 根据单次旋转角度和总圈数计算总的旋转弧度
    private fun calculateRotationRadian(turnAngle: Float): Float {
        return (turnAngle + 360 * minRotationNumber) * (PI / 180f).toFloat()
    }

    // 启动定时器，用于定时更新转盘的旋转动画
    private fun startRotationTimer(i: Int,block:(Int)->Unit) {
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    post { rotationAnimation(i,block) }
                }
            }, 0, (1000 / 60).toLong())
        }
    }

    // 定时器每次触发时执行的动画逻辑，用于更新转盘的旋转角度实现旋转及缓慢停止效果
    private fun rotationAnimation(index: Int,block:(Int)->Unit) {
        val progressRatio = rotationRadian / totalRotationRadian
        val perAngle = max(decelerationRate, progressRatio * startSpeed)
        if (rotationRadian >= perAngle) {
            rotationRadian -= perAngle
            updateRotation(perAngle)
        }
        Log.d("sxx", "index: $index =======rotationAnimation: $rotationRadian")
        if (rotationRadian < perAngle) {
            stopTimer()
            Log.d(TAG, "rotationAnimation: end")
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


    /**
     * 获取随机角度
     * @param max 最大角度
     * @param min 最小角度
     * @return 随机角度
     */
    fun getRandomAngle(): Int {
        val array = listOf(11, 9, 7, 7, 5, 2, 0, -2, -5, -7, -7, -9, -9, -11, -11)
        val index = (array.indices).random()
        return array[index]
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
        val originalWidth = bitmap.width.toFloat()

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

    companion object {
        private const val TAG = "TurntableView"
    }
}