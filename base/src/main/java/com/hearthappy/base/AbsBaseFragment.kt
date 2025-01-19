package com.hearthappy.base

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 10/11/24
 * @Describe 基础fragment
 */
abstract class AbsBaseFragment<VB : ViewBinding> : Fragment() {


    private var _binding: VB? = null
    val viewBinding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = initViewBinding(inflater, container)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            initView(savedInstanceState)
            initViewModelListener()
            initListener()
            initData()
        }
    }

    abstract fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * 初始化页面相关配置
     */
    abstract fun VB.initView(savedInstanceState: Bundle?)

    abstract fun VB.initListener()

    abstract fun VB.initViewModelListener()


    /**
     * 初始化数据
     */
    abstract fun VB.initData()


    fun startActivity(clazz: Class<*>) {
        startActivity(Intent(context, clazz))
    }

    fun startActivityForClear(clazz: Class<*>) {
        startActivity(Intent.makeRestartActivityTask(context?.let { ComponentName(it, clazz) }))
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}