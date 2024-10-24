"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.StreamLayerInviteGroupType = exports.StreamLayer = void 0;
var _reactNative = require("react-native");
let StreamLayerInviteGroupType = exports.StreamLayerInviteGroupType = /*#__PURE__*/function (StreamLayerInviteGroupType) {
  StreamLayerInviteGroupType["WatchParty"] = "WatchParty";
  StreamLayerInviteGroupType["Chat"] = "Chat";
  return StreamLayerInviteGroupType;
}({});
class StreamLayer {
  static isInitialized() {
    return StreamLayerModule.isInitialized();
  }
  static authorizationBypass(schema, token) {
    return StreamLayerModule.authorizationBypass(schema, token);
  }
  static useAnonymousAuth() {
    return StreamLayerModule.useAnonymousAuth();
  }
  static isUserAuthorized() {
    return StreamLayerModule.isUserAuthorized();
  }
  static logout() {
    return StreamLayerModule.logout();
  }
  static createEventSession(id) {
    return StreamLayerModule.createEventSession(id);
  }
  static releaseEventSession() {
    StreamLayerModule.releaseEventSession();
  }
  static getInvite(json) {
    return StreamLayerModule.getInvite(json);
  }
  static getDemoEvents(date) {
    return StreamLayerModule.getDemoEvents(date);
  }
  static initSdk(sdkKey, isDebug) {
    StreamLayerModule.initSdk(sdkKey, isDebug);
  }
}
exports.StreamLayer = StreamLayer;
const LINKING_ERROR = `The package 'react-native-streamlayer' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
const StreamLayerModule = _reactNative.NativeModules.StreamLayerModule ? _reactNative.NativeModules.StreamLayerModule : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
//# sourceMappingURL=StreamLayer.js.map