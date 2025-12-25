package com.wyx.examplebase.utils

import android.os.Build.VERSION.SDK_INT
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import com.wyx.examplebase.app.BaseApplication

/**
 * 用于加载 Gif 的 Coil ImageLoader
 *
 * @author WangYuxiao
 * @since 2021/9/6 4:26 下午
 */
object CoilGIFImageLoader {

    val imageLoader = ImageLoader.Builder(BaseApplication.sContext)
        .components {
            if (SDK_INT >= 28) {
                add(AnimatedImageDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
}