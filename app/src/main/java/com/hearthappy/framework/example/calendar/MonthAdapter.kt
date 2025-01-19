package com.hearthappy.framework.example.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hearthappy.framework.databinding.ItemYearMonthBinding
import java.util.Calendar

class MonthAdapter(private val context: Context, private val yearMonth: YearMonth) : RecyclerView.Adapter<MonthAdapter.ViewHolder>() {
    inner class ViewHolder(val viewBinding: ItemYearMonthBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemYearMonthBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewBinding.apply {
            val item = getItem(position)

            val dayAdapter = DayAdapter(context, if (item.year == yearMonth.year && item.month == yearMonth.month) yearMonth else null)
            rvCalendar.adapter = dayAdapter
            rvCalendar.layoutManager = GridLayoutManager(context, 7) // 根据当前位置计算出真实的年月数据

            dayAdapter.initData(generateCalendarCells(item.year, item.month))
        }
    }

    fun getItem(position: Int): YearMonth { // position 为当前页码，假设初始为0
        val totalMonths = (yearMonth.year - 1) * 12 + (yearMonth.month - 1) + position - Int.MAX_VALUE / 2
        val year = totalMonths / 12 + 1
        val month = totalMonths % 12 + 1
        return YearMonth(year, month, yearMonth.day)
    }


    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }


    /**
     * 获取所有日期，并推断上月空位
     * @param year Int
     * @param month Int
     * @return MutableList<CalendarCell>
     */
    private fun generateCalendarCells(year: Int, month: Int): MutableList<CalendarCell> {
        val cells = mutableListOf<CalendarCell>()

        val instance = Calendar.getInstance()
        instance.set(year, month - 1, 1)
        val dayInMonth = instance.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = instance.get(Calendar.DAY_OF_WEEK) //add empty white calendar
        for (i in 1 until firstDayOfWeek) {
            cells.add(CalendarCell(0))
        }
        for (day in 1..dayInMonth) {
            cells.add(CalendarCell(day, isEnabled = true, isGrayedOut = false))
        }
        return cells
    }

}