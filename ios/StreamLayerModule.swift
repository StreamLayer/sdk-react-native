//
//  StreamLayerModule.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 16.05.2024.
//

import Foundation
import React
import StreamLayerSDK
import OSLog
import UIKit

@objc(StreamLayerModule)
class StreamLayerModule: NSObject {

    private var logger = Logger(subsystem: "StreamLayerModule", category: "SDK")

    @objc(initSdk:resolver:rejecter:)
    func initSdk(config: NSDictionary?, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        logger.log(level: .debug, "setup started")
        guard let config = config, config.count > 0 else { return }
        let slConfiguration = StreamLayerConfiguration.fromDictionary(config)

        StreamLayer.config.triviaBalanceButtonVerticalCustomPadding = UIEdgeInsets(top: 45, left:
                                                                                    0, bottom: 0, right: -UIScreen.main.bounds.width + 20)
        StreamLayer.config.triviaBalanceButtonHorizontalCustomPadding = UIEdgeInsets(top: 41, left: 37, bottom: 0, right: -UIScreen.main.bounds.height + 54)
        if(!StreamLayer.isInitialized()) {
            StreamLayer.initSDK(with: slConfiguration.sdkKey,
                                isDebug: slConfiguration.isLoggingEnabled,
                                delegate: self,
                                loggerDelegate: self)
        }
        StreamLayer.config.appStyle = .blue
        StreamLayer.config.wpStatusViewTopOffset = -12.0
        StreamLayer.config.pullDownTooltipTopOffset = 60.0
        StreamLayer.config.gamificationOptions = SLRGamificationOptions(globalLeaderBoardEnabled: slConfiguration.isGlobalLeaderboardEnabled, invitesEnabled: slConfiguration.isGamesInviteEnabled)

        logger.log(level: .debug, "setup completed")

        resolve(())
    }

    // MARK: - Exposed methods

    @objc(createEventSession:resolver:rejecter:)
    func createEventSession(eventId: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        _ = StreamLayer.createSession(for: eventId)
        resolve(())
    }

    @objc(releaseEventSession:rejecter:)
    func releaseEventSession(resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        // not implemented
        resolve(())
    }

    @objc(logout:rejecter:)
    func logout(resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        StreamLayer.logout()
        resolve(())
    }

      // Deep links handler
    @objc(handleDeepLink:resolver:rejecter:)
    func handleDeepLink(params: NSDictionary, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) -> Void {
        print("DEEP LINK BRIDGE")
        print(params)
        
        
        let paramsDict = params as? [String: AnyObject]
        
        let result = StreamLayer.handleDeepLink(params: paramsDict)
 
        resolver(result)
    }

    @objc(authorizationBypass:token:resolver:rejecter:)
    func authorizationBypass(schema: String,
                             token: String,
                             resolver resolve: @escaping RCTPromiseResolveBlock,
                             rejecter reject: @escaping RCTPromiseRejectBlock
    ) -> Void {
        Task {
            do {
                try await StreamLayer.setAuthorizationBypass(token: token, schema: schema)
                resolve(())
            } catch {
                reject("", error.localizedDescription, error)
            }
        }
    }

    @objc(useAnonymousAuth:rejecter:)
    func useAnonymousAuth(resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        Task {
            do {
                try await StreamLayer.useAnonymousAuth()
                resolve(())
            } catch {
                reject("", error.localizedDescription, error)
            }
        }
    }

    @objc(isUserAuthorized:rejecter:)
    func isUserAuthorized(resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        let isUserAuthorized = StreamLayer.isUserAuthorized()
        resolve(isUserAuthorized)
    }

    // MARK: - isInitialized

    @objc(isInitialized:rejecter:)
    func isInitialized(resolver resolve: @escaping RCTPromiseResolveBlock,
                       rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        if StreamLayer.isInitialized() {
            resolve(true) 
        } else {
            resolve(false)
        }
    }

    // MARK: - removeOverlay

    @objc(removeOverlay:rejecter:)
    func removeOverlay(resolver resolve: @escaping RCTPromiseResolveBlock,
                       rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {

        StreamLayer.removeOverlay()

        resolve(nil)  

    }

    // MARK: - Invites

    @objc(getInvite:resolver:rejecter:)
    func getInvite(json: NSDictionary, 
                   resolver resolve: @escaping RCTPromiseResolveBlock,
                   rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let opts = json as? [String: AnyObject],
              let associatedData = opts["streamlayer"] as? [String: Any],
              let data: SLRInviteLinkData = associatedData.object()
        else {
          return resolve(nil)
        }
        resolve(encodeToDictionary(data))
    }


    // MARK: - Invites

//    @objc(isInitialized:resolver:rejecter:)
//    func isInitialized(resolver resolve: @escaping RCTPromiseResolveBlock,
//                       rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
//        if StreamLayerModule.isInitialized() {
//            resolve(true)
//        } else {
//            resolve(false)
//        }
//    }


    // MARK: - Demo events

        @objc(getDemoEvents:resolver:rejecter:)
        func getDemoEvents(date: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        if !date.isEmpty{
            UserDefaults.standard.setValue(date, forKey: "EventsDemoDate")
        }
        StreamLayer.shared.requestDemoStreams(showAllStreams: true) { error, streams in
            if let error = error {
            reject("", error.localizedDescription, error)
            return
            }
            let data = streams.map {
            $0.toDict()
            }
            resolve(data)
        }
        }

}

extension StreamLayer.SLRStreamModel {

    func toDict() -> [String: String] {
        return [
            "id": "\(eventId)",
            "title": titleText,
            "subtitle": subtitle,
            "previewUrl": preview,
            "videoUrl": streamURL
        ]
    }
}

// MARK: - StreamLayerDelegate

extension StreamLayerModule: StreamLayerDelegate {
  public func inviteHandled(invite: SLRInviteData, completion: @escaping (Bool) -> Void) {
    completion(false)
  }

  public func requireAuthentication(nameInputOptions: StreamLayer.Auth.SLRRequireAuthOptions, completion: @escaping (Bool) -> Void) {
    guard let vc = UIApplication.shared.topWindow?.topViewController else { return }

    let provider = StreamLayerProvider()
    let authFlow = SLRAuthFlow(authProvider: provider)

    authFlow.show(from: vc, options: nameInputOptions) { error in
      completion(error != nil)
    }
  }
}

// MARK: - SLROverlayLoggerDelegate

extension StreamLayerModule: SLROverlayLoggerDelegate {

  public func sendLogdata(userInfo: String) {
    logger.log(level: .debug, "\(userInfo)")
  }

  public func receiveLogs(userInfo: String) {
    logger.log(level: .debug, "\(userInfo)")
  }
}

extension UIWindow {
  var topViewController: UIViewController? {
    var topMostViewController = self.rootViewController
    while let presentedViewController = topMostViewController?.presentedViewController {
      topMostViewController = presentedViewController
    }
    return topMostViewController
  }
}


extension UIApplication {

    var topWindow: UIWindow? {
        return UIApplication.shared.connectedScenes
            .filter { $0.activationState == .foregroundActive }
            .first(where: { $0 is UIWindowScene })
            .flatMap({ $0 as? UIWindowScene })?.windows
            .first(where: \.isKeyWindow)
    }

}

