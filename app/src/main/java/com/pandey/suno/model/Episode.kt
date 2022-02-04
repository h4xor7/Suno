package com.pandey.suno.model

import java.util.*

data class Episode(var guid: String = "",
                   var title: String = "",
                   var description: String = "",
                   var mediaUrl: String = "",
                   var mimeType: String = "",
                   var releaseDate: Date = Date(),
                   var duration: String = ""
)
