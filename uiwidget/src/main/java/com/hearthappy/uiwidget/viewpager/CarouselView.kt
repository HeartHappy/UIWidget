package com.hearthappy.uiwidget.viewpager

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hearthappy.uiwidget.R
import kotlin.Int.Companion.MAX_VALUE

/**
 * Created Date: 2025/4/27
 * @author ChenRui
 * ClassDescription：基于ViewPager2封装的无限轮播控件
 */
class CarouselView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(
    context, attrs, defStyleAttr
) {
    private val viewPager2 = ViewPager2(context)
    private var isViewVisible = false
    private var interval: Long = 3000
    private var duration: Long = 1000

    var isUserInputEnabled = false
        set(value) {
            field = value
            viewPager2.isUserInputEnabled = value
        }


    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        isViewVisible = visibility == VISIBLE
    }

    init {
        addView(viewPager2)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CarouselView)
        if (typedArray.hasValue(R.styleable.CarouselView_carousel_interval)) {
            interval = typedArray.getInteger(
                R.styleable.CarouselView_carousel_interval, interval.toInt()
            ).toLong()
        }
        if (typedArray.hasValue(R.styleable.CarouselView_carousel_duration)) {
            duration = typedArray.getInteger(
                R.styleable.CarouselView_carousel_duration, duration.toInt()
            ).toLong()
        }
        if (typedArray.hasValue(R.styleable.CarouselView_carousel_orientation)) {
            viewPager2.orientation = typedArray.getInt(
                R.styleable.CarouselView_carousel_orientation, ViewPager2.ORIENTATION_HORIZONTAL
            )
        }
        if (typedArray.hasValue(R.styleable.CarouselView_isUserInputEnabled)) {
            isUserInputEnabled = typedArray.getBoolean(
                R.styleable.CarouselView_isUserInputEnabled, true
            )
        }

        typedArray.recycle()
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        viewPager2.adapter = adapter //轮播图滑动监听
    }

    fun getAdapter(): RecyclerView.Adapter<*>? {
        return viewPager2.adapter
    }

    fun setOrientation(orientation: Int) {
        viewPager2.orientation = orientation
    }

    fun getOrientation(): Int {
        return viewPager2.orientation
    }

    fun setPageTransformer(transformer: ViewPager2.PageTransformer) {
        viewPager2.setPageTransformer(transformer)
    }

    fun addListener(onPageSelected: (Int) -> Unit, onPageScrolled: (Int, Float, Int) -> Unit = { p, po, pop -> }, onPageScrollStateChanged: (Int) -> Unit = {}) {
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                onPageScrolled(
                    position, positionOffset, positionOffsetPixels
                )
            }
        })
    }

    fun setScrollInterval(interval: Long, duration: Long = 1000) {
        this.interval = interval
        this.duration = duration
    }

    fun startAutoScroll() {
        stopAutoScroll()
        postDelayed(carouselTask, interval)
    }

    private val carouselTask = object : Runnable {
        override fun run() {
            if (isViewVisible) {
                var targetItem = viewPager2.currentItem
                val itemCount = viewPager2.adapter?.itemCount ?: MAX_VALUE
                if (++targetItem >= itemCount) targetItem = 0
                scrollAnimator(targetItem, duration)
            }
            postDelayed(this, interval)
        }

    }

    fun stopAutoScroll() {
        removeCallbacks(carouselTask)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAutoScroll()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoScroll()
    }

    private fun scrollAnimator(item: Int, duration: Long, interpolator: TimeInterpolator = LinearInterpolator()) {
        viewPager2.apply {
            if (item < currentItem) { //                setCurrentItem(item,false)
                return
            }
            val offsetDistance: Int =
                if (orientation == ViewPager2.ORIENTATION_VERTICAL) height else width
            val pxToDrag: Int = offsetDistance * (item - currentItem)
            val animator = ValueAnimator.ofInt(0, pxToDrag)
            var previousValue = 0
            animator.addUpdateListener { valueAnimator ->
                val currentValue = valueAnimator.animatedValue as Int
                val currentPxToDrag = (currentValue - previousValue).toFloat()
                fakeDragBy(-currentPxToDrag)
                previousValue = currentValue
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) { //            Log.d(CarouselViewPager2.TAG, "onAnimationStart: $item")
                    beginFakeDrag()
                }

                override fun onAnimationEnd(animation: Animator) { //            Log.d(CarouselViewPager2.TAG, "onAnimationEnd: $item")
                    //            val aiRecordBean = adapter.bannerList[item % Constant.QUERY_FIRST_NUMBER]
                    //            val blurBitmap = aiRecordBean.imgBase64.base64ToBitmap().blur(this@scrollAnimator.context, 2f)
                    //            Glide.with(this@scrollAnimator.context).load(blurBitmap).dontAnimate().into(imgView)
                    //            bannerBlurBlock(blurBitmap)
                    endFakeDrag()
                }

                override fun onAnimationCancel(animation: Animator) { /* Ignored */
                }

                override fun onAnimationRepeat(animation: Animator) { /* Ignored */
                }
            })
            animator.interpolator = interpolator
            animator.duration = duration
            animator.start()
        }
    }
}