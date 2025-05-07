package com.hearthappy.framework.example.ripple

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ActivityWaterRippleViewerBinding
import com.hearthappy.framework.example.tools.ImageUtil

class WaveSwitchImageActivity: AbsBaseActivity<ActivityWaterRippleViewerBinding>() {

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
            setAdapter(WaterRippleAdapter(this@WaveSwitchImageActivity,ImageUtil.urlsData))
            setOnLongPressListener { pos ->
                Toast.makeText(this@WaveSwitchImageActivity, "长按：$pos", Toast.LENGTH_SHORT).show()
            }
            setOnDoubleClickListener { pos ->
                Toast.makeText(this@WaveSwitchImageActivity, "双击：$pos", Toast.LENGTH_SHORT).show()
            }
        }
        Glide.with(this@WaveSwitchImageActivity).load(ImageUtil.urlsData[0]).into(rivImage)
        rivImage.apply {
//            app:blendGravity="left|top"
//            app:blendHeight="26dp"
//            app:blendSrc="@mipmap/ic_digital_8"
//            app:blendWidth="20dp"
//            app:blendMarginTop="10dp"
//            app:innerGlowColor="@color/color_yellow"
//            app:innerGlowRadius="8dp"
//            app:layersBlendMode="DARKEN"
//            app:layersWatermarkOn="true"
//            app:layersVerSpacing="40dp"
//            app:layersHorSpacing="50dp"
//            app:outerBorderColor="@color/color_blue"
//            app:outerBorderWidth="8dp"
//            app:radius="12dp"
//            setGrayscale(true)
            setOuterBorder(8f, Color.YELLOW)
            setBorder(8f, Color.BLUE)
            setInnerGlow(8f,Color.YELLOW)
            setRadius(8f)
//            setColorBlendMode(PorterDuff.Mode.MULTIPLY)
//            setLayersBlendMode(PorterDuff.Mode.MULTIPLY)
            setBlendSize(26, 20)
            setBlendGravity(Gravity.START or Gravity.TOP)
            setBlendMargin(20)
            setLayersWatermarkOn(true)
            setLayersVerSpacing(100)
            setLayersHorSpacing(100)
            ContextCompat.getDrawable(this@WaveSwitchImageActivity, R.mipmap.ic_digital_8)?.let { setBlendResource(it) }

        }

    }


    override fun ActivityWaterRippleViewerBinding.initViewModelListener() {
    }

    companion object {
        private const val TAG = "WaveSwitchImageActivity"
    }
}