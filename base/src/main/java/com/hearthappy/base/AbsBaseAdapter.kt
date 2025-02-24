package com.hearthappy.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 10/11/24
 * @Describe 万用适配器
 */
abstract class AbsBaseAdapter<VB : ViewBinding, T>(var list: MutableList<T> = mutableListOf()) : RecyclerView.Adapter<AbsBaseAdapter<VB, T>.ViewHolder>() {

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
            notifyItemRangeChanged(position, list.size - position)
            return removeAt
        }
        return null
    }

    fun removeAll(){
        list.clear()
        notifyDataSetChanged()
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

    fun updateData(position: Int,data: T){
        list[position] = data
        notifyItemChanged(position)
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