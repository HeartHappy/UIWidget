package com.hearthappy.framework.example.carouse

import android.util.Log
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.framework.databinding.ActivityCarouseViewBinding
import com.hearthappy.uiwidget.viewpager.PagerTransformer

class CarouseViewActivity : AbsBaseActivity<ActivityCarouseViewBinding>() {

    override fun ActivityCarouseViewBinding.initViewModelListener() {
    }

    override fun ActivityCarouseViewBinding.initView() {
        val url = "http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/5aa3cfcea145a90d7de7b73f68c35e52.png"
        val carouselAdapter = CarouselAdapter()
        vp.setAdapter(carouselAdapter)
//        vp.setPageTransformer(PagerTransformer(PagerTransformer.AnimType.SCALE))
        carouselAdapter.initData(listOf(url, url, url, url, url, url))
        vp.addListener(onPageSelected = {
            Log.d("TAG", "onPageSelected: ${it%6}")
        })
    }

    override fun ActivityCarouseViewBinding.initListener() {
    }

    override fun ActivityCarouseViewBinding.initData() {
    }
}