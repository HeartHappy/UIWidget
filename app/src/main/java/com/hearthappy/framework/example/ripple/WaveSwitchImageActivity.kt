package com.hearthappy.framework.example.ripple

import android.widget.Toast
import com.bumptech.glide.Glide
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.framework.databinding.ActivityWaterRippleViewerBinding
import com.hearthappy.framework.example.tools.ImageUtil

class WaveSwitchImageActivity : AbsBaseActivity<ActivityWaterRippleViewerBinding>() {

    override fun ActivityWaterRippleViewerBinding.initData() {
    }

    override fun ActivityWaterRippleViewerBinding.initListener() {
    }

    override fun ActivityWaterRippleViewerBinding.initView() {
        rvWaterRipple.apply {
            onSelectedListener { position, itemCount ->
                tvTitle.text = String.format("${position + 1}/${itemCount}")
            } //        onLoadMoreListener {
            //            Toast.makeText(this@WaveSwitchImageActivity, "加载更多数据...", Toast.LENGTH_SHORT)
            //                .show()
            //            showLoadingDialog(root)
            //            lifecycleScope.launch(Dispatchers.IO) {
            //                delay(2000)
            //                withContext(Dispatchers.Main) {
            //                    dismissLoadingDialog()
            //                    addData(ImageUtil.getUrls2())
            //                }
            //            }
            //        }
            setAdapter(WaterRippleAdapter(this@WaveSwitchImageActivity, ImageUtil.urlsData))
            setOnLongPressListener { pos ->
                Toast.makeText(this@WaveSwitchImageActivity, "长按：$pos", Toast.LENGTH_SHORT).show()
            }
            setOnDoubleClickListener { pos ->
                Toast.makeText(this@WaveSwitchImageActivity, "双击：$pos", Toast.LENGTH_SHORT).show()
            }
        }
        Glide.with(this@WaveSwitchImageActivity).load(ImageUtil.urlsData[0]).into(rivImage)
    }


    override fun ActivityWaterRippleViewerBinding.initViewModelListener() {
    }

    companion object {
        private const val TAG = "WaveSwitchImageActivity"
    }
}