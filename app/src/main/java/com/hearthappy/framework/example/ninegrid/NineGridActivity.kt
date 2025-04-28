package com.hearthappy.framework.example.ninegrid

import android.widget.Toast
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.loadUrl
import com.hearthappy.framework.databinding.ActivityNineGridBinding

class NineGridActivity : AbsBaseActivity<ActivityNineGridBinding>() {
    override fun ActivityNineGridBinding.initData() {
    }

    override fun ActivityNineGridBinding.initListener() {
    }

    override fun ActivityNineGridBinding.initView() { //        val url = "http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/5aa3cfcea145a90d7de7b73f68c35e52.png"
        val url = "https://dongting10.oss-cn-beijing.aliyuncs.com/upload/sociaty/head/eb4c1aec62eaeb9a7a649e06c7d49096.png"
        (ngv initialize listOf(url, url, url, url, url  /*url, url, url, url, url*/)).onBindView { r, i, d, p ->
            i.loadUrl(d)
            r.setOnClickListener {
                Toast.makeText(this@NineGridActivity, "position:$p", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun ActivityNineGridBinding.initViewModelListener() {
    }
}