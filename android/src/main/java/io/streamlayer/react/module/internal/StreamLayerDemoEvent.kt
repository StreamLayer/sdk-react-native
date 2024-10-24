package io.streamlayer.react.module.internal

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import io.streamlayer.sdk.StreamLayerDemo

internal data class StreamLayerDemoEvent(
  val id: String,
  val title: String?,
  val subtitle: String?,
  val previewUrl: String?,
  val videoUrl: String?,
) {

  internal fun toMap(): ReadableMap = Arguments.createMap().apply {
    putString(PROP_ID, id)
    title?.let { putString(PROP_TITLE, title) }
    subtitle?.let { putString(PROP_SUBTITLE, subtitle) }
    previewUrl?.let { putString(PROP_PREVIEW_URL, previewUrl) }
    videoUrl?.let { putString(PROP_VIDEO_URL, videoUrl) }
  }

  companion object {
    private const val PROP_ID = "id"
    private const val PROP_TITLE = "title"
    private const val PROP_SUBTITLE = "subtitle"
    private const val PROP_PREVIEW_URL = "previewUrl"
    private const val PROP_VIDEO_URL = "videoUrl"
  }
}

internal fun StreamLayerDemo.Stream.toDomain(): StreamLayerDemoEvent =
  StreamLayerDemoEvent(eventId.toString(), title, subtitle, previewUrl, stream)


