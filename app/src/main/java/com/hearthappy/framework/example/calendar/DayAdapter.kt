package com.hearthappy.framework.example.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ItemCalendarBinding
import com.hearthappy.base.AbsBaseAdapter

class DayAdapter(private val context: Context, private val yearMonth: YearMonth?) : AbsBaseAdapter<ItemCalendarBinding, CalendarCell>() {

    override fun initViewBinding(parent: ViewGroup, viewType: Int): ItemCalendarBinding {
        return ItemCalendarBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    private var preSelect: TextView? = null

    override fun ItemCalendarBinding.bindViewHolder(data: CalendarCell, position: Int) {
        tvCalendarTitle.text = String.format(data.date.toString())
        if (!data.isEnabled) {
            tvCalendarTitle.setTextColor(ContextCompat.getColor(context, R.color.gray))
            tvCalendarTitle.visibility = View.GONE
        } else {
            tvCalendarTitle.setTextColor(ContextCompat.getColor(context, yearMonth?.takeIf { it.day == data.date }?.run {
                tvCalendarTitle.setBackgroundResource(R.drawable.bg_sel_current_day)
                R.color.white
            } ?: run { R.color.black }))

        }
        tvCalendarTitle.setOnClickListener {
            preSelect?.let {
                it.isSelected = false
            }
            tvCalendarTitle.isSelected = !tvCalendarTitle.isSelected
            if (tvCalendarTitle.isSelected) {
                preSelect = tvCalendarTitle
            }
        }
    }

    companion object {
        private const val TAG = "CalendarAdapter"
    }
}