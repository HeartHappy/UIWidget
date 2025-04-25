package com.hearthappy.uiwidget.ninegrid

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hearthappy.uiwidget.R


/**
 * Created Date: 2025/4/24
 * @author ChenRui
 * ClassDescription：九宫格RecyclerView
 * 1、支持自定义不同列的item宽高
 * 2、支持自定义item，修改UI
 * 3、支持自定义属性
 */
class NineGridView : RecyclerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { //自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NineGridView)
        nineAdapter.singleWidth = typedArray.getDimension(R.styleable.NineGridView_ngv_row1_column1_width, nineAdapter.singleWidth.toFloat()).toInt()
        nineAdapter.singleHeight = typedArray.getDimension(R.styleable.NineGridView_ngv_row1_column1_height, nineAdapter.singleHeight.toFloat()).toInt()
        nineAdapter.itemSize = typedArray.getDimension(R.styleable.NineGridView_ngv_item_size, nineAdapter.itemSize.toFloat()).toInt()
        nineAdapter.itemMargin = typedArray.getDimension(R.styleable.NineGridView_ngv_item_margin, nineAdapter.itemMargin)
        nineAdapter.itemMarginVertical = typedArray.getDimension(R.styleable.NineGridView_ngv_item_margin_vertical, nineAdapter.itemMarginVertical)
        nineAdapter.itemMarginHorizontal = typedArray.getDimension(R.styleable.NineGridView_ngv_item_margin_horizontal, nineAdapter.itemMarginHorizontal)
        typedArray.recycle()
    }

    private var data: List<String> = emptyList()
    private val nineAdapter: NineGridAdapter by lazy { NineGridAdapter(context) }


    infix fun display(data: List<String>): NineGridView {
        initLayout(data)
        return this
    }


    private fun initLayout(data: List<String>) {
        this.data = data
        val spanCount: Int = getSpanCount(this.data.size)
        val layoutManager = StaggeredGridLayoutManager(spanCount, GridLayoutManager.VERTICAL)
        setLayoutManager(layoutManager)
        adapter = nineAdapter
        nineAdapter.initData(data.take(9))
    }

    private fun getSpanCount(itemCount: Int): Int {
        return when (itemCount) {
            1 -> 1
            2, 4 -> 2
            else -> 3
        }
    }

    fun setSingleSize(width: Int, height: Int) {
        nineAdapter.singleWidth = width
        nineAdapter.singleHeight = height
        nineAdapter.notifyItemRangeChanged(0, nineAdapter.itemCount)
    }

    fun setItemSize(size: Int) {
        nineAdapter.itemSize = size
        nineAdapter.notifyItemRangeChanged(0, nineAdapter.itemCount)
    }

    fun setMargin(margin: Float) {
        nineAdapter.itemMargin = margin
        nineAdapter.notifyItemRangeChanged(0, nineAdapter.itemCount)
    }
    fun setMarginVertical(margin: Float) {
        nineAdapter.itemMarginVertical = margin
        nineAdapter.notifyItemRangeChanged(0, nineAdapter.itemCount)
    }

    fun setMarginHorizontal(margin: Float) {
        nineAdapter.itemMarginHorizontal = margin
        nineAdapter.notifyItemRangeChanged(0, nineAdapter.itemCount)
    }

    fun setNineGridListener(listener: OnNineGridListener) {
        nineAdapter.setNineGridListener(listener)
    }


    fun onBindView(listener: (root: View, img: ImageView, data: String, position: Int) -> Unit) {
        nineAdapter.setNineGridListener(object : OnNineGridListener {
            override fun onBindView(root: View, img: ImageView, data: String, position: Int) {
                listener(root, img, data, position)
            }
        })
    }

    interface OnNineGridListener {
        fun onBindView(root: View, img: ImageView, data: String, position: Int)
    }
}