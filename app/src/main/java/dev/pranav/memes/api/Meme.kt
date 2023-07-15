package dev.pranav.memes.api

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
)