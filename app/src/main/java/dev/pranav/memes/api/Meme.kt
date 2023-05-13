package dev.pranav.memes.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import java.util.concurrent.Executors


data class Meme(
        val postLink: String,
        val subreddit: String,
        val title: String,
        val url: String,
        val nsfw: Boolean,
        val spoiler: Boolean,
        val author: String,
        val ups: Int,
        val preview: List<String>
) {
        val bitmap: Bitmap
                get() {
                        return BitmapFactory.decodeStream(URL(url).openStream())
                }

}
