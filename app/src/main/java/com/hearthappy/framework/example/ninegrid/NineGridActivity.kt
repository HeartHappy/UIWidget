package com.hearthappy.framework.example.ninegrid

import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.framework.databinding.ActivityNineGridBinding

class NineGridActivity : AbsBaseActivity<ActivityNineGridBinding>() {
    val url = "https://dongting10.oss-cn-beijing.aliyuncs.com/upload/sociaty/head/eb4c1aec62eaeb9a7a649e06c7d49096.png"
    private lateinit var nineGridAdapter: NineGridAdapter
    override fun ActivityNineGridBinding.initData() {
        val list = mutableListOf<List<String>>()
        val dataList = mutableListOf<String>()
        for (a in 0..3) {
            for (i in 0..9) {
                dataList.add(url)
            }
            list.add(dataList)
        }
        nineGridAdapter.initData(list)
    }

    override fun ActivityNineGridBinding.initListener() {
    }

    override fun ActivityNineGridBinding.initView() {

        nineGridAdapter = NineGridAdapter(this@NineGridActivity)
        rvNgv.adapter = nineGridAdapter
        rvNgv.layoutManager = LinearLayoutManager(this@NineGridActivity, LinearLayoutManager.VERTICAL, false)
        btnTest.setOnClickListener {
            nineGridAdapter.notifyItemChanged(1)
        }

    }

    override fun ActivityNineGridBinding.initViewModelListener() {
    }
}