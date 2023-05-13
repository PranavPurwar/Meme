package dev.pranav.memes.api

data class MemeRequest(
        val count: Int,
        var memes: List<Meme>
)
