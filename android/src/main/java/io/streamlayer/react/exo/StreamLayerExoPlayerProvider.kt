package io.streamlayer.react.exo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import io.streamlayer.react.BuildConfig
import io.streamlayer.react.R
import io.streamlayer.sdk.SLRVideoPlayer
import io.streamlayer.sdk.SLRVideoPlayerProvider
import io.streamlayer.sdk.SLRVideoPlayerView

internal class ExoVideoPlayer(internal val simpleExoPlayer: SimpleExoPlayer) : SLRVideoPlayer {

  // keep it for mapping different listeners
  private val listeners: MutableList<Pair<SLRVideoPlayer.Listener, Player.Listener>> = mutableListOf()

  override fun play() {
    simpleExoPlayer.playWhenReady = true
  }

  override fun pause() {
    simpleExoPlayer.playWhenReady = false
  }

  override fun isPlaying(): Boolean = simpleExoPlayer.isPlaying

  override fun release() {
    simpleExoPlayer.release()
  }

  override fun seekTo(position: Long) {
    simpleExoPlayer.seekTo(position)
  }

  override fun getCurrentPosition(): Long {
    return simpleExoPlayer.currentPosition
  }

  override fun getDuration(): Long {
    return simpleExoPlayer.duration
  }

  override fun addListener(listener: SLRVideoPlayer.Listener) {
    val exoListener = object : Player.Listener {
      override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        listener.onPlayerStateChanged(
          when (playbackState) {
            Player.STATE_BUFFERING -> SLRVideoPlayer.State.BUFFERING
            Player.STATE_READY -> SLRVideoPlayer.State.READY
            Player.STATE_ENDED -> SLRVideoPlayer.State.ENDED
            else -> SLRVideoPlayer.State.IDLE
          }
        )
      }

      override fun onPlayerError(error: PlaybackException) {
        listener.onPlayerError(error.cause)
      }
    }
    simpleExoPlayer.addListener(exoListener)
    listeners.add(Pair(listener, exoListener))
  }

  override fun removeListener(listener: SLRVideoPlayer.Listener) {
    listeners.find { it.first == listener }?.let {
      simpleExoPlayer.removeListener(it.second)
      listeners.remove(it)
    }
  }
}

internal class ExoVideoPlayerView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PlayerView(context, attrs, defStyleAttr), SLRVideoPlayerView {

  override fun getView(): View = this

  override fun setVideoPlayer(player: SLRVideoPlayer?) {
    if (player is ExoVideoPlayer?) this.player = player?.simpleExoPlayer
  }

  override fun setShowControls(showControls: Boolean) {
    useController = showControls
    controllerAutoShow = showControls
  }

  override fun setResizeMode(mode: SLRVideoPlayerView.ResizeMode) {
    resizeMode = when (mode) {
      SLRVideoPlayerView.ResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
      SLRVideoPlayerView.ResizeMode.FIXED_WIDTH -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
      SLRVideoPlayerView.ResizeMode.FIXED_HEIGHT -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
      SLRVideoPlayerView.ResizeMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
      SLRVideoPlayerView.ResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    }
  }
}


internal class ExoVideoPlayerProvider(private val context: Context) : SLRVideoPlayerProvider {

  private val bandwidthMeter by lazy { DefaultBandwidthMeter.Builder(context).build() }

  private val agent by lazy { Util.getUserAgent(context, BuildConfig.LIBRARY_PACKAGE_NAME) }

  private fun defaultDataSourceFactory(): DefaultDataSourceFactory =
    DefaultDataSourceFactory(context, agent, bandwidthMeter)

  override fun getVideoPlayer(url: String, type: SLRVideoPlayer.Type, mode: SLRVideoPlayer.RepeatMode): SLRVideoPlayer {
    val player = SimpleExoPlayer.Builder(context).build()
    val streamUri = MediaItem.Builder().setUri(url)
    val mediaSource = when (type) {
      SLRVideoPlayer.Type.HLS -> {
        streamUri.setMimeType(MimeTypes.APPLICATION_M3U8)
        HlsMediaSource.Factory(defaultDataSourceFactory()).createMediaSource(streamUri.build())
      }
      else -> {
        streamUri.setMimeType(MimeTypes.APPLICATION_MP4)
        ProgressiveMediaSource.Factory(defaultDataSourceFactory()).createMediaSource(streamUri.build())
      }
    }
    player.setMediaSource(mediaSource)
    player.repeatMode = when (mode) {
      SLRVideoPlayer.RepeatMode.OFF -> Player.REPEAT_MODE_OFF
      SLRVideoPlayer.RepeatMode.ONE -> Player.REPEAT_MODE_ONE
      SLRVideoPlayer.RepeatMode.ALL -> Player.REPEAT_MODE_ALL
    }
    player.prepare()
    return ExoVideoPlayer(player)
  }

  override fun getVideoPlayerView(context: Context, type: SLRVideoPlayerView.Type): SLRVideoPlayerView = when (type) {
    SLRVideoPlayerView.Type.SURFACE -> LayoutInflater.from(context)
      .inflate(R.layout.streamlayer_surface_player_view, null) as ExoVideoPlayerView
    SLRVideoPlayerView.Type.TEXTURE -> LayoutInflater.from(context)
      .inflate(R.layout.streamlayer_texture_player_view, null) as ExoVideoPlayerView
  }
}
