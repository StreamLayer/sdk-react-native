//
//  StreamLayerRTCViewManager.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 10.05.2024.
//

import Foundation
import React
import StreamLayerSDK

@objc(StreamLayerRCTViewManager)
class StreamLayerRCTViewManager: RCTViewManager {

  override func view() -> UIView! {
    let uiView = StreamLayerWrapperView()
    return uiView
  }

  override class func requiresMainQueueSetup() -> Bool {
    true
  }

  // MARK: - Commands

  @objc func hideMenu(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      guard self.bridge.uiManager.view(forReactTag: node) is StreamLayerWrapperView else {
        return
      }
      StreamLayer.hideLaunchButton(true)
      StreamLayer.hideLaunchControls(true)
    }
  }

  @objc func hideOverlay(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      guard self.bridge.uiManager.view(forReactTag: node) is StreamLayerWrapperView else {
        return
      }
      StreamLayer.closeCurrentOverlay()
    }
  }

  @objc func showOverlay(_ node: NSNumber, args: NSString) {
    DispatchQueue.main.async {
      guard self.bridge.uiManager.view(forReactTag: node) is StreamLayerWrapperView else {
        return
      }

      guard let overlayType = StreamLayerShowOverlayMapper.map(String(args)) else {
        return
      }

      try? StreamLayer.showOverlay(overlayType: overlayType, mainContainerViewController: UIViewController())
    }
  }

  @objc func handleInvite(_ node: NSNumber, args: NSDictionary) {
    guard let opts = args as? [String: AnyObject], let data: SLRInviteLinkData = opts.object() else {
      return
    }
    DispatchQueue.main.async {
      let params = ["streamlayer": encodeToDictionary(data)]
      _ = StreamLayer.handleDeepLink(params: params as [AnyHashable : Any])
    }
  }

}


