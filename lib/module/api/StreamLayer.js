import { NativeModules, Platform } from 'react-native';
export let StreamLayerInviteGroupType = /*#__PURE__*/function (StreamLayerInviteGroupType) {
  StreamLayerInviteGroupType["WatchParty"] = "WatchParty";
  StreamLayerInviteGroupType["Chat"] = "Chat";
  return StreamLayerInviteGroupType;
}({});
export class StreamLayer {
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
const LINKING_ERROR = `The package 'react-native-streamlayer' doesn't seem to be linked. Make sure: \n\n` + Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
const StreamLayerModule = NativeModules.StreamLayerModule ? NativeModules.StreamLayerModule : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
//# sourceMappingURL=StreamLayer.js.map