package com.hearthappy.uiwidget.turntable

interface TurntableCallback {
    fun onSingleDrawEndListener(index: Int, text: String?)

    fun onMoreDrawEndListener(multipleLottery: List<MultipleLottery>)

    fun onRotationAngleListener(totalAngle: Float, currentAngle: Float)
}