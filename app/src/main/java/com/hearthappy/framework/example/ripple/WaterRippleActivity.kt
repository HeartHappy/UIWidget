package com.hearthappy.framework.example.ripple

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.addAdapter
import com.hearthappy.basic.ext.dp2px
import com.hearthappy.framework.databinding.ActivityWaterRippleViewerBinding
import com.hearthappy.framework.example.carouse.CarouselFragment
import com.hearthappy.framework.example.tools.ImageUtil
import com.hearthappy.uiwidget.viewpager.PagerTransformer
import com.hearthappy.uiwidget.viewpager.ZoomOutPageTransformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaterRippleActivity : AbsBaseActivity<ActivityWaterRippleViewerBinding>() {
    private lateinit var waterRippleAdapter: WaterRippleAdapter

    override fun ActivityWaterRippleViewerBinding.initData() {
    }

    override fun ActivityWaterRippleViewerBinding.initListener() {
    }

    override fun ActivityWaterRippleViewerBinding.initView() {
        waterRippleAdapter = WaterRippleAdapter(this@WaterRippleActivity, ImageUtil.urlsData)
        rvWaterRipple.apply {
            onSelectedStartListener { position, itemCount ->
                tvTitle.text = String.format("${position + 1}/${itemCount}")
            }
            onLoadMoreListener {
                Toast.makeText(
                    this@WaterRippleActivity, "加载更多数据...", Toast.LENGTH_SHORT
                ).show()
                showLoadingDialog(root)
                lifecycleScope.launch(Dispatchers.IO) {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        dismissLoadingDialog()
                        waterRippleAdapter.addData(ImageUtil.getUrls2())
                    }
                }
            }
            setAdapter(waterRippleAdapter)
            setOnLongPressListener { pos ->
                Toast.makeText(this@WaterRippleActivity, "长按：$pos", Toast.LENGTH_SHORT).show()
            }
            setOnDoubleClickListener { pos ->
                Toast.makeText(this@WaterRippleActivity, "双击：$pos", Toast.LENGTH_SHORT).show()
            }
        }

        vp.addAdapter(supportFragmentManager, 3) { CarouselFragment.newInstance(it) }
        vp.setPageTransformer(false, ZoomOutPageTransformer())
        vp.setOffscreenPageLimit(3)
        vp.pageMargin= -100
    }


    override fun ActivityWaterRippleViewerBinding.initViewModelListener() {
    }

    companion object {
        private const val TAG = "WaveSwitchImageActivity"
    }
}