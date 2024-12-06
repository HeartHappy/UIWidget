package com.hearthappy.uiwidget.widget.turntable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.hearthappy.uiwidget.R
import java.util.Timer
import java.util.TimerTask
import kotlin.math.PI
import kotlin.math.max


/**
 *    desc   : 转盘12个
 *    author : W
 *    date   : 2023/12/2413:41
 */

class TurntableView12 @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    /**
     * 奖品标题
     */
    private val mStrings = ArrayList<String>()
    fun getStrings(): ArrayList<String> {
        return mStrings
    }

    /**
     * 奖品价格
     */
    private val priceStrs = ArrayList<String>()
    private var mCount = mStrings.size

    /**
     * 奖品id
     */
    private val giftIds = ArrayList<String>()

    /**
     * 奖品图片
     */
    private val mBitmaps = ArrayList<Bitmap>()

    /**
     * 画背景
     */
    private var mBgPaint: Paint? = null

    /**
     * 绘制文字
     */
    private var mTextPaint: Paint? = null

    /**
     * 半径
     */
    private var mRadius = 0

    /**
     * 圆心坐标
     */
    private var mCenter = 0

    /**
     * 弧形的起始角度
     */
    private var startAngle = 0
    private var angles = IntArray(mCount)
    private var sectorRectF: RectF? = null

    /**
     * 当前初始角度
     */
    private var mCurrentAngle = 0f

    /**
     * 每片扇形的角度
     */
    private var sweepAngle = 0
    private var listener: RotateListener? = null


    // 高亮的奖品
    // private val highLightDto = ArrayList<AwardGiftHighLightDto>()

    private var timer: Timer? = null

    // 旋转剩余的弧度
    private var rotationRadian: Float = 0f

    // 旋转总弧度
    private var totalRotationRadian: Float = 0f


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val width = Math.min(w, h)
        mCenter = width / 2 //半径
        mRadius = (width - paddingLeft * 2) / 2

        //设置框高都一样
        setMeasuredDimension(width, width)
    }

    private var mBgBitmap: Bitmap? = null // private var mCoinBitmap: Bitmap? = null
    /**
     * 初始化
     */
    private fun init() {
        mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint!!.color = resources.getColor(R.color.luck_rotate_price_color)
        mTextPaint!!.textSize = SizeUtils.sp2px(context, 9f).toFloat()
        mBgBitmap = BitmapFactory.decodeResource(resources, R.mipmap.bg_turntable) // mCoinBitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_turn_coin)
        //this.rotation = 18f
    }

    private var mBgRect: RectF? = null
    override fun onDraw(canvas: Canvas) {
        mCount = mStrings.size
        angles = IntArray(mCount)
        if (mCount <= 0) {
            return
        } //1.绘制背景
        if (mBgRect == null) {
            mBgRect = RectF(0f, 0f, (mRadius * 2).toFloat(), (mRadius * 2).toFloat())
        }
        canvas.drawBitmap(mBgBitmap!!, null, mBgRect!!, mBgPaint) //2.绘制扇形
        //2.1设置每一个扇形的角度
        sweepAngle = 360 / mCount //因为canvas.drawArc() 在右边x轴正方向开始绘制------------即在3点钟方向
        startAngle = -(sweepAngle * 3 + sweepAngle / 2) //2.2设置扇形绘制的范围
        if (sectorRectF == null) {
            sectorRectF = RectF(paddingLeft.toFloat(), paddingLeft.toFloat(), (mCenter * 2 - paddingLeft).toFloat(), (mCenter * 2 - paddingLeft).toFloat())
        }
        for (i in 0 until mCount) { //            mArcPaint.setColor(sectorColor[i % 2]);
            //sectorRectF 扇形绘制范围  startAngle 弧开始绘制角度 sweepAngle 每次绘制弧的角度
            // useCenter 是否连接圆心
            //            canvas.drawArc(sectorRectF, startAngle, sweepAngle, true, mArcPaint);
            //3.绘制图片
            drawIcons(canvas, mBitmaps[i]) //4.绘制文字
            drawTexts(canvas, priceStrs[i], priceStrs[i])

            angles[i] = sweepAngle * i + sweepAngle / 2
            Log.d(TAG, "onDraw: " + angles[i] + "     " + i)
            startAngle += sweepAngle

            // drawHighIcons(canvas, highLightDto[i].bitmap,i)
        }

        super.onDraw(canvas)
    }


    /**
     * 以二分之一的半径的长度，扇形的一半作为图片的中心点
     * 图片的宽度为imageWidth
     *
     * @param canvas
     * @param mBitmap
     */
    private fun drawHighIcons(canvas: Canvas, mBitmap: Bitmap, i: Int) {

        val imageWidth = SizeUtils.dp2px(context, 74.25f)
        val imageHight = SizeUtils.dp2px(context, 143.46f) //计算半边扇形的角度 度=Math.PI/180 弧度=180/Math.PI
        val angle = ((startAngle + sweepAngle / 2) * Math.PI / 180).toFloat() //计算中心点的坐标
        val r = mRadius
        val x = ((mCenter - imageWidth / 2)).toFloat()
        val y = (mCenter - r).toFloat()


        //设置绘制图片的范围
        val mIconRect = RectF(x, y, x + imageWidth, y + imageHight)
        canvas.drawArc(mIconRect, 15f, (i * 30).toFloat(), true, mTextPaint!!);
        canvas.drawBitmap(mBitmap, null, mIconRect, null)


    }

    private fun drawIcons(canvas: Canvas, mBitmap: Bitmap) {


        val imageWidth = mRadius / 10 //计算半边扇形的角度 度=Math.PI/180 弧度=180/Math.PI
        val angle = ((startAngle + sweepAngle / 2) * Math.PI / 180).toFloat() //计算中心点的坐标
        val r = (mRadius / 8.0 * 4.3).toInt() + 50
        val x = (mCenter + r * Math.cos(angle.toDouble())).toFloat()
        val y = (mCenter + r * Math.sin(angle.toDouble())).toFloat() //设置绘制图片的范围
        val mIconRect = RectF(x - imageWidth, y - imageWidth, x + imageWidth, y + imageWidth)
        canvas.drawBitmap(mBitmap, null, mIconRect, null)

    }

    /**
     * 使用path添加一个路径
     * 绘制文字的路径
     *
     * @param canvas
     * @param mString
     */
    private fun drawTexts(canvas: Canvas, mStr: String, priceStr: String) {


        // TODO: 这是扇形内得小图标
        val drawable: Drawable = ContextCompat.getDrawable(context, R.mipmap.icon_dial_2)!!

        // 2. 创建一个ImageSpan
        val imageSpan: ImageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)

        // 3. 创建一个SpannableString，可以是你想显示的文本
        val spannableString = SpannableString(mStr)

        // 4. 将ImageSpan加入到SpannableString中
        spannableString.setSpan(imageSpan, spannableString.length - 1, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)


        val mString = spannableString.toString()


        val path = Path() //添加一个圆弧的路径
        path.addArc(sectorRectF!!, startAngle.toFloat(), sweepAngle.toFloat())
        var startText: String? = null
        var endText: String? = null //测量文字的宽度
        val textWidth = mTextPaint!!.measureText(mString) //水平偏移
        var hOffset = (mRadius * 2 * Math.PI / mCount / 2 - textWidth / 2).toInt() //计算弧长 处理文字过长换行
        val l = (360 / mCount * Math.PI * mRadius / 180).toInt()
        if (textWidth > l * 4 / 5) {
            val index = mString.length / 2
            startText = mString.substring(0, index)
            endText = mString.substring(index, mString.length)
            val startTextWidth = mTextPaint!!.measureText(startText)
            val endTextWidth = mTextPaint!!.measureText(endText) //水平偏移
            hOffset = (mRadius * 2 * Math.PI / mCount / 2 - startTextWidth / 2).toInt()
            val endHOffset = (mRadius * 2 * Math.PI / mCount / 2 - endTextWidth / 2).toInt() //文字高度
            val h = ((mTextPaint!!.ascent() + mTextPaint!!.descent()) * 1.5).toInt()

            //根据路径绘制文字
            //hOffset 水平的偏移量 vOffset 垂直的偏移量
            //            canvas.drawTextOnPath(startText, path, hOffset, mRadius / 6, mTextPaint);
            //            canvas.drawTextOnPath(endText, path, endHOffset.toFloat(), (mRadius / 6 - h).toFloat(), mTextPaint!!);
        } else { //根据路径绘制文字
            canvas.drawTextOnPath(mString, path, hOffset.toFloat(), (mRadius / 11).toFloat(), mTextPaint!!)


            // drawCoinIcon(canvas, mCoinBitmap,textWidth)
        }

        //绘制价格
        //测量文字的宽度
        //        float priceTextWidth = mTextPaint.measureText(priceStr);
        //        int priceHOffset = (int) (mRadius * 2 * Math.PI / mCount / 2 - priceTextWidth / 2);
        //        canvas.drawTextOnPath(priceStr, path, priceHOffset, mRadius / 7f * 3, mTextPaint);
    }

    private fun drawCoinIcon(canvas: Canvas, mCoinBitmap: Bitmap?, textWidth: Float) {

        val imageWidth = SizeUtils.dp2px(context, 9f) //计算半边扇形的角度 度=Math.PI/180 弧度=180/Math.PI
        val angle = ((startAngle + sweepAngle / 2) * Math.PI / 180).toFloat() //计算中心点的坐标
        val r = (mRadius).toInt()
        val x = (mCenter + r * Math.cos(angle.toDouble())).toFloat()
        val y = (mCenter + r * Math.sin(angle.toDouble())).toFloat() //设置绘制图片的范围
        val mIconRect = RectF(x - imageWidth, y - imageWidth, x + imageWidth, y + imageWidth)
        canvas.drawBitmap(mCoinBitmap!!, null, mIconRect, null)

    }


    /**
     * 是否正在抽奖
     */
    private var isDrawingLottery = false

    init {
        init()
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

    fun rotate(s: String) {
        if (isDrawingLottery) {
            return
        }
        val i = giftIds.indexOf(s) //val angle = 360f / mCount
        // val randomAngle = getRandomAngle((angle / 2 - 5).toInt(), (-(angle / 2) + 5).toInt())

        val randomAngle = getRandomAngle()

        val rotateToPosition = 360 / mCount * (mCount - i) + randomAngle
        val toDegree: Float = 360f * 5 + rotateToPosition

        this.post {
            startTurningAnimation(i)
        }


        //

        //        val duration = 9500L
        //        this.post {
        //
        //
        //            val holder = PropertyValuesHolder.ofFloat("rotation", mCurrentAngle, toDegree)
        //            val mAnim = ObjectAnimator.ofPropertyValuesHolder(this@TurntableView12, holder)
        //            mAnim.setDuration(duration)
        //            mAnim.repeatCount = 0
        //               mAnim.interpolator = EasingInterpolator(Ease.CIRC_OUT)
        //
        //            mAnim.addUpdateListener { animation: ValueAnimator ->
        //                val animatedValue = animation.animatedValue as Float
        //                //控制mCurrentAngle在0到360之间
        //                mCurrentAngle = (animatedValue % 360 + 360) % 360
        //                ViewCompat.postInvalidateOnAnimation(this@TurntableView12)
        //
        //
        //
        //
        //            }
        //            mAnim.addListener(object : Animator.AnimatorListener {
        //                override fun onAnimationStart(animation: Animator) {
        //                    isDrawingLottery = true
        //                }
        //
        //                override fun onAnimationEnd(animation: Animator) {
        //                    try {
        //                        setLayerType(LAYER_TYPE_NONE, null)
        //                        isDrawingLottery = false
        //                        if (listener != null) {
        //                            if (i < mStrings.size) {
        //                                listener?.onEnd(mStrings[i])
        //                            }
        //                        }
        //                    } catch (e: Exception) {
        //
        //                    }
        //                }
        //
        //                override fun onAnimationCancel(animation: Animator) {}
        //                override fun onAnimationRepeat(animation: Animator) {}
        //            })
        //            mAnim.start()
        //        }


    }


    // 控制转盘开始速度 值越大开始的速度越快
    val SPEED = 0.35f

    // 慢下来的速率  值越小停下的越来越慢
    val decelerate = 0.001f

    // 旋转总圈数
    val TurnsNum = 5
    fun startTurningAnimation(index: Int) {
        setLayerType(LAYER_TYPE_HARDWARE, null)

        val angle = 360f / mStrings.size
        val randomAngle = getRandomAngle()

        val turnsNum = TurnsNum
        val turnAngle = 360 - (index * angle) + randomAngle

        val rotationRadian = (turnAngle + 360 * turnsNum) * (PI / 180f).toFloat()

        stopTimer()
        this.rotation = 0f
        totalRotationRadian = rotationRadian
        this.rotationRadian = rotationRadian
        startRotationTimer(index)
    }

    private fun startRotationTimer(i: Int) {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    post { rotationAnimation(i) }
                }
            }, 0, (1000 / 60).toLong())
        }
    }

    private fun rotationAnimation(i: Int) {
        val per = rotationRadian / totalRotationRadian
        var perAngle = per * SPEED

        perAngle = max(decelerate, perAngle)
        if (rotationRadian >= perAngle) {
            rotationRadian -= perAngle
        }
        Log.d("sxx", "index: $i" + "=======rotationAnimation: $rotationRadian")
        if (rotationRadian < perAngle) {
            stopTimer()
            listener?.onEnd(giftIds[i])

        } else { // 旋转弧度
            this.rotation += perAngle * (180f / PI.toFloat())
        }

    }

    private fun stopTimer() {
        setLayerType(LAYER_TYPE_NONE, null)
        timer?.cancel()
        timer = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTimer()
        recycleBitmap();
    }

    private fun recycleBitmap() {
        if (mBgBitmap != null) {
            mBgBitmap!!.recycle()
            mBgBitmap = null
        }

        if (mBitmaps.isNotEmpty()) {
            mBitmaps.forEach {
                if (it != null) {
                    it.recycle()
                }
            }
            mBitmaps.clear()
        }
        mBitmaps == null
    }

    fun setListener(listener: RotateListener?) {
        this.listener = listener
    }

    interface RotateListener {
        fun onEnd(s: String?)
    }

    fun setData(
        bgBitmaps: List<Bitmap>,
        titles: List<String>,
        contents: List<String>,
        gift_ids: List<String>,

        ) {
        mStrings.clear()
        mBitmaps.clear()
        priceStrs.clear()
        mBitmaps.addAll(bgBitmaps)
        mStrings.addAll(titles)
        priceStrs.addAll(contents)

        giftIds.addAll(gift_ids)
        invalidate()
    }

    companion object {
        private val TAG = TurntableView12::class.java.simpleName
        private const val ANIM_DURATION = 3750L
    }

    //    class AccelerateThenDecelerateInterpolator : TimeInterpolator {
    //
    //        private val accelerationThreshold: Float = 0.3f // 加速到这一点后开始减速
    //
    //        override fun getInterpolation(input: Float): Float {
    //            return 0.0f
    //        }
    //    }
}
