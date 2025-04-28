package com.hearthappy.uiwidget.layoutmanager.water

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.hearthappy.uiwidget.layoutmanager.swipecard.SlideCardsCallback
import kotlin.math.hypot

class WaterRipplesRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var duration: Long = 0
    private var animatorSet: AnimatorSet? = null
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> { // 找到被点击的 Item
                val child = findChildViewUnder(e.x, e.y)
                child?.let {
                    val holder = getChildViewHolder(it) // 直接触发动画，不传递点击事件
                    val position = getChildAdapterPosition(it)
                    switchView(holder.itemView, e.x, e.y, position)
                    return true // 拦截事件
                }
            }
        }
        return super.onInterceptTouchEvent(e)
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        layoutManager = WaterRipplesLayoutManger()
        super.setAdapter(adapter)
    }

    fun switchView(itemView: View, x: Float, y: Float, position: Int) {
        val widthPixels = resources.displayMetrics.widthPixels
        val heightPixels = resources.displayMetrics.heightPixels
        val endRadius = hypot(widthPixels.toDouble(), heightPixels.toDouble()).toFloat()

        val circularReveal = ViewAnimationUtils.createCircularReveal(itemView, x.toInt(), y.toInt(), 0f, endRadius) //        val circularReveal = ViewAnimationUtils.createCircularReveal(itemView, x.toInt(), y.toInt(), endRadius, 0f)

        val objectAnimator = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator, circularReveal)
        animatorSet.duration = 1000
        animatorSet.addListener(onEnd = {
            if (position != NO_POSITION) {
                (adapter as? SlideCardsCallback.AdvancedAdapter<*, *>)?.apply {
                    removeData(position)
                    this.notifyDataSetChanged()
                }

            }
        })
        animatorSet.start()
    }

    inner class WaterRipplesLayoutManger : RecyclerView.LayoutManager() {
        override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
            return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
            if (itemCount == 0) {
                detachAndScrapAttachedViews(recycler)
                return
            } // 先将所有子视图从 RecyclerView 分离并放入回收池
            detachAndScrapAttachedViews(recycler)
            for (i in itemCount - 1 downTo 0) {
                val view = recycler.getViewForPosition(i)
                addView(view)
                measureChildWithMargins(view, 0, 0)
                val width = getDecoratedMeasuredWidth(view)
                val height = getDecoratedMeasuredHeight(view)
                val left = paddingLeft
                val top = paddingTop
                val right = left + width
                val bottom = top + height
                layoutDecorated(view, left, top, right, bottom)
            }
        } //        override fun supportsPredictiveItemAnimations(): Boolean { //            return true
        //        }
        //        override fun onItemsRemoved(
        //            recyclerView: RecyclerView,
        //            positionStart: Int,
        //            itemCount: Int
        //        ) {
        //            super.onItemsRemoved(recyclerView, positionStart, itemCount)
        //            requestLayout()
        //        }
    }

}