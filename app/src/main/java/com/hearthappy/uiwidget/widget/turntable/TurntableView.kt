package com.social.coco.a_refactor.tools.widget.turntable

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.animation.doOnEnd
import com.hearthappy.uiwidget.R
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Created Date: 2024/12/3
 * @author ChenRui
 * ClassDescription：自定义转盘 VIEW
 */
class TurntableView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { //        init()
    }

    private val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.bg_turntable)
    private var selectBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.bg_turntable_select)
    private var scaleFactor: Float = 1f
    private var scaledWidth: Float = 0f
    private var scaledHeight: Float = 0f
    var equalNumber = 12 //等分数量
    val singleAngle = 360 / equalNumber

    //    private var bitmapRect: RectF? = null
    private val wheelMatrix = Matrix() // 控制转盘的旋转
    private val selectMatrix = Matrix() // 控制转盘的旋转
    private var currentAngle = 0f // 当前旋转的角度

    //记录上次旋转角度
    private var recordAngle = 0f
    private var selectDefaultAngle = singleAngle / 2
    private var onSpinEndListener: ((Int) -> Unit)? = null // 回调，返回落脚扇区
    private val ranges = listOf("")

    private var isFinishLottery = false
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.YELLOW
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleFactor = calculateScaleFactor(bitmap.width, bitmap.height, width, height)
        selectBitmap = scaleBitmapToCircleRadius(selectBitmap, (width / 2).toFloat())
        scaledWidth = (bitmap.width * scaleFactor)
        scaledHeight = (bitmap.height * scaleFactor)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // Draw background
        bitmap.let {

            // 使用Matrix或其他方式应用缩放
            Log.d(TAG, "onDraw: $scaleFactor")
            wheelMatrix.setScale(scaleFactor, scaleFactor)
            selectMatrix.setTranslate((width / 2).toFloat() - selectBitmap.width / 2, 0f) // 将Matrix应用到Drawable或Bitmap上
            // 将图片绘制到中心
            val centerX = width / 2f
            val centerY = height / 2f
            val wheelRadius = width / 2f


            wheelMatrix.postRotate(currentAngle, centerX, centerY)
            selectMatrix.postRotate(selectDefaultAngle + currentAngle, centerX, centerY)


            canvas.drawBitmap(bitmap, wheelMatrix, null)
            for (i in 0 until equalNumber) {
                val textPosition = calculateTextPosition(centerX, centerY, wheelRadius, (i * singleAngle).toFloat() + currentAngle-90, ((i + 1) * singleAngle).toFloat() + currentAngle-90)
                canvas.drawText("$i", textPosition.first, textPosition.second, paint)
            } //            if (isFinishLottery) {
            canvas.drawBitmap(selectBitmap, selectMatrix, null) //            }
        }
    }

    fun calculateTextPosition(cx: Float, cy: Float, r: Float, startAngle: Float, endAngle: Float): Pair<Float, Float> { // Convert angles from degrees to radians
        val midAngleRad = Math.toRadians(((startAngle + endAngle) / 2).toDouble())

        // Calculate the distance from the center where we want to place the text
        val textDistance = r * 0.7 // Adjust this ratio as needed

        // Calculate the position of the text based on the middle angle and the chosen distance
        val textX = cx + textDistance * cos(midAngleRad).toFloat()
        val textY = cy + textDistance * sin(midAngleRad).toFloat()

        return Pair(textX.toFloat(), textY.toFloat())
    }

    fun spin() { // 计算随机的旋转角度
        isFinishLottery = false
        val randomDegrees = Random.nextInt(360) + 720f // 至少转两圈 //        val randomDegrees = 35 // 至少转两圈
        val targetAngle = (currentAngle + randomDegrees) % 360 // 动画控制旋转
        val animator = ObjectAnimator.ofFloat(currentAngle, currentAngle + randomDegrees)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
            invalidate()
        }
        animator.doOnEnd { // 动画结束后，计算旋转的最终落脚点
            val sectorIndex = ceil(currentAngle % 360 / singleAngle).toInt() // 转换到 [0, 360) //            val sectorIndex = calculateSector(finalAngle)
            val winningSegment = (targetAngle / (360f / equalNumber)).toInt() % equalNumber
            Log.d(TAG, "spin: $randomDegrees，sectorIndex:$sectorIndex，totalAngle:$currentAngle,winningSegment:$winningSegment")
            val calculateSectorIndex = calculateSectorIndex(currentAngle, equalNumber)
            Toast.makeText(context, "选中：$sectorIndex,索引：$calculateSectorIndex", Toast.LENGTH_SHORT).show() //            recordAngle+=spinAngle
            isFinishLottery = true
            onSpinEndListener?.invoke(sectorIndex.toInt()) //            currentAngle=0f

        }
        animator.start()
    }

    fun calculateSectorIndex(stopHour: Float, numberOfSectors: Int): Int {
        // 将小时转换为角度
        val stopAngle = (stopHour % 12) * 30f // 每个小时间隔30度

        // 计算每个扇形的角度宽度
        val sectorAngleWidth = 360f / numberOfSectors

        // 计算索引
        val index = ((stopAngle + 180) % 360 / sectorAngleWidth).toInt() // 加180是为了处理负角度的情况

        return index % numberOfSectors // 确保索引在有效范围内
    }


    //    private fun calculateSector(angle: Float): Int { // 每个扇区的角度
    //        val sectorAngle = 360f / equalNumber // 计算角度对应的扇区编号
    //        val normalizedAngle = (angle + sectorAngle / 2) % 360 // 偏移半个扇区角度对齐12点方向
    //        return (normalizedAngle / sectorAngle).toInt()
    //    }


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

    companion object {
        private const val TAG = "TurntableView"
    }
}