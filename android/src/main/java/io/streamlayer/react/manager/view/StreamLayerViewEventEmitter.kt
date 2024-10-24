package io.streamlayer.react.manager.view

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.events.RCTEventEmitter

internal class StreamLayerViewEventEmitter(
  private val reactContext: ReactApplicationContext,
  private val getReactNativeViewId: (() -> Int?)
) {

  fun onRequestStream(id: String) {
    val map = Arguments.createMap()
    map.putString(ARG_ID, id)
    sendEvent(EVENT_REQUEST_STREAM, map)
  }

  fun onLBarStateChanged(slideX: Int, slideY: Int) {
    val map = Arguments.createMap()
    map.putInt(ARG_SLIDE_X, slideX)
    map.putInt(ARG_SLIDE_Y, slideY)
    sendEvent(EVENT_LBAR_STATE_CHANGED, map)
  }

  fun requestAudioDucking(level: Float) {
    val map = Arguments.createMap()
    map.putDouble(ARG_LEVEL, level.toDouble())
    sendEvent(EVENT_REQUEST_AUDIO_DUCKING, map)
  }

  fun disableAudioDucking() {
    sendEvent(EVENT_DISABLE_AUDIO_DUCKING, null)
  }

  private fun sendEvent(type: String, event: WritableMap?) {
    getReactNativeViewId()?.let {
      (reactContext.getJSModule(RCTEventEmitter::class.java) as RCTEventEmitter)
        .receiveEvent(it, type, event)
    }
  }

  companion object {

    // arguments
    private const val ARG_ID = "id"
    private const val ARG_SLIDE_X = "slideX"
    private const val ARG_SLIDE_Y = "slideY"
    private const val ARG_LEVEL = "level"

    // events
    private const val EVENT_REQUEST_STREAM = "onNativeRequestStream"
    private const val EVENT_LBAR_STATE_CHANGED = "onNativeLBarStateChanged"
    private const val EVENT_REQUEST_AUDIO_DUCKING = "onNativeRequestAudioDucking"
    private const val EVENT_DISABLE_AUDIO_DUCKING = "onNativeDisableAudioDucking"

    // setup events
    fun setupEvents(): MutableMap<String, Any> = MapBuilder.builder<String, Any>().apply {
      arrayOf(
        EVENT_REQUEST_STREAM,
        EVENT_LBAR_STATE_CHANGED,
        EVENT_REQUEST_AUDIO_DUCKING,
        EVENT_DISABLE_AUDIO_DUCKING
      ).forEach {
        put(it, MapBuilder.of("registrationName", it))
      }
    }.build()
  }

}
