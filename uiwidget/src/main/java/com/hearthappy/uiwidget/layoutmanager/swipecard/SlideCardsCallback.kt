package com.hearthappy.uiwidget.layoutmanager.swipecard

import android.graphics.Canvas
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.uiwidget.layoutmanager.swipecard.SlideCardsRecyclerView.OnCallback
import kotlin.math.sqrt


/**
 * @Author ChenRui
 * @Email  1096885636@qq.com
 * @Date  2024/11/7 13:42
 * @description 滑动卡片Callback
 */
open class SlideCardsCallback<T>(dragDirs: Int, swipeDirs: Int, @JvmField protected var mRv: RecyclerView, protected var mAdapter: AdvancedAdapter<*, T>, private val cycleMode: SlideCardsRecyclerView.CycleMode, private val onCallback: OnCallback?) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    var isDraggingOrSwiping: Boolean = false

    constructor(rv: RecyclerView, adapter: AdvancedAdapter<*, T>, cycleMode: SlideCardsRecyclerView.CycleMode, onCallback: OnCallback?) : this(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, rv, adapter, cycleMode, onCallback)

    //水平方向是否可以被回收掉的阈值
    fun getThreshold(viewHolder: RecyclerView.ViewHolder): Float { //2016 12 26 考虑 探探垂直上下方向滑动，不删除卡片，这里参照源码写死0.5f
        return mRv.width *  /*getSwipeThreshold(viewHolder)**/0.5f
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { //Log.e("swipecard", "onSwiped() called with: viewHolder = [" + viewHolder + "], direction = [" + direction + "]");
        //rollBack(viewHolder);


        when (cycleMode) {
            SlideCardsRecyclerView.CycleMode.Cycle -> { //★实现循环的要点
                val removeData = mAdapter.removeData(viewHolder.layoutPosition)
                removeData?.let {
                    mAdapter.insertData(it, 0)
                    mAdapter.notifyDataSetChanged()
                }

            }

            SlideCardsRecyclerView.CycleMode.Loaded -> { //★实现隐式加载
                mAdapter.removeData(viewHolder.layoutPosition)
                mAdapter.notifyDataSetChanged()
                if (mAdapter.list.size == CardConfig.MAX_SHOW_COUNT) {
                    Log.d(TAG, "onSwiped: ★ 实现隐式加载")
                    onCallback?.onLoaded(viewHolder, direction)
                }
            }
        }

    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive) //Log.e("swipecard", "onChildDraw()  viewHolder = [" + viewHolder + "], dX = [" + dX + "], dY = [" + dY + "], actionState = [" + actionState + "], isCurrentlyActive = [" + isCurrentlyActive + "]");
        //人人影视的效果
        //if (isCurrentlyActive) {
        //先根据滑动的dxdy 算出现在动画的比例系数fraction
        val swipValue = sqrt((dX * dX + dY * dY).toDouble())
        var fraction = swipValue / getThreshold(viewHolder) //边界修正 最大为1
        if (fraction > 1) {
            fraction = 1.0
        } //对每个ChildView进行缩放 位移
        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {
            val child = recyclerView.getChildAt(i) //第几层,举例子，count =7， 最后一个TopView（6）是第0层，
            val level = childCount - i - 1
            if (level > 0) {
                child.scaleX = (1 - CardConfig.SCALE_GAP * level + fraction * CardConfig.SCALE_GAP).toFloat()

                if (level < CardConfig.MAX_SHOW_COUNT - 1) {
                    child.scaleY = (1 - CardConfig.SCALE_GAP * level + fraction * CardConfig.SCALE_GAP).toFloat()
                    child.translationY = (CardConfig.TRANS_Y_GAP * level - fraction * CardConfig.TRANS_Y_GAP).toFloat()
                } else { //child.setTranslationY((float) (mTranslationYGap * (level - 1) - fraction * mTranslationYGap));
                }
            }
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState) // 当拖拽或滑动开始时设置状态变量
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            isDraggingOrSwiping = true
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        isDraggingOrSwiping = false
    }

    companion object {
        private const val TAG = "RenRenCallback"

    }
    abstract class AdvancedAdapter<VB : ViewBinding, T>(var list: MutableList<T> = mutableListOf()) : RecyclerView.Adapter<AdvancedAdapter<VB, T>.ViewHolder>() {

        inner class ViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)


        private lateinit var viewBinding: VB

        abstract fun initViewBinding(parent: ViewGroup, viewType: Int): VB

        abstract fun VB.bindViewHolder(data: T, position: Int)

        fun initData(list:List<T>) {
            if (list.isEmpty()) return
            notifyItemRangeRemoved(0, this.list.size)
            this.list.clear()
            this.list.addAll(list)
            notifyItemRangeChanged(0, list.size)
        }

        fun insertData(data: T) {
            this.list.add(data)
            notifyItemRangeChanged(list.size - 1, list.size)
        }

        fun insertData(data: T, position: Int) {
            this.list.add(position, data)
            notifyItemRangeChanged(position, list.size)
        }

        fun removeData(position: Int):T? {
            if (position >= 0 && position < list.size) {
                val removeAt = this.list.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, list.size - 1)
                return removeAt
            }
            return null
        }

        fun addData(list: List<T>) {
            val oldPosition = this.list.size
            if (list.isEmpty()) return
            this.list.addAll(list)
            notifyItemRangeChanged(oldPosition, this.list.size)
        }

        fun addData(list: List<T>, position: Int) {
            val oldPosition = this.list.size
            if (list.isEmpty()) return
            this.list.addAll(position, list)
            notifyItemRangeChanged(oldPosition, this.list.size)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            viewBinding = initViewBinding(parent, viewType)
            return ViewHolder(viewBinding)
        }


        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.viewBinding.bindViewHolder(list[position], position)
        }
    }
}
