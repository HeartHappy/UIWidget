package com.hearthappy.uiwidget.carouse

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * Created Date 2020/6/2.
 * @author ChenRui
 * ClassDescription:ViewPager切换动画
 */
class PagerTransformer(private var animType: AnimType) : ViewPager2.PageTransformer {


    sealed class AnimType {
         object ALPHA : AnimType()
         object SCALE : AnimType()
         object TRANSLATE : AnimType()
    }

    companion object {

        const val MIN_SCALE = 0.5f
    }


    override fun transformPage(page: View, position: Float) {

        when (animType) {
            AnimType.ALPHA -> alphaAnim(position, page)
            AnimType.SCALE -> scaleAnim(position, page)
            AnimType.TRANSLATE -> animTranslation(position, page)
        }
    }


    /**
     * 平移、缩放、淡入淡出
     */
    private fun animTranslation(position: Float, page: View) {
        if (position >= -1 || position <= 1) {
            //隐藏
            when {
                position < -1 -> {
                    page.alpha = 0f
                }

                position <= 0 -> {
                    page.alpha = 1f
                    page.translationX = 0f
                    page.scaleX = 1f
                    page.scaleY = 1f
                }

                position <= 1 -> {
                    //显示
                    page.alpha = 1 - position
                    // Counteract the default slide transition
                    page.translationX = page.width * -position
//                    Log.i(TAG, "显示transformPage: ${page.width * -position},transX:${page.translationX}")
                    // Scale the page down (between MIN_SCALE and 1)
                    val scaleFactor: Float = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                }

                else -> {
                    page.alpha = 0f
                }
            }
        }
    }

    /**
     * 缩放动画
     */
    private fun scaleAnim(position: Float, page: View) {
        if (position >= -1 || position <= 1) {
            if (position < 0) {
                page.scaleX = abs(1 + position)
                page.scaleY = abs(1 + position)
            } else {
                page.scaleX = abs(1 - position)
                page.scaleY = abs(1 - position)
            }
        }
    }


    /**
     * 淡入淡出动画
     */
    private fun alphaAnim(position: Float, page: View) {
        if (position >= -1 || position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            if (position < 0) { //[0,-1]
                page.alpha = abs(1 + position)
                //                    page.translationY = -position * maxTransform
                //                    page.alpha = Math.abs(position)
            } else {  //[1,0]
                //                    page.translationY = position * maxTransform
                page.alpha = abs(1 - position)
            }
        }
    }
}

