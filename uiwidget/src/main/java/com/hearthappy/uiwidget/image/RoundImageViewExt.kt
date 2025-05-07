package com.hearthappy.uiwidget.image

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.hearthappy.uiwidget.utils.dp2px


fun RoundImageView.setGradientInnerBorderAnger(angle: Float) {
    innerBorderGradientAngle = angle
    updateGradient(innerBorderGradientColors, innerBorderGradientPositions, innerBorderGradientAngle, innerBorderMatrix, innerBorderWidth, innerBorderPaint, borderGradientType)
    invalidate()
}

fun RoundImageView.setGradientInnerBorderColors(colors: IntArray, positions: FloatArray? = null) {
    innerBorderGradientColors = colors
    innerBorderGradientPositions = positions
    updateGradient(innerBorderGradientColors, innerBorderGradientPositions, innerBorderGradientAngle, innerBorderMatrix, innerBorderWidth, innerBorderPaint, innerBorderGradientType)
    invalidate()
}

fun RoundImageView.setGradientBorderAnger(angle: Float) {
    borderGradientAngle = angle
    updateGradient(borderGradientColors, borderGradientPositions, borderGradientAngle, borderMatrix, borderWidth, borderPaint, borderGradientType)
    invalidate()
}

// 动态设置渐变
fun RoundImageView.setGradientBorderColors(colors: IntArray, positions: FloatArray? = null) {
    borderGradientColors = colors
    borderGradientPositions = positions
    updateGradient(borderGradientColors, borderGradientPositions, borderGradientAngle, borderMatrix, borderWidth, borderPaint, borderGradientType)
    invalidate()
}


// 动态设置颜色滤镜
fun RoundImageView.setViewColorFilter(@ColorInt color: Int) {
    colorFilterColor = color
    isGrayscale = false // 关闭灰度
    updateColorFilter()
    invalidate()
}

// 动态设置灰度模式
fun RoundImageView.setGrayscale(enabled: Boolean) {
    isGrayscale = enabled
    updateColorFilter()
    invalidate()
}

// 动态设置内边框
fun RoundImageView.setInnerBorder(width: Float, @ColorInt color: Int) {
    if (isReasonable(width, color)) {
        innerBorderWidth = width.dp2px()
        innerBorderColor = color
        innerBorderPaint.strokeWidth = borderWidth + innerBorderWidth
        innerBorderPaint.color = color
        invalidate()
    }
}

//设置外边框
fun RoundImageView.setBorder(width: Float, @ColorInt color: Int) {
    if (isReasonable(width, color)) {
        borderWidth = width.dp2px()
        borderColor = color
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = color
        invalidate()
    }
}

fun RoundImageView.setInnerGlow(radius: Float, @ColorInt color: Int) {
    if (radius > 0f && color != Color.TRANSPARENT) {
        innerGlowRadius = radius.dp2px()
        innerGlowColor = color
        innerGlowPaint.maskFilter = BlurMaskFilter(innerGlowRadius, BlurMaskFilter.Blur.NORMAL)
        innerGlowPaint.color = innerGlowColor
        innerGlowPaint.strokeWidth = borderWidth + innerBorderWidth + innerGlowRadius // 动态调整描边宽度
        invalidate()
    }
}

fun RoundImageView.setColorBlendMode(mode: PorterDuff.Mode) {
    colorBlendMode = mode
    updateColorFilter()
    invalidate()
}

fun RoundImageView.setLayersBlendMode(mode: PorterDuff.Mode) {
    layersBlendModel = mode
    layersPaint.xfermode = PorterDuffXfermode(colorBlendMode)
    invalidate()
}

// 新增设置方法
fun RoundImageView.setBlendSize(width: Int, height: Int) {
    blendWidth = width.dp2px()
    blendHeight = height.dp2px()
    invalidate()
}

fun RoundImageView.setBlendGravity(gravity: Int) {
    blendGravity = gravity
    invalidate()
}

fun RoundImageView.setBlendMargin(margin: Int) {
    setBlendMargin(margin, margin, margin, margin)
    invalidate()
}

fun RoundImageView.setBlendMargin(left: Int, top: Int, right: Int, bottom: Int) {
    blendMargin.set(left.dp2px(), top.dp2px(), right.dp2px(), bottom.dp2px())
    invalidate()
}

fun RoundImageView.setRadius(radius: Float) = setRadius(radius, radius, radius, radius)

// 动态设置圆角（支持动画）
fun RoundImageView.setRadius(topLeft: Float = radii[0], topRight: Float = radii[2], bottomRight: Float = radii[4], bottomLeft: Float = radii[6]) {
    setCorners(topLeft.dp2px(), topRight.dp2px(), bottomRight.dp2px(), bottomLeft.dp2px())
    updateClipPath()
    invalidate()
}


fun RoundImageView.setBlendResource(drawable: Drawable) {
    blendDrawable = drawable
    invalidate()
}

fun RoundImageView.setLayersHorSpacing(spacing: Int) {
    layersHorSpacing = spacing
    invalidate()
}

fun RoundImageView.setLayersVerSpacing(spacing: Int) {
    layersVerSpacing = spacing
    invalidate()
}

fun RoundImageView.setLayersWatermarkOn(enable: Boolean) {
    layersWatermarkOn = enable
    invalidate()
}

fun RoundImageView.setIsCircle(enable: Boolean) {
    isCircle = enable
    updateClipPath()
    invalidate()
}

fun RoundImageView.setBorderGradientType(type: RoundImageView.GradientType) {
    updateGradient(borderGradientColors, borderGradientPositions, borderGradientAngle, borderMatrix, borderWidth, borderPaint, type.value())
    invalidate()
}

fun RoundImageView.setInnerBorderGradientType(type: RoundImageView.GradientType) {
    updateGradient(innerBorderGradientColors, innerBorderGradientPositions, innerBorderGradientAngle, innerBorderMatrix, innerBorderWidth, innerBorderPaint, type.value())
    invalidate()
}
