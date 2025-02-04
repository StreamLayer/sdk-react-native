import { NativeModules, Platform, TouchableHighlight, UIManager, findNodeHandle } from 'react-native';
import Config from "react-native-config";

export interface StreamLayerDemoEvent {
  id: string,
  title?: string,
  subtitle?: string,
  previewUrl?: string,
  videoUrl?: string
}

export enum StreamLayerInviteGroupType {
  WatchParty = "WatchParty",
  Chat = "Chat"
}

export interface StreamLayerInviteUser {
  id?: string,
  tinodeUserId?: string,
  name?: string,
  username?: string,
  avatar?: string,
}

export interface StreamLayerInvite {
  linkId?: string,
  eventId?: string,
  externalEventId?: string,
  groupId?: string,
  externalGroupId?: string,
  gamification?: boolean,
  groupType?: StreamLayerInviteGroupType,
  user?: StreamLayerInviteUser
}

export enum StreamLayerTheme {
  Blue = "Blue",
  Green = "Green"
}

export interface StreamLayerConfiguration {
  sdkKey: string,
  theme?: StreamLayerTheme,
  isLoggingEnabled?: boolean,
  isGlobalLeaderboardEnabled?: boolean,
  isGamesInviteEnabled?: boolean
}

export interface DeepLinkParams {
  [key: string]: any;
}


export class StreamLayer {

  static isInitialized(): Promise<boolean> {
      return StreamLayerModule.isInitialized();
  }

  static authorizationBypass(schema: string, token: string): Promise<void> {
    return StreamLayerModule.authorizationBypass(schema, token)
  }

  static useAnonymousAuth(): Promise<void> {
    return StreamLayerModule.useAnonymousAuth()
  }

  static isUserAuthorized(): Promise<boolean> {
    return StreamLayerModule.isUserAuthorized()
  }

  static logout(): Promise<void> {
    return StreamLayerModule.logout() 
  }

  static removeOverlay(): Promise<void> {
      return StreamLayerModule.removeOverlay() 
  }

  static createEventSession(id: string): Promise<void> {
    return StreamLayerModule.createEventSession(id)
  }

  static releaseEventSession(): void {
    StreamLayerModule.releaseEventSession()
  }

  static getInvite(json: Object): Promise<StreamLayerInvite> {
    return StreamLayerModule.getInvite(json);
  }

  static getDemoEvents(date: string): Promise<Array<StreamLayerDemoEvent>> {
    return StreamLayerModule.getDemoEvents(date)
  }

  static initSdk(config: StreamLayerConfiguration): Promise<void> {
    return StreamLayerModule.initSdk({...config, sdkKey: Config.SL_SDK_API_KEY })
  }

  static handleDeepLink(params: DeepLinkParams): Promise<boolean> {
    return StreamLayerModule.handleDeepLink(params);
  }

}

const LINKING_ERROR =
  `The package 'react-native-streamlayer' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const StreamLayerModule = NativeModules.StreamLayerModule
  ? NativeModules.StreamLayerModule
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );