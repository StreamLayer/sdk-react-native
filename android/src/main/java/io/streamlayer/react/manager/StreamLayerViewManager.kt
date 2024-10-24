package io.streamlayer.react.manager
import android.util.Log

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import io.streamlayer.react.manager.view.StreamLayerView
import io.streamlayer.react.manager.view.StreamLayerViewCommandReceiver
import io.streamlayer.react.manager.view.StreamLayerViewConfiguration
import io.streamlayer.react.manager.view.StreamLayerViewEventEmitter

internal class StreamLayerViewManager(private val reactContext: ReactApplicationContext) :
  ViewGroupManager<StreamLayerView>() {

  override fun getName() = REACT_CLASS

  override fun getCommandsMap() =
    StreamLayerViewCommandReceiver.setupCommands()

  override fun getExportedCustomDirectEventTypeConstants() =
    StreamLayerViewEventEmitter.setupEvents()

  override fun createViewInstance(context: ThemedReactContext) =
    StreamLayerView(reactContext, context)

  @ReactProp(name = StreamLayerViewConfiguration.PROP_CONFIG)
  fun setConfig(view: StreamLayerView, config: ReadableMap?) {
    view.setConfig(config)
  }

  @ReactProp(name = StreamLayerViewConfiguration.PROP_APPLY_WINDOW_INSETS)
  fun applyWindowInsets(view: StreamLayerView, value: Boolean) {
    view.applyWindowInsets(value)
  }

  override fun receiveCommand(view: StreamLayerView, commandId: String, args: ReadableArray?) {
        val commandIdType = commandId::class.java.simpleName
     Log.d("Received command:", "Received command: $commandId (Type: $commandIdType) with args: $args")
    view.receiveCommand(commandId, args)
  }

// override fun receiveCommand(view: StreamLayerView, commandId: String, args: ReadableArray?) {
//     when (commandId) {
//         // "handleInvite" -> {
//             val invite = args?.getMap(0)?.toHashMap() // Преобразование аргумента в объект StreamLayerInvite
//             // view.handleInvite(invite)
//         // }
//         // Другие команды
//     }
// }
  companion object {
    private const val REACT_CLASS = "StreamLayerRCTView"
  }
}
