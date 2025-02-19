package com.hearthappy.framework.example.turntable

import android.graphics.Bitmap

class TurntableBean : ArrayList<TurntableBean.TurntableBeanItem>() {
    data class TurntableBeanItem(val gift_id: Int, val img: String, val price: Int, val title: String, var iconBitmap: Bitmap? = null)
}

data class TurntableItemList(val icons: List<Bitmap>, val title: List<String>,val prices:List<String>)