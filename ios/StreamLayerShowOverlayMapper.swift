//
//  StreamLayerShowOverlayMapper.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 04.06.2024.
//

import Foundation
import StreamLayerSDK

struct StreamLayerShowOverlayMapper {

  enum ViewOverlay: String {
    case games = "Games"
    case watchParty = "WatchParty"
    case twitter = "Twitter"
    case statistics = "Statistics"
  }

  static func map(_ stringOverlayValue: String) -> StreamLayerOverlayType? {
    guard let viewOverlay = ViewOverlay(rawValue: stringOverlayValue) else {
      return nil
    }

    switch viewOverlay {
    case .games:
      return .games
    case .statistics:
      return .statistic
    case .twitter:
      return .twitter
    case .watchParty:
      return .watchParty
    }
  }
}
