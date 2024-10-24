//
//  StreamLayerModuleBridge.m
//  react-native-streamlayer
//
//  Created by Kirill Kunst on 10.05.2024.
//

#import <React/RCTViewManager.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTConvert.h>

// MARK: - StreamLayer View

@interface RCT_EXTERN_MODULE(StreamLayerRCTViewManager, RCTViewManager)

RCT_EXTERN_METHOD(showOverlay:(nonnull NSNumber *)node args:(NSString *))
RCT_EXTERN_METHOD(hideMenu:(nonnull NSNumber *)node args:(NSDictionary *))
RCT_EXTERN_METHOD(hideOverlay:(nonnull NSNumber *)node args:(NSDictionary *))
RCT_EXTERN_METHOD(handleInvite:(nonnull NSNumber *)node args:(NSDictionary *))
RCT_EXPORT_VIEW_PROPERTY(config, NSDictionary *)
RCT_EXPORT_VIEW_PROPERTY(hocModeEnabled, BOOL)
@end

// MARK: - StreamLayer Module
@interface RCT_EXTERN_MODULE(StreamLayerModule, NSObject)

RCT_EXTERN_METHOD(initSdk:(NSDictionary *)config
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(createEventSession:(NSString *)name
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(releaseEventSession:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(authorizationBypass:(NSString *)schema
                    token: (NSString *)token
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(logout:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(useAnonymousAuth:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(isUserAuthorized:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)
                    
RCT_EXTERN_METHOD(isInitialized:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getDemoEvents:(NSString *)date
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getInvite:(NSDictionary *)json
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(handleDeepLink:(NSDictionary *)json
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(removeOverlay: (RCTPromiseResolveBlock) resolve
                    rejecter:(RCTPromiseRejectBlock)reject)

@end

// MARK: - Event Emitter

@interface RCT_EXTERN_MODULE(StreamLayerViewEventEmitter, RCTEventEmitter)

RCT_EXTERN_METHOD(supportedEvents)

@end
