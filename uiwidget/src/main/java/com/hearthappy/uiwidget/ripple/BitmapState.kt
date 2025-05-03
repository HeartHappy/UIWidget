package com.hearthappy.uiwidget.ripple

import android.graphics.Bitmap
import android.util.Log

class BitmapState {
    // 当前可见的 Bitmap 集合（最多保留3个）
    private val bitmaps = mutableMapOf<Int, Bitmap>()

    // 当前中心位置
    var centerPosition = 0
        private set

    // 可见范围（当前位及前后各一个）
    private val visibleRange: IntRange
        get() = (centerPosition - 1)..(centerPosition + 1)

    // 更新位置时自动管理缓存
    fun updatePosition(newPosition: Int) {
        centerPosition = newPosition
        trimCache()
    }

    // 获取指定位置 Bitmap
    fun get(position: Int): Bitmap? = bitmaps[position]

    // 添加 Bitmap
    fun put(position: Int, bitmap: Bitmap) {
        bitmaps[position] = bitmap
    }

    // 清理超出可见范围的缓存
    internal fun trimCache() {
        val toRemove = bitmaps.keys.filter { it !in visibleRange }
        toRemove.forEach {
            bitmaps[it]?.recycle()
            bitmaps.remove(it)
        }
    }

    fun printMemoryUsage() {
        val totalSize = bitmaps.values.sumBy { it.allocationByteCount }
        Log.d("Memory", "Cached: ${bitmaps.size} bitmaps, Total: ${totalSize / 1024}KB")
    }
    // 销毁所有
    fun destroy() {
        bitmaps.values.forEach { it.recycle() }
        bitmaps.clear()
    }

}