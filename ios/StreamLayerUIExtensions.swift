//
//  StreamLayerUIExtensions.swift
//  DoubleConversion
//
//  Created by Kirill Kunst on 19.06.2024.
//

import Foundation
import UIKit

extension UIWindow {
    static var isLandscape: Bool {
        return UIApplication.shared.keyWindow?
            .windowScene?
            .interfaceOrientation
            .isLandscape ?? false
    }
}

extension UIApplication {

    var keyWindow: UIWindow? {
        return self.connectedScenes
            .filter { $0.activationState == .foregroundActive }
            .first(where: { $0 is UIWindowScene })
            .flatMap({ $0 as? UIWindowScene })?.windows
            .first(where: \.isKeyWindow)
    }

}

extension UIView {

    func superview(at level: Int) -> UIView? {
        if level == 0 { return self }
        guard let superview = self.superview else { return nil }

        return superview.superview(at: level - 1)
    }

    func className() -> String {
        NSStringFromClass(type(of: self))
    }
}
