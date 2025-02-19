package com.hearthappy.base

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentController
import androidx.viewbinding.ViewBinding

/**
 * Created Date: 2024/11/25
 * @author ChenRui
 * ClassDescription： Activity基类
 */
abstract class AbsBaseActivity<VB : ViewBinding> : AppCompatActivity() {
    lateinit var viewBinding: VB

    /** 等待对话框 */

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //        AdaptScreenUtils.adaptWidth(resources, 750)
        //        setLightMode()
        viewBinding = initViewBinding()
        setContentView(viewBinding.root)
        viewBinding.apply {
            initView()
            initViewModelListener()
            initListener()
            initData()
        }
        window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)



        if (openGrayscaleSwitch()) openGrayscale()
    }

    open fun openGrayscaleSwitch(): Boolean = false


    /**
     * 开启灰度页面
     */
    private fun openGrayscale() {
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0F) //灰度效果
        paint.colorFilter = ColorMatrixColorFilter(cm)
        window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
    }

    abstract fun initViewBinding(): VB

    abstract fun VB.initView()
    abstract fun VB.initViewModelListener()
    abstract fun VB.initListener()
    abstract fun VB.initData()


    fun startActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun startActivityForClear(clazz: Class<*>) {
        startActivity(Intent.makeRestartActivityTask(ComponentName(this, clazz)))
    }

    fun initNavController() { //获取mFragments成员变量
        val mFragmentsField = FragmentActivity::class.java.getDeclaredField("mFragments").apply {
            isAccessible = true
        } //获取mCreated成员变量
        val mCreatedField = FragmentActivity::class.java.getDeclaredField("mCreated").apply {
            isAccessible = true
        } //获取dispatchActivityCreated方法
        val dispatchActivityCreatedMethod = FragmentController::class.java.getDeclaredMethod("dispatchActivityCreated").apply {
            isAccessible = true
        } //调用dispatchActivityCreated方法
        dispatchActivityCreatedMethod.invoke(mFragmentsField.get(this))

        //别忘了把mCreated设置为true，防止dispatchActivityCreated在onStart中再次调用
        mCreatedField.set(this, true)
    }


    fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) view = View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}