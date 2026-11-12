package com.metroglass.innertube.pages

import com.metroglass.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
