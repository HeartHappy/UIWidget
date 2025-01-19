package com.hearthappy.base.ext

import java.util.Locale

/**
 * w/kw
 * @receiver Int
 * @param locale Locale
 * @return String
 */
fun Int.formatNumberKW(locale: Locale = Locale.getDefault()): String {
    return when {
        this >= 10000000 -> String.format(locale, "%.1fkw", this / 10000000.0)
        this >= 10000 -> String.format(locale, "%.1fw", this / 10000.0)
        else -> this.toString()
    }
}

/**
 * k\w\wk
 * @receiver Int
 * @return String
 */
fun Int.formatNumber(): String {
    val locale = Locale.getDefault() // 或者选择一个固定的区域设置，如 Locale.US
    return if (this >= 10000000) { // 千万及以上
        String.format(locale, "%.1fkw", this / 10000000.0)
    } else if (this >= 10000) { // 万及以上
        String.format(locale, "%.1fw", this / 10000.0)
    } else if (this >= 1000) { // 千及以上
        String.format(locale, "%.1fk", this / 1000.0)
    } else {
        this.toString()
    }
}