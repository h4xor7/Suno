package com.pandey.suno.repository

import com.pandey.suno.model.Episode
import com.pandey.suno.model.Podcast
import com.pandey.suno.service.RssFeedResponse
import com.pandey.suno.service.RssFeedService
import com.pandey.suno.util.DateUtils

class PodcastRepo(private var feedService: RssFeedService) {

    private fun rssItemsToEpisodes(
        episodeResponses: List<RssFeedResponse.EpisodeResponse>
    ): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

    private fun rssResponseToPodcast(
        feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse
    ): Podcast? {
        val items = rssResponse.episodes ?: return null
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        return Podcast(feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }


    suspend fun getPodcast(feedUrl: String): Podcast? {
        var podcast: Podcast? = null
       // val feedResponse = feedService.getFeed(feedUrl)

      val feedResponse=RssFeedService.instance.getFeed(feedUrl)

        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

}