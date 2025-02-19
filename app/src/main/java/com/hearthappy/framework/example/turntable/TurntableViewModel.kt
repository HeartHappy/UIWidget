package com.hearthappy.framework.example.turntable

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TurntableViewModel : ViewModel() {
    private val _ldTurntableData = MutableLiveData<TurntableItemList>()
    val ldTurntableData: LiveData<TurntableItemList> = _ldTurntableData

    fun getTurntableBean(context: Context) {
        val str = "[\n" + "{\n" + "\"gift_id\":66,\n" + "\"title\":\"希望之塔\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/4b826002c4f45464bf8c37194fa356d3.png\",\n" + "\"price\":777777\n" + "},\n" + "{\n" + "\"gift_id\":65,\n" + "\"title\":\"至臻嘉年华\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/d6b85bf79e077a6a4087e7a40f76a64d.png\",\n" + "\"price\":444444\n" + "},\n" + "{\n" + "\"gift_id\":64,\n" + "\"title\":\"海底星空\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/fb2140ffd31d5668b8f88981592bb287.png\",\n" + "\"price\":199999\n" + "},\n" + "{\n" + "\"gift_id\":63,\n" + "\"title\":\"玫瑰之约\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/a4fca45c4748ba2bc65f5371815e6438.png\",\n" + "\"price\":133333\n" + "},\n" + "{\n" + "\"gift_id\":62,\n" + "\"title\":\"爱堡之约\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/c702004529f277883376ed3a98f4befa.png\",\n" + "\"price\":88888\n" + "},\n" + "{\n" + "\"gift_id\":61,\n" + "\"title\":\"雪之精灵\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/3d4479030e9cff9d725c6085d2aaa28c.png\",\n" + "\"price\":52000\n" + "},\n" + "{\n" + "\"gift_id\":60,\n" + "\"title\":\"爱悦之音\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/5246f20e46462810e6d7bad88e739afe.png\",\n" + "\"price\":26666\n" + "},\n" + "{\n" + "\"gift_id\":55,\n" + "\"title\":\"飞爱之邮\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/bf11c3f6972c04bb31109b58bd5aaebd.png\",\n" + "\"price\":10000\n" + "},\n" + "{\n" + "\"gift_id\":56,\n" + "\"title\":\"音浪\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/9eec0bf678a69c06be6a5b4d9cc721b4.png\",\n" + "\"price\":5000\n" + "},\n" + "{\n" + "\"gift_id\":54,\n" + "\"title\":\"小呲花\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/4fd1f47c33f9f3fa02a6bf74b6128a03.png\",\n" + "\"price\":2000\n" + "},\n" + "{\n" + "\"gift_id\":53,\n" + "\"title\":\"芭音舞盒\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/4860c1876c505fab0990b59a3fd59c58.png\",\n" + "\"price\":1000\n" + "},\n" + "{\n" + "\"gift_id\":52,\n" + "\"title\":\"爱心熊\",\n" + "\"img\":\"http://qingye1125.oss-cn-beijing.aliyuncs.com/admin/png/e91492b3815de6d17549fb61a49fb56d.png\",\n" + "\"price\":300\n" + "}\n" + "]"
        val fromJson = Gson().fromJson(str, TurntableBean::class.java)
        loadLuckBitmap(context, fromJson) { bitmaps ->
            val titles = fromJson.map { it.title }
            val prices = fromJson.map { it.price.toString() }
            _ldTurntableData.postValue(TurntableItemList(bitmaps,titles,prices))
        }
    }

    /**
     * 图片转bitmap
     */
    private fun loadLuckBitmap(context: Context, list: TurntableBean, block: (MutableList<Bitmap>) -> Unit) {
        val iconBitmaps = mutableListOf<Bitmap>()
        viewModelScope.launch(Dispatchers.IO) {
            for (it in list) {
                val myBitmap: Bitmap = Glide.with(context).asBitmap().load(it.img).submit(80, 80).get()
                val bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.width, myBitmap.height)
                iconBitmaps.add(bitmap)
            }
            withContext(Dispatchers.Main) { block(iconBitmaps) }
        }
    }
}