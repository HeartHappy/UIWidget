package com.hearthappy.uiwidget.utils.ext

import android.content.res.Resources
import androidx.annotation.ArrayRes

fun Resources.getFloatArray(@ArrayRes resId: Int): FloatArray? {
    return try {
        obtainTypedArray(resId).use { ta ->
            FloatArray(ta.length()).apply { for (i in indices) this[i] = ta.getFloat(i, 0f) }
        }
    } catch (e: Exception) {
        null
    }
}