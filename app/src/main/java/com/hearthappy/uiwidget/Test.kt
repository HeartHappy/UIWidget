package com.hearthappy.uiwidget

import com.hearthappy.uiwidget.turntable.MultipleLottery

fun limitTargetAngle(targetAngle: Float): Float {
    val circleDegree = 360f  // 每圈的度数
    val minCircle = 5  // 最少转动的圈数
    val targetCircle = (targetAngle / circleDegree).toInt()  // 计算当前角度对应的总圈数
    return if (targetCircle >= minCircle) {
        targetAngle % (minCircle * circleDegree)
    } else {
        targetAngle
    }
}

/**
 * 获取相对角度
 * @param index Int
 * @return Float
 */
fun getRelativeAngle(relativeIndex: Int, index: Int): Float {
    val indexDiff = index - relativeIndex
    return when {
        indexDiff >= 0 -> (indexDiff * 30f)
        indexDiff < 0 && index >= 0 -> (indexDiff + 12) * 30f
        index == 0 -> (12 + indexDiff) * 30f
        else -> throw IllegalArgumentException("无效的索引值")
    }
}

fun main() {
    val relativeAngle = getRelativeAngle(6, 2)
//    val of = setOf(MultipleLottery(1, 1.0f), MultipleLottery(1, 1.0f), MultipleLottery(2, 1.0f), MultipleLottery(3, 2.0f), MultipleLottery(2, 1.0f))
//    println("限制后的角度: $relativeAngle,${of.size},${of.toList()}")
}