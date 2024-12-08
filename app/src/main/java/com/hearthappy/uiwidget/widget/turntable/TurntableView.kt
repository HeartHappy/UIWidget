package com.hearthappy.uiwidget.widget.turntable

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
import kotlin.math.cos
import kotlin.math.roundToInt
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
    private var offsetAngle=-15f//默认偏移角度
    private var currentAngle = offsetAngle // 当前旋转的角度

    private var onSpinEndListener: ((Int) -> Unit)? = null // 回调，返回落脚扇区

    private var isFinishLottery = false
    private var lastFinalAngle: Float = 0f  // 新增变量，用于记录上次动画结束的角度
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



            canvas.drawBitmap(bitmap, wheelMatrix, null)
            for (i in 0 until equalNumber) {
                val textPosition = calculateTextPosition(centerX, centerY, wheelRadius, (i * singleAngle).toFloat() + currentAngle-90, ((i + 1) * singleAngle).toFloat() + currentAngle-90)
                canvas.drawText("$i", textPosition.first, textPosition.second, paint)
            } //            if (isFinishLottery) {
            if(isFinishLottery){
                selectMatrix.postRotate(0f, centerX, centerY)
            canvas.drawBitmap(selectBitmap, selectMatrix, null) //            }
            }
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
    fun startLottery() {
        isFinishLottery=false
//        currentAngle=0f
        val randomIndex = Random.nextInt(12)  // 随机生成抽中的索引，范围0 - 1"
        println("lastFinalAngle:$lastFinalAngle")
        val targetAngle = getTargetAngle(randomIndex) +lastFinalAngle-offsetAngle // 获取对应索引的角度

        // 创建旋转动画，将转盘旋转到对应角度，使得抽中的区域位于12点钟方向
        val rotateAnimation = ObjectAnimator.ofFloat(currentAngle,targetAngle)
        rotateAnimation.duration = 1000
        rotateAnimation.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
            invalidate()
        }
        rotateAnimation.doOnEnd {
            val finalIndex = getIndexFromAngle(targetAngle)
            isFinishLottery=true
            lastFinalAngle=targetAngle-offsetAngle
            val index = getIndexAtTwelveOClock(0, finalIndex)
            // 这里可以添加逻辑，使用finalIndex和finalAngle去高亮显示对应的扇形区域，比如通过绘制等方式，此处暂未详细实现
            println("最终在12点钟方向的索引是: ${index}")
            Toast.makeText(context, "最终在12点钟方向的索引是: ${index}", Toast.LENGTH_SHORT).show()
            invalidate()
        }
        rotateAnimation.interpolator= CustomAccelerateDecelerateInterpolator()
        rotateAnimation.duration = 7000 // 动画时长，可根据需求调整
        rotateAnimation.start()
    }


    // 根据索引获取对应的角度（以圆盘中心为原点，顺时针方向）
    private fun getTargetAngle(index: Int): Float {
        return (index * (360f / equalNumber))+3*360
    }

    // 根据角度反推对应的索引（考虑角度可能存在一定误差，进行适当取整判断）
    private fun getIndexFromAngle(angle: Float): Int {
        val normalizedAngle = angle % 360
        val index = (normalizedAngle / (360f / equalNumber)).roundToInt()
        return if (index < 0) index + equalNumber else index % equalNumber
    }

    // 根据已知旋转到某个时钟方向的索引及该时钟方向，获取12点钟方向对应的索引
    private fun getIndexAtTwelveOClock(currentIndex: Int, clockDirection: Int): Int {
        val anglePerPiece = 30 // 每一份对应的角度（单位：度）
        val anglePerHour = 30 // 每个时钟小时对应的角度（单位：度）
        val rotatedAngle = clockDirection * anglePerHour
        val offsetIndex = (rotatedAngle / anglePerPiece).toInt()
        return (currentIndex + (12 - offsetIndex)) % 12
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

    companion object {
        private const val TAG = "TurntableView"
    }
}