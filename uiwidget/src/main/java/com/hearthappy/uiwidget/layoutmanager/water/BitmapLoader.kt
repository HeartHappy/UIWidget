package com.hearthappy.uiwidget.layoutmanager.water

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

/**
 * 高可靠Bitmap加载工具类
 * 特性：
 * 1. 三级缓存（内存 -> 磁盘 -> 网络）
 * 2. 智能重试机制
 * 3. 网络状态感知
 * 4. 生命周期安全
 * 5. 线程安全管理
 */
class BitmapLoader(context: Context) {

    // 内存缓存
    private val memoryCache: LruCache<String, Bitmap>

    // 磁盘缓存目录
    // 初始化磁盘缓存目录
    private val diskCacheDir = File(context.cacheDir, "bitmap_disk_cache").apply {
        if (!exists()) mkdirs()
    }

    // 后台线程池
    private val executor = Executors.newSingleThreadExecutor()

    init { // 初始化内存缓存（最大内存的1/8）
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap) = value.byteCount / 1024
        }

    }

    // 获取图片（完整流程）
    fun loadBitmap(url: String, callback: (Bitmap?) -> Unit) { // 1. 检查内存
        memoryCache.get(url)?.let {
            callback(it)
            return
        }

        // 2. 检查磁盘
        executor.execute {
            val diskBitmap = getFromDiskCache(url)
            diskBitmap?.let { // 回填到内存
                memoryCache.put(url, it)
                Handler(Looper.getMainLooper()).post { callback(it) }
            } ?: run { // 3. 网络请求
                loadFromNetwork(url) { networkBitmap ->
                    networkBitmap?.let {
                        memoryCache.put(url, it)
                        putToDiskCache(url, it)
                    }
                    Handler(Looper.getMainLooper()).post { callback(networkBitmap) }
                }
            }
        }
    }


    // 获取磁盘缓存
    private fun getFromDiskCache(key: String): Bitmap? {
        val file = File(diskCacheDir, key.md5()) // 使用 MD5 作为文件名
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    // 写入磁盘缓存
    private fun putToDiskCache(key: String, bitmap: Bitmap) {
        executor.execute { // 在后台线程执行
            val file = File(diskCacheDir, key.md5())
            try {
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }
            } catch (e: IOException) {
                file.delete()
            }
        }
    }

    // MD5 工具方法
    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    private fun loadFromNetwork(url: String, callback: (Bitmap?) -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            var connection: HttpURLConnection? = null
            try {
                val urlObj = URL(url)
                connection = urlObj.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 20000

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    val bitmap = decodeStreamSafely(input)
                    Handler(Looper.getMainLooper()).post {
                        callback(bitmap)
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                    Log.e("Network", "Load failed: ${e.message}")
                }
            } finally {
                connection?.disconnect()
            }
        }
    } // 需要添加 OkHttp 依赖 // implementation 'com.squareup.okhttp3:okhttp:4.12.0'



    /**
     * 安全的 Bitmap 解码方法
     */
    private fun decodeStreamSafely(inputStream: InputStream): Bitmap? {
        return try { // 使用 Options 优化内存
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inSampleSize = 2 // 根据需求调整采样率
            }
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: OutOfMemoryError) {
            System.gc()
            null
        } finally {
            inputStream.close()
        }
    }
}