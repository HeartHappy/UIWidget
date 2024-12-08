package com.hearthappy.uiwidget.widget.turntable

import android.animation.TimeInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

class CustomAccelerateDecelerateInterpolator(private val slowRatio: Float = 0.0001f):TimeInterpolator {
    private val startSlowRatio = 0.35f // 开始阶段的慢速占比
    private val endSlowRatio = 0.01f // 结束阶段的慢速占比
//    override fun getInterpolation(input: Float): Float {
//        return if (input < startSlowRatio) {
//            // 开始慢速阶段，使用自定义的缓慢加速曲线，这里使用简单的线性关系示例，你可以根据需求调整为更复杂的曲线
//            input / startSlowRatio
//        } else if (input > 1 - endSlowRatio) {
//            // 结束慢速阶段，使用自定义的缓慢减速曲线，同样使用简单线性关系示例
//            (1 - input) / endSlowRatio
//        } else {
//            // 中间快速阶段，使用线性关系将输入值映射到对应的输出范围，确保过渡平滑
//            (input - startSlowRatio) / (1 - startSlowRatio - endSlowRatio)
//        }
//    }

    // 确保慢速占比在合理范围内
    init {
        require(slowRatio in 0.0f..0.5f) { "Slow ratio must be between 0.0 and 0.5" }
    }
    private val accelerateInterpolator = AccelerateInterpolator()
    private val decelerateInterpolator = DecelerateInterpolator()

    override fun getInterpolation(input: Float): Float {
        val acceleratePart = slowRatio
        if (input < acceleratePart) {
            // 加速阶段，使用加速插值器计算
            return accelerateInterpolator.getInterpolation(input / acceleratePart)
        } else {
            // 减速阶段，使用减速插值器计算，需要对输入值进行转换以适配减速插值器的输入范围
            val decelerateStart = acceleratePart
            val deceleratePart = 1 - slowRatio
            return decelerateInterpolator.getInterpolation((input - decelerateStart) / deceleratePart)
        }
    }
}