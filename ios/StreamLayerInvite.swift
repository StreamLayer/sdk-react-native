//
//  StreamLayerInvite.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 19.06.2024.
//

import Foundation

struct SLRInviteLinkUserData: Codable {
  let id: String
  let tinodeUserId: String?
  let name: String?
  let username: String?
  let avatar: String?
}

enum SLRInviteLinkGroupType: Int, Codable {
  case unset
  case chat
  case watchParty
}

struct SLRInviteLinkData: Codable {
  let linkId: String?
  let eventId: String?
  let groupId: String?
  let externalEventId: String?
  var gamification: Bool?
  let user: SLRInviteLinkUserData?
  let groupType: SLRInviteLinkGroupType?
}

private let decoder = JSONDecoder()

extension Dictionary where Key == String, Value: Any {
  func object<T: Decodable>() -> T? {
    if let data = try? JSONSerialization.data(withJSONObject: self, options: []) {
      return try? decoder.decode(T.self, from: data)
    } else {
      return nil
    }
  }
}

func encodeToDictionary<T: Codable>(_ object: T) -> [String: Any]? {
    let encoder = JSONEncoder()
    guard let data = try? encoder.encode(object) else {
        return nil
    }

    // Convert JSON data to dictionary
    guard let jsonObject = try? JSONSerialization.jsonObject(with: data, options: []),
          let dictionary = jsonObject as? [String: Any] else {
        return nil
    }

    return dictionary
}
