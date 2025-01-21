package com.hearthappy.uiwidget.turntable

private fun calculateAcceleration() {
    var rotationRadian: Float = 0f //旋转弧度，持续变化
    var totalRotationRadian: Float = 720f //旋转总弧度
    val startingPoint = -90 //默认是在3点方向绘制，-90度让起点在12点方向执行
    var startSpeed = 0.35f // 控制转盘开始速度，值越大开始的速度越快
    var druration = 6f
    val decelerationTime = druration * 0.2f //减速时间
    val accelerationTime = druration - decelerationTime //加速时间
    val finalVelocity = totalRotationRadian / druration
    startSpeed = finalVelocity / accelerationTime


    // 输出加速度
    println("Calculated Acceleration: $startSpeed,finalVelocity:$finalVelocity,减速时间：$decelerationTime,加速时间：$accelerationTime")
}

fun main() {
    calculateAcceleration()
}