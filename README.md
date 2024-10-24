# react-native-streamlayer

React Native StreamLayer SDK

## System Requirements

iOS:
- Xcode 15.3
- Swift 5.9
- iOS 15+

Android:
- Android Studio
- Kotlin/Java
- Minimum SDK version 21

## Installation

```sh
npm install react-native-streamlayer
```

## Native iOS Part

1. First, update cocoapods dependencies:

```sh
cd ios/ && pod install && cd ../
```

2. For quick setup you can use our small wrapper that can be used in the AppDelegate.m

```swift
import StreamLayerSDK
import OSLog

struct Configuration {
  static let sdkKey = "YOUR_SDK_KEY"
}

@objc(SLRObjCWrapper)
public class SLRObjCWrapper: NSObject {

  private var logger = Logger(subsystem: "SLRObjCWrapper", category: "SDK")

  @objc public func setup() {
    logger.log(level: .debug, "setup started")
    StreamLayer.initSDK(with: Configuration.sdkKey,
                        isDebug: false,
                        delegate: self,
                        loggerDelegate: self)
    logger.log(level: .debug, "setup completed")
  }
}

// MARK: - StreamLayerDelegate

extension SLRObjCWrapper: StreamLayerDelegate {
  public func inviteHandled(invite: SLRInviteData, completion: @escaping (Bool) -> Void) {
    completion(false)
  }

  public func requireAuthentication(nameInputOptions: StreamLayer.Auth.SLRRequireAuthOptions, completion: @escaping (Bool) -> Void) {
    completion(true)
  }
}

// MARK: - SLROverlayLoggerDelegate

extension SLRObjCWrapper: SLROverlayLoggerDelegate {

  public func sendLogdata(userInfo: String) {
    logger.log(level: .debug, "\(userInfo)")
  }

  public func receiveLogs(userInfo: String) {
    logger.log(level: .debug, "\(userInfo)")
  }
}

```

3. In the `AppDelegate.h`, please save property to the SDK:

```objc
#import <RCTAppDelegate.h>
#import <UIKit/UIKit.h>

@interface AppDelegate : RCTAppDelegate

@property (nonatomic, strong) SLRObjCWrapper *sdk;

@end
```

4. In the `AppDelegate.mm` do following:

```objc

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.moduleName = @"example";
  self.initialProps = @{};

  // Initialize sdk
  self.sdk = [[SLRObjCWrapper alloc] init];
  [self.sdk setup];

  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

```

## Native Android Part

1. For register the StreamLayer package, you need add `StreamLayerPackage` to the list of packages returned in ReactNativeHost's getPackages() method. Open up your `Application` class and add this code:

```kotlin
override fun getPackages(): List<ReactPackage> =
    PackageList(this).packages.apply {
        // Packages that cannot be autolinked yet can be added manually here:
        add(StreamLayerPackage())
    }
```

2. For quick setup you need initialize StreamLayer SDK in the Application's onCreate() method. Open up your `Application` class and add this code:

```kotlin
StreamLayer.initializeApp(this, "YOUR_SDK_KEY")
```


## React Native Part

TBD

## Building and running the app

Finally, build and deploy the app. Make sure an emulator is available, or there is a physical device connected to deploy to.

```sh
npm run android
npm run ios
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
