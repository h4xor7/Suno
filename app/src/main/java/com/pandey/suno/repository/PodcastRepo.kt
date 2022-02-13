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


    private suspend fun getNewEpisodes(localPodcast: Podcast):
            List<Episode> {

        val response = feedService.getFeed(localPodcast.feedUrl)
        if (response != null) {
            val remotePodcast =
                rssResponseToPodcast(localPodcast.feedUrl,
                    localPodcast.imageUrl, response)
            remotePodcast?.let {
                val localEpisodes =
                    podcastDao.loadEpisodes(localPodcast.id!!)
                return remotePodcast.episodes.filter { episode ->
                    localEpisodes.find { episode.guid == it.guid } == null
                }
            }
        }
        return listOf()
    }

    private fun saveNewEpisodes(podcastId: Long, episodes:
    List<Episode>){
        GlobalScope.launch {
            for (episode in episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }

    }

    class PodcastUpdateInfo(
        val feedUrl: String,
        val name: String,
        val newCount: Int
    )



    suspend fun updatePodcastEpisodes() :
            MutableList<PodcastUpdateInfo> {

        val updatedPodcasts: MutableList<PodcastUpdateInfo> =
            mutableListOf()

        val podcasts = podcastDao.loadPodcastsStatic()

        for (podcast in podcasts) {

            val newEpisodes = getNewEpisodes(podcast)

            if (newEpisodes.count() > 0) {
                podcast.id?.let {
                    saveNewEpisodes(it, newEpisodes)
                    updatedPodcasts.add(PodcastUpdateInfo(
                        podcast.feedUrl, podcast.feedTitle,
                        newEpisodes.count()))
                }
            }
        }

        return updatedPodcasts
    }



}