package io.streamlayer.react.module.internal

import com.facebook.react.bridge.ReadableMap

// TODO: refactor later using Json parser
internal class StreamLayerConfiguration(configProps: ReadableMap?) {
  var sdkKey: String = ""
    private set

  var theme: Theme = Theme.Blue
    private set

  var isLoggingEnabled: Boolean = false
    private set

  var isGlobalLeaderboardEnabled: Boolean = false
    private set

  var isGamesInviteEnabled: Boolean = true
    private set

  init {
    configProps?.let { props ->
      // set sdk key
      if (props.hasKey(PROP_SDK_KEY)) {
        sdkKey = props.getString(PROP_SDK_KEY) ?: ""
      }
      // set theme
      if (props.hasKey(PROP_THEME)) {
        getTheme(props.getString(PROP_THEME))?.let {
          theme = it
        }
      }
      // set logging
      if (props.hasKey(PROP_IS_LOGGING_ENABLED)) {
        isLoggingEnabled = props.getBoolean(PROP_IS_LOGGING_ENABLED)
      }
      // set games options
      if (props.hasKey(PROP_IS_GLOBAL_LEADERBOARD_ENABLED)) {
        isGlobalLeaderboardEnabled = props.getBoolean(PROP_IS_GLOBAL_LEADERBOARD_ENABLED)
      }
      if (props.hasKey(PROP_IS_GAMES_INVITE_ENABLED)) {
        isGamesInviteEnabled = props.getBoolean(PROP_IS_GAMES_INVITE_ENABLED)
      }
    }
  }

  enum class Theme {
    Blue,
    Green
  }

  companion object {
    // props
    private const val PROP_SDK_KEY = "sdkKey"
    private const val PROP_THEME = "theme"
    private const val PROP_IS_LOGGING_ENABLED = "isLoggingEnabled"
    private const val PROP_IS_GLOBAL_LEADERBOARD_ENABLED = "isGlobalLeaderboardEnabled"
    private const val PROP_IS_GAMES_INVITE_ENABLED = "isGamesInviteEnabled"

    // values
    private const val VALUE_BLUE = "Blue"
    private const val VALUE_GREEN = "Green"

    fun getTheme(viewOverlayMode: String?) = when (viewOverlayMode) {
      VALUE_BLUE -> Theme.Blue
      VALUE_GREEN -> Theme.Green
      else -> null
    }
  }
}
