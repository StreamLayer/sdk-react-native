package io.streamlayer.react.module
import com.facebook.react.ReactActivity

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import io.streamlayer.react.BuildConfig
import io.streamlayer.react.exo.ExoVideoPlayerProvider
import io.streamlayer.react.R
import io.streamlayer.react.module.internal.StreamLayerConfiguration
import io.streamlayer.react.module.internal.StreamLayerInvite
import io.streamlayer.react.module.internal.toDomain
import io.streamlayer.sdk.SLREventSession
import io.streamlayer.sdk.SLRInviteData
import io.streamlayer.sdk.SLRLogListener
import io.streamlayer.sdk.SLRTheme
import io.streamlayer.sdk.StreamLayer
import io.streamlayer.sdk.StreamLayerDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

internal class StreamLayerModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  // base coroutines scope
  private val scope = CoroutineScope(Dispatchers.Default)

  // event session support
  private var eventSession: SLREventSession? = null
  private var createEventSessionJob: Job? = null

  // auth support
  private var authJob: Job? = null
  private var logoutJob: Job? = null

  // demo streams job
  private var demoStreamsJob: Job? = null

  override fun getName(): String = REACT_CLASS

  @ReactMethod
  fun initSdk(configParams: ReadableMap, promise: Promise) {
    val config = StreamLayerConfiguration(configParams)
    Log.d(
      REACT_CLASS,
      "initSdk sdkKey=${config.sdkKey}" +
        " isLoggingEnabled=${config.isLoggingEnabled}" +
        " exoEnabled=${BuildConfig.EXTENSION_EXO_PLAYER}"
    )
    if (config.isLoggingEnabled) {
      // setup SL logger
      StreamLayer.setLogListener(object : SLRLogListener {
        override fun log(level: SLRLogListener.Level, msg: String) {
          when (level) {
            SLRLogListener.Level.DEBUG -> Log.d(REACT_CLASS, msg)
            SLRLogListener.Level.VERBOSE -> Log.v(REACT_CLASS, msg)
            SLRLogListener.Level.INFO -> Log.i(REACT_CLASS, msg)
            SLRLogListener.Level.ERROR -> Log.e(REACT_CLASS, msg)
            else -> {}
          }
        }
      })
    }
    StreamLayer.initializeApp(reactApplicationContext, config.sdkKey)
    StreamLayer.setGamificationOptions(
      isGlobalLeaderboardEnabled = config.isGlobalLeaderboardEnabled,
      isInvitesEnabled = config.isGamesInviteEnabled
    )
    if (BuildConfig.EXTENSION_EXO_PLAYER) {
      // setup SL video player
      StreamLayer.setVideoPlayerProvider(ExoVideoPlayerProvider(reactApplicationContext))
    }
    when (config.theme) {
      // setup SL theme
      StreamLayerConfiguration.Theme.Green -> {
        // set custom themes
        // TODO: add more attrs later
        StreamLayer.setCustomTheme(
          SLRTheme(
            mainTheme = R.style.GreenMainOverlayTheme,
            profileTheme = R.style.GreenProfileOverlayTheme,
            baseTheme = R.style.GreenBaseOverlayTheme,
            watchPartyTheme = R.style.GreenWatchPartyOverlayTheme,
            inviteTheme = R.style.GreenInviteOverlayTheme,
            gamesTheme = R.style.GreenGamesOverlayTheme,
            statisticsTheme = R.style.GreenStatisticsOverlayTheme,
            chatTheme = R.style.GreenMessengerOverlayTheme,
            notificationsStyle = SLRTheme.NotificationsStyle.DESIGN_NUMBER_ONE
          )
        )
      }

      else -> {}
    }
    promise.resolve(null)
  }

  @ReactMethod
  fun isInitialized(promise: Promise) {
    Log.d(REACT_CLASS, "isInitialized")
    runCatching {
      StreamLayer.isInitialized()
    }.onSuccess {
      Log.d(REACT_CLASS, "isInitialized onSuccess result=$it")
      promise.resolve(it)
    }.onFailure {
      Log.e(REACT_CLASS, "isInitialized onFailure", it)
      promise.reject(it)
    }
  }



  @ReactMethod
  fun authorizationBypass(schema: String, token: String, promise: Promise) {
    Log.d(REACT_CLASS, "authorizationBypass schema=$schema token=$token")
    logoutJob?.cancel()
    authJob?.cancel()
    authJob = scope.launch {
      runCatching {
        StreamLayer.authorizationBypass(schema, token)
      }.onSuccess {
        Log.d(REACT_CLASS, "authorizationBypass onSuccess")
        promise.resolve(null)
      }.onFailure {
        Log.e(REACT_CLASS, "authorizationBypass onFailure", it)
        promise.reject(it)
      }
    }
  }

  @ReactMethod
  fun useAnonymousAuth(promise: Promise) {
    Log.d(REACT_CLASS, "useAnonymousAuth")
    logoutJob?.cancel()
    authJob?.cancel()
    authJob = scope.launch {
      runCatching {
        StreamLayer.useAnonymousAuth()
      }.onSuccess {
        Log.d(REACT_CLASS, "useAnonymousAuth onSuccess")
        promise.resolve(null)
      }.onFailure {
        Log.e(REACT_CLASS, "useAnonymousAuth onFailure", it)
        promise.reject(it)
      }
    }
  }

  @ReactMethod
  fun logout(promise: Promise) {
    Log.d(REACT_CLASS, "logout")
    authJob?.cancel()
    logoutJob?.cancel()
    logoutJob = scope.launch {
      runCatching {
        StreamLayer.logout()
      }.onSuccess {
        Log.d(REACT_CLASS, "logout onSuccess")
        promise.resolve(null)
      }.onFailure {
        Log.e(REACT_CLASS, "logout onFailure", it)
        promise.reject(it)
      }
    }
  }

  @ReactMethod
  fun isUserAuthorized(promise: Promise) {
    Log.d(REACT_CLASS, "isUserAuthorized")
    runCatching {
      StreamLayer.isUserAuthorized()
    }.onSuccess {
      Log.d(REACT_CLASS, "isUserAuthorized onSuccess result=$it")
      promise.resolve(it)
    }.onFailure {
      Log.e(REACT_CLASS, "isUserAuthorized onFailure", it)
      promise.reject(it)
    }
  }

  @ReactMethod
  fun createEventSession(eventId: String, promise: Promise) {
    Log.d(REACT_CLASS, "createEventSession requested for eventId=$eventId")
    // check if session still is active
    if (eventSession?.getExternalEventId() == eventId && eventSession?.isReleased() == false) {
      promise.resolve(null)
    } else {
      releaseEventSession()
      createEventSessionJob = scope.launch {
        runCatching {
          eventSession = StreamLayer.createEventSession(eventId, null)
        }.onSuccess {
          Log.d(REACT_CLASS, "createEventSession onSuccess eventId=$eventId")
          promise.resolve(null)
        }.onFailure {
          Log.e(REACT_CLASS, "createEventSession onFailure eventId=$eventId", it)
          promise.reject(it)
        }
      }
    }
  }

  @ReactMethod
  fun releaseEventSession() {
    Log.d(REACT_CLASS, "releaseEventSession")
    createEventSessionJob?.cancel()
    eventSession?.release()
  }

  @ReactMethod
  fun getInvite(json: ReadableMap, promise: Promise) {
    Log.d(REACT_CLASS, "getInviteData requested for json=$json")
    runCatching {
      SLRInviteData.fromJsonObject(JSONObject(json.toString()))?.toDomain()?.toMap()
    }.onSuccess {
      Log.d(REACT_CLASS, "getInviteData onSuccess $it")
      promise.resolve(it)
    }.onFailure {
      Log.d(REACT_CLASS, "getInviteData onError $it")
      promise.resolve(null)
    }
  }

  @ReactMethod
  fun getDemoEvents(date: String, promise: Promise) {
    Log.d(REACT_CLASS, "getDemoEvents requested for date=$date")
    demoStreamsJob?.cancel()
    demoStreamsJob = scope.launch {
      runCatching {
        val array = Arguments.createArray()
        StreamLayerDemo.getDemoStreams(date).forEach {
          array.pushMap(it.toDomain().toMap())
        }
        array
      }.onSuccess {
        Log.d(REACT_CLASS, "getDemoEvents onSuccess $it")
        promise.resolve(it)
      }.onFailure {
        Log.e(REACT_CLASS, "getDemoEvents onFailure", it)
        promise.reject(it)
      }
    }
  }

  companion object {
    const val REACT_CLASS = "StreamLayerModule"
  }
}
