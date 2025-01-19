package com.hearthappy.base.ext

import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView

const val HORIZONTAL = 0
const val VERTICAL = 1
fun TextView.setTextGradientColor(colors: IntArray, orientation: Int = VERTICAL) {
    if (orientation == HORIZONTAL) {
        paint.shader = LinearGradient(0f, 0f, paint.measureText(text.toString()), 0f, colors, null, Shader.TileMode.CLAMP)
    } else {
        paint.shader = LinearGradient(0f, 0f, 0f, 30f, colors, null, Shader.TileMode.CLAMP)
    }
}