package com.hearthappy.uiwidget.turntable

import android.graphics.Bitmap

interface ITurntableSource {

    //显示图标
    fun icons(): List<Bitmap>

    //奖品名称,如果是价格，多抽则会进行排序，最高的值指向12点
    fun titles(): List<String>

    //显示小图标，排列在文本后
    fun smallIcons(): List<Bitmap>
}

