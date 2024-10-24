import React, { PureComponent } from 'react';
import { UIManager, findNodeHandle, Platform, PixelRatio, View, StyleSheet, NativeModules, NativeEventEmitter } from 'react-native';
import { requireNativeComponent } from 'react-native';
// TODO: probably replace it with Animated view --- need to check with implementation is better
class LBarState {
  constructor(slideX, slideY) {
    this.slideX = slideX;
    this.slideY = slideY;
  }
}
export class StreamLayerView extends PureComponent {
  static initialState = {
    lbarState: new LBarState(0, 0),
    volumeBeforeDucking: undefined
  };
  constructor(props) {
    super(props);
    this._root = /*#__PURE__*/React.createRef();
    this.state = StreamLayerView.initialState;
    if (Platform.OS === 'ios') {
      this.registerEvents();
    }
  }
  registerEvents() {
    const emitter = new NativeEventEmitter(NativeModules.StreamLayerViewEventEmitter);
    emitter.addListener('onNativeLBarStateChanged', this._onNativeLBarStateChanged);
    emitter.addListener('onNativeRequestStream', this._onNativeRequestStream);
    emitter.addListener('onNativeRequestAudioDucking', this._onNativeRequestAudioDucking);
    emitter.addListener('onNativeDisableAudioDucking', this._onNativeDisableAudioDucking);
  }
  _onNativeRequestStream = event => {
    const nativeEvent = Platform.select({
      ios: event,
      default: event.nativeEvent
    });
    this.props.onRequestStream?.(nativeEvent.id);
  };
  _onNativeLBarStateChanged = event => {
    const nativeEvent = Platform.select({
      ios: event,
      default: event.nativeEvent
    });
    const ratio = PixelRatio.get();
    const slideX = Platform.select({
      ios: Math.round(nativeEvent.slideX),
      default: Math.round(nativeEvent.slideX / ratio)
    });
    const slideY = Platform.select({
      ios: Math.round(nativeEvent.slideY),
      default: Math.round(nativeEvent.slideY / ratio)
    });
    if (this.props.onLBarStateChanged !== undefined) {
      this.props.onLBarStateChanged?.(slideX, slideY);
    } else {
      const lbar = new LBarState(slideX, slideY);
      this.setState({
        lbarState: lbar
      });
    }
  };
  _onNativeRequestAudioDucking = event => {
    const nativeEvent = Platform.select({
      ios: event,
      default: event.nativeEvent
    });
    const level = nativeEvent.level;
    const player = this.props.player;
    if (player !== undefined) {
      const playerVolume = player.volume;
      if (this.state.volumeBeforeDucking == undefined) {
        this.setState({
          volumeBeforeDucking: playerVolume
        });
      }
      console.debug("_onNativeRequestAudioDucking change player volume=" + playerVolume + " level=" + level);
      player.volume = Math.min(playerVolume, level);
    } else {
      this.props.onRequestAudioDucking?.(level);
    }
  };
  _onNativeDisableAudioDucking = () => {
    const player = this.props.player;
    if (player !== undefined) {
      if (this.state.volumeBeforeDucking !== undefined) {
        player.volume = this.state.volumeBeforeDucking;
        console.log("_onNativeDisableAudioDucking change player volume=" + this.state.volumeBeforeDucking);
        this.setState({
          volumeBeforeDucking: undefined
        });
      }
    } else {
      this.props.onDisableAudioDucking?.();
    }
  };
  componentDidMount() {
    if (Platform.OS === 'android') {
      // create view
      const viewId = findNodeHandle(this._root.current);
      const command = UIManager[ComponentName].Commands.create.toString();
      UIManager.dispatchViewManagerCommand(viewId, command, [viewId]);
    } else {}
  }
  componentWillUnmount() {
    if (Platform.OS === 'android') {
      // destroy view
      const viewId = findNodeHandle(this._root.current);
      const command = UIManager[ComponentName].Commands.destroy.toString();
      UIManager.dispatchViewManagerCommand(viewId, command, [null]);
    }
  }
  hideMenu() {
    const viewId = findNodeHandle(this._root.current);
    const command = UIManager[ComponentName].Commands.hideMenu;
    UIManager.dispatchViewManagerCommand(viewId, Platform.select({
      ios: command,
      default: command.toString()
    }), [null]);
  }
  hideOverlay() {
    const viewId = findNodeHandle(this._root.current);
    const command = UIManager[ComponentName].Commands.hideOverlay;
    UIManager.dispatchViewManagerCommand(viewId, Platform.select({
      ios: command,
      default: command.toString()
    }), [null]);
  }
  showOverlay(viewOverlay) {
    const viewId = findNodeHandle(this._root.current);
    const command = UIManager[ComponentName].Commands.showOverlay;
    UIManager.dispatchViewManagerCommand(viewId, Platform.select({
      ios: command,
      default: command.toString()
    }), [viewOverlay]);
  }
  handleInvite(invite) {
    const viewId = findNodeHandle(this._root.current);
    const command = UIManager[ComponentName].Commands.handleInvite;
    UIManager.dispatchViewManagerCommand(viewId, Platform.select({
      ios: command,
      default: command.toString()
    }), [invite]);
  }
  render() {
    const {
      config,
      style,
      playerView,
      applyWindowInsets
    } = this.props;
    return /*#__PURE__*/React.createElement(View, {
      style: [style]
    }, playerView !== undefined && /*#__PURE__*/React.createElement(View, {
      style: {
        marginEnd: this.state.lbarState.slideX,
        marginBottom: this.state.lbarState.slideY
      }
    }, playerView), /*#__PURE__*/React.createElement(StreamLayerRCTView, {
      style: StyleSheet.absoluteFill,
      ref: this._root,
      config: config || {},
      hocModeEnabled: playerView !== undefined,
      applyWindowInsets: applyWindowInsets || true,
      onNativeRequestStream: this._onNativeRequestStream,
      onNativeLBarStateChanged: this._onNativeLBarStateChanged,
      onNativeRequestAudioDucking: this._onNativeRequestAudioDucking,
      onNativeDisableAudioDucking: this._onNativeDisableAudioDucking
    }));
  }
}
const ComponentName = 'StreamLayerRCTView';
const LINKING_ERROR = `The package 'react-native-streamlayer' doesn't seem to be linked. Make sure: \n\n` + Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo managed workflow\n';
const StreamLayerRCTView = UIManager.getViewManagerConfig(ComponentName) != null ? requireNativeComponent(ComponentName) : () => {
  throw new Error(LINKING_ERROR);
};
//# sourceMappingURL=StreamLayerView.js.map