package com.hearthappy.uiwidget.viewpager

import android.view.View
import androidx.viewpager.widget.ViewPager

class ZoomOutPageTransformer : ViewPager.PageTransformer {
    companion object {
        private const val MIN_SCALE = 0.75f
        private const val MIN_ALPHA = 0.7f
    }

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        val pageHeight = view.height

        when {
            position < -1 -> { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.alpha = 0f
            }
            position <= 1 -> { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                val scaleFactor = kotlin.math.max(MIN_SCALE, 1 - kotlin.math.abs(position))
                val vertMargin = pageHeight * (1 - scaleFactor) / 2
                val horzMargin = pageWidth * (1 - scaleFactor) / 2

                view.translationX = if (position < 0) {
                    horzMargin - vertMargin / 2
                } else {
                    -horzMargin + vertMargin / 2
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor

                // Fade the page relative to its size.
                view.alpha =
                    MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
            }
            else -> { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.alpha = 0f
            }
        }
    }
}