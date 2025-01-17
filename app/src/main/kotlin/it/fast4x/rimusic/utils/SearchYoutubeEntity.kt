package it.fast4x.rimusic.utils

import androidx.annotation.OptIn
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import it.fast4x.compose.persist.persistMap
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.ContinuationBody
import it.fast4x.innertube.models.bodies.SearchBody
import it.fast4x.innertube.requests.searchPage
import it.fast4x.innertube.utils.from
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.ui.items.VideoItemPlaceholder
import it.fast4x.rimusic.ui.screens.searchresult.ItemsPage
import it.fast4x.rimusic.ui.styling.LocalAppearance

@ExperimentalAnimationApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun SearchYoutubeEntity (
    navController: NavController,
    onDismiss: () -> Unit,
    query: String,
    filter: Innertube.SearchFilter = Innertube.SearchFilter.Video
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val hapticFeedback = LocalHapticFeedback.current
    //val context = LocalContext.current
    val thumbnailHeightDp = 72.dp
    val thumbnailWidthDp = 128.dp
    val emptyItemsText = stringResource(R.string.no_results_found)
    val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = {
        Title(
            title = stringResource(id = R.string.videos),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }

    Box(
        modifier = Modifier
            .background(LocalAppearance.current.colorPalette.background0)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            ItemsPage(
                tag = "searchYTEntity/$query/videos",
                itemsPageProvider = { continuation ->
                    if (continuation == null) {
                        Innertube.searchPage(
                            body = SearchBody(
                                query = query,
                                params = Innertube.SearchFilter.Video.value
                            ),
                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                        )
                    } else {
                        Innertube.searchPage(
                            body = ContinuationBody(continuation = continuation),
                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                        )
                    }
                },
                emptyItemsText = emptyItemsText,
                headerContent = headerContent,
                itemContent = { video ->
                    SwipeablePlaylistItem(
                        mediaItem = video.asMediaItem,
                        onSwipeToRight = {
                            binder?.player?.addNext(video.asMediaItem)
                        }
                    ) {
                        VideoItem(
                            video = video,
                            thumbnailWidthDp = thumbnailWidthDp,
                            thumbnailHeightDp = thumbnailHeightDp,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                navController = rememberNavController(),
                                                mediaItem = video.asMediaItem,
                                                onDismiss = menuState::hide
                                            )
                                        };
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.LongPress
                                        )
                                    },
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.playVideo(video.asMediaItem)
                                        //binder?.setupRadio(video.info?.endpoint)
                                        onDismiss()
                                    }
                                )
                        )
                    }
                },
                itemPlaceholderContent = {
                    VideoItemPlaceholder(
                        thumbnailHeightDp = thumbnailHeightDp,
                        thumbnailWidthDp = thumbnailWidthDp
                    )
                }
            )
        }
    }
}