package com.hearthappy.framework.example.turntable

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.addListener
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ActivityTurntableBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class TurntableActivity : AbsBaseActivity<ActivityTurntableBinding>() {
    private lateinit var viewModel: TurntableViewModel

    override fun initViewBinding(): ActivityTurntableBinding {
        return ActivityTurntableBinding.inflate(layoutInflater)
    }

    override fun ActivityTurntableBinding.initListener() {
        btnSingle.setOnClickListener {

            //随机单抽
            turntableView.startSingleDraw()

            //指定单抽
            //turntableView.specifySingleDraw(8)
        }
        btnTen.setOnClickListener {

            //随机多抽
            //turntableView.startMultipleDraws(12)
            //指定多抽
            turntableView.specifyMultipleDraws(listOf(0, 2, 4, 7, 9, 11, 8))
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
    }


    private fun ActivityTurntableBinding.initTurntableListener(titles: List<String>, prices: List<String>) {
        turntableView.onSingleDrawEndListener = { i, s ->
            Toast.makeText(this@TurntableActivity, "index:$i,title:$s", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onCreate onSingleDrawEndListener: index:$i,title:$s")
        }
        turntableView.onMoreDrawEndListener = { list ->
            val sumOf = list.sumOf { it.number }
            Log.d(TAG, "onCreate: total:$sumOf")
            Toast.makeText(this@TurntableActivity, list.joinToString(separator = "\n"), Toast.LENGTH_SHORT).show()
            for (multipleLottery in list) {
                Log.d(TAG, "onCreate onMoreDrawEndListener: index:${multipleLottery.index},number:${multipleLottery.number},title:${titles.get(index = multipleLottery.index)},price:${prices[multipleLottery.index]}")
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

    companion object {
        private const val TAG = "TurntableActivity"
    }
}