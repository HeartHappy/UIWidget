package com.hearthappy.framework.example.waveswitch

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ActivityWaveSwitchImagesBinding
import com.hearthappy.framework.example.tools.ImageUtil
import com.hearthappy.uiwidget.ripple.WaterRippleViewer

class WaveSwitchImageActivity : AbsBaseActivity<ActivityWaveSwitchImagesBinding>() {

    override fun ActivityWaveSwitchImagesBinding.initData() {
    }

    override fun ActivityWaveSwitchImagesBinding.initListener() {
    }

    override fun ActivityWaveSwitchImagesBinding.initView() {

        rvWaveSwitch.setImageLoader(object : WaterRippleViewer.OnImageListener {
            override fun onBindView(
                url: String, position: Int, callback: WaterRippleViewer.ImageLoadCallback
            ) {
                Log.d(TAG, "loadImage: position:$position,url:$url") // 重试机制
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.ic_launcher_background) // 设置错误占位图
                    .timeout(10000) // 设置超时时间为 10 秒
                Glide.with(this@WaveSwitchImageActivity).asBitmap().apply(requestOptions)
                    .transition(BitmapTransitionOptions.withCrossFade()).load(url).into(object :
                        CustomTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {

                        override fun onResourceReady(
                            resource: Bitmap, transition: Transition<in Bitmap>?
                        ) {
                            callback.onSuccess(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            callback.onFailed(Exception("Image load failed"))
                        }
                    })
            }

            override fun onSelected(position: Int) {

            }

            override fun onPreSelected(position: Int) {
                tvTitle.text = String.format("${position + 1}/${rvWaveSwitch.getItemCount()}")
            }
        })
        rvWaveSwitch.setOnLoadMoreListener(object : WaterRippleViewer.OnLoadMoreListener {
            override fun onLoadMore() {
                Toast.makeText(this@WaveSwitchImageActivity, "加载更多数据...", Toast.LENGTH_SHORT)
                    .show()
                showLoadingDialog(root)
                root.postDelayed({
                    dismissLoadingDialog()
                    rvWaveSwitch.addData(ImageUtil.getUrls2())
                }, 2000)
            }
        })
        rvWaveSwitch.setEnableSliding(false)
        rvWaveSwitch.setScaleType(WaterRippleViewer.ScaleType.CENTER_CROP)
        rvWaveSwitch.initData(ImageUtil.urlsData)
        rvWaveSwitch.startAutoCarouse()
    }

    override fun ActivityWaveSwitchImagesBinding.initViewModelListener() {
    }

    companion object {
        private const val TAG = "WaveSwitchImageActivity"
    }
}