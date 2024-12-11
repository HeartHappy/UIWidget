package com.hearthappy.uiwidget.example.calendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.hearthappy.uiwidget.databinding.ActivityCalendarBinding
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCalendarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding=ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding.apply {
            val calendarAdapter = CalendarAdapter(this@CalendarActivity)
            rvCalendar.adapter=calendarAdapter
            rvCalendar.layoutManager=GridLayoutManager(this@CalendarActivity,7)
            calendarAdapter.initData(generateCalendarCells(2024,11))
        }
    }
    private fun generateCalendarCells( year:Int, month:Int ): MutableList<CalendarCell> {
        val cells = mutableListOf<CalendarCell>()

        val instance = Calendar.getInstance()
        instance.set(year,month-1,1)
        val dayInMonth = instance.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = instance.get(Calendar.DAY_OF_WEEK)
        //add empty white calendar
        for (i in 1 until firstDayOfWeek){
            cells.add(CalendarCell(0, false, true))
        }
        for ( day in 1..dayInMonth){
            val isCurrentMonth=true
            val isGrayedOut=false
            cells.add(CalendarCell(day, isCurrentMonth, isGrayedOut))
        }
        return cells
    }
}