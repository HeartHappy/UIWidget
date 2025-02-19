package com.hearthappy.uiwidget.turntable

import android.graphics.Bitmap

open class TurntableSourceAdapter : ITurntableSource {
    override fun icons(): List<Bitmap> = listOf()

    override fun titles(): List<String> = emptyList()

    override fun smallIcons(): List<Bitmap> = emptyList()
}