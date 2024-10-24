package io.streamlayer.react.manager.view

import android.util.Log
import com.facebook.react.bridge.ReadableArray
import io.streamlayer.react.module.internal.StreamLayerInvite

internal class StreamLayerViewCommandReceiver(private val listener: Listener) {

  interface Listener {

    fun onCreate(reactNativeViewId: Int)

    fun onDestroy()

    fun onHideMenu()

    fun onHideOverlay()

    fun onShowOverlay(viewOverlay: String)

    fun onHandleInvite(invite: StreamLayerInvite)
  }

  fun receiveCommand(commandId: String, args: ReadableArray?) {
    val commandIdType = commandId::class.java.simpleName
    Log.d("HandlingDeepLink:", "Received command: $commandId (Type: $commandIdType) with args: $args")

    
    when (commandId.toInt()) {
      COMMAND_CREATE_CODE -> {

        Log.d("HandlingDeepLink:", "CREATING CODE STREAMLAYERCOMMANDRECEIVER")
        val reactNativeViewId = requireNotNull(args).getInt(0)
        listener.onCreate(reactNativeViewId)
      }

      COMMAND_DESTROY_CODE -> listener.onDestroy()
      COMMAND_HIDE_MENU_CODE -> listener.onHideMenu()
      COMMAND_HIDE_OVERLAY_CODE -> listener.onHideOverlay()
      COMMAND_SHOW_OVERLAY_CODE -> {
        val viewOverlay = requireNotNull(args).getString(0)
        listener.onShowOverlay(viewOverlay)
      }

      COMMAND_HANDLE_INVITE_CODE -> {
        Log.d("HandlingDeepLink:", "HANDLING INVITE STREAMLAYERCOMMANDRECEIVER")
        val invite = StreamLayerInvite.fromMap(requireNotNull(args).getMap(0))
        listener.onHandleInvite(invite)
      }
    }
  }

  companion object {

    // commands names
    private const val COMMAND_CREATE_NAME = "create"
    private const val COMMAND_DESTROY_NAME = "destroy"
    private const val COMMAND_HIDE_MENU_NAME = "hideMenu"
    private const val COMMAND_HIDE_OVERLAY_NAME = "hideOverlay"
    private const val COMMAND_SHOW_OVERLAY_NAME = "showOverlay"
    private const val COMMAND_HANDLE_INVITE_NAME = "handleInvite"

    // commands codes
    private const val COMMAND_CREATE_CODE = 1
    private const val COMMAND_DESTROY_CODE = 2
    private const val COMMAND_HIDE_MENU_CODE = 3
    private const val COMMAND_HIDE_OVERLAY_CODE = 4
    private const val COMMAND_SHOW_OVERLAY_CODE = 5
    private const val COMMAND_HANDLE_INVITE_CODE = 6

    // setup commands
    fun setupCommands() = mapOf(
      COMMAND_CREATE_NAME to COMMAND_CREATE_CODE,
      COMMAND_DESTROY_NAME to COMMAND_DESTROY_CODE,
      COMMAND_HIDE_MENU_NAME to COMMAND_HIDE_MENU_CODE,
      COMMAND_HIDE_OVERLAY_NAME to COMMAND_HIDE_OVERLAY_CODE,
      COMMAND_SHOW_OVERLAY_NAME to COMMAND_SHOW_OVERLAY_CODE,
      COMMAND_HANDLE_INVITE_NAME to COMMAND_HANDLE_INVITE_CODE
    )
  }

}
