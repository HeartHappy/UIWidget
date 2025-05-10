package com.hearthappy.framework.example.image

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.framework.databinding.ActivityRoundImageBinding
import com.hearthappy.framework.example.tools.ImageUtil

class RoundImageActivity : AbsBaseActivity<ActivityRoundImageBinding>() {
    override fun ActivityRoundImageBinding.initData() {
    }

    override fun ActivityRoundImageBinding.initListener() {
    }

    override fun ActivityRoundImageBinding.initView() {
        Glide.with(this@RoundImageActivity).load(ImageUtil.urlsData[0]).into(rivImage1)

        rivImage1.setOnClickListener {

            animateGradientAngle(0f, 360f, 5000) { //                rivImage.setGradientBorderAnger(it)
                rivImage1.setGradientInnerBorderAnger(it)
            }
        }
    }

    override fun ActivityRoundImageBinding.initViewModelListener() {
    }

    private fun animateGradientAngle(start: Float, end: Float, duration: Long, value: (Float) -> Unit) {
        ValueAnimator.ofFloat(start, end).apply {
            interpolator = LinearInterpolator()
            this.duration = duration
            addUpdateListener {
                value(it.animatedValue as Float)
            }
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.RESTART
            start()
        }
    }
}