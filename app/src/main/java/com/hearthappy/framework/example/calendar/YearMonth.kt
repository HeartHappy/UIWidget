package com.hearthappy.framework.example.calendar

data class YearMonth(var year: Int, var month: Int,var day:Int=1){
    fun previousMonth(): YearMonth {
        var prevMonth = month - 1
        var prevYear = year
        if (prevMonth == 0) {
            prevMonth = 12
            prevYear -= 1
        }
        return YearMonth(prevYear, prevMonth)
    }

    fun nextMonth(): YearMonth {
        var nextMonth = month + 1
        var nextYear = year
        if (nextMonth == 13) {
            nextMonth = 1
            nextYear += 1
        }
        return YearMonth(nextYear, nextMonth)
    }
}