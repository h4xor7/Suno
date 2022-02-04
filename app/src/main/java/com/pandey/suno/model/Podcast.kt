package com.pandey.suno.model

import java.util.*

data class Podcast(var feedUrl: String = "",
                   var feedTitle: String = "",
                   var feedDesc: String = "",
                   var imageUrl: String = "",
                   var lastUpdated: Date = Date(),
                   var episodes: List<Episode> = listOf()
)
