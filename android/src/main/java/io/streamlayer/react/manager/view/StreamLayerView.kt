package io.streamlayer.react.manager.view

import android.util.Log
import android.os.Bundle
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.StreamLayerJSTouchDispatcher
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.common.UIManagerType
import io.streamlayer.react.module.internal.StreamLayerInvite
import io.streamlayer.react.module.internal.toData
import io.streamlayer.sdk.R
import io.streamlayer.sdk.SLRAppHost
import io.streamlayer.sdk.StreamLayer
import io.streamlayer.sdk.StreamLayer.withStreamLayerUI
import io.streamlayer.sdk.main.StreamLayerFragment
import kotlin.math.max


internal class StreamLayerView(
  private val reactContext: ReactApplicationContext,
  context: ThemedReactContext
) : FrameLayout(context) {

  private var reactNativeViewId: Int? = null
  private var applyWindowInsets = true
  private var viewConfiguration: StreamLayerViewConfiguration? = null
  private var eventEmitter: StreamLayerViewEventEmitter? = null
  private var commandReceiver: StreamLayerViewCommandReceiver? = null
  private var jsTouchDispatcher: StreamLayerJSTouchDispatcher? = null
  private var pendingInvite: StreamLayerInvite? = null

  private val appHostDelegate by lazy {
    object : SLRAppHost.Delegate {

      override fun requestStream(id: String) {
        super.requestStream(id)
        eventEmitter?.onRequestStream(id)
      }

      override fun onLBarStateChanged(slideX: Int, slideY: Int) {
        eventEmitter?.onLBarStateChanged(slideX, slideY)
      }

      override fun requestAudioDucking(level: Float) {
        eventEmitter?.requestAudioDucking(level)
      }

      override fun disableAudioDucking() {
        eventEmitter?.disableAudioDucking()
      }
    }
  }

  private val fragmentManagerListener by lazy {
    FragmentOnAttachListener { _, fragment ->
      if (fragment is SLRAppHost) fragment.apply {
        delegate = appHostDelegate
        applyViewConfiguration(this)
        pendingInvite?.let { handleInvite(it) }
      }
    }
  }

  // TODO: support multi-fragments mode later
  private val fragmentLifecycleListener by lazy {
    object : FragmentManager.FragmentLifecycleCallbacks() {
      override fun onFragmentViewCreated(
        fm: FragmentManager,
        fragment: Fragment,
        view: View,
        savedInstanceState: Bundle?
      ) {
        if (fragment is SLRAppHost) {
          // attempt to prevent com.facebook.react.bridge.ReactNoCrashSoftException: Cannot get UIManager for UIManagerType: 2
          // react native ViewUtil doesn't agree with native fragments inner android view ids
          // we can only use a react native TouchTargetHelper(181L) backdoor - disable conflict view groups
          // keep track of disabled views below:
          // 1. disable root view
          view.findViewById<View>(R.id.slHomeRootView)?.isEnabled = false
          // 2. disable player space view
          view.findViewById<View>(R.id.slSpaceView)?.isEnabled = false
          // 3. disable overlay sheet container
          view.findViewById<View>(R.id.slOverlaySheetContainer)?.isEnabled = false
          // 4. disable notification container
          view.findViewById<View>(R.id.slNotificationsView)?.isEnabled = false
          // 5. disable menu containers
          view.findViewById<ViewGroup>(R.id.slMenuLayout)?.isEnabled = false
          view.findViewById<ViewGroup>(R.id.slMenuLabelContainer)?.isEnabled = false
          view.findViewById<ViewGroup>(R.id.slMenuIconContainer)?.isEnabled = false
        }
      }
    }
  }

  init {
    eventEmitter = StreamLayerViewEventEmitter(reactContext) { reactNativeViewId }
    commandReceiver = StreamLayerViewCommandReceiver(
      object : StreamLayerViewCommandReceiver.Listener {

        override fun onCreate(reactNativeViewId: Int) {
          createFragment(reactNativeViewId)
        }

        override fun onDestroy() {
          destroyFragment()
        }

        override fun onHideMenu() {
          hideMenu()
        }

        override fun onHideOverlay() {
          hideOverlay()
        }

        override fun onShowOverlay(viewOverlay: String) {
          showOverlay(viewOverlay)
        }

        override fun onHandleInvite(invite: StreamLayerInvite) {
          handleInvite(invite)
        }
      })
  }

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    val result = super.onTouchEvent(event)
    if (!result) event?.let {
      // TODO: Support tap in portrait area
      hideMenu()
      // TODO: Support new RN architecture later
      val eventDispatcher = UIManagerHelper.getEventDispatcher(reactContext, UIManagerType.DEFAULT)
      jsTouchDispatcher?.handleTouchEvent(it, eventDispatcher)
    }
    return result
  }

  fun setConfig(config: ReadableMap?) {
    viewConfiguration = StreamLayerViewConfiguration(config)
    (reactContext.currentActivity as ReactActivity).withStreamLayerUI {
      applyViewConfiguration(this)
    }
  }

  fun applyWindowInsets(value: Boolean) {
    this.applyWindowInsets = value
  }

  fun receiveCommand(commandId: String, args: ReadableArray?) {
    Log.d("Received command:", "Received command: $commandId with args: $args")
    commandReceiver?.receiveCommand(commandId, args)
  }

  private fun createFragment(reactNativeViewId: Int) {
    this.reactNativeViewId = reactNativeViewId
    val activity = reactContext.currentActivity as ReactActivity
    val parentView = findViewById<ViewGroup>(reactNativeViewId)
    setupLayout(parentView)
    activity.reactDelegate?.reactRootView?.let {
      jsTouchDispatcher = StreamLayerJSTouchDispatcher(it) { parent as ViewGroup }
    }
    activity.supportFragmentManager.apply {
      addFragmentOnAttachListener(fragmentManagerListener)
      registerFragmentLifecycleCallbacks(fragmentLifecycleListener, false)
      beginTransaction()
        .replace(reactNativeViewId, StreamLayerFragment::class.java, null, getBackStackTag())
        .commit()
    }
  }

  private fun destroyFragment() {
    reactNativeViewId = null
    jsTouchDispatcher = null
    pendingInvite = null
    val activity = reactContext.currentActivity as ReactActivity
    activity.supportFragmentManager.apply {
      removeFragmentOnAttachListener(fragmentManagerListener)
      unregisterFragmentLifecycleCallbacks(fragmentLifecycleListener)
      findSLRFragment()?.let { fragment ->
        if (fragment is SLRAppHost) fragment.delegate = null
        activity.supportFragmentManager
          .beginTransaction()
          .remove(fragment)
          // allow state loss to prevent crash - we don't need save state here
          .commitNowAllowingStateLoss()
      }
    }
  }

  private fun hideMenu() {
    (reactContext.currentActivity as ReactActivity).withStreamLayerUI { hideMenu() }
  }

  private fun hideOverlay() {
    (reactContext.currentActivity as ReactActivity).withStreamLayerUI { hideOverlay() }
  }

  private fun showOverlay(viewOverlay: String) {
    StreamLayerViewConfiguration.getAppHostOverlay(viewOverlay)?.let { overlay ->
      (reactContext.currentActivity as ReactActivity).withStreamLayerUI { showOverlay(overlay) }
    }
  }

  fun handleInvite(invite: StreamLayerInvite) {
    Log.d("Received command handleInvite:", "Received command: with args: $invite ")
    val activity = reactContext.currentActivity as ReactActivity
    activity.supportFragmentManager.apply {
      findSLRFragment()?.let { _ ->
        invite.toData()?.let {
          pendingInvite = null
          Log.d("Received command handleInvite:", "Received command: with args: $it")
          Log.d("Received command handleInvite:", "Received command: with args: $activity")
          StreamLayer.handleInvite(it, activity)
        }
      } ?: kotlin.run {
        pendingInvite = invite
      }
    }
  }

  private fun FragmentManager.findSLRFragment(): Fragment? = findFragmentByTag(getBackStackTag())

  // TODO: support multi-fragments mode later
  private fun getBackStackTag(): String = StreamLayerFragment::class.java.simpleName

  private fun setupLayout(view: View) {
    Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
      override fun doFrame(frameTimeNanos: Long) {
        manuallyLayoutChildren(view)
        view.viewTreeObserver.dispatchOnGlobalLayout()
        Choreographer.getInstance().postFrameCallback(this)
      }
    })
  }

  private fun manuallyLayoutChildren(view: View) {
    val width = view.right - view.left
    val height = view.bottom - view.top
    view.measure(
      MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    )
    view.layout(view.left, view.top, view.left + width, view.top + height)
    if (applyWindowInsets) {
      // TODO: improve this code later using window insets compat
      val bottomPadding = view.rootWindowInsets.let {
        max(
          runCatching { it.systemWindowInsetBottom - it.systemWindowInsetTop }.getOrDefault(0), 0
        )
      }
      if (view.paddingBottom != bottomPadding) view.setPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, bottomPadding
      )
    }
  }

  private fun applyViewConfiguration(appHost: SLRAppHost) {
    viewConfiguration?.let { config ->
      appHost.apply {
        inAppNotificationsMode = config.inAppNotificationsMode
        isGamesPointsEnabled = config.isGamesPointsEnabled
        isGamesPointsStartSide = config.isGamesPointsStartSide
        isLaunchButtonEnabled = config.isLaunchButtonEnabled
        isMenuAlwaysOpened = config.isMenuAlwaysOpened
        isMenuLabelsVisible = config.isMenuLabelsVisible
        isMenuProfileEnabled = config.isMenuProfileEnabled
        isTooltipsEnabled = config.isTooltipsEnabled
        isWatchPartyReturnButtonEnabled = config.isWatchPartyReturnButtonEnabled
        isWhoIsWatchingViewEnabled = config.isWhoIsWatchingViewEnabled
        isOverlayExpandable = config.isOverlayExpandable
        overlayHeightSpace = config.overlayHeightSpace
        overlayWidth = config.overlayWidth
        overlayLandscapeMode = config.overlayLandscapeMode
      }
    }
  }
}
