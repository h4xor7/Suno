package com.pandey.suno.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.pandey.suno.db.PodPlayDatabase
import com.pandey.suno.db.PodcastDao
import com.pandey.suno.model.Episode
import com.pandey.suno.model.Podcast
import com.pandey.suno.repository.PodcastRepo
import com.pandey.suno.util.DateUtils
import kotlinx.coroutines.launch
import java.util.*

class PodcastViewModel(application: Application) : AndroidViewModel(application) {

    var podcastRepo: PodcastRepo? = null
    private val _podcastLiveData = MutableLiveData<PodcastViewData?>()
    val podcastLiveData: LiveData<PodcastViewData?> = _podcastLiveData
    var livePodcastSummaryData: LiveData<List<SearchViewModel.PodcastSummaryViewData>>? = null
    var activeEpisodeViewData: EpisodeViewData? = null
    var isVideo: Boolean = false


    val podcastDao: PodcastDao = PodPlayDatabase
        .getInstance(application, viewModelScope)
        .podcastDao()

    private var activePodcast: Podcast? = null

    suspend fun getPodcast(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        podcastSummaryViewData.feedUrl?.let { url ->
            podcastRepo?.getPodcast(url)?.let {
                it.feedTitle = podcastSummaryViewData.name ?: ""
                it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                _podcastLiveData.value = podcastToPodcastView(it)
                activePodcast = it
            } ?: run {
                _podcastLiveData.value = null
            }
        } ?: run {
            _podcastLiveData.value = null
        }
    }

    fun getPodcasts(): LiveData<List<SearchViewModel.PodcastSummaryViewData>>? {
        val repo = podcastRepo ?: return null
        // 1
        if (livePodcastSummaryData == null) {
            // 2
            val liveData = repo.getAll()
            // 3
            livePodcastSummaryData = Transformations.map(liveData) { podcastList ->
                podcastList.map { podcast ->
                    podcastToSummaryView(podcast)
                }
            }
        }

        // 4
        return livePodcastSummaryData
    }

    fun saveActivePodcast() {
        val repo = podcastRepo ?: return
        activePodcast?.let {
            repo.save(it)
        }
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
        return PodcastViewData(
            podcast.id != null,
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodesToEpisodesView(podcast.episodes)
        )
    }

    private fun podcastToSummaryView(podcast: Podcast):
            SearchViewModel.PodcastSummaryViewData {
        return SearchViewModel.PodcastSummaryViewData(
            podcast.feedTitle,
            DateUtils.dateToShortDate(podcast.lastUpdated),
            podcast.imageUrl,
            podcast.feedUrl
        )
    }

    private fun episodesToEpisodesView(episodes: List<Episode>): List<EpisodeViewData> {
        return episodes.map {
            val isVideo = it.mimeType.startsWith("video")
            EpisodeViewData(
                it.guid,
                it.title,
                it.description,
                it.mediaUrl,
                it.releaseDate,
                it.duration,
                isVideo
            )
        }
    }

    fun deleteActivePodcast() {
        val repo = podcastRepo ?: return
        activePodcast?.let {
            repo.delete(it)
        }
    }


    suspend fun setActivePodcast(feedUrl: String):
            SearchViewModel.PodcastSummaryViewData? {
        val repo = podcastRepo ?: return null
        val podcast = repo.getPodcast(feedUrl)
        if (podcast == null) {
            return null
        } else {
            _podcastLiveData.value = podcastToPodcastView(podcast)
            activePodcast = podcast
            return podcastToSummaryView(podcast)
        }
    }


    data class PodcastViewData(
        var subscribed: Boolean = false, var feedTitle: String? = "",
        var feedUrl: String? = "", var feedDesc: String? = "",
        var imageUrl: String? = "", var episodes: List<EpisodeViewData>
    )

    data class EpisodeViewData(
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = "",
        var isVideo: Boolean = false
    )
}