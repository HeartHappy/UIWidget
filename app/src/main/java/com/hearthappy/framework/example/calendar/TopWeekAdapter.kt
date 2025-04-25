package com.hearthappy.framework.example.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hearthappy.basic.AbsBaseAdapter
import com.hearthappy.framework.databinding.ItemTopWeekBinding

class TopWeekAdapter(private val context: Context) : AbsBaseAdapter<ItemTopWeekBinding, String>() {
    override fun initViewBinding(parent: ViewGroup, viewType: Int): ItemTopWeekBinding {
        return ItemTopWeekBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemTopWeekBinding.bindViewHolder(data: String, position: Int) {
        tvCalendarTitle.text = data
    }
}