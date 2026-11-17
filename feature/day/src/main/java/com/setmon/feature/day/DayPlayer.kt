package com.setmon.feature.day

import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.setmon.core.designsystem.component.StatusBadge
import com.setmon.core.designsystem.component.VlogStatus
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.model.DayVlogStatus

/** 화면 높이에서 플레이어가 차지할 수 있는 최대 비율 (아래 타임라인·버튼이 한 화면에 보이게) */
private const val MAX_HEIGHT_FRACTION = 0.55f

/** 클립·브이로그는 세로 720x1280 */
private const val VIDEO_ASPECT = 9f / 16f

/**
 * S-05 플레이어. 브이로그가 완성됐으면 브이로그, 아니면 그날 클립을 시간순으로 이어 재생(ExoPlayer 플레이리스트).
 * 누르면 재생/일시정지. 재생 전에는 썸네일과 가운데 재생 버튼, 왼쪽 위에 상태 배지.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun DayPlayer(
    playback: Playback?,
    status: DayVlogStatus,
    isLoading: Boolean,
    onPlaybackError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SetmonTheme.colors
    val context = LocalContext.current
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * MAX_HEIGHT_FRACTION
    val currentOnError by rememberUpdatedState(onPlaybackError)

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) hasStarted = true
            }

            override fun onPlayerError(error: PlaybackException) {
                currentOnError()
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // 재생할 영상이 바뀔 때만 다시 채운다 (브이로그 완성, 칸 선택, 주소 만료 후 재발급)
    LaunchedEffect(playback?.key) {
        hasStarted = false
        if (playback == null) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            return@LaunchedEffect
        }
        exoPlayer.setMediaItems(playback.urls.map(MediaItem::fromUri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playback.autoPlay
    }

    // 다른 화면으로 가거나 앱을 내리면 멈춘다
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { exoPlayer.pause() }

    val playLabel = stringResource(if (isPlaying) R.string.day_pause else R.string.day_play)

    Box(
        modifier = modifier
            .heightIn(max = maxHeight)
            .aspectRatio(VIDEO_ASPECT)
            .clip(SetmonTheme.shapes.card)
            .background(colors.textPrimary)
            .then(
                if (playback != null) {
                    Modifier
                        .semantics { contentDescription = playLabel }
                        .clickable {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                if (exoPlayer.playbackState == Player.STATE_ENDED) exoPlayer.seekTo(0, 0)
                                exoPlayer.play()
                            }
                        }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // 처음 재생하기 전에는 썸네일
        if (!hasStarted && playback?.thumbnailUrl != null) {
            AsyncImage(
                model = playback.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(colors.outline),
                error = ColorPainter(colors.outline),
                modifier = Modifier.fillMaxSize(),
            )
        }

        when {
            isLoading && playback == null -> CircularProgressIndicator(color = colors.onPrimary)
            playback == null -> Text(
                text = stringResource(R.string.day_no_clips),
                style = SetmonTheme.typography.label,
                color = colors.onPrimary,
            )
            !isPlaying -> PlayButton()
        }

        statusBadge(status)?.let { (badge, label) ->
            StatusBadge(
                status = badge,
                label = stringResource(label),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )
        }
    }
}

/** NONE 이면 배지 없음 */
private fun statusBadge(status: DayVlogStatus): Pair<VlogStatus, Int>? = when (status) {
    DayVlogStatus.PENDING, DayVlogStatus.PROCESSING -> VlogStatus.Generating to R.string.day_status_generating
    DayVlogStatus.DONE -> VlogStatus.Done to R.string.day_status_done
    DayVlogStatus.FAILED -> VlogStatus.Failed to R.string.day_status_failed
    DayVlogStatus.NONE -> null
}

/** 피그마의 가운데 둥근 재생 버튼 (아이콘 라이브러리 없이 그림) */
@Composable
private fun PlayButton() {
    val colors = SetmonTheme.colors
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(colors.surface.copy(alpha = 0.85f), SetmonTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp).padding(start = 3.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, colors.primary)
        }
    }
}
