//
//  StreamLayerProvider.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 22.07.2024.
//

import Foundation
import StreamLayerSDK
import UIKit

public class StreamLayerProvider: SLRAuthFlowProvider, SLRAuthFlowProfileProvider {

  public func requestOTP(phoneNumber: String) async throws {
    try await StreamLayer.Auth.requestOTP(phoneNumber: phoneNumber)
  }

  public func authenticate(phoneNumber: String, code: String) async throws -> AuthUser {
    return AuthUser.test()
  }

  public func setUserName(_ name: String) async throws {
    try await StreamLayer.Auth.setUserName(name)
  }

  public func setPublicUserName(_ name: String) async throws {
    try await StreamLayer.Auth.setPublicUserName(name)
  }

  public func updateAvatar(to image: UIImage) async throws -> String {
    try await StreamLayer.Auth.uploadAvatar(image)
  }

  public func deleteAvatar() {
    StreamLayer.Auth.deleteAvatar()
  }

  public func logout() {
    StreamLayer.logout()
  }

  public var termsOfService: String? {
    nil
  }

  public var privacyPolicy: String? {
    nil
  }

  public func user() -> AuthUser? {
    guard let user = StreamLayer.Auth.authenticatedUser() else {
      return nil
    }

    return AuthUser(id: user.id,
                    username: user.username,
                    name: user.name,
                    avatar: user.avatar,
                    alias: user.alias,
                    publicName: user.publicName)
  }

}

