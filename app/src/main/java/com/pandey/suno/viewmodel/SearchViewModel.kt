package com.pandey.suno.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pandey.suno.repository.ItunesRepo
import com.pandey.suno.service.PodcastResponse
import com.pandey.suno.util.DateUtils

class SearchViewModel(application: Application) :
    AndroidViewModel(application) {
    var iTunesRepo: ItunesRepo? = null



    suspend fun searchPodcasts(term:String):List<PodcastSummaryViewData>{
        val results = iTunesRepo?.searchByTerm(term)
        if (results != null && results.isSuccessful) {
            val podcasts = results.body()?.results
            if (!podcasts.isNullOrEmpty()) {
                return podcasts.map { podcast ->
                    itunesPodcastToPodcastSummaryView(podcast)
                }
            }
        }
        return emptyList()

    }


   //helper method to convert from the raw model data to the view data

    private fun itunesPodcastToPodcastSummaryView(
        itunesPodcast: PodcastResponse.ItunesPodcast
    ):
            PodcastSummaryViewData {
        return PodcastSummaryViewData(
            itunesPodcast.collectionCensoredName,
            DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
            itunesPodcast.artworkUrl30,
            itunesPodcast.feedUrl
        )
    }


    // data that’s necessary for the View

    data class PodcastSummaryViewData(
        var name: String? = "",
        var lastUpdated: String? = "",
        var imageUrl: String? = "",
        var feedUrl: String? = ""
    )


}



