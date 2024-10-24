
//
//  StreamLayerConfiguration.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 31.07.2024.
//

import Foundation
import StreamLayerSDK

struct StreamLayerConfiguration: Codable {
    var sdkKey: String
    var isLoggingEnabled: Bool
    var isGlobalLeaderboardEnabled: Bool
    var isGamesInviteEnabled: Bool

    static func fromDictionary(_ dict: NSDictionary) -> StreamLayerConfiguration {
        return StreamLayerConfiguration(
            sdkKey: dict["sdkKey"] as? String ?? "",
            isLoggingEnabled: dict["isLoggingEnabled"] as? Bool ?? false,
            isGlobalLeaderboardEnabled: dict["isGlobalLeaderboardEnabled"] as? Bool ?? false,
            isGamesInviteEnabled: dict["isGamesInviteEnabled"] as? Bool ?? false
        )
    }
}
