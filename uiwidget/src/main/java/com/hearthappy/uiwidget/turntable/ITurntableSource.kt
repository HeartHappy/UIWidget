package com.hearthappy.uiwidget.turntable

import android.graphics.Bitmap

interface ITurntableSource {

    //显示图标
    fun icons(): List<Bitmap>

    //奖品名称
    fun titles(): List<String>

    //价格，如果有价格，连抽中奖会将价格最大的置顶显示
    fun prices(): List<String>

    //显示小图标，排列在文本后
    fun smallIcons(): List<Bitmap>
}