package com.hearthappy.base.utils


import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtil {
    const val BUTTON_CLICK_TRAN = 500L
    const val MINUTE_MILLIS = 60000L
    const val HOUR_MILLIS = 3600000L
    const val DAY_MILLIS = 86400000L
    fun durationToToolBarString(start: Long, end: Long): String {
        var duration = end - start
        val day: Int = (duration / DAY_MILLIS).toInt()
        duration %= DAY_MILLIS
        val hour: Int = (duration / HOUR_MILLIS).toInt()
        duration %= HOUR_MILLIS
        val min: Int = (duration / MINUTE_MILLIS).toInt()

        return "$day D $hour H $min M"
    }

    fun millisToToolBarString(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("EEE MM/dd HH:mm",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToDate(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToYearMonth2Day(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToHourMinutes(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToYearMonth3Day(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy/MMM/dd",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToYearMonth3(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy/MMM",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToYear(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToDay(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToMonth3(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("MMM",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    fun millisToHour(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH",Locale.getDefault())
        return simpleDateFormat.format(Date(time))
    }

    /**
     * 日期字符串转时间戳
     * @param dateString String
     * @param pattern String
     * @return Long
     */
    fun strToMillis(dateString: String,pattern: String="yyyy-MM-dd HH:mm:ss"): Long {
        // 定义日期格式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern(pattern)
            // 将日期字符串解析为 LocalDateTime
            val localDateTime = LocalDateTime.parse(dateString, formatter)

            // 将 LocalDateTime 转换为 ZonedDateTime（默认使用 UTC 时区）
            val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())

            // 将 ZonedDateTime 转换为 Instant
            val instant = zonedDateTime.toInstant()

            // 获取时间戳（以毫秒为单位）
            val timestamp = instant.toEpochMilli()
            return timestamp
        } else {
            return 0L
        }
    }


}