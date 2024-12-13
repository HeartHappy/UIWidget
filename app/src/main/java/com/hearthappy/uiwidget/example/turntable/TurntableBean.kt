package com.hearthappy.uiwidget.example.turntable

import android.graphics.Bitmap

class TurntableBean : ArrayList<TurntableBean.TurntableBeanItem>(){
    data class TurntableBeanItem(
        val gift_id: Int,
        val img: String,
        val price: Int,
        val title: String,
        var iconBitmap:Bitmap?=null
    )
}