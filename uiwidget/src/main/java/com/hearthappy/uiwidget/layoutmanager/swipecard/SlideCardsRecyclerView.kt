package com.hearthappy.uiwidget.layoutmanager.swipecard

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author ChenRui
 * @Email  1096885636@qq.com
 * @Date  2024/11/7 10:08
 * @description 简单封装卡片效果的rv，增加对事件拦截处理的优化（主要解决与ViewPager，滑动和刷新组件的滑动冲突）
 */
class SlideCardsRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    /**
     * Initializes
     * @param adapter KBaseAdapter<*, T> 适配器
     * @param cycleMode CycleMode 循环模式
     * @param onCallback OnCallback
     */
    fun <T> initialization(adapter: SlideCardsCallback.AdvancedAdapter<*, T>, cycleMode: CycleMode, onCallback: OnCallback) {
        CardConfig.initConfig(this.context)
        this.layoutManager = SlideCardsLayoutManager()
        this.adapter = adapter
        // 初始化ItemTouchHelper并附加到RecyclerView
        val callback = SlideCardsCallback(this, adapter, cycleMode, onCallback)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(this)
    }


    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // 如果callback正在处理滑动事件，则消费此事件，不向上层传递
//        if (callback.isDraggingOrSwiping) {
//            return true // 消费了事件
//        }
        when (e.action) {
            MotionEvent.ACTION_DOWN                          -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.onInterceptTouchEvent(e)
    }

    interface OnCallback {
        //加载
        fun onLoaded(viewHolder: ViewHolder, direction: Int)

    }

    sealed class CycleMode {
         object Cycle : CycleMode() //数据循环
         object Loaded : CycleMode()//数据不够时，隐式加载
    }
}