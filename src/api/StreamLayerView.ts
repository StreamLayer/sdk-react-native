import { ReactNode } from 'react';
import type { StyleProp, ViewStyle } from 'react-native';

export enum StreamLayerViewOverlay {
  Games = "Games",
  WatchParty = "WatchParty",
  Twitter = "Twitter",
  Statistics = "Statistics",
}

export enum StreamLayerViewOverlayLandscapeMode {
  Start = "Start",
  End = "End",
  Lbar = "Lbar",
}

export enum StreamLayerViewNotificationFeature {
  WatchParty = "WatchParty",
  Games = "Games",
  Chat = "Chat",
  Twitter = "Twitter",
}

export interface StreamLayerViewConfiguration {
  viewNotificationFeatures?: StreamLayerViewNotificationFeature[]
  isGamesPointsEnabled?: boolean,
  isGamesPointsStartSide?: boolean,
  isLaunchButtonEnabled?: boolean,
  isMenuAlwaysOpened?: boolean,
  isMenuLabelsVisible?: boolean,
  isMenuProfileEnabled?: boolean,
  isTooltipsEnabled?: boolean,
  isWatchPartyReturnButtonEnabled?: boolean,
  isWhoIsWatchingViewEnabled?: boolean,
  isOverlayExpandable?: boolean,
  overlayHeightSpace?: number,
  overlayWidth?: number,
  overlayLandscapeMode?: StreamLayerViewOverlayLandscapeMode
}

export interface StreamLayerViewProps {
  style?: StyleProp<ViewStyle>;
  config?: StreamLayerViewConfiguration;
  applyWindowInsets?: Boolean;
  playerView?: ReactNode;
  player?: StreamLayerViewPlayer;
  onRequestStream?: (id: string) => void;
  onLBarStateChanged?: (slideX: number, slideY: number) => void;
  onRequestAudioDucking?: (level: number) => void;
  onDisableAudioDucking?: () => void;
}

export interface StreamLayerViewPlayer {
  volume: number;
}