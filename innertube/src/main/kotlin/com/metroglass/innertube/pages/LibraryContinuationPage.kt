package com.metroglass.innertube.pages

import com.metroglass.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
