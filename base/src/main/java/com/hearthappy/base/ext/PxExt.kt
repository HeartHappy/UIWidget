package com.hearthappy.base.ext

import android.content.res.Resources
import kotlin.math.roundToInt

fun Int.toPx(): Int {
    return (Resources.getSystem().displayMetrics.density * this).roundToInt()
}

fun Float.toDp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)
}