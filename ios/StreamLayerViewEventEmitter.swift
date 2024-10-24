//
//  StreamLayerViewEventEmitter.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 10.06.2024.
//

import Foundation
import React

@objc(StreamLayerViewEventEmitter)
class StreamLayerViewEventEmitter: RCTEventEmitter {

    public static var shared: StreamLayerViewEventEmitter?

    enum Args: String {
        case id
        case slideX
        case slideY
        case level
    }

    enum Events: String, CaseIterable {
        case onNativeRequestStream
        case onNativeLBarStateChanged
        case onNativeRequestAudioDucking
        case onNativeDisableAudioDucking
    }

    private var hasObservers: Bool = false

    override init() {
        super.init()
        StreamLayerViewEventEmitter.shared = self
    }

    override class func requiresMainQueueSetup() -> Bool {
        true
    }

    override func startObserving() {
        super.startObserving()
        hasObservers = true
    }

    override func stopObserving() {
        super.stopObserving()
        hasObservers = false
    }

    // MARK: - Events

    func onRequestStream(id: String) {
        guard hasObservers else { return }
        sendEvent(withName: Events.onNativeRequestStream.rawValue, body: [Args.id.rawValue: id])
    }

    func onLBarStateChanged(slideX: Int, slideY: Int) {
        guard hasObservers else { return }
        sendEvent(withName: Events.onNativeLBarStateChanged.rawValue, body: [
            Args.slideX.rawValue: slideX,
            Args.slideY.rawValue: slideY,
        ])
    }

    func requestAudioDucking(level: Float) {
        guard hasObservers else { return }
        sendEvent(withName: Events.onNativeRequestAudioDucking.rawValue, body: [Args.level.rawValue: level])
    }

    func disableAudioDucking() {
        guard hasObservers else { return }
        sendEvent(withName: Events.onNativeDisableAudioDucking.rawValue, body: nil)
    }

    // MARK: RCTEventEmitter overrides

    override func supportedEvents() -> [String]! {
        Events.allCases.map({ $0.rawValue })
    }
}
