package com.hearthappy.uiwidget.ninegrid

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.hearthappy.uiwidget.databinding.ItemNineGridBinding
import com.hearthappy.uiwidget.ninegrid.NineGridView.OnNineGridListener
import com.hearthappy.uiwidget.utils.dp2px


class NineGridAdapter(val context: Context) : RecyclerView.Adapter<NineGridAdapter.ViewHolder>() {
    private var onNineGridListener: OnNineGridListener? = null
    private var list: ArrayList<String> = arrayListOf()
    private var defaultMargin = 2f.dp2px()
    internal var singleWidth = 300.dp2px()
    internal var singleHeight = 200.dp2px()
    internal var itemSize = 120.dp2px()
    internal var itemMargin = defaultMargin
    internal var itemMarginVertical = defaultMargin
    internal var itemMarginHorizontal = defaultMargin

    inner class ViewHolder(val viewBinding: ItemNineGridBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    }

    fun initData(list: List<String>) {
        this.list.addAll(list)
        notifyItemRangeChanged(0, list.size)
    }

    fun setNineGridListener(listener: OnNineGridListener) {
        onNineGridListener = listener
    }


    companion object {
        private const val TAG = "NineGridAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNineGridBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.viewBinding.apply {
            root.layoutParams.apply {
                if (itemCount == 1) {
                    width = singleWidth
                    height = singleHeight
                } else if (itemCount > 1) {
                    width = itemSize
                    height = itemSize
                }
                (this as RecyclerView.LayoutParams).apply {
                    if (itemMargin != defaultMargin) {
                        setMargins(itemMargin.toInt())
                    } else if (itemMarginVertical != defaultMargin || itemMarginHorizontal != defaultMargin) {
                        setMargins(itemMarginHorizontal.toInt(), itemMarginVertical.toInt(), itemMarginHorizontal.toInt(), itemMarginVertical.toInt())
                    }
                }
            }

            onNineGridListener?.onBindView(root, ivPicture, data, position)
        }
    }
}