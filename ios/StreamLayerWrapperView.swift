//
//  StreamLayerWrapperView.swift
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 11.05.2024.
//

import Foundation
import UIKit
import React
import StreamLayerSDK

@objc(StreamLayerWrapperView)
class StreamLayerWrapperView: RCTView {
    var onPlayerVolumeChange: (() -> Void)?

    struct LBarSlideState {
        var slideX: Int
        var slideY: Int
    }

    private var widgetsViewController: SLRWidgetsViewController?
    private weak var parentViewController: UIViewController?
    private var hocEnabled: Bool = false
    private var eventEmitter: StreamLayerViewEventEmitter? = StreamLayerViewEventEmitter.shared
    private var lbarState = LBarSlideState(slideX: 0, slideY: 0)

    override init(frame: CGRect) {
        super.init(frame: frame)
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }

    deinit {
        StreamLayer.removeOverlay()
    }

    private func setup() {
        guard let parentViewController = findParentViewController() else {
            return
        }

        if widgetsViewController == nil {
            self.widgetsViewController = StreamLayer.createOverlay(
                self,
                mainContainerViewController: parentViewController,
                overlayDelegate: self,
                overlayDataSource: self,
                lbarDelegate: self
            )
            self.widgetsViewController?.view.translatesAutoresizingMaskIntoConstraints = false
        }

        self.parentViewController = parentViewController
        self.widgetsViewController!.willMove(toParent: parentViewController)
        parentViewController.addChild(self.widgetsViewController!)
        addSubview(self.widgetsViewController!.view)
        self.widgetsViewController!.didMove(toParent: parentViewController)
        setupConstraints()
    }

    private func setupConstraints() {
        let constraints = [
            self.widgetsViewController!.view.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            self.widgetsViewController!.view.trailingAnchor.constraint(equalTo: self.trailingAnchor),
            self.widgetsViewController!.view.topAnchor.constraint(equalTo: self.topAnchor),
            self.widgetsViewController!.view.bottomAnchor.constraint(equalTo: self.bottomAnchor)
        ]
        self.widgetsViewController!.view.removeConstraints(self.widgetsViewController!.view.constraints)
        NSLayoutConstraint.activate(constraints)
    }

    func findParentViewController() -> UIViewController? {
        var responder: UIResponder? = self
        while let nextResponder = responder?.next {
            if let viewController = nextResponder as? UIViewController {
                return viewController
            }
            responder = nextResponder
        }
        return nil
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        if widgetsViewController == nil {
            setup()
        }
    }

    @objc func setConfig(_ config: NSDictionary?) {
        guard let config = config, config.count > 0 else { return }
        let viewConfig = ViewConfiguration.fromDictionary(config)
        apply(configuration: viewConfig)
    }

    @objc func setHocModeEnabled(_ enabled: Bool) {
        hocEnabled = enabled
    }

    // MARK: - Configuration

    private func apply(configuration: ViewConfiguration) {
        StreamLayer.hideLaunchButton(!(configuration.isLaunchButtonEnabled ?? true))
        StreamLayer.config.isAlwaysOpened = configuration.isMenuAlwaysOpened ?? false
        StreamLayer.config.isUserProfileOverlayHidden = !(configuration.isMenuProfileEnabled ?? true)
        StreamLayer.config.tooltipsEnabled = configuration.isTooltipsEnabled ?? true
        StreamLayer.config.whoIsWatchingEnabled = configuration.isWhoIsWatchingViewEnabled ?? true
        StreamLayer.config.isExpandableOverlayEnabled = configuration.isOverlayExpandable ?? true
        StreamLayer.config.isLBarEnabled = configuration.isLBarEnabled
        StreamLayer.config.notificationsMode = configuration.notificationMode
    }

    override open func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard let view = super.hitTest(point, with: event) else {
            return nil
        }

        let playerTouchLevel = 3
        let otherTouchLevel = hocEnabled ? playerTouchLevel : playerTouchLevel + 1

        if view.className() != "StreamLayerSDK.SLRTouchForwardingView" {
            return view
        }

        var superViewToFind = view.superview(at: playerTouchLevel)
        if hocEnabled {
            superViewToFind = view.superview(at: playerTouchLevel - 1)?.subviews.first?.subviews.first
        }
        if let hitTestView = findHitTestView(
            in: superViewToFind,
            point: point,
            event: event,
            filter: { $0 != self }
        ) {
            return hitTestView
        }

        if let hitTestView = findHitTestView(
            in: view.superview(at: otherTouchLevel),
            point: point,
            event: event,
            filter: { $0.className() == "RCTScrollView" }
        ) {
            return hitTestView
        }

        return view
    }

    private func findHitTestView
    (
        in
        superview: UIView?,
        point: CGPoint,
        event: UIEvent?,
        filter: (UIView) -> Bool
    ) -> UIView? {
        guard let superview = superview else {
            return nil
        }

        for subview in superview.subviews.reversed() where subview.subviews.contains(where: filter) {
            let convertedPoint = convert(point, to: subview)
            if let hitView = subview.hitTest(convertedPoint, with: event) {
                return hitView
            }
        }

        return nil
    }

}

// MARK: - SLROverlayDelegate & SLROverlayDataSource

extension StreamLayerWrapperView: SLROverlayDelegate, SLROverlayDataSource {
    func requestAudioDucking() {
        eventEmitter?.requestAudioDucking(level: 1.0)
    }

    func disableAudioDucking() {
        eventEmitter?.disableAudioDucking()
    }

    func prepareAudioSession(for type: SLRAudioSessionType) {}

    func disableAudioSession(for type: SLRAudioSessionType) {}

    func shareInviteMessage() -> String { "" }

    func waveMessage() -> String { "" }

    func switchStream(to streamId: String) {
        eventEmitter?.onRequestStream(id: streamId)
    }

    func pauseVideo(_ userInitiated: Bool) {}

    func playVideo(_ userInitiated: Bool) {}

    func setPlayerVolume(_ volume: Float) {}

    func getPlayerVolume() -> Float { 0 }

    func currentPresentingViewController() -> UIViewController {
        self.parentViewController ?? UIViewController()
    }

    func onReturnToWP(isActive: Bool) {}

    func handleActionClicked(_ action: SLRActionClicked) async -> Bool { true }

    func handleActionShown(_ action: SLRActionShown) {}

    func overlayHeight() -> CGFloat {
        if UIWindow.isLandscape {
            return self.frame.height
        }

        let frame = self.frame
        let height = frame.width > frame.height ? frame.width : frame.height
        return height - 230
    }
}

// MARK: - SLRLBarDelegate

extension StreamLayerWrapperView: SLRLBarDelegate {

    func moveRightSide(for points: CGFloat) {
        lbarState = LBarSlideState(slideX: Int(points), slideY: lbarState.slideY)
        eventEmitter?.onLBarStateChanged(slideX: lbarState.slideX, slideY: lbarState.slideY)
    }

    func moveBottomSide(for points: CGFloat) {
        lbarState = LBarSlideState(slideX: lbarState.slideX, slideY: Int(points))
        eventEmitter?.onLBarStateChanged(slideX: lbarState.slideX, slideY: lbarState.slideY)
    }
}
