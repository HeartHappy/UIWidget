package com.hearthappy.framework.example.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer

/**
 * Created Date 2019/2/12.
 *
 * @author RayChen
 * ClassDescription：图片url集合
 */
object ImageUtil {
    val urlsData: List<String>
        /**
         * 网络url
         *
         * @return
         */
        get() {
            val objects: MutableList<String> = ArrayList()
//            objects.add("http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg")
//            objects.add("http://c.hiphotos.baidu.com/image/pic/item/30adcbef76094b36de8a2fe5a1cc7cd98d109d99.jpg")
//            objects.add("http://h.hiphotos.baidu.com/image/pic/item/7c1ed21b0ef41bd5f2c2a9e953da81cb39db3d1d.jpg")
//            objects.add("http://g.hiphotos.baidu.com/image/pic/item/55e736d12f2eb938d5277fd5d0628535e5dd6f4a.jpg")
//            objects.add("http://e.hiphotos.baidu.com/image/pic/item/4e4a20a4462309f7e41f5cfe760e0cf3d6cad6ee.jpg")
//            objects.add("http://b.hiphotos.baidu.com/image/pic/item/9d82d158ccbf6c81b94575cfb93eb13533fa40a2.jpg")
//            objects.add("http://e.hiphotos.baidu.com/image/pic/item/4bed2e738bd4b31c1badd5a685d6277f9e2ff81e.jpg")
//            objects.add("http://g.hiphotos.baidu.com/image/pic/item/0d338744ebf81a4c87a3add4d52a6059252da61e.jpg")
//            objects.add("http://a.hiphotos.baidu.com/image/pic/item/f2deb48f8c5494ee5080c8142ff5e0fe99257e19.jpg")
//            objects.add("http://f.hiphotos.baidu.com/image/pic/item/4034970a304e251f503521f5a586c9177e3e53f9.jpg")
//            objects.add("http://b.hiphotos.baidu.com/image/pic/item/279759ee3d6d55fbb3586c0168224f4a20a4dd7e.jpg")


//            objects.add("https://i.loli.net/2019/09/09/ecX4R7bluak8TgN.jpg")
//            objects.add("https://i.loli.net/2019/09/09/F6D2ZnQobVcMWCz.jpg")
//            objects.add("https://i.loli.net/2019/09/09/bNTgQSwukUYefFI.jpg")
//            objects.add("https://i.loli.net/2019/09/09/VHUdaMECYSOLcJg.jpg")
//            objects.add("https://i.loli.net/2019/09/09/CrS8H2cWqTkRVaw.jpg")
//            objects.add("https://i.loli.net/2019/09/09/VGIoObW2M3DmSYX.jpg")
//            objects.add("https://i.loli.net/2019/09/09/IJhforasCNd46FK.jpg")
//            objects.add("https://i.loli.net/2019/09/09/wsUAxSIMtXfVh5W.jpg")
//            objects.add("https://i.loli.net/2019/09/09/eHfs1vYJDtMzyNP.jpg")
//            objects.add("https://i.loli.net/2019/09/09/1gktxsnzqJLSaVm.jpg")


            objects.add("https://i.loli.net/2019/09/09/fS4r1aKhVvbz5JF.jpg")
            objects.add("https://i.loli.net/2019/09/09/AQRoOnbycmTgwWF.jpg")
            objects.add("https://i.loli.net/2019/09/09/Ekba7zI95TywMNK.jpg")
            objects.add("https://i.loli.net/2019/09/09/CSi1tkGJYonBMxV.jpg")
            objects.add("https://i.loli.net/2019/09/09/Up1IvFCJPhmwHed.jpg")
            objects.add("https://i.loli.net/2019/09/09/E7goR89IqH4wxiK.jpg")
            objects.add("https://i.loli.net/2019/09/09/zqvDRAUk2jKhZfT.jpg")
            objects.add("https://i.loli.net/2019/09/09/tQ9gwTiJMR1bq5s.jpg")
            objects.add("https://i.loli.net/2019/09/09/u5NMgOH8jkEa6Xw.jpg")
            return objects
        }


    fun getUrls2(): MutableList<String> {
        return mutableListOf<String>().apply {
            add("https://i.loli.net/2019/09/09/ecX4R7bluak8TgN.jpg")
            add("https://i.loli.net/2019/09/09/F6D2ZnQobVcMWCz.jpg")
            add("https://i.loli.net/2019/09/09/bNTgQSwukUYefFI.jpg")
            add("https://i.loli.net/2019/09/09/VHUdaMECYSOLcJg.jpg")
            add("https://i.loli.net/2019/09/09/CrS8H2cWqTkRVaw.jpg")
            add("https://i.loli.net/2019/09/09/VGIoObW2M3DmSYX.jpg")
            add("https://i.loli.net/2019/09/09/IJhforasCNd46FK.jpg")
            add("https://i.loli.net/2019/09/09/wsUAxSIMtXfVh5W.jpg")
            add("https://i.loli.net/2019/09/09/eHfs1vYJDtMzyNP.jpg")
            add("https://i.loli.net/2019/09/09/1gktxsnzqJLSaVm.jpg")
        }

    }

    /**
     * 网络url转bitmap
     *
     * @param url 网络url
     * @return bitmap
     */
    @Throws(IOException::class) fun urlToBitmap(url: String?): Bitmap? {
        if (TextUtils.isEmpty(url)) return null
        val bitmap: Bitmap
        val myFileUrl = URL(url)
        val conn = myFileUrl.openConnection() as HttpURLConnection
        conn.doInput = true
        conn.connect()
        val `is` = conn.inputStream
        bitmap = BitmapFactory.decodeStream(`is`)
        `is`.close()
        return bitmap
    }


    /**
     * Bitmap转字节
     *
     * @param bitmap
     * @return
     */
    fun bitmapToByte(bitmap: Bitmap): ByteArray {
        val bytes = bitmap.byteCount
        val buffer = ByteBuffer.allocate(bytes)
        bitmap.copyPixelsToBuffer(buffer) //Move the byte data to the buffer
        return buffer.array()
    }
}
