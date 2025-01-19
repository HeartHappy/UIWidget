package com.hearthappy.uiwidget.layoutmanager.swipecard

import android.content.Context


/**
 * @Author ChenRui
 * @Email  1096885636@qq.com
 * @Date  2024/11/7 14:56
 * @description   介绍：一些配置
 *  1、 界面最多显示几个View
 *  2、 每一级View之间的Scale差异、translationY等等
 */
object CardConfig {
    //屏幕上最多同时显示几个Item
    @JvmField
    var MAX_SHOW_COUNT: Int = 0

    //每一级Scale相差0.05f，translationY相差7dp左右
    @JvmField
    var SCALE_GAP: Float = 0f

    @JvmField
    var TRANS_Y_GAP: Int = 0

    fun initConfig(context: Context) {
        MAX_SHOW_COUNT = 3
        SCALE_GAP = 0.05f
        TRANS_Y_GAP = 25
    }
}
