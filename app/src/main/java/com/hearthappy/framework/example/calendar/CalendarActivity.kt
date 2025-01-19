package com.hearthappy.framework.example.calendar

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.hearthappy.framework.databinding.ActivityCalendarBinding
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCalendarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding.apply {
            val currentYearMonth = getCurrentYearMonth()
            updateYearMonth(currentYearMonth)
            val topWeekAdapter = TopWeekAdapter(this@CalendarActivity)
            rvWeek.adapter=topWeekAdapter
            rvWeek.layoutManager= GridLayoutManager(this@CalendarActivity, 7)
            topWeekAdapter.initData(listOf("日","一","二","三","四","五","六"))

            val monthAdapter = MonthAdapter(this@CalendarActivity, currentYearMonth)
            vpCalendar.adapter = monthAdapter
            vpCalendar.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 设置初始页面为初始年月的索引
            vpCalendar.setCurrentItem(Int.MAX_VALUE / 2, false)
            vpCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val yearMonth = monthAdapter.getItem(position)
                    updateYearMonth(yearMonth)

                }
            })
        }
    }

    fun ActivityCalendarBinding.updateYearMonth(yearMonth: YearMonth) {
        tvYearMonth.text = String.format(Locale.CANADA, "%04d-%02d", yearMonth.year, yearMonth.month)
    }

    private fun getCurrentYearMonth(): YearMonth {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            val year: Int = now.year
            val month: Int = now.monthValue
            val day = now.dayOfMonth
            YearMonth(year, month, day)
        } else {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            YearMonth(year, month, day)
        }
    }
}