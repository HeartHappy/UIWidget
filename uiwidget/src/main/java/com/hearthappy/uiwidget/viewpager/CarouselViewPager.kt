package com.hearthappy.uiwidget.viewpager

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * Created Date: 2025/4/22
 * @author ChenRui
 * ClassDescription：轮播ViewPager
 */
class CarouselViewPager : ViewPager {
    private var isSwipeEnabled = false
    private var isViewVisible = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() { // The majority of the magic happens here
        setPageTransformer(true, VerticalPageTransformer()) // The easiest way to get rid of the overscroll drawing that happens on the left and right
        overScrollMode = OVER_SCROLL_NEVER //        (context as? AppCompatActivity)?.lifecycle?.addObserver(activityLifecycleObserver)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        isViewVisible = visibility == VISIBLE
    }


    override fun canScrollVertically(direction: Int): Boolean {
        return false
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean { //        val intercepted = super.onInterceptTouchEvent(swapXY(ev))
        //        swapXY(ev) // return touch coordinates to original reference frame for any child views
        return isSwipeEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isSwipeEnabled && super.onTouchEvent(ev)
    }

    fun setSwipeEnabled(enabled: Boolean) {
        this.isSwipeEnabled = enabled
    }


    var carouselIndex = 0
    var delay = 3000L

    private val carouseTask = object : Runnable {
        override fun run() {
            if (isViewVisible) {
                ++carouselIndex
                if (carouselIndex >= Int.MAX_VALUE) {
                    carouselIndex = 0
                }
                setCurrentItem(carouselIndex, carouselIndex != 0)
                Log.d(TAG, "run isViewVisible:${isViewVisible},index: $carouselIndex")
            }
            postDelayed(this, delay)
        }
    }

    fun startCarouse() {
        carouselIndex = 0
        setSwipeEnabled(true)
        stopCarouse()
        postDelayed(carouseTask, delay)
    }

    fun stopCarouse() {
        removeCallbacks(carouseTask)
    }

    private inner class VerticalPageTransformer : PageTransformer {
        override fun transformPage(view: View, position: Float) {
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.alpha = 0f
            } else if (position <= 1) { // [-1,1]
                view.alpha = 1f

                // Counteract the default slide transition
                view.translationX = view.width * -position

                //set Y position to swipe in from top
                val yPosition = position * view.height
                view.translationY = yPosition
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.alpha = 0f
            }
        }
    }

    fun CarouselViewPager.addCarouselAdapter(fragmentManager: FragmentManager, count: Int, item: (Int) -> Fragment) {
        adapter = object : FragmentStatePagerAdapter(fragmentManager) {
            override fun getCount(): Int {
                return Int.MAX_VALUE
            }

            override fun getItem(position: Int): Fragment {
                return item(position % count)
            }
        }
        startCarouse()
    }

    companion object {
        private const val TAG = "CarouselViewPager"
    }
}