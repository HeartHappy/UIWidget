package com.hearthappy.uiwidget.turntable

import android.animation.TimeInterpolator
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.pow

class CustomTimeInterpolator : TimeInterpolator {
    private val accelerateInterpolator = AccelerateInterpolator()
    private val decelerateInterpolator = DecelerateInterpolator()

    var seepd = 0.35f
    var end = 0.3f

    override fun getInterpolation(input: Float): Float {
        Log.d("CustomTimeInterpolator", "getInterpolation: $input")
        if (input < end) {
            return (1 - (1 - (input - 0.99) * 100).pow(2.0)).toFloat()
        } else {
            return input
        }

    }
}