package io.streamlayer.react.manager.view

import com.facebook.react.bridge.ReadableMap
import io.streamlayer.common.extensions.dp
import io.streamlayer.sdk.SLRAppHost
import kotlin.math.max

// TODO: refactor later using Json parser
internal class StreamLayerViewConfiguration(configProps: ReadableMap?) {

  var inAppNotificationsMode: SLRAppHost.NotificationMode = SLRAppHost.NotificationMode.All
    private set

  var isGamesPointsEnabled: Boolean = true
    private set

  var isGamesPointsStartSide: Boolean = false
    private set

  var isLaunchButtonEnabled: Boolean = true
    private set

  var isMenuAlwaysOpened: Boolean = false
    private set

  var isMenuLabelsVisible: Boolean = true
    private set

  var isMenuProfileEnabled: Boolean = true
    private set

  var isTooltipsEnabled: Boolean = true
    private set

  var isWatchPartyReturnButtonEnabled: Boolean = true
    private set

  var isWhoIsWatchingViewEnabled: Boolean = true
    private set

  var isOverlayExpandable: Boolean = false
    private set

  var overlayHeightSpace: Int = 0
    private set

  var overlayWidth: Int = 0
    private set

  var overlayLandscapeMode: SLRAppHost.OverlayLandscapeMode = SLRAppHost.OverlayLandscapeMode.START
    private set

  init {
    configProps?.let { props ->
      // set in app notifications mode
      if (props.hasKey(PROP_VIEW_NOTIFICATION_FEATURES)) {
        props.getArray(PROP_VIEW_NOTIFICATION_FEATURES)?.let {
          it.toArrayList().mapNotNull { getAppHostNotificationModeFeature(it as String?) }
            .let { features ->
              inAppNotificationsMode = if (features.isEmpty()) SLRAppHost.NotificationMode.Silent
              else SLRAppHost.NotificationMode.List(features.toSet().toList())
            }
        }
      }
      // set games points visibility
      if (props.hasKey(PROP_IS_GAMES_POINTS_ENABLED)) {
        isGamesPointsEnabled = props.getBoolean(PROP_IS_GAMES_POINTS_ENABLED)
      }
      // get games points side
      if (props.hasKey(PROP_IS_GAMES_POINTS_START_SIDE)) {
        isGamesPointsStartSide = props.getBoolean(PROP_IS_GAMES_POINTS_START_SIDE)
      }
      // set launch button visibility
      if (props.hasKey(PROP_IS_LAUNCH_BUTTON_ENABLED)) {
        isLaunchButtonEnabled = props.getBoolean(PROP_IS_LAUNCH_BUTTON_ENABLED)
      }
      // set menu always opened
      if (props.hasKey(PROP_IS_MENU_ALWAYS_OPENED)) {
        isMenuAlwaysOpened = props.getBoolean(PROP_IS_MENU_ALWAYS_OPENED)
      }
      // set menu labels visible
      if (props.hasKey(PROP_IS_MENU_LABELS_VISIBLE)) {
        isMenuLabelsVisible = props.getBoolean(PROP_IS_MENU_LABELS_VISIBLE)
      }
      // set menu profile enabled
      if (props.hasKey(PROP_IS_MENU_PROFILE_ENABLED)) {
        isMenuProfileEnabled = props.getBoolean(PROP_IS_MENU_PROFILE_ENABLED)
      }
      // set tooltips visibility
      if (props.hasKey(PROP_IS_TOOLTIPS_ENABLED)) {
        isTooltipsEnabled = props.getBoolean(PROP_IS_TOOLTIPS_ENABLED)
      }
      // set watch party button visibility
      if (props.hasKey(PROP_IS_WATCH_PARTY_RETURN_BUTTON_ENABLED)) {
        isWatchPartyReturnButtonEnabled =
          props.getBoolean(PROP_IS_WATCH_PARTY_RETURN_BUTTON_ENABLED)
      }
      // set whos watching view visibility
      if (props.hasKey(PROP_IS_WHOS_WATCHING_VIEW_ENABLED)) {
        isWhoIsWatchingViewEnabled = props.getBoolean(PROP_IS_WHOS_WATCHING_VIEW_ENABLED)
      }
      // set overlay expandable
      if (props.hasKey(PROP_IS_OVERLAY_EXPANDABLE)) {
        isOverlayExpandable = props.getBoolean(PROP_IS_OVERLAY_EXPANDABLE)
      }
      // set overlay height space
      if (props.hasKey(PROP_OVERLAY_HEIGHT_SPACE)) {
        overlayHeightSpace = max(0, props.getInt(PROP_OVERLAY_HEIGHT_SPACE)).toFloat().dp
      }
      // set overlay width
      if (props.hasKey(PROP_OVERLAY_WIDTH)) {
        overlayWidth = max(0, props.getInt(PROP_OVERLAY_WIDTH)).toFloat().dp
      }
      // set overlay landscape mode
      if (props.hasKey(PROP_OVERLAY_LANDSCAPE_MODE)) {
        getAppHostOverlayLandscapeMode(props.getString(PROP_OVERLAY_LANDSCAPE_MODE))?.let {
          overlayLandscapeMode = it
        }
      }
    }
  }

