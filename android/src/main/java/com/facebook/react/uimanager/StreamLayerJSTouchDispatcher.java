package com.facebook.react.uimanager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.uimanager.events.TouchEvent;
import com.facebook.react.uimanager.events.TouchEventCoalescingKeyHelper;
import com.facebook.react.uimanager.events.TouchEventType;

// Keep track of the all code changes here - we need support manual updates of this class (lib version 0.74.1)
// https://github.com/facebook/react-native/blob/main/packages/react-native/ReactAndroid/src/main/java/com/facebook/react/uimanager/JSTouchDispatcher.java
// 1. Add ScopeProvider interface, which allow define root of the search scope
// 2. Change constructor - add ScopeProvider as second param
// 3. Change findTargetTagAndSetCoordinates() - use ScopeProvider root for finding touch target
// 4. Replace FLog with Log class for logging

/**
 * JSTouchDispatcher handles dispatching touches to JS from RootViews. If you implement RootView you
 * need to call handleTouchEvent from onTouchEvent and onInterceptTouchEvent. It will correctly find
 * the right view to handle the touch and also dispatch the appropriate event to JS
 */
public class StreamLayerJSTouchDispatcher {

  public static final String TAG = "StreamLayerJSTouchDispatcher";

  private int mTargetTag = -1;
  private final float[] mTargetCoordinates = new float[2];
  private boolean mChildIsHandlingNativeGesture = false;
  private long mGestureStartTime = TouchEvent.UNSET;
  private final ViewGroup mRootViewGroup;
  private final ScopeProvider mScopeProvider;
  private final TouchEventCoalescingKeyHelper mTouchEventCoalescingKeyHelper =
    new TouchEventCoalescingKeyHelper();

  public StreamLayerJSTouchDispatcher(ViewGroup reactRootViewGroup, ScopeProvider scopeProvider) {
    this.mRootViewGroup = reactRootViewGroup;
    this.mScopeProvider = scopeProvider;
  }

  public void onChildStartedNativeGesture(
    MotionEvent androidEvent, EventDispatcher eventDispatcher) {
    if (mChildIsHandlingNativeGesture) {
      // This means we previously had another child start handling this native gesture and now a
      // different native parent of that child has decided to intercept the touch stream and handle
      // the gesture itself. Example where this can happen: HorizontalScrollView in a ScrollView.
      return;
    }

    dispatchCancelEvent(androidEvent, eventDispatcher);
    mChildIsHandlingNativeGesture = true;
    mTargetTag = -1;
  }

  public void onChildEndedNativeGesture(MotionEvent androidEvent, EventDispatcher eventDispatcher) {
    // There should be only one child gesture at any given time. We can safely turn off the flag.
    mChildIsHandlingNativeGesture = false;
  }

