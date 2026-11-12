/**
 * MetroGlass (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metroglass.music.ui.screens.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachReversed
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.metroglass.music.LocalPlayerAwareWindowInsets
import com.metroglass.music.LocalPlayerConnection
import com.metroglass.music.R
import com.metroglass.music.constants.HideExplicitKey
import com.metroglass.music.constants.SongSortDescendingKey
import com.metroglass.music.constants.SongSortType
import com.metroglass.music.constants.SongSortTypeKey
import com.metroglass.music.db.entities.Song
import com.metroglass.music.playback.queues.ListQueue
import com.metroglass.music.ui.component.GlassButton
import com.metroglass.music.ui.component.GlassCard
import com.metroglass.music.ui.component.GlassChip
import com.metroglass.music.ui.component.ItemThumbnail
import com.metroglass.music.ui.component.LocalMenuState
import com.metroglass.music.ui.menu.SelectionSongMenu
import com.metroglass.music.ui.menu.SongMenu
import com.metroglass.music.ui.utils.backToMain
import com.metroglass.music.utils.rememberEnumPreference
import com.metroglass.music.utils.rememberPreference
import com.metroglass.music.viewmodels.CachePlaylistViewModel
import java.time.LocalDateTime

enum class OfflineFilter {
    ALL, SONGS, ALBUMS, PLAYLISTS, VIDEOS
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CachePlaylistScreen(
    navController: NavController,
    viewModel: CachePlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val cachedSongs by viewModel.cachedSongs.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(OfflineFilter.ALL) }
    var isSearching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    if (isSearching) {
        BackHandler {
            isSearching = false
            query = TextFieldValue()
        }
    }

    val filteredSongs = remember(cachedSongs, query, selectedFilter) {
        val base = if (query.text.isEmpty()) cachedSongs
        else cachedSongs.filter { song ->
            song.title.contains(query.text, true) ||
                song.artists.any { it.name.contains(query.text, true) }
        }
        when (selectedFilter) {
            OfflineFilter.ALL, OfflineFilter.SONGS -> base
            OfflineFilter.ALBUMS -> base.filter { it.album != null }
            OfflineFilter.PLAYLISTS -> emptyList() // offline playlist filter
            OfflineFilter.VIDEOS -> base.filter { it.isExplicit } // or video tag
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
        ) {
            // Header Card "Offline Vault"
            GlassCard(
                cornerRadius = 28.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Offline Vault",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${cachedSongs.size} lagu tersimpan di perangkat",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    GlassButton(
                        onClick = {
                            if (cachedSongs.isNotEmpty()) {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = "Offline Vault",
                                        items = cachedSongs.map { it.toMediaItem() },
                                        startIndex = 0
                                    )
                                )
                            }
                        },
                        cornerRadius = 28.dp
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            tint = Color(0xFFFF4D00),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Putar Semua",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    OfflineFilter.ALL to "Semua",
                    OfflineFilter.SONGS to "Lagu",
                    OfflineFilter.ALBUMS to "Album",
                    OfflineFilter.PLAYLISTS to "Playlist"
                ).forEach { (filter, label) ->
                    val isSelected = selectedFilter == filter
                    GlassChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFFFF4D00) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredSongs.isEmpty() && !isSearching) {
                // Empty State Glass Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.offline),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada yang diunduh",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Musik yang Anda unduh akan muncul di sini untuk pemutaran offline.",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            GlassButton(
                                onClick = { navController.navigate("search") },
                                cornerRadius = 28.dp
                            ) {
                                Text(
                                    text = "Jelajahi Musik",
                                    color = Color(0xFFFF4D00),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // 2-Column Grid Glass
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filteredSongs, key = { index, song -> "${song.id}_$index" }) { _, song ->
                        GlassCard(
                            cornerRadius = 24.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (song.id == mediaMetadata?.id) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = "Offline Vault",
                                                    items = cachedSongs.map { it.toMediaItem() },
                                                    startIndex = cachedSongs.indexOfFirst { it.id == song.id }
                                                )
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                onDismiss = menuState::dismiss,
                                                isFromCache = true,
                                            )
                                        }
                                    }
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                ) {
                                    AsyncImage(
                                        model = song.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (song.id == mediaMetadata?.id && isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.play),
                                                contentDescription = null,
                                                tint = Color(0xFFFF4D00),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = song.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = song.artists.joinToString { it.name },
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        TopAppBar(
            title = {
                if (isSearching) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                text = "Cari di Offline Vault...",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                } else {
                    Text(
                        "Offline Vault",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (isSearching) {
                        isSearching = false
                        query = TextFieldValue()
                        focusManager.clearFocus()
                    } else {
                        navController.navigateUp()
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null
                    )
                }
            },
            actions = {
                if (!isSearching) {
                    IconButton(onClick = { isSearching = true }) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null
                        )
                    }
                }
            }
        )
    }
}