  companion object {

    // props
    const val PROP_CONFIG = "config"
    const val PROP_APPLY_WINDOW_INSETS = "applyWindowInsets"
    private const val PROP_VIEW_NOTIFICATION_FEATURES = "viewNotificationFeatures"
    private const val PROP_IS_GAMES_POINTS_ENABLED = "isGamesPointsEnabled"
    private const val PROP_IS_GAMES_POINTS_START_SIDE = "isGamesPointsStartSide"
    private const val PROP_IS_LAUNCH_BUTTON_ENABLED = "isLaunchButtonEnabled"
    private const val PROP_IS_MENU_ALWAYS_OPENED = "isMenuAlwaysOpened"
    private const val PROP_IS_MENU_LABELS_VISIBLE = "isMenuLabelsVisible"
    private const val PROP_IS_MENU_PROFILE_ENABLED = "isMenuProfileEnabled"
    private const val PROP_IS_TOOLTIPS_ENABLED = "isTooltipsEnabled"
    private const val PROP_IS_WATCH_PARTY_RETURN_BUTTON_ENABLED = "isWatchPartyReturnButtonEnabled"
    private const val PROP_IS_WHOS_WATCHING_VIEW_ENABLED = "isWhoIsWatchingViewEnabled"
    private const val PROP_IS_OVERLAY_EXPANDABLE = "isOverlayExpandable"
    private const val PROP_OVERLAY_HEIGHT_SPACE = "overlayHeightSpace"
    private const val PROP_OVERLAY_WIDTH = "overlayWidth"
    private const val PROP_OVERLAY_LANDSCAPE_MODE = "overlayLandscapeMode"

    private const val VALUE_GAMES = "Games"
    private const val VALUE_WATCH_PARTY = "WatchParty"
    private const val VALUE_TWITTER = "Twitter"
    private const val VALUE_STATISTICS = "Statistics"
    private const val VALUE_CHAT = "Chat"
    private const val VALUE_START = "Start"
    private const val VALUE_END = "End"
    private const val VALUE_LBAR = "Lbar"

    fun getAppHostOverlay(viewOverlay: String?) = when (viewOverlay) {
      VALUE_GAMES -> SLRAppHost.Overlay.Games
      VALUE_WATCH_PARTY -> SLRAppHost.Overlay.WatchParty
      VALUE_TWITTER -> SLRAppHost.Overlay.Twitter
      VALUE_STATISTICS -> SLRAppHost.Overlay.Statistics
      else -> null
    }

    fun getAppHostOverlayLandscapeMode(viewOverlayMode: String?) = when (viewOverlayMode) {
      VALUE_START -> SLRAppHost.OverlayLandscapeMode.START
      VALUE_END -> SLRAppHost.OverlayLandscapeMode.END
      VALUE_LBAR -> SLRAppHost.OverlayLandscapeMode.LBAR
      else -> null
    }

    fun getAppHostNotificationModeFeature(viewOverlayMode: String?) = when (viewOverlayMode) {
      VALUE_WATCH_PARTY -> SLRAppHost.NotificationMode.Feature.WATCH_PARTY
      VALUE_GAMES -> SLRAppHost.NotificationMode.Feature.GAMES
      VALUE_CHAT -> SLRAppHost.NotificationMode.Feature.CHAT
      VALUE_TWITTER -> SLRAppHost.NotificationMode.Feature.TWITTER
      else -> null
    }
  }
}
