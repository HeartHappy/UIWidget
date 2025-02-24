package com.hearthappy.framework.example.turntable

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.addListener
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ActivityTurntableBinding
import com.hearthappy.uiwidget.turntable.MultipleLottery
import com.hearthappy.uiwidget.turntable.TurntableCallback
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class TurntableActivity : AbsBaseActivity<ActivityTurntableBinding>() {
    private lateinit var viewModel: TurntableViewModel

    private var playCount = 0
    private val maxPlays = 8 // 最大播放次数
    private var anglePerPlay = 0f // 每次播放的角度间隔
    private lateinit var mediaPlayer: MediaPlayer
    private var lastPlayedAngle = 0f // 上次播放音频的角度

    override fun initViewBinding(): ActivityTurntableBinding {
        return ActivityTurntableBinding.inflate(layoutInflater)
    }

    override fun ActivityTurntableBinding.initListener() {
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
    }


    override fun ActivityTurntableBinding.initData() {
        viewModel.getTurntableBean(this@TurntableActivity)
    }

    override fun ActivityTurntableBinding.initViewModelListener() {
        viewModel.ldTurntableData.observe(this@TurntableActivity) { items ->
            turntableView.setSourceData(items.icons, items.prices) //转盘监听
            initTurntableListener(items.title, items.prices)
        }
    }

    override fun ActivityTurntableBinding.initView() {
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(TurntableViewModel::class.java)
        seekbarTextVerOffset.max = 100
        seekbarTextVerOffset.progress = 60
        seekbarIconVerOffset.max = 100
        seekbarIconVerOffset.progress = 90
        seekbarOutlineText.max = 10
        seekbarOutlineText.progress = 3
        tvTextVerOffset.text = getString(R.string.text_ver_offset).plus(60)
        tvIconVerOffset.text = getString(R.string.icon_ver_offset).plus(0.9)
        tvOutlineText.text = getString(R.string.text_outline_range).plus(3)
        mediaPlayer = MediaPlayer.create(this@TurntableActivity, R.raw.play_turan)
    }


    private fun ActivityTurntableBinding.initTurntableListener(titles: List<String>, prices: List<String>) {
        turntableView.turntableListener = object : TurntableCallback {
            override fun onSingleDrawEndListener(index: Int, text: String?) {
                Toast.makeText(this@TurntableActivity, "index:$index,title:$text", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onSingleDrawEndListener: index:$index,title:$text")
            }

            override fun onMoreDrawEndListener(multipleLottery: List<MultipleLottery>) {
                val sumOf = multipleLottery.sumOf { it.number }
                Log.d(TAG, "onMoreDrawEndListener: total:$sumOf")
                Toast.makeText(this@TurntableActivity, multipleLottery.joinToString(separator = "\n"), Toast.LENGTH_SHORT).show()
                for (lottery in multipleLottery) {
                    Log.d(TAG, "onMoreDrawEndListener onMoreDrawEndListener: index:${lottery.index},number:${lottery.number},title:${titles.get(index = lottery.index)},price:${prices[lottery.index]}")
                }

            }

            override fun onRotationAngleListener(totalAngle: Float, currentAngle: Float) {
                Log.d(TAG, "onRotationAngleListener:totalAngle: $totalAngle,currentAngle:$currentAngle") // 检查是否已经播放了 5 次
                if (currentAngle.toInt() % 30 == 0 && isFinish && !mediaPlayer.isPlaying) { // 播放音频
                    mediaPlayer.start()
                    playCount++
                }
                if (totalAngle == currentAngle) {
                    mediaPlayer.start()
                    turntableView.postDelayed({ // 停止音频
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                            mediaPlayer.reset() // 可选，重置 MediaPlayer
                        }
                        playCount = 0 // 如果需要重置计数器
                    }, 1000)
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

    fun startPlaySoundTask(){
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