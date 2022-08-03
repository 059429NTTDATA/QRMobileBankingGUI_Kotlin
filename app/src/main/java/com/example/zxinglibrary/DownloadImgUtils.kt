package com.example.zxinglibrary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import java.util.concurrent.Executors

class DownloadImgUtils {
    companion object {
        public fun downloadImgFromUrlAndSetToImageView(iv: ImageView, imgUrl: String?) {
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            var image: Bitmap?
            executor.execute {
                try {
                    val `in` = java.net.URL(imgUrl).openStream()
                    image = BitmapFactory.decodeStream(`in`)

                    // Only for making changes in UI
                    handler.post {
                        iv.setImageBitmap(image)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

}