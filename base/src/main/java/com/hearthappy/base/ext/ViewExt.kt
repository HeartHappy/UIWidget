package com.hearthappy.base.ext

import android.view.View

/**
 * Created Date: 2024/12/24
 * @author ChenRui
 * ClassDescription：View扩展类
 */

/**
 * 根据条件是否显示View
 * @receiver T
 * @param conditions Boolean
 * @param showBlock [@kotlin.ExtensionFunctionType] Function1<T, Unit>
 * @param hideBlock [@kotlin.ExtensionFunctionType] Function1<T, Unit>
 */
fun <T : View> T.show(conditions: Boolean, showBlock: T.() -> Unit = {}, hideBlock: T.() -> Unit = {}) {
    if (conditions) {
        showBlock()
        visible()
    } else {
        hideBlock()
        gone()
    }
}

fun View?.visible() {
    this?.let { if (visibility != View.VISIBLE) visibility = View.VISIBLE }
}

fun View?.gone() {
    this?.let { if (visibility != View.GONE) visibility = View.GONE }
}

fun View?.invisible() {
    this?.let { if (visibility != View.INVISIBLE) visibility = View.INVISIBLE }
}