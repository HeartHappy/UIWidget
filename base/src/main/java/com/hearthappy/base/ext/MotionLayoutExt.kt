package com.hearthappy.base.ext

import android.view.View
import androidx.annotation.IdRes
import androidx.constraintlayout.motion.widget.MotionLayout

fun MotionLayout.addAlphaListener(view: View, @IdRes startId: Int, @IdRes endId: Int) {
    setTransitionListener(object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(p0: MotionLayout, p1: Int, p2: Int) {
        }

        override fun onTransitionChange(p0: MotionLayout, p1: Int, p2: Int, p3: Float) {
            val alphaValue = 1 - p3
            view.alpha = alphaValue
        }

        override fun onTransitionCompleted(p0: MotionLayout, p1: Int) {
            if (p1 == startId) view.alpha = 1f
            else if (p1 == endId) view.alpha = 0f
        }

        override fun onTransitionTrigger(p0: MotionLayout, p1: Int, p2: Boolean, p3: Float) {
        }
    })
}