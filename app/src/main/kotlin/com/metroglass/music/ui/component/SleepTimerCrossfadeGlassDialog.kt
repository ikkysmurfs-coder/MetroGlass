/**
 * MetroGlass (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metroglass.music.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.metroglass.music.LocalPlayerConnection
import com.metroglass.music.R
import com.metroglass.music.constants.CrossfadeDurationKey
import com.metroglass.music.constants.CrossfadeEnabledKey
import com.metroglass.music.constants.SleepTimerDefaultKey
import com.metroglass.music.utils.rememberPreference
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SleepTimerCrossfadeGlassDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    val playerConnection = LocalPlayerConnection.current ?: return
    val scope = rememberCoroutineScope()

    var crossfadeEnabled by rememberPreference(CrossfadeEnabledKey, false)
    var crossfadeDuration by rememberPreference(CrossfadeDurationKey, 5f)
    var sleepTimerMinutes by rememberPreference(SleepTimerDefaultKey, 30f)

    val options = listOf(0, 5, 10, 15, 30, 45, 60, -1) // -1 for end of song

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Sleep Timer & Crossfade",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section A: Sleep Timer
                    GlassCard(
                        cornerRadius = 24.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sleep Timer",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (playerConnection.service.sleepTimer?.isActive == true) {
                                    Text(
                                        text = "Aktif",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF4D00)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Pill buttons grid / row
                            val timerLabels = mapOf(
                                0 to "Off",
                                5 to "5m",
                                10 to "10m",
                                15 to "15m",
                                30 to "30m",
                                45 to "45m",
                                60 to "60m",
                                -1 to "Sampai Lagu Habis"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(listOf(0, 5, 10, 15), listOf(30, 45, 60, -1)).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowItems.forEach { minutes ->
                                            val isSelected = sleepTimerMinutes.roundToInt() == minutes
                                            val label = timerLabels[minutes] ?: "$minutes m"
                                            GlassButton(
                                                onClick = { sleepTimerMinutes = minutes.toFloat() },
                                                cornerRadius = 24.dp,
                                                isSelected = isSelected,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color(0xFFFF4D00) else Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section B: Crossfade
                    GlassCard(
                        cornerRadius = 24.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Crossfade",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${crossfadeDuration.toInt()}s",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                Switch(
                                    checked = crossfadeEnabled,
                                    onCheckedChange = { crossfadeEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFFF4D00),
                                        checkedTrackColor = Color(0xFFFF4D00).copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }

                            if (crossfadeEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Slider(
                                    value = crossfadeDuration,
                                    onValueChange = { crossfadeDuration = it },
                                    valueRange = 1f..12f,
                                    steps = 11,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFF4D00),
                                        activeTrackColor = Color(0xFFFF4D00),
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save / Apply Button
                    GlassButton(
                        onClick = {
                            if (sleepTimerMinutes > 0f) {
                                playerConnection.service.sleepTimer?.start(
                                    minute = sleepTimerMinutes.roundToInt(),
                                    stopAfterCurrentSong = false,
                                    fadeOut = true
                                )
                            } else if (sleepTimerMinutes == -1f) {
                                playerConnection.service.sleepTimer?.start(
                                    minute = -1,
                                    stopAfterCurrentSong = true,
                                    fadeOut = true
                                )
                            } else {
                                playerConnection.service.sleepTimer?.clear()
                            }
                            onDismiss()
                        },
                        cornerRadius = 32.dp,
                        isSelected = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Simpan & Terapkan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4D00)
                            )
                        }
                    }
                }
            }
        }
    }
}
