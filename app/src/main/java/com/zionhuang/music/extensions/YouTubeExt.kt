package com.zionhuang.music.extensions

import androidx.paging.PagingSource.LoadResult
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.*
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.SongEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.Page

suspend fun YouTube.browse(browseEndpoint: BrowseEndpoint, block: suspend (List<YTBaseItem>) -> Unit) = withContext(IO) {
    var browseResult: BrowseResult? = null
    do {
        browseResult = if (browseResult == null) {
            browse(browseEndpoint)
        } else {
            browse(browseResult.continuations!!)
        }
        block(browseResult.items)
    } while (!browseResult?.continuations.isNullOrEmpty())
}

// the SongItem should be produced by get_queue endpoint to have detailed information
fun SongItem.toSongEntity() = SongEntity(
    id = id,
    title = title,
    duration = duration!!,
    thumbnailUrl = thumbnails.last().url,
    albumId = album?.navigationEndpoint?.browseId,
    albumName = album?.text
)

fun PlaylistItem.toPlaylistEntity() = PlaylistEntity(
    id = id,
    name = title,
    thumbnailUrl = thumbnails.last().url
)

fun <T : InfoItem> ListInfo<T>.toPage() = LoadResult.Page<Page, InfoItem>(
    data = relatedItems,
    nextKey = nextPage,
    prevKey = null
)

fun <T : InfoItem> InfoItemsPage<T>.toPage() = LoadResult.Page<Page, InfoItem>(
    data = items,
    nextKey = nextPage,
    prevKey = null
)

fun BrowseResult.toPage() = LoadResult.Page(
    data = items,
    nextKey = continuations?.ifEmpty { null },
    prevKey = null
)