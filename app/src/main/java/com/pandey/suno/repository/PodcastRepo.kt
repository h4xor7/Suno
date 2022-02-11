package com.pandey.suno.repository

import androidx.lifecycle.LiveData
import com.pandey.suno.db.PodcastDao
import com.pandey.suno.model.Episode
import com.pandey.suno.model.Podcast
import com.pandey.suno.service.RssFeedResponse
import com.pandey.suno.service.RssFeedService
import com.pandey.suno.util.DateUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: RssFeedService,private val podcastDao: PodcastDao) {

    private fun rssItemsToEpisodes(
        episodeResponses: List<RssFeedResponse.EpisodeResponse>
    ): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                null,
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
        return Podcast(null,feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }


    suspend fun getPodcast(feedUrl: String): Podcast? {

        val podcastLocal = podcastDao.loadPodcast(feedUrl)
        if (podcastLocal != null) {
            podcastLocal.id?.let {
                podcastLocal.episodes = podcastDao.loadEpisodes(it)
                return podcastLocal
            }
        }

        var podcast: Podcast? = null
       // val feedResponse = feedService.getFeed(feedUrl)

      val feedResponse=RssFeedService.instance.getFeed(feedUrl)

        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

    fun save(podcast: Podcast) {
        GlobalScope.launch {

            val podcastId = podcastDao.insertPodcast(podcast)

            for (episode in podcast.episodes) {

                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    fun delete(podcast: Podcast) {
        GlobalScope.launch {
            podcastDao.deletePodcast(podcast)
        }
    }


    fun getAll(): LiveData<List<Podcast>> {
        return podcastDao.loadPodcasts()
    }

}