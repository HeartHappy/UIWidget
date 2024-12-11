package com.hearthappy.uiwidget.example.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.base.KBaseAdapter
import com.hearthappy.uiwidget.databinding.ItemCalendarBinding

class CalendarAdapter(val context: Context) : KBaseAdapter<ItemCalendarBinding, CalendarCell>() {
    override fun initViewBinding(parent: ViewGroup, viewType: Int): ItemCalendarBinding {
        return ItemCalendarBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemCalendarBinding.bindViewHolder(data: CalendarCell, position: Int) {
        tvCalendarTitle.text = String.format(data.date.toString())
        if (!data.isEnabled) {
            tvCalendarTitle.setTextColor(ContextCompat.getColor(context, R.color.gray))
        } else {
            tvCalendarTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }
}