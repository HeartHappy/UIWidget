package com.hearthappy.uiwidget.widget.gantt

data class CalendarCell(
    val date: Int,  // 日期数字，比如1号、2号等
    val isEnabled: Boolean , // 是否可正常显示（即不是灰色的那种），用于区分前面灰色部分和正常部分
    val isGrayedOut:Boolean
)