  /**
   * Main catalyst view is responsible for collecting and sending touch events to JS. This method
   * reacts for an incoming android native touch events ({@link MotionEvent}) and calls into {@link
   * com.facebook.react.uimanager.events.EventDispatcher} when appropriate. It uses {@link
   * com.facebook.react.uimanager.TouchTargetHelper#findTargetTagAndCoordinatesForTouch} helper
   * method for figuring out a react view ID in the case of ACTION_DOWN event (when the gesture
   * starts).
   */
  public void handleTouchEvent(MotionEvent ev, EventDispatcher eventDispatcher) {
    int action = ev.getAction() & MotionEvent.ACTION_MASK;
    if (action == MotionEvent.ACTION_DOWN) {
      if (mTargetTag != -1) {
        Log.e(TAG, "Got DOWN touch before receiving UP or CANCEL from last gesture");
      }

      // First event for this gesture. We expect tag to be set to -1, and we use helper method
      // {@link #findTargetTagForTouch} to find react view ID that will be responsible for handling
      // this gesture
      mChildIsHandlingNativeGesture = false;
      mGestureStartTime = ev.getEventTime();

      mTargetTag = findTargetTagAndSetCoordinates(ev);
      eventDispatcher.dispatchEvent(
        TouchEvent.obtain(
          UIManagerHelper.getSurfaceId(mRootViewGroup),
          mTargetTag,
          TouchEventType.START,
          ev,
          mGestureStartTime,
          mTargetCoordinates[0],
          mTargetCoordinates[1],
          mTouchEventCoalescingKeyHelper));
    } else if (mChildIsHandlingNativeGesture) {
      // If the touch was intercepted by a child, we've already sent a cancel event to JS for this
      // gesture, so we shouldn't send any more touches related to it.
      return;
    } else if (mTargetTag == -1) {
      // All the subsequent action types are expected to be called after ACTION_DOWN thus target
      // is supposed to be set for them.
      Log.e(TAG,
        "Unexpected state: received touch event but didn't get starting ACTION_DOWN for this "
          + "gesture before");
    } else if (action == MotionEvent.ACTION_UP) {
      // End of the gesture. We reset target tag to -1 and expect no further event associated with
      // this gesture.
      findTargetTagAndSetCoordinates(ev);
      eventDispatcher.dispatchEvent(
        TouchEvent.obtain(
          UIManagerHelper.getSurfaceId(mRootViewGroup),
          mTargetTag,
          TouchEventType.END,
          ev,
          mGestureStartTime,
          mTargetCoordinates[0],
          mTargetCoordinates[1],
          mTouchEventCoalescingKeyHelper));
      mTargetTag = -1;
      mGestureStartTime = TouchEvent.UNSET;
    } else if (action == MotionEvent.ACTION_MOVE) {
      // Update pointer position for current gesture
      findTargetTagAndSetCoordinates(ev);
      eventDispatcher.dispatchEvent(
        TouchEvent.obtain(
          UIManagerHelper.getSurfaceId(mRootViewGroup),
          mTargetTag,
          TouchEventType.MOVE,
          ev,
          mGestureStartTime,
          mTargetCoordinates[0],
          mTargetCoordinates[1],
          mTouchEventCoalescingKeyHelper));
    } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
      // New pointer goes down, this can only happen after ACTION_DOWN is sent for the first pointer
      eventDispatcher.dispatchEvent(
        TouchEvent.obtain(
          UIManagerHelper.getSurfaceId(mRootViewGroup),
          mTargetTag,
          TouchEventType.START,
          ev,
          mGestureStartTime,
          mTargetCoordinates[0],
          mTargetCoordinates[1],
          mTouchEventCoalescingKeyHelper));
    } else if (action == MotionEvent.ACTION_POINTER_UP) {
      // Exactly one of the pointers goes up
      eventDispatcher.dispatchEvent(
        TouchEvent.obtain(
          UIManagerHelper.getSurfaceId(mRootViewGroup),
          mTargetTag,
          TouchEventType.END,
          ev,
          mGestureStartTime,
          mTargetCoordinates[0],
          mTargetCoordinates[1],
          mTouchEventCoalescingKeyHelper));
    } else if (action == MotionEvent.ACTION_CANCEL) {
      if (mTouchEventCoalescingKeyHelper.hasCoalescingKey(ev.getDownTime())) {
        dispatchCancelEvent(ev, eventDispatcher);
      } else {
        Log.e(
          TAG,
          "Received an ACTION_CANCEL touch event for which we have no corresponding ACTION_DOWN");
      }
      mTargetTag = -1;
      mGestureStartTime = TouchEvent.UNSET;
    } else {
      Log.w(
        TAG,
        "Warning : touch event was ignored. Action=" + action + " Target=" + mTargetTag);
    }
  }

  private int findTargetTagAndSetCoordinates(MotionEvent ev) {
    // This method updates `mTargetCoordinates` with coordinates for the motion event.
    return StreamLayerTouchTargetHelper.findTargetTagAndCoordinatesForTouch(
      ev.getX(), ev.getY(), mScopeProvider.getRoot(), mTargetCoordinates, null);
  }

  private void dispatchCancelEvent(MotionEvent androidEvent, EventDispatcher eventDispatcher) {
    // This means the gesture has already ended, via some other CANCEL or UP event. This is not
    // expected to happen very often as it would mean some child View has decided to intercept the
    // touch stream and start a native gesture only upon receiving the UP/CANCEL event.
    if (mTargetTag == -1) {
      Log.w(
       TAG,
        "Can't cancel already finished gesture. Is a child View trying to start a gesture from "
          + "an UP/CANCEL event?");
      return;
    }

    Assertions.assertCondition(
      !mChildIsHandlingNativeGesture,
      "Expected to not have already sent a cancel for this gesture");
    Assertions.assertNotNull(eventDispatcher)
      .dispatchEvent(
        TouchEvent.obtain(
          UIManagerHelper.getSurfaceId(mRootViewGroup),
          mTargetTag,
          TouchEventType.CANCEL,
          androidEvent,
          mGestureStartTime,
          mTargetCoordinates[0],
          mTargetCoordinates[1],
          mTouchEventCoalescingKeyHelper));
  }

  public interface ScopeProvider{

    ViewGroup getRoot();
  }
}
