package com.pandey.suno.repository

import com.pandey.suno.service.ItunesService

class ItunesRepo(private val itunesService: ItunesService) {

    suspend fun searchByTerm(term: String) =
        itunesService.searchPodcastByTerm(term)
}