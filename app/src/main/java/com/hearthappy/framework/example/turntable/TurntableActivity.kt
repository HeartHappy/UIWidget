package com.hearthappy.framework.example.turntable

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.hearthappy.base.ext.popupWindow
import com.hearthappy.base.ext.showLocation
import com.hearthappy.framework.databinding.ActivityTurntableBinding
import com.hearthappy.framework.databinding.ItemExampleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TurntableActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityTurntableBinding
    private lateinit var viewModel: TurntableViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTurntableBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(TurntableViewModel::class.java)

        viewBinding.apply {
            var redCount=0f
            var blueCount=0f
            pkView.setValues(redCount,blueCount)

            lifecycleScope.launch {
                repeat(Int.MAX_VALUE){
                    delay(1000)
                    pkView.post {
                        redCount+= Random.nextInt(10)
                        blueCount+=Random.nextInt(10)
                        pkView.setValues(redCount, blueCount)
                        Log.d(TAG, "onCreate: redCount:$redCount,blueCount:$blueCount")
                    }
                }
            }
            viewModel.getTurntableBean()?.let {
                val titles = it.map { it.title }
                val prices = it.map { it.price }
                loadLuckBitmap(it) { bitmaps ->
                    turntableView.setSourceData(bitmaps, it.map { it.price.toString() })
                }

                turntableView.onSingleDrawEndListener = { i, s ->
                    Toast.makeText(this@TurntableActivity, "index:$i,title:$s", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate onSingleDrawEndListener: index:$i,title:$s")
                }
                turntableView.onMoreDrawEndListener = { list ->
                    val sumOf = list.sumOf { it.number }
                    Log.d(TAG, "onCreate: total:$sumOf")
                    for (multipleLottery in list) {
                        Log.d(TAG, "onCreate onMoreDrawEndListener: index:${multipleLottery.index},number:${multipleLottery.number},title:${titles.get(index = multipleLottery.index)},price:${prices[multipleLottery.index]}")
                    }
                }
            }


            btnSingle.setOnClickListener {

                //随机单抽
                turntableView.startSingleDraw()


                //指定单抽
//                turntableView.specifySingleDraw(8)
            }
            btnTen.setOnClickListener { //随机多抽
                //                turntableView.startMultipleDraws(12)
                //指定多抽
                turntableView.specifyMultipleDraws(listOf(0, 2, 4, 7, 9,11, 8))
            }

        }
    }


    /**
     * 图片转bitmap
     */
    private fun loadLuckBitmap(list: TurntableBean, block: (MutableList<Bitmap>) -> Unit) {
        val iconBitmaps = mutableListOf<Bitmap>()
        CoroutineScope(Dispatchers.IO).launch {
            for (it in list) {
                val myBitmap: Bitmap = Glide.with(this@TurntableActivity).asBitmap().load(it.img).submit(80, 80).get()
                val bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.width, myBitmap.height)
                iconBitmaps.add(bitmap)
            }
            withContext(Dispatchers.Main) { block(iconBitmaps) }
        }
    }


    companion object {
        private const val TAG = "TurntableActivity"
    }
}