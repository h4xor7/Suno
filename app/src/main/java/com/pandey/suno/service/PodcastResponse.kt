package com.pandey.suno.service

data class PodcastResponse(
    val resultCount: Int,
    val results: List<ItunesPodcast>) {
    data class ItunesPodcast(
        val collectionCensoredName: String,
        val feedUrl: String,
        val artworkUrl600: String,
        val releaseDate: String
    )
}