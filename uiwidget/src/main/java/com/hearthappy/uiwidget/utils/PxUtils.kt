package com.hearthappy.uiwidget.utils

import android.content.Context
import android.content.res.Resources


/**
 * Created Date: 2025/2/7
 * @author ChenRui
 * ClassDescription：像素转换
 *
 */

fun Int.sp2px(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
}

fun Float.sp2px(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
}

fun Int.dp2px(): Float {
    return Resources.getSystem().displayMetrics.density * this
}

fun Float.dp2px(): Float {
    return Resources.getSystem().displayMetrics.density * this
}

fun Int.px2dp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)

}

fun Float.px2dp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)
}
