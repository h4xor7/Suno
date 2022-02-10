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

class PodcastRepo(private var feedService: RssFeedService,
                  private var podcastDao: PodcastDao) {

    suspend fun getPodcast(feedUrl: String): Podcast? {
        val podcastLocal = podcastDao.loadPodcast(feedUrl)
        if (podcastLocal != null) {
            podcastLocal.id?.let {
                podcastLocal.episodes = podcastDao.loadEpisodes(it)
                return podcastLocal
            }
        }
        var podcast: Podcast? = null
        val feedResponse = feedService.getFeed(feedUrl)
        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

    private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodeResponse>): List<Episode> {
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
        // 1
        val items = rssResponse.episodes ?: return null
        // 2
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        // 3
        return Podcast(null, feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
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

    suspend fun updatePodcastEpisodes() : MutableList<PodcastUpdateInfo> {
        // 1
        val updatedPodcasts: MutableList<PodcastUpdateInfo> = mutableListOf()
        // 2
        val podcasts = podcastDao.loadPodcastsStatic()
        // 3
        for (podcast in podcasts) {
            // 4
            val newEpisodes = getNewEpisodes(podcast)
            // 5
            if (newEpisodes.count() > 0) {
                podcast.id?.let {
                    saveNewEpisodes(it, newEpisodes)
                    updatedPodcasts.add(PodcastUpdateInfo(podcast.feedUrl, podcast.feedTitle, newEpisodes.count()))
                }
            }
        }
        // 6
        return updatedPodcasts
    }

    private suspend fun getNewEpisodes(localPodcast: Podcast): List<Episode> {
        // 1
        val response = feedService.getFeed(localPodcast.feedUrl)
        if (response != null) {
            // 2
            val remotePodcast = rssResponseToPodcast(localPodcast.feedUrl, localPodcast.imageUrl, response)
            remotePodcast?.let {
                // 3
                val localEpisodes = podcastDao.loadEpisodes(localPodcast.id!!)
                // 4
                return remotePodcast.episodes.filter { episode ->
                    localEpisodes.find { episode.guid == it.guid } == null
                }
            }
        }
        // 6
        return listOf()
    }

    private fun saveNewEpisodes(podcastId: Long, episodes: List<Episode>) {
        GlobalScope.launch {
            for (episode in episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    class PodcastUpdateInfo(val feedUrl: String, val name: String, val newCount: Int)
}