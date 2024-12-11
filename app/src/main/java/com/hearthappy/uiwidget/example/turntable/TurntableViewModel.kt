package com.hearthappy.uiwidget.example.turntable

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.hearthappy.uiwidget.turntable.TurntableBean

class TurntableViewModel : ViewModel() {

    fun getTurntableBean(): TurntableBean? {
        val str = "\n" + "[\n" + "{\n" + "\"gift_id\":226,\n" + "\"title\":\"甜蜜深秋\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/f524fa06c5da9e556c4ab287ce217332.png\",\n" + "\"price\":555555\n" + "},\n" + "{\n" + "\"gift_id\":225,\n" + "\"title\":\"冬日约会\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/eb3bbeb70a9b9dac632babb7c034058e.png\",\n" + "\"price\":333333\n" + "},\n" + "{\n" + "\"gift_id\":224,\n" + "\"title\":\"夜魔城堡\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/742bae670f76f57ffa4f2add05445b1a.png\",\n" + "\"price\":188888\n" + "},\n" + "{\n" + "\"gift_id\":223,\n" + "\"title\":\"光明天使\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/7be5d4fe0692999aa0d82df23a957735.png\",\n" + "\"price\":88888\n" + "},\n" + "{\n" + "\"gift_id\":222,\n" + "\"title\":\"夜之约\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/cd88ffb0c2b52afc9dd586bdf36bbd7c.png\",\n" + "\"price\":52000\n" + "},\n" + "{\n" + "\"gift_id\":221,\n" + "\"title\":\"琉璃仙踪\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/116b7062f04d2dfef46c461446afcc9c.png\",\n" + "\"price\":26666\n" + "},\n" + "{\n" + "\"gift_id\":220,\n" + "\"title\":\"浪漫国度\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/8bec50e9773c7f3e042500b951c81a10.png\",\n" + "\"price\":13140\n" + "},\n" + "{\n" + "\"gift_id\":219,\n" + "\"title\":\"捕梦网\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/89ffe6a80efac59cef4df02aac35865d.png\",\n" + "\"price\":5200\n" + "},\n" + "{\n" + "\"gift_id\":218,\n" + "\"title\":\"夏饮\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/cb1ae9ef5589e793e10093b9aad26528.png\",\n" + "\"price\":2999\n" + "},\n" + "{\n" + "\"gift_id\":217,\n" + "\"title\":\"钻戒\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/e87b9efee0714a5b6336bd634ed015a8.png\",\n" + "\"price\":1888\n" + "},\n" + "{\n" + "\"gift_id\":216,\n" + "\"title\":\"米其林\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/4a6f613541887ed70fbcfbc3fb1e6493.png\",\n" + "\"price\":600\n" + "},\n" + "{\n" + "\"gift_id\":215,\n" + "\"title\":\"小气球\",\n" + "\"img\":\"http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/6e5d2a1ac09929013c02f55eccee2835.png\",\n" + "\"price\":300\n" + "}\n" + "]"
        val fromJson = Gson().fromJson(str, TurntableBean::class.java)
        return fromJson
    }

}