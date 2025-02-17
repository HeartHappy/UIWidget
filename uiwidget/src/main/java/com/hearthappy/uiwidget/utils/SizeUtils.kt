package com.hearthappy.uiwidget.utils

import android.content.Context
import android.content.res.Resources
import kotlin.math.roundToInt


/**
 * Created Date: 2025/2/7
 * @author ChenRui
 * ClassDescription：像素转换
 *
 */

object SizeUtils {
    /**
     * dp转px
     *
     * @param context 上下文
     * @param dp      dp值
     * @return px值
     */
    fun dp2px(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param sp      sp值
     * @return px值
     */
    fun sp2px(context: Context, sp: Float): Int {
        return (sp * context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
    }
}
fun Int.toPx(): Int {
    return (Resources.getSystem().displayMetrics.density * this).roundToInt()
}

fun Float.toPx(): Int {
    return ((Resources.getSystem().displayMetrics.density * this).roundToInt())
}

fun Float.toDp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)
}
