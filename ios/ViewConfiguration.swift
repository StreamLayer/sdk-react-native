//
//  ViewConfiguration.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 04.06.2024.
//

import Foundation
import StreamLayerSDK

struct ViewConfiguration: Codable {

  enum ViewNotificationFeature: String {
    case WatchParty
    case Games
    case Chat
    case Twitter
  }

  enum ViewOverlayLandscapeMode: String {
    case Start
    case End
    case Lbar
  }

  var viewNotificationFeatures: [String]?
  var isGamesPointsEnabled: Bool?
  var isGamesPointsStartSide: Bool?
  var isLaunchButtonEnabled: Bool?
  var isMenuAlwaysOpened: Bool?
  var isMenuLabelsVisible: Bool?
  var isMenuProfileEnabled: Bool?
  var isTooltipsEnabled: Bool?
  var isWatchPartyReturnButtonEnabled: Bool?
  var isWhoIsWatchingViewEnabled: Bool?
  var isOverlayExpandable: Bool?
  var overlayHeightSpace: Double?
  var overlayWidth: Double?
  var overlayLandscapeMode: String?

  static func fromDictionary(_ dict: NSDictionary) -> ViewConfiguration {
    return ViewConfiguration(
      viewNotificationFeatures: dict["viewNotificationFeatures"] as? [String],
      isGamesPointsEnabled: dict["isGamesPointsEnabled"] as? Bool,
      isGamesPointsStartSide: dict["isGamesPointsStartSide"] as? Bool,
      isLaunchButtonEnabled: dict["isLaunchButtonEnabled"] as? Bool,
      isMenuAlwaysOpened: dict["isMenuAlwaysOpened"] as? Bool,
      isMenuLabelsVisible: dict["isMenuLabelsVisible"] as? Bool,
      isMenuProfileEnabled: dict["isMenuProfileEnabled"] as? Bool,
      isTooltipsEnabled: dict["isTooltipsEnabled"] as? Bool,
      isWatchPartyReturnButtonEnabled: dict["isWatchPartyReturnButtonEnabled"] as? Bool,
      isWhoIsWatchingViewEnabled: dict["isWhoIsWatchingViewEnabled"] as? Bool,
      isOverlayExpandable: dict["isOverlayExpandable"] as? Bool,
      overlayHeightSpace: dict["overlayHeightSpace"] as? Double,
      overlayWidth: dict["overlayWidth"] as? Double,
      overlayLandscapeMode: dict["overlayLandscapeMode"] as? String
    )
  }

  var notificationMode: SLRNotificationsMode {
    guard let viewNotificationFeatures else {
      return SLRNotificationsMode()
    }

    var mode: SLRNotificationsMode = []
    if viewNotificationFeatures.contains(ViewNotificationFeature.Games.rawValue) {
      mode.insert(.promotion)
      mode.insert(.vote)
    }
    if viewNotificationFeatures.contains(ViewNotificationFeature.Chat.rawValue) {
      mode.insert(.messaging)
    }
    if viewNotificationFeatures.contains(ViewNotificationFeature.WatchParty.rawValue) {
      mode.insert(.watchParty)
    }
    if viewNotificationFeatures.contains(ViewNotificationFeature.Twitter.rawValue) {
      mode.insert(.twitter)
    }

    return mode
  }

  var isLBarEnabled: Bool {
    overlayLandscapeMode == ViewOverlayLandscapeMode.Lbar.rawValue
  }
}
