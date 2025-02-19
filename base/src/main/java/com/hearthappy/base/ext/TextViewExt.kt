package com.hearthappy.base.ext

import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

const val HORIZONTAL = 0
const val VERTICAL = 1
fun TextView.setTextGradientColor(colors: IntArray, orientation: Int = VERTICAL) {
    if (orientation == HORIZONTAL) {
        paint.shader = LinearGradient(0f, 0f, paint.measureText(text.toString()), 0f, colors, null, Shader.TileMode.CLAMP)
    } else {
        paint.shader = LinearGradient(0f, 0f, 0f, 30f, colors, null, Shader.TileMode.CLAMP)
    }
}
fun TextView.setBackgroundGradient(startColor: Int,endColor: Int){
    // 创建一个 GradientDrawable 对象
    val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,  // 渐变方向从左到右
        intArrayOf(startColor, endColor) // 渐变颜色数组
    )
    val rightRadius =8f.dp2px()
    val radii = floatArrayOf(0f, 0f, rightRadius, rightRadius, rightRadius, rightRadius, 0f, 0f)
    gradientDrawable.cornerRadii = radii
    // 将 GradientDrawable 设置为 BLTextView 的背景
    background = gradientDrawable
}

fun TextView.setLinkText(linkText: String, @ColorRes linkTextColor: Int, content: String, linkBlock: () -> Unit) {
    val spannableString = SpannableString(linkText.plus(content))
    spannableString.setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) {
            linkBlock()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(context, linkTextColor)
        }
    }, 0, linkText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = spannableString
    movementMethod = LinkMovementMethod.getInstance()
}