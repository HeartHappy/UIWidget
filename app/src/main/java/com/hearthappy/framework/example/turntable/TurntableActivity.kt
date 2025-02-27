package com.hearthappy.framework.example.turntable

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.addListener
import com.hearthappy.base.ext.px2dp
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ActivityTurntableBinding
import com.hearthappy.uiwidget.turntable.MultipleLottery
import com.hearthappy.uiwidget.turntable.OnTurntableListener
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class TurntableActivity : AbsBaseActivity<ActivityTurntableBinding>() {
    private lateinit var viewModel: TurntableViewModel

    private lateinit var mediaPlayer: MediaPlayer

    override fun initViewBinding(): ActivityTurntableBinding {
        return ActivityTurntableBinding.inflate(layoutInflater)
    }

    override fun ActivityTurntableBinding.initListener() {
        initTurntableListener()
        btnSingle.setOnClickListener {

            //随机单抽
            turntableView.startSingleDraw()

            //指定单抽
            //turntableView.specifySingleDraw(8)
            startPlaySoundTask()
        }
        btnTen.setOnClickListener {

            //随机多抽
            //turntableView.startMultipleDraws(12)
            //指定多抽
            turntableView.specifyMultipleDraws(listOf(0, 2, 4, 7, 9, 11, 8))
            startPlaySoundTask()
        }
        viewTextColor.setOnClickListener {
            showColorSelector("选择文本颜色") {
                turntableView.setTextColor(it)
                viewTextColor.setBackgroundColor(it)
            }
        }
        viewStrokeTextColor.setOnClickListener {
            showColorSelector("选择文本描边颜色") {
                turntableView.setOutlineColor(it)
                viewStrokeTextColor.setBackgroundColor(it)
            }
        }
        seekbarTextVerOffset.addListener({
            val value = seekbarTextVerOffset.progress.toFloat()
            tvTextVerOffset.text = getString(R.string.text_ver_offset).plus(value)
            turntableView.setTextOffsetY(value)
        })

        seekbarIconVerOffset.addListener({
            val value = seekbarIconVerOffset.progress.toFloat() / seekbarIconVerOffset.max
            tvIconVerOffset.text = getString(R.string.icon_ver_offset).plus(value)
            turntableView.setIconPositionPercent(value)
        })

        seekbarOutlineText.addListener({
            val value = seekbarOutlineText.progress.toFloat()
            tvOutlineText.text = getString(R.string.text_outline_range).plus(value)
            turntableView.setOutlineWidth(value)
        })
        switchShowHighlight.setOnCheckedChangeListener { _, isChecked -> turntableView.setShowHighlight(isChecked) }

    }


    override fun ActivityTurntableBinding.initData() {
        viewModel.getTurntableBean(this@TurntableActivity)
    }

    override fun ActivityTurntableBinding.initViewModelListener() {
        viewModel.ldTurntableData.observe(this@TurntableActivity) { items ->
            turntableView.setSourceData(items.icons, items.prices) //转盘监听
        }
    }

    override fun ActivityTurntableBinding.initView() {
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(TurntableViewModel::class.java)

        //图标垂直偏移量
        seekbarIconVerOffset.max = 100
        val iconPositionPercent = turntableView.getIconPositionPercent()
        seekbarIconVerOffset.progress =(iconPositionPercent* 100).toInt()
        tvIconVerOffset.text = getString(R.string.icon_ver_offset).plus(iconPositionPercent)
        //文本垂直偏移量
        seekbarTextVerOffset.max = 100
        val textVerticalOffset = turntableView.getTextVerticalOffset().px2dp().toInt()
        seekbarTextVerOffset.progress = textVerticalOffset
        tvTextVerOffset.text = getString(R.string.text_ver_offset).plus(textVerticalOffset)
        //文本描边范围
        seekbarOutlineText.max = 10
        val outlineWidth = turntableView.getOutlineWidth().px2dp().toInt()
        seekbarOutlineText.progress = outlineWidth
        tvOutlineText.text = getString(R.string.text_outline_range).plus(outlineWidth)
        mediaPlayer = MediaPlayer.create(this@TurntableActivity, R.raw.play_sound)
    }


    private fun ActivityTurntableBinding.initTurntableListener() {
        turntableView.onTurntableListener = object : OnTurntableListener {
            override fun onSingleDrawEndListener(index: Int, text: String?) {
                Toast.makeText(this@TurntableActivity, "index:$index,title:$text", Toast.LENGTH_SHORT).show()
            }

            override fun onMoreDrawEndListener(multipleLottery: List<MultipleLottery>) {
                Toast.makeText(this@TurntableActivity, multipleLottery.joinToString(separator = "\n"), Toast.LENGTH_SHORT).show()
                for (lottery in multipleLottery) {
                    Log.d(TAG, "onMoreDrawEndListener: ${lottery.index},${lottery.title}")
                }
            }

            override fun onRotationAngleListener(totalAngle: Float, currentAngle: Float) {
                if (totalAngle == currentAngle) {
                    mediaPlayer.start()
                    turntableView.postDelayed({ // 停止音频
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                            mediaPlayer.reset() // 可选，重置 MediaPlayer
                        }
                    }, mediaPlayer.duration.toLong())
                }else if(currentAngle.toInt() % 30 == 0 && isFinish && !mediaPlayer.isPlaying){
                    mediaPlayer.start()
                }
            }
        }
    }


    private fun showColorSelector(title: String, selectColor: (Int) -> Unit) {
        ColorPickerDialog.Builder(this).setTitle(title).setPositiveButton(getString(R.string.confirm), object : ColorEnvelopeListener {
            override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                selectColor(envelope.color)
            }
        }).setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }.attachAlphaSlideBar(true).attachBrightnessSlideBar(true).setBottomSpace(12).show()

    }

    var isFinish = false

    fun startPlaySoundTask() {
        isFinish = false
        startMedia()
        startTime()
    }

    fun startMedia() {
        if (!isFinish) {
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { startMedia() }
        }
    }

    fun startTime() {
        viewBinding.turntableView.postDelayed({
            isFinish = true
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    companion object {
        private const val TAG = "TurntableActivity"
    }
}