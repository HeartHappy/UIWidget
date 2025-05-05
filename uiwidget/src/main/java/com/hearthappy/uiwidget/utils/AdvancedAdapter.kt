package com.hearthappy.uiwidget.utils

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

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