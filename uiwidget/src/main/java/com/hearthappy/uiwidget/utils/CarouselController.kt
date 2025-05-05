package com.hearthappy.uiwidget.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created Date: 5/4/25
 * @author ChenRui
 * ClassDescription：轮播控制器
 * 1、精准手势控制暂停/恢复
 * 2、可见性控制 暂停/恢复
 * 3、协程安全：使用synchronized块保证状态一致性
 * 4、生命周期安全：通过lifecycleScope自动取消协程
 */
class CarouselController {
    private var job: Job? = null
    private var lastDelayRemaining = 0L
    private val lock = Any()
    private var paused = false
    private var visible = true
    private var weakScope: WeakReference<CoroutineScope>? = null
    fun start(
        scope: CoroutineScope,
        interval: Long,
        onSwitch: suspend () -> Unit
    ) {
        weakScope = WeakReference(scope)
        synchronized(lock) {
            if (job?.isActive == true) return

            job = weakScope?.get()?.launch(Dispatchers.IO) {
                var delayRemaining = lastDelayRemaining.takeIf { it > 0 } ?: interval

                while (true) {
                    // 分段检测（每50ms检查一次状态）
                    while (delayRemaining > 0) {

                        val checkInterval = when {
                            delayRemaining > interval -> 1000L
                            delayRemaining > 1000 -> 500L
                            else -> 50L
                        }
                        delay(checkInterval)
                        delayRemaining -= checkInterval

                        if (shouldPause()) {
                            lastDelayRemaining = delayRemaining
                            break
                        }
                    }

                    if (shouldSwitch()) {
                        onSwitch()
                    }
                    delayRemaining = interval
                }
            }
        }
    }

    fun pause() = synchronized(lock) { paused = true }

    fun resume() = synchronized(lock) {
        paused = false
        lastDelayRemaining = 0 // 重置剩余时间
    }

    fun setVisible(visible: Boolean) = synchronized(lock) {
        this.visible = visible
        if (!visible) pause() else resume()
    }

    private fun shouldPause() = synchronized(lock) {
        paused || !visible
    }

    private fun shouldSwitch() = synchronized(lock) {
        !paused && visible
    }

    fun cancel() {
        job?.cancel()
    }
    companion object{
        private const val TAG = "CarouselController"
    }
}