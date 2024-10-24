"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.StreamLayerView = void 0;
var _react = _interopRequireWildcard(require("react"));
var _reactNative = require("react-native");
function _getRequireWildcardCache(e) { if ("function" != typeof WeakMap) return null; var r = new WeakMap(), t = new WeakMap(); return (_getRequireWildcardCache = function (e) { return e ? t : r; })(e); }
function _interopRequireWildcard(e, r) { if (!r && e && e.__esModule) return e; if (null === e || "object" != typeof e && "function" != typeof e) return { default: e }; var t = _getRequireWildcardCache(r); if (t && t.has(e)) return t.get(e); var n = { __proto__: null }, a = Object.defineProperty && Object.getOwnPropertyDescriptor; for (var u in e) if ("default" !== u && {}.hasOwnProperty.call(e, u)) { var i = a ? Object.getOwnPropertyDescriptor(e, u) : null; i && (i.get || i.set) ? Object.defineProperty(n, u, i) : n[u] = e[u]; } return n.default = e, t && t.set(e, n), n; }
// TODO: probably replace it with Animated view --- need to check with implementation is better
class LBarState {
  constructor(slideX, slideY) {
    this.slideX = slideX;
    this.slideY = slideY;
  }
}
class StreamLayerView extends _react.PureComponent {
  static initialState = {
    lbarState: new LBarState(0, 0),
    volumeBeforeDucking: undefined
  };
  constructor(props) {
    super(props);
    this._root = /*#__PURE__*/_react.default.createRef();
    this.state = StreamLayerView.initialState;
    if (_reactNative.Platform.OS === 'ios') {
      this.registerEvents();
    }
  }
  registerEvents() {
    const emitter = new _reactNative.NativeEventEmitter(_reactNative.NativeModules.StreamLayerViewEventEmitter);
    emitter.addListener('onNativeLBarStateChanged', this._onNativeLBarStateChanged);
    emitter.addListener('onNativeRequestStream', this._onNativeRequestStream);
    emitter.addListener('onNativeRequestAudioDucking', this._onNativeRequestAudioDucking);
    emitter.addListener('onNativeDisableAudioDucking', this._onNativeDisableAudioDucking);
  }
  _onNativeRequestStream = event => {
    const nativeEvent = _reactNative.Platform.select({
      ios: event,
      default: event.nativeEvent
    });
    this.props.onRequestStream?.(nativeEvent.id);
  };
  _onNativeLBarStateChanged = event => {
    const nativeEvent = _reactNative.Platform.select({
      ios: event,
      default: event.nativeEvent
    });
    const ratio = _reactNative.PixelRatio.get();
    const slideX = _reactNative.Platform.select({
      ios: Math.round(nativeEvent.slideX),
      default: Math.round(nativeEvent.slideX / ratio)
    });
    const slideY = _reactNative.Platform.select({
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
    const nativeEvent = _reactNative.Platform.select({
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
    if (_reactNative.Platform.OS === 'android') {
      // create view
      const viewId = (0, _reactNative.findNodeHandle)(this._root.current);
      const command = _reactNative.UIManager[ComponentName].Commands.create.toString();
      _reactNative.UIManager.dispatchViewManagerCommand(viewId, command, [viewId]);
    } else {}
  }
  componentWillUnmount() {
    if (_reactNative.Platform.OS === 'android') {
      // destroy view
      const viewId = (0, _reactNative.findNodeHandle)(this._root.current);
      const command = _reactNative.UIManager[ComponentName].Commands.destroy.toString();
      _reactNative.UIManager.dispatchViewManagerCommand(viewId, command, [null]);
    }
  }
  hideMenu() {
    const viewId = (0, _reactNative.findNodeHandle)(this._root.current);
    const command = _reactNative.UIManager[ComponentName].Commands.hideMenu;
    _reactNative.UIManager.dispatchViewManagerCommand(viewId, _reactNative.Platform.select({
      ios: command,
      default: command.toString()
    }), [null]);
  }
  hideOverlay() {
    const viewId = (0, _reactNative.findNodeHandle)(this._root.current);
    const command = _reactNative.UIManager[ComponentName].Commands.hideOverlay;
    _reactNative.UIManager.dispatchViewManagerCommand(viewId, _reactNative.Platform.select({
      ios: command,
      default: command.toString()
    }), [null]);
  }
  showOverlay(viewOverlay) {
    const viewId = (0, _reactNative.findNodeHandle)(this._root.current);
    const command = _reactNative.UIManager[ComponentName].Commands.showOverlay;
    _reactNative.UIManager.dispatchViewManagerCommand(viewId, _reactNative.Platform.select({
      ios: command,
      default: command.toString()
    }), [viewOverlay]);
  }
  handleInvite(invite) {
    const viewId = (0, _reactNative.findNodeHandle)(this._root.current);
    const command = _reactNative.UIManager[ComponentName].Commands.handleInvite;
    _reactNative.UIManager.dispatchViewManagerCommand(viewId, _reactNative.Platform.select({
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
    return /*#__PURE__*/_react.default.createElement(_reactNative.View, {
      style: [style]
    }, playerView !== undefined && /*#__PURE__*/_react.default.createElement(_reactNative.View, {
      style: {
        marginEnd: this.state.lbarState.slideX,
        marginBottom: this.state.lbarState.slideY
      }
    }, playerView), /*#__PURE__*/_react.default.createElement(StreamLayerRCTView, {
      style: _reactNative.StyleSheet.absoluteFill,
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
exports.StreamLayerView = StreamLayerView;
const ComponentName = 'StreamLayerRCTView';
const LINKING_ERROR = `The package 'react-native-streamlayer' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo managed workflow\n';
const StreamLayerRCTView = _reactNative.UIManager.getViewManagerConfig(ComponentName) != null ? (0, _reactNative.requireNativeComponent)(ComponentName) : () => {
  throw new Error(LINKING_ERROR);
};
//# sourceMappingURL=StreamLayerView.js.map